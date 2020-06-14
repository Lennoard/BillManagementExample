package com.lennoardsilva.androidmobillschallenge.activities

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lennoardsilva.androidmobillschallenge.R
import com.lennoardsilva.androidmobillschallenge.isDarkMode
import com.lennoardsilva.androidmobillschallenge.toast


open class BaseActivity : AppCompatActivity() {
    private val connectivityManager : ConnectivityManager? by lazy {
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    }
    protected var networkFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with (window) {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = ContextCompat.getColor(
                this@BaseActivity,
                R.color.statusBarColor
            )

            if (isDarkMode()) {
                decorView.systemUiVisibility = 0
            } else {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager?.registerDefaultNetworkCallback(networkCallback)
        } else {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(request, networkCallback)
        }
    }

    override fun onStop() {
        super.onStop()
        connectivityManager?.unregisterNetworkCallback(networkCallback)
    }

    fun showErrorDialog(message: String?, cancelable: Boolean = false, onOkClick: (() -> Unit)? = null) {
        MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.ic_error)
            .setTitle(R.string.error)
            .setMessage(message ?: getString(R.string.error))
            .setPositiveButton(android.R.string.ok) { _, _ ->
                onOkClick?.let { it() }
            }
            .setCancelable(cancelable)
            .show()
    }

    open val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            if (networkFlag) {
                //toast(R.string.connected)
            }
        }

        override fun onLost(network: Network?) {
            networkFlag = true
            toast(R.string.network_unavailable, Toast.LENGTH_LONG)
        }
    }

    private val recyclerViewColumns: Int
        get () {
            val isLandscape = resources.getBoolean(R.bool.is_landscape)
            return if (isLandscape) 2 else 1
        }
}

