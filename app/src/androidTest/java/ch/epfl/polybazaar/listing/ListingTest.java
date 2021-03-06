package ch.epfl.polybazaar.listing;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import ch.epfl.polybazaar.listing.Listing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
public class ListingTest {

    private Listing listing1 = new Listing("Livre", "Livre d'algèbre linéaire", "22 CHF", "test.user@epfl.ch", "Furniture");

    @Test
    public void checkTitle() {
        assertEquals("Livre", listing1.getTitle());
    }

    @Test
    public void checkDesc() {
        assertEquals("Livre d'algèbre linéaire", listing1.getDescription());
    }

    @Test
    public void checkPrice() {
        assertEquals("22 CHF", listing1.getPrice());
    }

    @Test
    public void checkEmail() {
        assertEquals("test.user@epfl.ch", listing1.getUserEmail());
    }

    @Test
    public void checkStringImage() {
        assertThat(listing1.getImagesRefs(), is(empty()));
    }

    @Test (expected = IllegalArgumentException.class)
    public void checkFalseUserEmail() {
        new Listing("Livre", "Livre d'algèbre linéaire", "22 CHF", "test.user@gmail.com", "Furniture");
    }
}
