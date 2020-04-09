package ch.epfl.polybazaar.permissions;

import android.Manifest;
import android.view.View;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.polybazaar.FillListingActivity;
import ch.epfl.polybazaar.widgets.permissions.PermissionRequest;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


@RunWith(AndroidJUnit4.class)
public class PermissionRequestTest {

    @Rule
    public final ActivityTestRule<FillListingActivity> permissionActivityTestRule = new ActivityTestRule<>(FillListingActivity.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA);

    @Test
    public void permissionAlreadyGivenReturnsTrue() throws Throwable {
        runOnUiThread(() -> {
            PermissionRequest permissionRequest = new PermissionRequest(permissionActivityTestRule.getActivity(), "CAMERA", null, null, result -> {
                assertThat(result, is(true));
            });
            permissionRequest.assertPermission();
        });
        Thread.sleep(1000);
    }

    @Test
    public void messageCanBeDisplayed() throws Throwable {
        runOnUiThread(() -> {
            PermissionRequest permissionRequest = new PermissionRequest(permissionActivityTestRule.getActivity(), "ACCESS_FINE_LOCAtION", "hello", null, result -> {
                // do nothing
            });
            permissionRequest.assertPermission();
        });
        Thread.sleep(1000);
        onView(withText("hello")).check(matches(isDisplayed()));
    }


}
