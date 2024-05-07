package com.example.kodaapplication


import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.kodaapplication.Activities.LoginActivity
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @Rule
    @JvmField
    var mActivityScenarioRule = ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun loginActivityTest() {
        val appCompatEditText = onView(
            allOf(
                withId(R.id.login_username),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.inputLayout_email),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        appCompatEditText.perform(replaceText("dan"), closeSoftKeyboard())

        val appCompatEditText2 = onView(
            allOf(
                withId(R.id.login_password),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.inputLayout_password2),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        appCompatEditText2.perform(replaceText("dan"), closeSoftKeyboard())

        val materialButton = onView(
            allOf(
                withId(R.id.login_button), withText("Login"),
                childAtPosition(
                    childAtPosition(
                        withId(android.R.id.content),
                        0
                    ),
                    3
                ),
                isDisplayed()
            )
        )
        materialButton.perform(click())

        val appCompatEditText3 = onView(
            allOf(
                withId(R.id.login_password), withText("dan"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.inputLayout_password2),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        appCompatEditText3.perform(click())

        val appCompatEditText4 = onView(
            allOf(
                withId(R.id.login_password), withText("dan"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.inputLayout_password2),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        appCompatEditText4.perform(replaceText("dan123"))

        val appCompatEditText5 = onView(
            allOf(
                withId(R.id.login_password), withText("dan123"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.inputLayout_password2),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        appCompatEditText5.perform(closeSoftKeyboard())

        val materialButton2 = onView(
            allOf(
                withId(R.id.login_button), withText("Login"),
                childAtPosition(
                    childAtPosition(
                        withId(android.R.id.content),
                        0
                    ),
                    3
                ),
                isDisplayed()
            )
        )
        materialButton2.perform(click())

        val materialButton3 = onView(
            allOf(
                withId(R.id.child_button), withText("Child"),
                childAtPosition(
                    childAtPosition(
                        withId(android.R.id.content),
                        0
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        materialButton3.perform(click())

        val extendedFloatingActionButton = onView(
            allOf(
                withId(R.id.fab), withText("Add Child"),
                childAtPosition(
                    allOf(
                        withId(R.id.cchild_homescreen),
                        childAtPosition(
                            withId(android.R.id.content),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        extendedFloatingActionButton.perform(click())

        val materialButton4 = onView(
            allOf(
                withId(R.id.buttonChild), withText("Add child"),
                childAtPosition(
                    allOf(
                        withId(R.id.inner_Layout_AB),
                        childAtPosition(
                            withId(R.id.near_Out),
                            5
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        materialButton4.perform(click())

        pressBack()

        val materialButton5 = onView(
            allOf(
                withId(R.id.parent_button), withText("Parent"),
                childAtPosition(
                    childAtPosition(
                        withId(android.R.id.content),
                        0
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        materialButton5.perform(click())

        val recyclerView = onView(
            allOf(
                withId(R.id.main_recyclerView),
                childAtPosition(
                    withClassName(`is`("androidx.coordinatorlayout.widget.CoordinatorLayout")),
                    0
                )
            )
        )
        recyclerView.perform(actionOnItemAtPosition<ViewHolder>(3, click()))

        val materialButton6 = onView(
            allOf(
                withId(R.id.button_lock_apps), withText("Lock Apps"),
                childAtPosition(
                    allOf(
                        withId(R.id.the_LayoutForThisChildGetter),
                        childAtPosition(
                            withId(R.id.main),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        materialButton6.perform(click())

        pressBack()

        val materialButton7 = onView(
            allOf(
                withId(R.id.button_WebFilter), withText("Web Filtering"),
                childAtPosition(
                    allOf(
                        withId(R.id.the_LayoutForThisChildGetter),
                        childAtPosition(
                            withId(R.id.main),
                            0
                        )
                    ),
                    3
                ),
                isDisplayed()
            )
        )
        materialButton7.perform(click())

        val materialButton8 = onView(
            allOf(
                withId(R.id.filter_button), withText("Filter Word"),
                childAtPosition(
                    allOf(
                        withId(R.id.main),
                        childAtPosition(
                            withId(android.R.id.content),
                            0
                        )
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        materialButton8.perform(click())

        val materialButton9 = onView(
            allOf(
                withId(R.id.block_button), withText("Block Site"),
                childAtPosition(
                    allOf(
                        withId(R.id.main),
                        childAtPosition(
                            withId(android.R.id.content),
                            0
                        )
                    ),
                    5
                ),
                isDisplayed()
            )
        )
        materialButton9.perform(click())

        pressBack()

        val materialButton10 = onView(
            allOf(
                withId(R.id.button_screentime), withText("Screentime\nManagement"),
                childAtPosition(
                    allOf(
                        withId(R.id.the_LayoutForThisChildGetter),
                        childAtPosition(
                            withId(R.id.main),
                            0
                        )
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        materialButton10.perform(click())

        pressBack()

        val recyclerView2 = onView(
            allOf(
                withId(R.id.main_recyclerView),
                childAtPosition(
                    withClassName(`is`("androidx.coordinatorlayout.widget.CoordinatorLayout")),
                    0
                )
            )
        )
        recyclerView2.perform(actionOnItemAtPosition<ViewHolder>(2, click()))

        pressBack()

        val recyclerView3 = onView(
            allOf(
                withId(R.id.main_recyclerView),
                childAtPosition(
                    withClassName(`is`("androidx.coordinatorlayout.widget.CoordinatorLayout")),
                    0
                )
            )
        )
        recyclerView3.perform(actionOnItemAtPosition<ViewHolder>(1, click()))

        pressBack()

        val recyclerView4 = onView(
            allOf(
                withId(R.id.main_recyclerView),
                childAtPosition(
                    withClassName(`is`("androidx.coordinatorlayout.widget.CoordinatorLayout")),
                    0
                )
            )
        )
        recyclerView4.perform(actionOnItemAtPosition<ViewHolder>(0, click()))

        pressBack()

        pressBack()

        val materialButton11 = onView(
            allOf(
                withId(android.R.id.button1), withText("Yes"),
                childAtPosition(
                    childAtPosition(
                        withId(androidx.appcompat.R.id.buttonPanel),
                        0
                    ),
                    3
                )
            )
        )
        materialButton11.perform(scrollTo(), click())

        pressBack()

        pressBack()
    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
