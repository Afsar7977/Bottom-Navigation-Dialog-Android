package com.afsar.bottomdialog

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.afsar.bottomdialog.base.BaseActivity
import com.afsar.bottomdialog.fragments.AFragment
import com.afsar.bottomdialog.fragments.BFragment
import com.afsar.bottomdialog.fragments.CFragment
import com.afsar.bottomdialog.fragments.nestedfragments.NestedFragment
import com.afsar.bottomdialog.utils.CommonUtils
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity(), NetworkStateReceiver.NetworkStateReceiverListener {

    private val TAG = this.javaClass.simpleName
    private val fragment1: Fragment = AFragment()
    private val fragment2: Fragment = BFragment()
    private val fragment3: Fragment = CFragment()
    private lateinit var moDialog: Dialog
    private var mbDoubleTapBackKey = false
    private var mbIsMFragmentAdded = false
    private lateinit var moNetworkReceiver: NetworkStateReceiver
    private val fragmentManager: FragmentManager = supportFragmentManager
    private var miUpdatedPosition = 0

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.navigation_journal -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        window.statusBarColor = Color.parseColor("#EEF1F4")
                    }
                    addFab.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_plus,
                            applicationContext.theme
                        )
                    )
                    closeBottomFragment()
                    val fragmentTransaction =
                        supportFragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.fragment_container, fragment1)
                    fragmentTransaction.addToBackStack(null)
                    fragmentTransaction.commit()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_helper -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        window.statusBarColor = Color.parseColor("#EEF1F4")
                    }
                    addFab.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_person_add,
                            applicationContext.theme
                        )
                    )
                    closeBottomFragment()
                    val fragmentTransaction3 =
                        supportFragmentManager.beginTransaction()
                    fragmentTransaction3.replace(R.id.fragment_container, fragment2)
                    fragmentTransaction3.addToBackStack(null)
                    fragmentTransaction3.commit()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_rules -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        window.statusBarColor = Color.parseColor("#EEF1F4")
                    }
                    addFab.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_plus,
                            applicationContext.theme
                        )
                    )
                    closeBottomFragment()
                    val fragmentTransaction4 =
                        supportFragmentManager.beginTransaction()
                    fragmentTransaction4.replace(R.id.fragment_container, fragment3)
                    fragmentTransaction4.addToBackStack(null)
                    fragmentTransaction4.commit()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_more -> {
                    createDialog()
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        CommonUtils.changeStatusColor(this)
        moNetworkReceiver = NetworkStateReceiver()
        moNetworkReceiver.addListener(this)
        registerReceiver(moNetworkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        var fragmentContainer: LinearLayout = findViewById(R.id.fragment_container)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.nav_view)
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        val fragmentTransaction =
            supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment1)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()

        fragmentManager.addOnBackStackChangedListener {
            when {
                getVisibleFragment() === fragment1 -> {
                    Log.d("fragment1", "isvisible")
                    bottomNavigationView.menu.getItem(0).isChecked = true
                }
                getVisibleFragment() === fragment2 -> {
                    Log.d("fragment2", "isvisible")
                    bottomNavigationView.menu.getItem(1).isChecked = true
                }
                getVisibleFragment() === fragment3 -> {
                    Log.d("fragment3", "isvisible")
                    bottomNavigationView.menu.getItem(2).isChecked = true
                }
                else -> {
                    bottomNavigationView.menu.getItem(0).isChecked = true
                }
            }
        }
    }

    private fun getVisibleFragment(): Fragment? {
        val fragmentManager =
            this.supportFragmentManager
        val fragments =
            fragmentManager.fragments
        for (fragment in fragments) {
            if (fragment != null && fragment.isVisible) return fragment
        }
        return null
    }

    fun createDialog(): Dialog {
        nav_view.menu.getItem(miUpdatedPosition).isChecked = true
        moDialog = Dialog(this)
        moDialog.setCanceledOnTouchOutside(true)
        moDialog.setCancelable(true)
        moDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        moDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        moDialog.setContentView(R.layout.custom_more_dialog)
        val window = moDialog.window
        val moCardLayout: CardView = moDialog.findViewById(R.id.card_layout)
        val parameter: FrameLayout.LayoutParams =
            moCardLayout.layoutParams as FrameLayout.LayoutParams
        parameter.setMargins(30, 30, 30, 100) // left, top, right, bottom
        moCardLayout.layoutParams = parameter
        val wlp = window?.attributes
        wlp?.gravity = Gravity.BOTTOM
        window?.attributes = wlp
        window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        moDialog.show()
        moDialog.setOnDismissListener {
            if (!mbIsMFragmentAdded) {
                nav_view.menu.getItem(miUpdatedPosition).isChecked = true
            }
        }
        val llOne: LinearLayout = moDialog.findViewById(R.id.llOne)
        val llTwo: LinearLayout = moDialog.findViewById(R.id.llTwo)
        val llThree: LinearLayout = moDialog.findViewById(R.id.llThree)
        val llFourth: LinearLayout = moDialog.findViewById(R.id.llFourth)
        val llFifth: LinearLayout = moDialog.findViewById(R.id.llFifth)
        val llSixth: LinearLayout = moDialog.findViewById(R.id.llSixth)
        llOne.setOnClickListener {
            moDialog.dismiss()
            closeBottomFragment()
            addFragmentsOnBottom(
                fragment = NestedFragment.newInstance(CommonUtils.TabPositions.SUMMARY),
                tag = getString(R.string.summary)
            )
        }
        llTwo.setOnClickListener {
            moDialog.dismiss()
            addFragmentsOnBottom(
                fragment = NestedFragment.newInstance(CommonUtils.TabPositions.PROFILE),
                tag = getString(R.string.patients)
            )
        }
        llThree.setOnClickListener {
            moDialog.dismiss()
            addFragmentsOnBottom(
                fragment = NestedFragment.newInstance(CommonUtils.TabPositions.HEALTH),
                tag = getString(R.string.health_info)
            )
        }
        llFourth.setOnClickListener {
            moDialog.dismiss()
            addFragmentsOnBottom(
                fragment = NestedFragment.newInstance(CommonUtils.TabPositions.ACCOUNT),
                tag = getString(R.string.support_txt)
            )
        }
        llFifth.setOnClickListener {
            moDialog.dismiss()
            addFragmentsOnBottom(
                fragment = NestedFragment.newInstance(CommonUtils.TabPositions.PATIENTS),
                tag = getString(R.string.faq_txt)
            )
        }
        llSixth.setOnClickListener {
            moDialog.dismiss()
            addFragmentsOnBottom(
                fragment = NestedFragment.newInstance(CommonUtils.TabPositions.PROFILE),
                tag = getString(R.string.profile_txt)
            )
        }
        return moDialog
    }

    private fun addFragmentsOnBottom(
        containerViewId: Int = R.id.frameSub,
        fragment: Fragment,
        tag: String
    ) {
        addFab.hide()
        mbIsMFragmentAdded = true
        fragment_container.visibility = View.GONE
        nav_view.menu.getItem(3).isChecked = true
        frameSub.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(containerViewId, fragment)
            .commit()
    }

    private fun closeBottomFragment() {
        addFab.show()
        mbIsMFragmentAdded = false
        if (this::moDialog.isInitialized) {
            moDialog.dismiss()
        }
        nav_view.menu.getItem(miUpdatedPosition).isChecked = true
        fragment_container.visibility = View.VISIBLE
        frameSub.visibility = View.GONE
        val loFragment: Fragment? =
            supportFragmentManager.findFragmentById(R.id.frameSub)
        when {
            loFragment != null -> {
                supportFragmentManager.beginTransaction()
                    .remove(loFragment)
                    .commit()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("home", "onPause")
    }

    override fun onResume() {
        super.onResume()
        Log.d("home", "onResume")
    }

    override fun onDestroy() {
        super.onDestroy()
        moNetworkReceiver.removeListener(this)
        unregisterReceiver(moNetworkReceiver)
    }

    override fun networkAvailable() {
        frameDemoHome.visibility = View.VISIBLE
        llOffline.visibility = View.GONE
    }

    override fun networkUnavailable() {
        frameDemoHome.visibility = View.GONE
        llOffline.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        if (mbDoubleTapBackKey) {
            finishAffinity()
            return
        }
        this.mbDoubleTapBackKey = true
        Toast.makeText(this, getString(R.string.are_you_sure_exit), Toast.LENGTH_SHORT)
            .show()
        Handler().postDelayed({ mbDoubleTapBackKey = false }, 2000)
    }
}