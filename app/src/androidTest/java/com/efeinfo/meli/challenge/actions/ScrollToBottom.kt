package com.efeinfo.meli.challenge.actions

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher

class ScrollToBottom : ViewAction {

    override fun getConstraints(): Matcher<View> { return CoreMatchers.any(View::class.java) }

    override fun getDescription(): String { return "Scrolls to bottom of a recycler view" }

    override fun perform(uiController: UiController, view: View) { (view as RecyclerView).apply {

        scrollToPosition(adapter!!.itemCount - 1) }
    }
}