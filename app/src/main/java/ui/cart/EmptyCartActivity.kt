package ui.cart

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.mall.anamall.R

class EmptyCartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_empty_cart)
        init()
    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        val mGoBackImg = findViewById<ImageView>(R.id.cartBackBtn)
        mGoBackImg.setOnClickListener { view: View? -> onBackPressed() }
    }
}