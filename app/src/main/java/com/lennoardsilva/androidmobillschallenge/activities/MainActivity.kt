package com.lennoardsilva.androidmobillschallenge.activities

import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.lennoardsilva.androidmobillschallenge.BillsApp
import com.lennoardsilva.androidmobillschallenge.R
import com.lennoardsilva.androidmobillschallenge.fragments.*
import com.lennoardsilva.androidmobillschallenge.getColorFromAttr
import com.lennoardsilva.androidmobillschallenge.utils.load
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*

class MainActivity : BaseActivity(),
    NavigationView.OnNavigationItemSelectedListener,
    BottomNavigationView.OnNavigationItemSelectedListener {

    private var selectedDrawerId = R.id.navExpenses
    private var selectedBottomNavId = R.id.navLists

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setUpDrawer(toolbar)

        bottomNavigation.setOnNavigationItemSelectedListener(this)
        replaceFragment(ExpensesFragment())
        supportActionBar?.title = getString(R.string.expenses)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawerLayout.closeDrawer(GravityCompat.START)

        return when (item.itemId) {
            R.id.navExpenses -> {
                selectedDrawerId = item.itemId
                supportActionBar?.title = getString(R.string.expenses)

                when (selectedBottomNavId) {
                    R.id.navLists -> {
                        replaceFragment(ExpensesFragment.newInstance())
                    }

                    R.id.navDashboard -> {
                        replaceFragment(ExpensesReportFragment.newInstance())
                    }
                }
                true
            }

            R.id.navEarnings -> {
                selectedDrawerId = item.itemId
                supportActionBar?.title = getString(R.string.earnings)
                when (selectedBottomNavId) {
                    R.id.navLists -> {
                        replaceFragment(RevenuesFragment.newInstance())
                    }

                    R.id.navDashboard -> {
                        replaceFragment(RevenuesReportFragment.newInstance())
                    }
                }
                true
            }

            // Bottom Navigation //
            R.id.navLists -> {
                selectedBottomNavId = item.itemId

                when (selectedDrawerId) {
                    R.id.navExpenses -> {
                        replaceFragment(ExpensesFragment.newInstance())
                    }

                    R.id.navEarnings -> {
                        replaceFragment(RevenuesFragment.newInstance())
                    }
                }
                true
            }

            R.id.navDashboard -> {
                selectedBottomNavId = item.itemId

                when (selectedDrawerId) {
                    R.id.navExpenses -> {
                        replaceFragment(ExpensesReportFragment.newInstance())
                    }

                    R.id.navEarnings -> {
                        replaceFragment(RevenuesReportFragment.newInstance())
                    }
                }
                true
            }

            else -> false
        }
    }

    private fun replaceFragment(fragment: Fragment?) {
        if (isFinishing || fragment == null || fragment.isAdded) return

        runCatching {
            val r = Runnable {
                supportFragmentManager.beginTransaction().apply {
                    setCustomAnimations(R.anim.fragment_open_enter, R.anim.fragment_open_exit)
                    replace(R.id.mainFragmentHolder, fragment)
                    commit()
                }
            }
            if (!Handler().postDelayed(r, 100)) {
                r.run()
            }
        }
        System.gc()
    }

    private fun setUpDrawer(toolbar: Toolbar) {
        val localUser = BillsApp.findCurrentAccount()
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        with (toggle) {
            drawerLayout.addDrawerListener(this)
            syncState()
            drawerArrowDrawable.color = getColorFromAttr(R.attr.colorOnBackground)
        }

        with (navigationView) {
            setCheckedItem(R.id.navExpenses)
            setNavigationItemSelectedListener(this@MainActivity)
        }

        navigationView.inflateHeaderView(R.layout.nav_header).also {
            it.findViewById<ImageView>(R.id.drawerProfilePic).load(localUser.photoUrl)
            it.findViewById<TextView>(R.id.drawerEmail).text = localUser.email
        }
    }
}