package ch.epfl.polybazaar;

import android.provider.ContactsContract;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.polybazaar.database.Datastore;
import ch.epfl.polybazaar.database.DatastoreFactory;
import ch.epfl.polybazaar.database.callback.ListingCallback;
import ch.epfl.polybazaar.database.callback.ListingCallbackAdapter;
import ch.epfl.polybazaar.database.callback.SuccessCallback;
import ch.epfl.polybazaar.listing.Listing;
import ch.epfl.polybazaar.listing.ListingDatabase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class ListingDatabaseTest {



    @Test
    public void callListingCallback() {
        final Listing[] listing = new Listing[1];
        ListingCallback callback = new ListingCallback() {
            @Override
            public void onCallback(Listing result) {
                listing[0] = result;
            }
        };
        Listing inside = new Listing("test", "none", "22CHF", "m.m@epfl.ch");
        callback.onCallback(inside);
        assertThat(listing[0].getTitle(), is("test"));
        assertThat(listing[0].getUserEmail(), is("m.m@epfl.ch"));
        assertThat(listing[0].getDescription(), is("none"));
        assertThat(listing[0].getPrice(), is("22CHF"));
    }

    @Test
    public void callListingCallbackAdapter() {
        final Listing[] listing = new Listing[1];
        ListingCallback callback = new ListingCallback() {
            @Override
            public void onCallback(Listing result) {
                listing[0] = result;
            }
        };
        ListingCallbackAdapter adapter = new ListingCallbackAdapter(callback);
    }

<<<<<<< HEAD
    private boolean storeSuccess;
    private Listing receivedListing;
=======
    boolean storeSuccess;
    Listing receivedListing;
>>>>>>> 997e2eb79f7c4293c72f095d72265156786b5e19
    @Test
    public void storeAndRetrieveListingTest(){
        MockDatastore myDatastore = new MockDatastore();
        myDatastore.addCollection("listings");
        DatastoreFactory.setDependency(myDatastore);

        Listing testListing = new Listing("testListing","testDescription","testPrice","test@epfl.ch");
        ListingDatabase.storeListing(testListing, "testId", result -> storeSuccess = true);
        assertThat(storeSuccess,is(true));

        ListingDatabase.fetchListing("testId",result -> receivedListing = result);
        assertThat(receivedListing.getTitle(),is("testListing"));
        assertThat(receivedListing.getDescription(),is("testDescription"));
        assertThat(receivedListing.getPrice(),is("testPrice"));
        assertThat(receivedListing.getUserEmail(),is("test@epfl.ch"));



    }

<<<<<<< HEAD
   private boolean deleteSuccess;
=======
    boolean deleteSuccess;
>>>>>>> 997e2eb79f7c4293c72f095d72265156786b5e19
    @Test
    public void deleteListing(){
        Listing testListing = new Listing("testListing","testDescription",
                "testPrice","test@epfl.ch");
        MockDatastore myDataStore = new MockDatastore();
        myDataStore.addCollection("listings");
        myDataStore.setupMockData("listings","testId",testListing);
        DatastoreFactory.setDependency(myDataStore);
        Datastore db = DatastoreFactory.getDependency();
        ListingDatabase.deleteListing("testId",result -> deleteSuccess= true);
        assertThat(deleteSuccess,is(true));


    }
<<<<<<< HEAD
    private Listing wrongListing;
=======
    Listing wrongListing;
>>>>>>> 997e2eb79f7c4293c72f095d72265156786b5e19
    @Test
    public void wrongIdReturnNull(){
        MockDatastore myDatastore = new MockDatastore();
        myDatastore.addCollection("listings");
        DatastoreFactory.setDependency(myDatastore);
        Datastore db = DatastoreFactory.getDependency();

        ListingDatabase.fetchListing("wrondId",result -> wrongListing = result);
        assert(wrongListing == null);
    }
}