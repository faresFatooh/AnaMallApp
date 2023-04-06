package ui.auth

import `in`.aabhasjindal.otptextview.OTPListener
import `in`.aabhasjindal.otptextview.OtpTextView
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mall.anamall.MainActivity
import com.mall.anamall.R
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.installations.InstallationTokenResult
import java.util.*

class OtpActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var mCurrentUser: FirebaseUser? = null
    private var mAuthVerificationId: String? = null
    private var phoneNum: String? = null
    private var deviceToken: String? = null
    private var mOtpText: OtpTextView? = null
    private var mVerifyBtn: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
        changestatusbarcolor()
        mAuthVerificationId = intent.getStringExtra("AuthCredentials")
        phoneNum = intent.getStringExtra("PhoneNumber")
        init()
        mOtpText!!.setOtpListener(object : OTPListener {
            override fun onInteractionListener() {}
            override fun onOTPComplete(otp: String) {
                var otp: String? = otp
                otp = mOtpText!!.getOTP()
                val credential = PhoneAuthProvider.getCredential(mAuthVerificationId!!, otp)
                signInWithPhoneAuthCredential(credential)
            }
        })
        mVerifyBtn!!.setOnClickListener { view: View? ->
            val otp: String = mOtpText!!.getOTP()
            if (otp.isEmpty()) {
                Toast.makeText(this, "Please enter correct OTP", Toast.LENGTH_LONG).show()
            } else {
                val credential = PhoneAuthProvider.getCredential(mAuthVerificationId!!, otp)
                signInWithPhoneAuthCredential(credential)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        mAuth = FirebaseAuth.getInstance()
        mCurrentUser = mAuth!!.currentUser
        mOtpText = findViewById<OtpTextView>(R.id.otpView)
        val mUserNum = findViewById<TextView>(R.id.userNum)
        mUserNum.text = "We have sent you an OTP at $phoneNum please enter the OTP."
        mVerifyBtn = findViewById(R.id.verifyOTP)
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

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    val user = Objects.requireNonNull(task.result).user
                    val uid = Objects.requireNonNull(user)!!.uid
                    val db = FirebaseFirestore.getInstance()
                    val docRef = db.collection("UserList").document(uid)
                    FirebaseInstallations.getInstance().getToken(true)
                        .addOnCompleteListener { task1: Task<InstallationTokenResult> ->
                            if (task1.isSuccessful) {
                                deviceToken = task1.result.token
                            }
                        }
                    docRef.get().addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                        if (documentSnapshot.exists()) {
                            sendUserToMain()
                        } else {
                            val intent = Intent(this@OtpActivity, AddInfoActivity::class.java)
                            intent.putExtra("PHONENUMBER", phoneNum)
                            intent.putExtra("UID", uid)
                            intent.putExtra("TOKEN", deviceToken)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                            finish()
                        }
                    }
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
                }
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