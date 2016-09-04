package com.android.wut.placereviewer;

import android.database.Cursor;
import android.os.SystemClock;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.assertion.PositionAssertions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.v7.widget.RecyclerView;
import android.test.suitebuilder.annotation.LargeTest;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.wut.placereviewer.view.list.CommentListFragment;
import com.android.wut.placereviewer.view.main.MainActivity;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Random;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.AdditionalMatchers.not;

/**
 * Created by soive on 16.06.2016.
 */
@RunWith(MockitoJUnitRunner.class)
@LargeTest
public class CommentsViewTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Before
    public void goToCommentView() {
        onView((withId(R.id.pubs_tile_list_btn))).perform(click());
        SystemClock.sleep(1000);
        onView(withId(R.id.place_list_recycleview))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        SystemClock.sleep(1000);
    }

    @Test
    public void addCommentTest() {
        Random rand = new Random();
        String testComm = "test comment " + rand.nextInt();
        onView(withId(R.id.et_new_comment))
                .perform(typeText(testComm), closeSoftKeyboard());
        onView(withId(R.id.btn_add_comment)).perform(click());
        SystemClock.sleep(3000);
        RecyclerView r = (RecyclerView) mActivityRule.getActivity().findViewById(R.id.comment_list_recycleview);
        int i = r.getChildCount();
        RelativeLayout o =  (RelativeLayout)r.getChildAt(i-1);
        TextView t = (TextView) o.getChildAt(0);
   //     assertThat(t.getText(),is(testComm));
    }
}
