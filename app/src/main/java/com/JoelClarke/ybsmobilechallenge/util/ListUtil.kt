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
import com.JoelClarke.ybsmobilechallenge.databinding.ListitemTagBinding
import com.JoelClarke.ybsmobilechallenge.databinding.ListitemUserPhotoBinding
import com.JoelClarke.ybsmobilechallenge.networking.entity.Photo
import com.JoelClarke.ybsmobilechallenge.networking.entity.PhotoTag

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
        const val TYPE_TAG = 995
        const val TYPE_USER_PHOTO = 994

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

        fun createTagViewHolder(
            inflater: LayoutInflater,
            parent: ViewGroup
        ) : TagViewHolder {
            val v = inflater.inflate(R.layout.listitem_tag, parent, false)
            return TagViewHolder(v)
        }

        fun createUserPhotoViewHolder(
            inflater: LayoutInflater,
            parent: ViewGroup
        ) : UserPhotoViewHolder {
            val v = inflater.inflate(R.layout.listitem_user_photo, parent, false)
            return UserPhotoViewHolder(v)
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

    class TagItem(
        tag : PhotoTag.PhotoTagDetails
    ) : ListItem {
        override fun getListItemType(): Int {
            return TYPE_TAG
        }
    }

    class UserPhotoItem(
        url : String
    ) : ListItem {
        override fun getListItemType(): Int {
            return TYPE_USER_PHOTO
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

    class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ListitemTagBinding.bind(itemView)
    }

    class UserPhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ListitemUserPhotoBinding.bind(itemView)
    }
}