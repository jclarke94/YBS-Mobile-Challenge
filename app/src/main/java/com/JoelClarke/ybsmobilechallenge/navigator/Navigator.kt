package com.JoelClarke.ybsmobilechallenge.navigator

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.JoelClarke.ybsmobilechallenge.R

/**
 * Navigator - A Navigation library
 * v2.0-k
 *
 * Navigator takes the complexity out of Navigating around Fragments in Android.
 *
 * This implementation is written natively for Kotlin apps. You will want to use NavigableActivity
 * and NavigableFragment with Navigator, both of which are included with this project.
 */
class Navigator {

    var fragment : Fragment? = null             // The fragment to transition to
    var backstackTag : String? = null           // The backstack tag to associate
    var backstackTo : String? = null            // The backstack tag to pop immediately
    var popLastBackStackTag : Boolean? = null   // If set the last backstack tag will be popped
    var transitionEnterRes : Int = 0            // Entry Animation Resource
    var transitionExitRes : Int = 0             // Exit Animation Resource
    var transitionPopEnterRes : Int = 0         // Popped Fragmnet Entry Animation
    var transitionPopExitRes : Int = 0          // Popped Fragment Exit Animation

    companion object Builder {
        /**
         * If passed to backstackTo will clear all Fragments on the stack
         */
        const val BACKSTACK_VOID_ALL = "__VOIDITALL__"

        /** Simple left-to-right animation  */
        const val TRANSITION_PRESET_SLIDE = 1
        /** Simple fade-out, fade-in animation  */
        const val TRANSITION_PRESET_FADE = 2
        /** Complex scaling and fading animation  */
        const val TRANSITION_PRESET_ZOOM = 3
        /** New fragment will slide up over the existing fragment  */
        const val TRANSITION_PRESET_SLIDE_OVER = 4

        var navigable : Navigable? = null
        var navigator : Navigator = Navigator()

        /**
         * Provide the host to perform a Fragment transaction on
         */
        fun with(navigable : Navigable) : Builder {
            this.navigable = navigable
            navigator = Navigator()

            return this
        }

        /**
         * Alternative to with(), provide an Activity (must implement Navigable)
         */
        fun withActivity(activity : Activity) : Builder {
            if (activity is Navigable) {
                this.navigable = activity
                navigator = Navigator()
            } else {
                throw ClassCastException("Activity passed to Navigator Builder was not a Navigable.")
            }

            return this
        }

        /**
         * Alternative to withActivity() specifically for FragmentActivity Activities.
         */
        fun withActivity(activity : FragmentActivity?) : Builder {
            return withActivity(activity as Activity)
        }

        /**
         * Provide the Fragment to transact
         */
        fun fragment(fragment : Fragment) : Builder {
            navigator.fragment = fragment
            return this;
        }

        /**
         * Specifies a Backstack Tag to associate with the Fragment Transaction
         */
        fun backstackTag(backstackTag : String) : Builder {
            navigator.backstackTag = backstackTag
            return this
        }

        /**
         * If set will pop to the specified backstack (or not, if it doesn't exist)
         */
        fun backstackTo(backstackTo : String) : Builder {
            navigator.backstackTo = backstackTo
            return this
        }

        /**
         * If set will pop the last Fragment on the stack. Ideal if you are navigating backwards.
         * Protip: Do not call activity.onBackPressed(), this is MUCH safer.
         */
        fun popBackstack() : Builder {
            navigator.popLastBackStackTag = true
            return this
        }

        /**
         * Commit the transaction to the currently specified Navigable
         */
        fun navigate() {
            navigable?.navigate(navigator)
        }

        /**
         * Return the current Navigator so you can do whatever you want with it
         */
        fun build() : Navigator {
            return navigator
        }

        /**
         * Specify the entry animation for the transacted Fragment
         */
        fun transitionEnterRes(res : Int) {
            navigator.transitionEnterRes = res
        }

        /**
         * Specify the animation you want for the transacted Fragment to use when popped later
         */
        fun transitionExitRes(res : Int) {
            navigator.transitionExitRes = res
        }

        /**
         * Specify the animation you want the popped Fragment to use if it returns to the front
         */
        fun transitionPopEnterRes(res : Int) {
            navigator.transitionPopEnterRes = res
        }

        /**
         * Specify the animation you want when the popped fragment gets transacted away
         */
        fun transitionPopExitRes(res : Int) {
            navigator.transitionPopExitRes = res
        }

        /**
         * Provide a Transition Preset that manages the heavy lifting of setting animations yourself
         *
         * Built in options include TRANSITION_PRESET_SLIDE, TRANSITION_PRESET_ZOOM,
         * TRANSITION_PRESET_FADE, TRANSITION_PRESET_SLIDE_OVER
         */
        fun transitionPreset(preset : Int) : Builder {
            if (preset == TRANSITION_PRESET_SLIDE) {
                navigator.transitionEnterRes = R.anim.fragment_slide_top_enter
                navigator.transitionExitRes = R.anim.fragment_slide_bottom_exit
                navigator.transitionPopEnterRes = R.anim.fragment_slide_bottom_enter
                navigator.transitionPopExitRes = R.anim.fragment_slide_top_exit
            } else if (preset == TRANSITION_PRESET_ZOOM) {
                navigator.transitionEnterRes = R.anim.fragment_zoom_enter
                navigator.transitionExitRes = R.anim.fragment_zoom_exit
                navigator.transitionPopEnterRes = R.anim.fragment_zoom_enter
                navigator.transitionPopEnterRes = R.anim.fragment_zoom_exit
            } else if (preset == TRANSITION_PRESET_FADE) {
                navigator.transitionEnterRes = R.anim.fragment_fade_enter
                navigator.transitionExitRes = R.anim.fragment_fade_exit
                navigator.transitionPopEnterRes = R.anim.fragment_fade_enter
                navigator.transitionPopEnterRes = R.anim.fragment_fade_exit
            } else if (preset == TRANSITION_PRESET_SLIDE_OVER) {
                navigator.transitionEnterRes = R.anim.fragment_slide_over_enter
                navigator.transitionExitRes = R.anim.fragment_slide_over_exit
                navigator.transitionPopEnterRes = R.anim.fragment_slide_over_pop_enter
                navigator.transitionPopEnterRes = R.anim.fragment_slide_over_pop_exit
            }

            return this
        }

    }

    /**
     * The Navigable Interface should be implemented by all Activities and Fragments you wish to use
     * with Navigator.
     */
    interface Navigable {
        fun navigate(navigator : Navigator)
        fun hasBackstackEntry(tag : String) : Boolean
    }

}