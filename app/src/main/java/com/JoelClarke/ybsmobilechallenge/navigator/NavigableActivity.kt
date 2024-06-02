package com.JoelClarke.ybsmobilechallenge.navigator

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.JoelClarke.ybsmobilechallenge.R

/**
 * Activity that implements Navigator compatibility
 */
open class NavigableActivity : AppCompatActivity(), Navigator.Navigable {

    var contentId : Int = R.id.fContent;

    override fun navigate(navigator: Navigator) {
        if (navigator.backstackTo != null) {
            if (navigator.backstackTo == Navigator.BACKSTACK_VOID_ALL) {
                voidBackstack(null)
            } else {
                voidBackstack(navigator.backstackTo)
            }
        } else if (navigator.popLastBackStackTag != null) {
            if (navigator.popLastBackStackTag!!) {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    val lastTag = supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 1)
                    voidBackstack(lastTag.name)
                }
            }
        }

        if (navigator.fragment != null) {
            val transaction = supportFragmentManager.beginTransaction()

            if (navigator.transitionEnterRes != 0 && navigator.transitionExitRes != 0) {
                if (navigator.transitionPopEnterRes != 0 && navigator.transitionPopExitRes != 0) {
                    transaction.setCustomAnimations(navigator.transitionEnterRes, navigator.transitionExitRes, navigator.transitionPopEnterRes, navigator.transitionPopExitRes)
                } else {
                    transaction.setCustomAnimations(navigator.transitionEnterRes, navigator.transitionExitRes)
                }
            }

            transaction.replace(contentId, navigator.fragment!!)
            if (navigator.backstackTag != null) {
                transaction.addToBackStack(navigator.backstackTag)
            }

            transaction.commit()
        }
    }

    override fun hasBackstackEntry(tag: String): Boolean {
        val manager = supportFragmentManager
        if (manager.backStackEntryCount > 0) {
            for (i in 0 until manager.backStackEntryCount) {
                if (manager.getBackStackEntryAt(i).name == tag) {
                    return true
                }
            }
        }
        return false
    }

    open fun voidBackstack(backUntilTag: String?) {
        var hasBackstackEntry = false
        for (i in 0 until supportFragmentManager.backStackEntryCount) {
            if (supportFragmentManager.getBackStackEntryAt(i).name == backUntilTag) {
                hasBackstackEntry = true
                break
            }
        }

        var outBackstackTag = backUntilTag
        if (!hasBackstackEntry) {
            outBackstackTag = null
        }

        supportFragmentManager.popBackStackImmediate(
            outBackstackTag,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    open fun voidBackstackAndNavigate(backUntilTag: String?, navigator: Navigator) {
        voidBackstack(backUntilTag)
        navigate(navigator)
    }
}