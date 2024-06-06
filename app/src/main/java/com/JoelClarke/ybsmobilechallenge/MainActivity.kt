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
    }
}