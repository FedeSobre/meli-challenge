package com.efeinfo.meli.challenge.actions

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher

class ClickItem(val id: Int) : ViewAction {

    override fun getConstraints(): Matcher<View> { return CoreMatchers.any(View::class.java) }

    override fun getDescription(): String { return "Click on a child view with specified id" }

    override fun perform(uiController: UiController, view: View) { view.findViewById<View>(id).performClick() }
}