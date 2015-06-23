package com.largerlife.demo.weatherwhere;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by LargerLife on 18/06/15.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Rule
    public final ActivityRule<MainActivity> main = new ActivityRule<>(MainActivity.class);

    @Test
    public void shouldBeAbleToLaunchMainScreen() {
        onView(withId(R.id.fab)).check(matches(isDisplayed()));
    }

    @Test
    public void MyLocationButoon_ShouldBeClickabble() {
        onView(withId(R.id.fab)).perform();
    }
}
