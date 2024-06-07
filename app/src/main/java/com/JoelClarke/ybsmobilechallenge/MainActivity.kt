package com.JoelClarke.ybsmobilechallenge

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import com.JoelClarke.ybsmobilechallenge.databinding.ActivityMainBinding
import com.JoelClarke.ybsmobilechallenge.fragments.HomeFragment
import com.JoelClarke.ybsmobilechallenge.navigator.NavigableActivity
import com.JoelClarke.ybsmobilechallenge.navigator.Navigator


class MainActivity : NavigableActivity() {

    private lateinit var bindings : ActivityMainBinding

    private val mainActivityViewModel : MainActivityViewModel by viewModels()
    private var onBackPressedListeners : MutableList<OnBackPressedListener> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindings = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindings.root)

        if (mainActivityViewModel.firstRun) {
            mainActivityViewModel.firstRun = false
            navigateToStartingFragment()
        }
    }

    private fun navigateToStartingFragment() {
        Log.d("MAIN_ACTIVITY", "navigate to start")
        Navigator.with(this)
            .fragment(HomeFragment())
            .navigate()
    }

    fun showBlocker() {
        bindings.clBlocker.visibility = View.VISIBLE
    }

    fun hideBlocker() {
        bindings.clBlocker.visibility = View.GONE
    }

    fun hideKeyboard() {
        var v : View? = currentFocus
        v?.let {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }

    }

    /**
     *  This section contains all the controls for the back press handling in the activity
     */
    override fun onBackPressed() {
        // Call listeners first otherwise bad things can happen with order-of-operations.
        for (i in onBackPressedListeners.indices.reversed()) {
            try {
                onBackPressedListeners[i].onBackPressed()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(
                    "MAIN-ACTIVITY",
                    "Exception when calling onBackPressedListener. This may be the result of not cleaning up onBackPressedListeners."
                )
                onBackPressedListeners.removeAt(i)
            }
        }
        if (mainActivityViewModel.allowBackPress) {
            super.onBackPressed()
        }
    }

    fun onBackPressed(override: Boolean) {
        if (override) {
            super.onBackPressed()
        } else {
            onBackPressed()
        }
    }

    fun setAllowBackPress(allowBackPress: Boolean) {
        mainActivityViewModel.allowBackPress = allowBackPress
    }

    fun addOnBackPressedListener(onBackPressedListener: OnBackPressedListener) {
        if (!onBackPressedListeners.contains(onBackPressedListener)) {
            onBackPressedListeners.add(onBackPressedListener)
        }
    }

    fun removeOnBackPressedListener(onBackPressedListener: OnBackPressedListener) {
        onBackPressedListeners.remove(onBackPressedListener)
    }

    abstract class OnBackPressedListener {
        abstract fun onBackPressed()
    }

    fun showError(message : String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("Ok") { dialog, i ->
                dialog.dismiss()
            }.show()
    }

    class MainActivityViewModel : ViewModel() {
        var firstRun = true
        var allowBackPress = true
    }
}