package com.JoelClarke.ybsmobilechallenge.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.JoelClarke.ybsmobilechallenge.MainActivity
import com.JoelClarke.ybsmobilechallenge.databinding.FragmentHomeBinding
import com.JoelClarke.ybsmobilechallenge.networking.NetworkManager
import com.JoelClarke.ybsmobilechallenge.networking.Networking
import com.JoelClarke.ybsmobilechallenge.networking.responses.PhotosResponse
import okhttp3.Response

class HomeFragment: Fragment() {

    companion object {
        const val TAG = "_homeFragmentTag"

        const val NET_GET_PHOTOS = "_getRecentPhotosNetTag"
    }

    private var networkingListener: NetworkManager.NetworkRequestListener<Any>? = null

    private val homeViewModel : HomeViewModel by viewModels()

    private lateinit var bindings : FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        bindings = FragmentHomeBinding.inflate(inflater)
        val v = bindings.root



        setupObservers()
        getPhotos()

        return v
    }

    override fun onStart() {
        super.onStart()

        networkingListener = object : NetworkManager.NetworkRequestListener<Any>() {
            override fun onComplete(request: NetworkManager.NetworkRequest<Any>) {
                when(request.tag) {
                    NET_GET_PHOTOS -> {
                        homeViewModel.photosResponse.value = request.response as PhotosResponse?
                    }
                }

                // Make sure you finish a request so that it can be cleared from cache
                NetworkManager.getInstance()?.finish(request)
            }
        }

        NetworkManager.getInstance()?.addListener(arrayOf(

        ), networkingListener!!)

        val lastPhotosResponse = NetworkManager.getInstance()?.getLatest(NET_GET_PHOTOS)
        if (lastPhotosResponse != null) {
            homeViewModel.photosResponse.value = lastPhotosResponse.response as PhotosResponse?
            NetworkManager.getInstance()?.finish(lastPhotosResponse)
        }
    }

    override fun onStop() {
        super.onStop()

        networkingListener?.let {
            NetworkManager.getInstance()?.removeListener(
                arrayOf(
                    NET_GET_PHOTOS
                ), it
            )
        }
    }

    fun setupObservers() {
        homeViewModel.networkInFlight.removeObservers(viewLifecycleOwner)
        homeViewModel.networkInFlight.observe(viewLifecycleOwner) {
            if (it) {
                (activity as MainActivity).showBlocker()
            } else {
                (activity as MainActivity).hideBlocker()
            }
        }

        homeViewModel.photosResponse.removeObservers(viewLifecycleOwner)
        homeViewModel.photosResponse.observe(viewLifecycleOwner) {
            if (it != null) {
                //todo

            }
        }
    }

    fun processPhotosResponse() {

    }

    fun getPhotos() {
        Log.d(TAG, "getPhotos")
        NetworkManager.getInstance()?.enqueue(
            NetworkManager.NetworkRequest(
                NET_GET_PHOTOS,
                object : NetworkManager.NetworkRunnable<Any?>() {
            override fun run(networkManager: NetworkManager, id: String): Any? {

                return Networking.getInstance()?.getRecent()
            }
        }))
    }

    class HomeViewModel : ViewModel() {
        val networkInFlight = MutableLiveData<Boolean>()
        var photosResponse = MutableLiveData<PhotosResponse>()
    }
}



