package com.JoelClarke.ybsmobilechallenge.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.JoelClarke.ybsmobilechallenge.R
import com.JoelClarke.ybsmobilechallenge.databinding.ListitemDividerBinding
import com.JoelClarke.ybsmobilechallenge.databinding.ListitemLoadingBinding
import com.JoelClarke.ybsmobilechallenge.databinding.ListitemPhotoItemBinding
import com.JoelClarke.ybsmobilechallenge.databinding.ListitemSpacerBinding
import com.JoelClarke.ybsmobilechallenge.networking.entity.Photo

class ListUtil {

    /**
     * in the campanion object I'll hold all the different type integer values.
     * We'll also hold all the fun's to create the view holders for teh recyclerview adapters
     */
    companion object {
        const val TYPE_SPACER = 999
        const val TYPE_DIVIDER = 998
        const val TYPE_LOADING = 997
        const val TYPE_PHOTO = 996

        fun createSpacerViewHolder(
            inflater : LayoutInflater,
            parent : ViewGroup
        ) : SpacerViewHolder {
            val v = inflater.inflate(R.layout.listitem_spacer, parent, false)
            return SpacerViewHolder(v)
        }

        fun createDividerViewHolder(
            inflater : LayoutInflater,
            parent : ViewGroup
        ) : DividerViewHolder {
            val v = inflater.inflate(R.layout.listitem_divider, parent, false)
            return DividerViewHolder(v)
        }

        fun createLoadingViewHolder(
            inflater : LayoutInflater,
            parent : ViewGroup
        ) : LoadingViewHolder {
            val v = inflater.inflate(R.layout.listitem_loading, parent, false)
            return LoadingViewHolder(v)
        }

        fun createPhotosViewHolder(
            inflater: LayoutInflater,
            parent : ViewGroup
        ) : PhotosViewHolder {
            val v = inflater.inflate(R.layout.listitem_photo_item, parent, false)
            return PhotosViewHolder(v)
        }
    }

    /**
     *  Different list items used in RecyclerViews throughout the app
     */

    class PhotosItem(
        var photo : Photo
    ) : ListItem {
        override fun getListItemType(): Int {
            return TYPE_PHOTO
        }
    }

    class SpacerItem() : ListItem {
        override fun getListItemType(): Int {
            return TYPE_SPACER
        }
    }

    class LoadingItem() : ListItem {
        override fun getListItemType(): Int {
            return TYPE_LOADING
        }
    }

    class DividerItem() : ListItem {
        override fun getListItemType(): Int {
            return TYPE_DIVIDER
        }
    }

    /**
     * This section will contain all the view holders for the different items we use
     */
    class DummyViewHolder(view : View) : RecyclerView.ViewHolder(view)

    class SpacerViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val binding = ListitemSpacerBinding.bind(itemView)
    }

    class LoadingViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val binding = ListitemLoadingBinding.bind(itemView)
    }

    class DividerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ListitemDividerBinding.bind(itemView)
    }

    class PhotosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ListitemPhotoItemBinding.bind(itemView)
    }
}