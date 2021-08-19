package com.efeinfo.meli.challenge

import android.content.Context
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.runner.RunWith
import com.efeinfo.meli.challenge.activities.ActivityMain
import org.junit.Test
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.contrib.RecyclerViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.efeinfo.meli.challenge.actions.ClickItem
import com.efeinfo.meli.challenge.actions.ScrollToBottom
import com.efeinfo.meli.challenge.actions.WaitGone
import org.hamcrest.CoreMatchers.*
import java.lang.Exception

@RunWith(AndroidJUnit4::class)
@LargeTest
class UIFullTest {

    private lateinit var scenario: ActivityScenario<ActivityMain>
    private lateinit var context: Context

    // Fully automated user interface test that tests most of the aspects of the UI
    @Test
    fun fullTest() {

        scenario = ActivityScenario.launch(ActivityMain::class.java)
        context = InstrumentationRegistry.getInstrumentation().targetContext

        context.getSharedPreferences("com.efeinfo.meli.challenge", Context.MODE_PRIVATE).edit().remove("Favorites").commit()

        val queries = listOf("celular", "computadora", "cocina", "mesa", "cama")

        var iteration = 0

        while (true) {

            onView(withId(R.id.Progress)).perform(WaitGone())

            try { onView(withId(R.id.Categories)).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(iteration, click())) }
            catch (ex: Exception) { break }

            results()
            search(queries[iteration % 5])
            results()
            favorites()
            iteration++
        }
    }

    // Search the given query
    fun search(query: String) {

        onView(withId(R.id.SearchArea)).perform(click())

        onView(withId(R.id.Query)).perform(typeText(query))
        onView(withId(R.id.Query)).perform(pressImeActionButton())
    }

    // Navigates through the results, favorites first 50 results and loads first 250 results
    fun results() {

        onView(withId(R.id.Progress)).perform(WaitGone())

        var accumulate = 0

        for (refresh in 0 until 5) {

            var switch = false

            for (i in 0 until 10) {

                if (switch) onView(withId(R.id.Products)).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(accumulate + i, ClickItem(R.id.Favorite)))

                onView(withId(R.id.Products)).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(accumulate + i, ClickItem(R.id.Root)))

                onView(withId(R.id.Progress)).perform(WaitGone())

                if (!switch) onView(withId(R.id.Favorite)).perform(click())

                Espresso.pressBack()

                switch = !switch
            }

            accumulate += 10

            onView(withId(R.id.Products)).perform(ScrollToBottom())

            try { onView(allOf(instanceOf(ProgressBar::class.java), withEffectiveVisibility(Visibility.VISIBLE))).perform(WaitGone()) }
            catch (ex: Exception) { break }
        }

        Espresso.pressBack()
    }

    // Load all the favorites and then removes them
    fun favorites() {

        onView(withId(R.id.Favorites)).perform(click())

        onView(withId(R.id.Progress)).perform(WaitGone())

        while (true) {

            onView(withId(R.id.Products)).perform(ScrollToBottom())

            try { onView(allOf(instanceOf(ProgressBar::class.java), withEffectiveVisibility(Visibility.VISIBLE))).perform(WaitGone()) }
            catch (ex: Exception) { break }
        }

        var index = 0

        while (true) try { onView(withId(R.id.Products)).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(index++, ClickItem(R.id.Favorite))) } catch (ex: Exception) { break }

        Espresso.pressBack()
    }
}