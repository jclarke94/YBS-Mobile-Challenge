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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.JoelClarke.ybsmobilechallenge.MainActivity
import com.JoelClarke.ybsmobilechallenge.databinding.FragmentHomeBinding
import com.JoelClarke.ybsmobilechallenge.networking.NetworkManager
import com.JoelClarke.ybsmobilechallenge.networking.Networking
import com.JoelClarke.ybsmobilechallenge.networking.responses.PhotosResponse
import com.JoelClarke.ybsmobilechallenge.util.ListItem
import com.JoelClarke.ybsmobilechallenge.util.ListUtil
import com.bumptech.glide.Glide
import okhttp3.Response

class HomeFragment: Fragment() {

    companion object {
        const val TAG = "_homeFragmentTag"

        const val NET_GET_PHOTOS = "_getRecentPhotosNetTag"
    }

    private var networkingListener: NetworkManager.NetworkRequestListener<Any>? = null

    private val homeViewModel : HomeViewModel by viewModels()

    private lateinit var bindings : FragmentHomeBinding
    private lateinit var layoutManager: LayoutManager
    private lateinit var adapter : PhotoListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        bindings = FragmentHomeBinding.inflate(inflater)
        val v = bindings.root

        layoutManager = LinearLayoutManager(requireContext())
        adapter = PhotoListAdapter()
        bindings.rvItems.adapter = adapter
        bindings.rvItems.layoutManager = layoutManager

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

    private fun setupObservers() {
        homeViewModel.networkInFlight.removeObservers(viewLifecycleOwner)
        homeViewModel.networkInFlight.observe(viewLifecycleOwner) {
            if (it) {
                (activity as MainActivity).showBlocker()
            } else {
                (activity as MainActivity).hideBlocker()
            }
        }

        homeViewModel.photosResponse.removeObservers(viewLifecycleOwner)
        homeViewModel.photosResponse.observe(viewLifecycleOwner) { response ->
            response?.let {
                processPhotosResponse(it)
            }
        }
    }

    private fun processPhotosResponse(response : PhotosResponse) {
        homeViewModel.items.add(ListUtil.SpacerItem())
        homeViewModel.items.add(ListUtil.SpacerItem())
        response.photos?.photo?.let {
            for (photo in it) {
                homeViewModel.items.add(ListUtil.PhotosItem(photo))
            }
        }

    }

    private fun getPhotos() {
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

    inner class PhotoListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            when (viewType) {
                ListUtil.TYPE_PHOTO -> {
                    return ListUtil.createPhotosViewHolder(layoutInflater, parent)
                }
                ListUtil.TYPE_DIVIDER -> {
                    return ListUtil.createDividerViewHolder(layoutInflater, parent)
                }
                ListUtil.TYPE_LOADING -> {
                    return ListUtil.createLoadingViewHolder(layoutInflater, parent)
                }
                ListUtil.TYPE_SPACER -> {
                    return ListUtil.createSpacerViewHolder(layoutInflater, parent)
                }
            }

            return ListUtil.DummyViewHolder(View(requireContext()))
        }

        override fun getItemCount(): Int {
            return homeViewModel.items.size
        }

        override fun getItemViewType(position: Int): Int {
            super.getItemViewType(position)
            return homeViewModel.items[position].getListItemType()
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder.itemViewType) {
                ListUtil.TYPE_PHOTO -> {
                    val item = homeViewModel.items[position] as ListUtil.PhotosItem
                    val pHolder = holder as ListUtil.PhotosViewHolder

                    Glide.with(requireContext())
                        .load(item.photo.url_l)
                        .into(pHolder.binding.ivPhoto)

                    item.photo.tags?.let {
                        pHolder.binding.tvTags.text = it
                    }

                    item.photo.owner?.let {
                        pHolder.binding.tvUserId.text = it
                    }
                }
            }
        }
    }

    class HomeViewModel : ViewModel() {
        val items = mutableListOf<ListItem>()

        val networkInFlight = MutableLiveData<Boolean>()
        var photosResponse = MutableLiveData<PhotosResponse>()
    }
}



