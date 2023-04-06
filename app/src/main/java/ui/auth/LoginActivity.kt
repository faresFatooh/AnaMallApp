package ui.auth

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mall.anamall.MainActivity
import com.mall.anamall.R
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.hbb20.CountryCodePicker
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private var mSendOtpBtn: Button? = null
    private var mNumberText: EditText? = null
    private var countryCodePicker: CountryCodePicker? = null
    private var mAuth: FirebaseAuth? = null
    private var mCurrentUser: FirebaseUser? = null
    private var finalPhoneNumber: String? = null
    private var mCallBacks: OnVerificationStateChangedCallbacks? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        changestatusbarcolor()
        initialiseViews()
        mSendOtpBtn!!.setOnClickListener { view: View? ->
            val phoneNumber = mNumberText!!.text.toString()
            val ccp = countryCodePicker!!.selectedCountryCodeWithPlus
            finalPhoneNumber = ccp + phoneNumber
            if (phoneNumber.isEmpty() || ccp.isEmpty()) {
                Toast.makeText(this, "Please Enter Correct Credentials", Toast.LENGTH_LONG).show()
            } else {
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    finalPhoneNumber!!,
                    60,
                    TimeUnit.SECONDS,
                    this,
                    mCallBacks!!
                )
            }
        }
        mCallBacks = object : OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(
                    this@LoginActivity,
                    "Verification Failed, please try again!",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onCodeSent(s: String, forceResendingToken: ForceResendingToken) {
                super.onCodeSent(s, forceResendingToken)
                val otpIntent = Intent(this@LoginActivity, OtpActivity::class.java)
                otpIntent.putExtra("AuthCredentials", s)
                otpIntent.putExtra("PhoneNumber", finalPhoneNumber)
                startActivity(otpIntent)
            }
        }
    }

    private fun initialiseViews() {
        mSendOtpBtn = findViewById(R.id.sendOtpBtn)
        mNumberText = findViewById(R.id.loginInput)
        countryCodePicker = findViewById(R.id.countryCodeHolder)
        mAuth = FirebaseAuth.getInstance()
        mCurrentUser = mAuth!!.currentUser
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    sendUserToMain()
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun changestatusbarcolor() {
        val window = this.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    override fun onStart() {
        super.onStart()
        if (mCurrentUser != null) {
            sendUserToMain()
        }
    }

    fun sendUserToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}