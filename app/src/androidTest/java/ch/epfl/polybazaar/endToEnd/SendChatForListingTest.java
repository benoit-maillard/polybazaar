package ch.epfl.polybazaar.endToEnd;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;

import ch.epfl.polybazaar.MainActivity;
import ch.epfl.polybazaar.R;
import ch.epfl.polybazaar.chat.ChatMessage;
import ch.epfl.polybazaar.filestorage.FileStoreFactory;
import ch.epfl.polybazaar.filestorage.LocalCache;
import ch.epfl.polybazaar.filestorage.MockFileStore;
import ch.epfl.polybazaar.login.AuthenticatorFactory;
import ch.epfl.polybazaar.login.MockAuthenticator;
import ch.epfl.polybazaar.testingUtilities.DatabaseChecksUtilities;
import ch.epfl.polybazaar.testingUtilities.DatabaseStoreUtilities;
import ch.epfl.polybazaar.testingUtilities.SignInUtilities;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static ch.epfl.polybazaar.database.datastore.DataStoreFactory.useMockDataStore;

public class SendChatForListingTest {
    @Rule
    public final ActivityTestRule<MainActivity> activityRule =
            new ActivityTestRule<MainActivity>(MainActivity.class){
                @Override
                protected void beforeActivityLaunched() {
                    AuthenticatorFactory.setDependency(MockAuthenticator.getInstance());
                    useMockDataStore();
                    FileStoreFactory.setDependency(MockFileStore.getInstance());
                    LocalCache.setRoot("test-cache");
                }
                @Override
                protected void afterActivityFinished() {
                    MockAuthenticator.getInstance().reset();
                    MockFileStore.getInstance().cleanUp();
                    LocalCache.cleanUp(InstrumentationRegistry.getInstrumentation().getContext());
                }
            };

    //@Test
    public void testMessagesAreDisplayedInChat() throws InterruptedException {
        String otherUserEmail = "otherother.user@epfl.ch";
        String title = "Send chat";
        String message = "Hello how are you?";
        String message2 = "Fine and you?";
        String id = "MyFakeID";
        SignInUtilities.signInWithFromMainActivity(MockAuthenticator.TEST_USER_EMAIL, MockAuthenticator.TEST_USER_PASSWORD);
        DatabaseStoreUtilities.storeNewMessage(otherUserEmail, MockAuthenticator.TEST_USER_EMAIL, id, message);
        DatabaseStoreUtilities.storeNewMessage(MockAuthenticator.TEST_USER_EMAIL, otherUserEmail, id, message2);
        DatabaseStoreUtilities.storeNewListing(title, otherUserEmail, id);

        onView(withId(R.id.action_home)).perform(click());
        //onView(withText(title)).perform(click());
        //onView(withId(R.id.contactSel)).perform(scrollTo(), click());


        onView(withText(message)).check(matches(isDisplayed()));
        onView(withText(message2)).check(matches(isDisplayed()));
    }
}
