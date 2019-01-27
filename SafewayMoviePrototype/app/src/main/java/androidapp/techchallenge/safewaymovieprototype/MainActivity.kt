package androidapp.techchallenge.safewaymovieprototype

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log


class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme);
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        window.decorView.apply {
            // Hide both the navigation bar and the status bar.
            // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
            // a general rule, you should design your app to hide the status bar whenever you
            // hide the navigation bar.
            systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }
       // actionBar?.hide()
        if ((ContextCompat.checkSelfPermission(this,
                        Manifest.permission.SEND_SMS) !== PackageManager.PERMISSION_GRANTED))
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.SEND_SMS))

            else
            {
                ActivityCompat.requestPermissions(this,
                        arrayOf<String>(Manifest.permission.SEND_SMS),0
                )
            }
        }

        //checking calling permissions
        if ((ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CALL_PHONE) !== PackageManager.PERMISSION_GRANTED))
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.CALL_PHONE))

            else
            {
                ActivityCompat.requestPermissions(this,
                        arrayOf<String>(Manifest.permission.CALL_PHONE),0
                )
            }
        }

        //checking gps permission
        if ((ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) !== PackageManager.PERMISSION_GRANTED))
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION))
            else
            {
                ActivityCompat.requestPermissions(this,
                        arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),0
                )
            }
        }

        if ((ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) !== PackageManager.PERMISSION_GRANTED))
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION))

            else
            {
                ActivityCompat.requestPermissions(this,
                        arrayOf<String>(Manifest.permission.ACCESS_COARSE_LOCATION),0
                )
            }
        }


    }

    fun onButtonShowPopupWindowClick(view: View) {
            Log.d("button", "Button clicked!")
            val intent = Intent(this, ListeningActivity::class.java)
            startActivity(intent)
        }

}
