package com.JoelClarke.ybsmobilechallenge

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
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

    class MainActivityViewModel : ViewModel() {
        var firstRun = true
    }
}