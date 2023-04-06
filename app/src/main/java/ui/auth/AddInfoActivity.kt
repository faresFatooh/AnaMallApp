package ui.auth

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mall.anamall.MainActivity
import com.mall.anamall.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import java.io.IOException
import java.util.*

class AddInfoActivity : AppCompatActivity(), LocationListener, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {
    private var db: FirebaseFirestore? = null
    private var phoneNum: String? = null
    private var uid: String? = null
    private var devicetoken: String? = null
    private var city: String? = null
    private var postalCode: String? = null
    private var knownName: String? = null
    private var subLocality: String? = null
    private var finalAddress: String? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var mUserName: EditText? = null
    private var mUserEmail: EditText? = null
    private var mUserAddress: EditText? = null
    private var mSaveInfoBtn: Button? = null
    private var addresses: List<Address>? = null
    val TAG = "GPS"
    private val UPDATE_INTERVAL = (2 * 1000 /* 10 secs */).toLong()
    private val FASTEST_INTERVAL: Long = 10000 /* 2 sec */
    var gac: GoogleApiClient? = null
    var locationRequest: LocationRequest? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_info)
        changestatusbarcolor()
        init()
        db = FirebaseFirestore.getInstance()
        phoneNum = intent.getStringExtra("PHONENUMBER")
        uid = intent.getStringExtra("UID")
        devicetoken = intent.getStringExtra("TOKEN")
        mSaveInfoBtn!!.setOnClickListener { view: View? ->
            val name = mUserName!!.text.toString()
            val email = mUserEmail!!.text.toString()
            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please Enter Valid Info", Toast.LENGTH_SHORT).show()
            } else {
                val userData = HashMap<String, Any?>()
                userData["uid"] = uid
                userData["phonenumber"] = phoneNum
                userData["name"] = name
                userData["email"] = email
                userData["devicetoken"] = devicetoken
                userData["latitude"] = latitude
                userData["longitude"] = longitude
                userData["address"] = finalAddress
                userData["knownname"] = knownName
                userData["sublocality"] = subLocality
                userData["city"] = city
                userData["postalcode"] = postalCode
                db!!.collection("UserList").document(uid!!).set(userData)
                    .addOnSuccessListener { o: Void? ->
                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        finish()
                    }.addOnFailureListener { e: Exception? ->
                    Toast.makeText(
                        this,
                        "Something went Wrong!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun init() {
        mUserName = findViewById(R.id.userName)
        mUserEmail = findViewById(R.id.userEmail)
        mSaveInfoBtn = findViewById(R.id.addInfo)
        mUserAddress = findViewById(R.id.userAddress)
        isGooglePlayServicesAvailable
        if (!isLocationEnabled) showAlert()
        locationRequest = LocationRequest()
        locationRequest!!.interval = UPDATE_INTERVAL
        locationRequest!!.fastestInterval = FASTEST_INTERVAL
        locationRequest!!.priority =
            LocationRequest.PRIORITY_HIGH_ACCURACY
        gac = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
    }

    fun changestatusbarcolor() {
        val window = this.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    override fun onStart() {
        gac!!.connect()
        super.onStart()
    }

    override fun onStop() {
        gac!!.disconnect()
        super.onStop()
    }

    override fun onLocationChanged(location: Location) {
        if (location != null) {
            updateUI(location)
        }
    }

    override fun onConnected(bundle: Bundle?) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@AddInfoActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
            return
        }
        Log.d(TAG, "onConnected")
        val ll = LocationServices.FusedLocationApi.getLastLocation(gac)
        Log.d(TAG, "LastLocation: $ll")
        LocationServices.FusedLocationApi.requestLocationUpdates(gac, locationRequest, this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        LocationServices.FusedLocationApi.requestLocationUpdates(
                            gac, locationRequest, this
                        )
                    } catch (e: SecurityException) {
                        Toast.makeText(
                            this@AddInfoActivity, "SecurityException:\n$e",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(this@AddInfoActivity, "Permission denied!", Toast.LENGTH_LONG)
                        .show()
                }
                return
            }
        }
    }

    override fun onConnectionSuspended(i: Int) {}
    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Toast.makeText(
            this@AddInfoActivity, "onConnectionFailed: \n$connectionResult",
            Toast.LENGTH_LONG
        ).show()
        Log.d("DDD", connectionResult.toString())
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(loc: Location) {
        Log.d(TAG, "updateUI")
        val geocoder = Geocoder(this@AddInfoActivity, Locale.getDefault())
        try {
            addresses = geocoder.getFromLocation(
                loc.latitude,
                loc.longitude,
                1
            ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            latitude = loc.latitude
            longitude = loc.longitude
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (addresses != null && addresses!!.size > 0) {
            city = addresses!![0].locality
            postalCode = addresses!![0].postalCode
            knownName = addresses!![0].featureName
            subLocality = addresses!![0].subLocality
            finalAddress = "$knownName, $subLocality, $city, $postalCode"
            mUserAddress!!.setText(finalAddress)
        }
    }

    private val isLocationEnabled: Boolean
        private get() {
            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    private val isGooglePlayServicesAvailable: Boolean
        private get() {
            val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
            val apiAvailability = GoogleApiAvailability.getInstance()
            val resultCode = apiAvailability.isGooglePlayServicesAvailable(this)
            if (resultCode != ConnectionResult.SUCCESS) {
                if (apiAvailability.isUserResolvableError(resultCode)) {
                    apiAvailability.getErrorDialog(
                        this,
                        resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST
                    )
                        ?.show()
                } else {
                    Log.d(TAG, "This device is not supported.")
                    finish()
                }
                return false
            }
            Log.d(TAG, "This device is supported.")
            return true
        }

    private fun showAlert() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Enable Location")
            .setMessage(
                """
    Your Locations Settings is set to 'Off'.
    Please Enable Location to use this app
    """.trimIndent()
            )
            .setPositiveButton("Location Settings") { paramDialogInterface: DialogInterface?, paramInt: Int ->
                val myIntent = Intent(
                    Settings.ACTION_LOCATION_SOURCE_SETTINGS
                )
                startActivity(myIntent)
            }
            .setNegativeButton("Cancel") { paramDialogInterface: DialogInterface?, paramInt: Int -> }
        dialog.show()
    }

    companion object {
        const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }
}