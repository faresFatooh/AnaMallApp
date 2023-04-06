package ui.review

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mall.anamall.MainActivity
import com.mall.anamall.R
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.hsalf.smileyrating.SmileyRating
import java.util.*

class NewReviewActivity : AppCompatActivity() {
    private var uid: String? = null
    private var userName: String? = null
    private var userImage: String? = null
    private var recommendText: String? = null
    private var resUid: String? = null
    private var ratingBar: SmileyRating? = null
    private var mReviewEditText: EditText? = null
    private var mRecommendBtn: RadioButton? = null
    private var mNotRecommendBtn: RadioButton? = null
    private var db: FirebaseFirestore? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_review)
        changestatusbarcolor()
        init()
        fetchUserDetails()
    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        uid = intent.getStringExtra("UID")
        resUid = intent.getStringExtra("RUID")
        val resName = intent.getStringExtra("RES_NAME")
        db = FirebaseFirestore.getInstance()
        ratingBar = findViewById(R.id.smiley_rating)
        mReviewEditText = findViewById(R.id.reviewEditText)
        val mGoBackBtn = findViewById<ImageView>(R.id.cartBackBtn)
        mGoBackBtn.setOnClickListener { view: View? -> onBackPressed() }
        val mRatingResName = findViewById<TextView>(R.id.recommendLabel)
        mRatingResName.text = "Would you recommend $resName ?"
        mRecommendBtn = findViewById(R.id.recommend)
        mNotRecommendBtn = findViewById(R.id.notrecommend)
        val mSaveReviewBtn = findViewById<Button>(R.id.saveReviewBtn)
        mSaveReviewBtn.setOnClickListener { view: View? -> uploadReviewDetails() }
    }

    private fun fetchUserDetails() {
        db!!.collection("UserList").document(uid!!).get()
            .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                if (task.isSuccessful) {
                    val docRef = task.result
                    userName = Objects.requireNonNull(docRef)["name"] as String?
                    userImage = docRef["user_profile_image"] as String?
                }
            }
    }

    private fun uploadReviewDetails() {
        if (TextUtils.isEmpty(mReviewEditText!!.text) || ratingBar!!.selectedSmiley.rating == -1 || !mRecommendBtn!!.isChecked && !mNotRecommendBtn!!.isChecked) {
            Toast.makeText(this, "Please fill the review properly", Toast.LENGTH_SHORT).show()
        } else {
            if (mRecommendBtn!!.isChecked) {
                recommendText = "YES"
            } else if (mNotRecommendBtn!!.isChecked) {
                recommendText = "NO"
            }
            val rating = ratingBar!!.selectedSmiley.rating
            val review = mReviewEditText!!.text.toString()
            val uploadReviewMap = HashMap<String, String?>()
            uploadReviewMap["user_name"] = userName
            uploadReviewMap["user_image"] = userImage
            uploadReviewMap["uid"] = uid
            uploadReviewMap["rating"] = rating.toString()
            uploadReviewMap["recommended"] = recommendText
            uploadReviewMap["review"] = review
            db!!.collection("RestaurantList")
                .document(resUid!!)
                .collection("Reviews")
                .document()
                .set(uploadReviewMap)
                .addOnCompleteListener { task: Task<Void?> ->
                    if (task.isSuccessful) {
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        Toast.makeText(applicationContext, "Review Uploaded", Toast.LENGTH_SHORT)
                            .show()
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
}