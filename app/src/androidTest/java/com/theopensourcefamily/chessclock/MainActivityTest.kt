package com.theopensourcefamily.chessclock

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {

  @get:Rule
  var activityRule: ActivityTestRule<ClocksActivity> = ActivityTestRule(ClocksActivity::class.java)

  fun runOnUIThread(block: () -> Unit) {
    activityRule.runOnUiThread(block)
  }

  @Test
  fun showClocks() {
    runOnUIThread { activityRule.activity.render(ClockState.Stopped(300, 300)) }
    onView(withId(R.id.blackClock)).check(matches(isDisplayed()))
    onView(withId(R.id.blackClock)).check(matches(withText("300")))
    onView(withId(R.id.whiteClock)).check(matches(isDisplayed()))
    onView(withId(R.id.whiteClock)).check(matches(withText("300")))
  }
}
