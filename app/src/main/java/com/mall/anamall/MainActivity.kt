package com.mall.anamall

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.mall.anamall.databinding.ActivityMainBinding
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import fragments.*
import ui.auth.LoginActivity
import java.util.*

class MainActivity : AppCompatActivity() {
    private var mCurrentUser: FirebaseUser? = null
    private var bottomNav: BottomNavigationView? = null
    private var binding: ActivityMainBinding? = null
    lateinit var toggle: ActionBarDrawerToggle
    private var mUserRef: DocumentReference? = null
    private var db: FirebaseFirestore? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view: View = binding!!.getRoot()
        setContentView(view)
        db = FirebaseFirestore.getInstance()


        toggle = ActionBarDrawerToggle(
            this@MainActivity,
            binding!!.drawerLayout2,
            R.string.open,
            R.string.close
        )

        binding!!.drawerLayout2.addDrawerListener(toggle)
        binding!!.navView2.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, ClothesStoreFragment())
                        .commit()
                }
                R.id.call -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, HardwareStoreFragment())
                        .commit()
                }
                R.id.setting -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, MakeupAndPerfumesStoreFragment())
                        .commit()
                }
                R.id.setting2 -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, WatchStoreFragment())
                        .commit()
                }
                R.id.setting3 -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, VegetableAndFruitStoreFragment())
                        .commit()
                }
                R.id.setting4 -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, MeatAndFishStoreFragment())
                        .commit()
                }
            }
            true
        }


        toggle.syncState()
        changestatusbarcolor()
        init()
        binding!!.fab.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, SearchFragment())
                .commit()
        }
        bottomNav = binding!!.bottomNavigation
        bottomNav!!.setOnNavigationItemSelectedListener(navListener)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, MallsFragment())
            .commit()


        mUserRef = db!!.collection("UserList").document(
            Objects.requireNonNull<FirebaseUser>(FirebaseAuth.getInstance().currentUser).uid
        )

        getUserInfo(this);

    }

    private fun getUserInfo(context: Context) {
        mUserRef!!.get().addOnCompleteListener { task: Task<DocumentSnapshot> ->
            if (task.isSuccessful) {
                val docRef = task.result
                val imageRef =
                    Objects.requireNonNull(docRef)["user_profile_image"] as String?
                val userName = docRef["name"] as String?
                var v = binding!!.navView2.getHeaderView(0)
                Glide.with(context)
                    .load(imageRef)
                    .placeholder(R.drawable.user_placeholder)
                    .into(v.findViewById(R.id.nav_image))
                v.findViewById<TextView>(R.id.name_nav).setText(userName)
            }
        }
    }


    private fun init() {
        val mAuth = FirebaseAuth.getInstance()
        mCurrentUser = mAuth.currentUser
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

    private val navListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item: MenuItem ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.nav_malls -> selectedFragment = MallsFragment()
                R.id.nav_restaurants -> selectedFragment = RestaurantsFragment()
                R.id.nav_favourites -> selectedFragment = FavouriteFragment()
                R.id.nav_profile -> selectedFragment = MyProfileFragment()
                R.id.imageBadgeView -> bottomNav!!.visibility = View.GONE
            }

            supportFragmentManager.beginTransaction().replace(
                R.id.fragmentContainer,
                Objects.requireNonNull(selectedFragment!!)
            ).commit()
            true
        }

    private fun sendUserToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragments = supportFragmentManager.fragments
        for (f in fragments) {
            (f as? MyProfileFragment)?.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onStart() {
        super.onStart()
        if (mCurrentUser == null) {
            sendUserToLogin()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}