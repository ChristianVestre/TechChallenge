/* ====================================================================
 * Copyright (c) 2014 Alpha Cephei Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above ce
 *    distribution.
 *opyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with th
 * THIS SOFTWARE IS PROVIDED BY ALPHA CEPHEI INC. ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL CARNEGIE MELLON UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 */

package androidapp.techchallenge.safewaymovieprototype

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.TextView

import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.HashMap

import edu.cmu.pocketsphinx.Assets
import edu.cmu.pocketsphinx.Hypothesis
import edu.cmu.pocketsphinx.RecognitionListener
import edu.cmu.pocketsphinx.SpeechRecognizer
import edu.cmu.pocketsphinx.SpeechRecognizerSetup

class ListeningActivity2 : Activity(), RecognitionListener {

    private var recognizer: SpeechRecognizer? = null
    private var captions: HashMap<String, Int>? = null

    public override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        // Prepare the data for UI

        setContentView(R.layout.activity_main)
        (findViewById(R.id.caption_text) as TextView).text = "Preparing the recognizer"

        // Check if user has given permission to record audio
        val permissionCheck = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSIONS_REQUEST_RECORD_AUDIO)
            return
        }
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        SetupTask(this).execute()
    }

    private class SetupTask internal constructor(activity: ListeningActivity2) : AsyncTask<Void, Void, Exception>() {
        internal var activityReference: WeakReference<ListeningActivity2>

        init {
            this.activityReference = WeakReference(activity)
        }

        override fun doInBackground(vararg params: Void): Exception? {
            try {
                val assets = Assets(activityReference.get())
                val assetDir = assets.syncAssets()
                activityReference.get()!!.setupRecognizer(assetDir)
            } catch (e: IOException) {
                return e
            }

            return null
        }

        override fun onPostExecute(result: Exception?) {
            if (result != null) {
                (activityReference.get()!!.findViewById(R.id.caption_text) as TextView).text = "Failed to init recognizer $result"
            } else {
                activityReference.get()!!.switchSearch(KWS_SEARCH)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Recognizer initialization is a time-consuming and it involves IO,
                // so we execute it in async task
                SetupTask(this).execute()
            } else {
                finish()
            }
        }
    }

    public override fun onDestroy() {
        super.onDestroy()

        if (recognizer != null) {
            recognizer!!.cancel()
            recognizer!!.shutdown()
        }
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    override fun onPartialResult(hypothesis: Hypothesis?) {
        if (hypothesis == null)
            return

    }

    /**
     * This callback is called when we stop the recognizer.
     */
    override fun onResult(hypothesis: Hypothesis?) {
        print("it works")
        Log.i("info", "it works")
    }

    override fun onBeginningOfSpeech() {}

    /**
     * We stop recognizer here to get a final result
     */
    override fun onEndOfSpeech() {
        if (recognizer!!.searchName != KWS_SEARCH)
            switchSearch(KWS_SEARCH)
    }

    private fun switchSearch(searchName: String) {
        recognizer!!.stop()

        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
        if (searchName == KWS_SEARCH)
            recognizer!!.startListening(searchName)
        else
            recognizer!!.startListening(searchName, 10000)

        val caption = resources.getString(captions!![searchName]!!)
        (findViewById(R.id.caption_text) as TextView).text = caption
    }

    @Throws(IOException::class)
    private fun setupRecognizer(assetsDir: File) {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(File(assetsDir, "en-us-ptm"))
                .setDictionary(File(assetsDir, "cmudict-en-us.dict"))

                .setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)

                .recognizer
        recognizer!!.addListener(this)

        /* In your application you might not need to add all those searches.
          They are added here for demonstration. You can leave just one.
         */

        // Create keyword-activation search.
        recognizer!!.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE)

      /*  // Create grammar-based search for selection between demos
        val menuGrammar = File(assetsDir, "menu.gram")
        recognizer!!.addGrammarSearch(MENU_SEARCH, menuGrammar)

        // Create grammar-based search for digit recognition
        val digitsGrammar = File(assetsDir, "digits.gram")
        recognizer!!.addGrammarSearch(DIGITS_SEARCH, digitsGrammar)

        // Create language model search
        val languageModel = File(assetsDir, "weather.dmp")
        recognizer!!.addNgramSearch(FORECAST_SEARCH, languageModel)

        // Phonetic search
        val phoneticModel = File(assetsDir, "en-phone.dmp")
        recognizer!!.addAllphoneSearch(PHONE_SEARCH, phoneticModel)
        */
    }

    override fun onError(error: Exception) {
        (findViewById(R.id.caption_text) as TextView).text = error.message
    }

    override fun onTimeout() {
        switchSearch(KWS_SEARCH)
    }

    companion object {

        /* Named searches allow to quickly reconfigure the decoder */
        private val KWS_SEARCH = "wakeup"
        private val FORECAST_SEARCH = "forecast"
        private val DIGITS_SEARCH = "digits"
        private val PHONE_SEARCH = "phones"
        private val MENU_SEARCH = "menu"

        /* Keyword we are looking for to activate menu */
        private val KEYPHRASE = "unicorn"

        /* Used to handle permission request */
        private val PERMISSIONS_REQUEST_RECORD_AUDIO = 1
    }
}
