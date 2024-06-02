package com.JoelClarke.ybsmobilechallenge

import android.os.Bundle
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
        Navigator.with(this)
            .fragment(HomeFragment())
            .navigate()
    }

    class MainActivityViewModel : ViewModel() {
        var firstRun = true
    }
}