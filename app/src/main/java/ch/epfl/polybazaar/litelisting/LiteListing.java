package ch.epfl.polybazaar.litelisting;


/**
 * If you attributes of this class, also change its CallbackAdapter and Utilities
 */
public class LiteListing {

    // TODO: add attribute thumbnail

    private String listingID;
    private String title;
    private String price;
    private String category;

    public LiteListing(String listingID, String title, String price, String category) {
        this.title = title;
        this.listingID = listingID;
        this.price = price;
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public String getListingID() {
        return listingID;
    }

    public String getPrice() {
        return price;
    }

    public String getCategory(){
        return category;
    }

}
