package com.android.wut.placereviewer;

import android.os.SystemClock;
import android.support.test.rule.ActivityTestRule;
import android.test.suitebuilder.annotation.LargeTest;

import com.android.wut.placereviewer.view.main.MainActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by soive on 16.06.2016.
 */
@RunWith(MockitoJUnitRunner.class)
@LargeTest
public class PlaceMapFragmentTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);
    @Before
    public void goToMapFragment() {
        onView((withId(R.id.pubs_tile_map_btn))).perform(click());
        SystemClock.sleep(1000);
    }

    @Test
    public void mapFragmentItemsVisible() {
        onView(withId(R.id.map)).check(matches(isDisplayed()));
    }
}
