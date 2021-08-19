package com.efeinfo.meli.challenge.actions

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import org.hamcrest.CoreMatchers.any
import org.hamcrest.Matcher

class WaitGone : ViewAction {

    override fun getConstraints(): Matcher<View> { return any(View::class.java) }

    override fun getDescription(): String { return "Wait unconditionally until a view is gone" }

    override fun perform(uiController: UiController, view: View) {

        while (true) {

            if (view.visibility == View.GONE) return
            uiController.loopMainThreadForAtLeast(50)
        }
    }
}