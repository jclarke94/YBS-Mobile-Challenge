package com.JoelClarke.ybsmobilechallenge.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.fragment.app.Fragment
import com.JoelClarke.ybsmobilechallenge.ui.theme.YBSMobileChallengeTheme

class HomeFragment: Fragment() {

    companion object {
        const val TAG = "_homeFragmentTag"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)


        return ComposeView(requireContext()).apply {
            setContent {
                YBSMobileChallengeTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Greeting(
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(modifier: Modifier = Modifier) {
    Text(text = "Hello World!")
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    YBSMobileChallengeTheme {
        Greeting()
    }
}