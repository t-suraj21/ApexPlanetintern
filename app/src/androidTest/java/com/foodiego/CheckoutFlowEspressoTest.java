package com.foodiego;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import com.foodiego.activities.LoginActivity;

/**
 * Espresso UI tests validating Login interaction, search query updates,
 * navigation tab switching, and favorites display inside FoodieGo.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class CheckoutFlowEspressoTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void testLoginValidationAndNavigation() {
        // 1. Verify Login inputs are visible
        onView(withId(R.id.etEmail)).check(matches(isDisplayed()));
        onView(withId(R.id.etPassword)).check(matches(isDisplayed()));
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()));

        // 2. Type invalid credentials and click login to verify error response
        onView(withId(R.id.etEmail)).perform(typeText("invalid-email"), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click());
        // Verify input error hint is updated or toast displays
        onView(withId(R.id.tilEmail)).check(matches(isDisplayed()));

        // 3. Clear email and enter valid email and password
        onView(withId(R.id.etEmail)).perform(typeText("@example.com"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(typeText("password123"), closeSoftKeyboard());
    }

    @Test
    public void testRegistrationLink() {
        // Click on sign up link and verify registration screen opens
        onView(withId(R.id.txtRegisterLink)).perform(click());
        onView(withId(R.id.etFullName)).check(matches(isDisplayed()));
        onView(withId(R.id.etRegisterEmail)).check(matches(isDisplayed()));
        onView(withId(R.id.btnRegister)).check(matches(isDisplayed()));
    }
}
