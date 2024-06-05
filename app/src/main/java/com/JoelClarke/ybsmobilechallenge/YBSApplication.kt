package com.JoelClarke.ybsmobilechallenge

import android.app.Application
import com.JoelClarke.ybsmobilechallenge.networking.NetworkManager
import com.JoelClarke.ybsmobilechallenge.networking.Networking

class YBSApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        NetworkManager.initialise(this)
        Networking.initialise(this)

    }
}