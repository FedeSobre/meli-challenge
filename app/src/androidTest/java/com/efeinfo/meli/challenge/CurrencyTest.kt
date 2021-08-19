package com.efeinfo.meli.challenge

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.text.NumberFormat

@RunWith(AndroidJUnit4::class)
class CurrencyTest {

    lateinit var context: Context

    @Before
    fun before() { context = InstrumentationRegistry.getInstrumentation().targetContext }

    // Test a single currency parse
    @Test
    fun single() = runBlocking {

        val number = NumberFormat.getNumberInstance().format(1234)
        Assert.assertEquals("$ $number", Currency.parse("ARS", 1234))
    }

    // Test multiple currency parse
    @Test
    fun multiple() = runBlocking {

        val number = NumberFormat.getNumberInstance().format(1234)

        Assert.assertEquals("$ $number", Currency.parse("ARS", 1234))
        Assert.assertEquals("€ $number", Currency.parse("EUR", 1234))
        Assert.assertEquals("U\$S $number", Currency.parse("USD", 1234))
    }
}