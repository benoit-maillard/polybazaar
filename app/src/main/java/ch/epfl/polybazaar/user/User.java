package ch.epfl.polybazaar.user;

import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;

import ch.epfl.polybazaar.database.SimpleField;
import ch.epfl.polybazaar.database.Model;
import ch.epfl.polybazaar.database.ModelTransaction;
import ch.epfl.polybazaar.listing.Listing;

import static ch.epfl.polybazaar.Utilities.emailIsValid;
import static ch.epfl.polybazaar.Utilities.nameIsValid;


/**
 * If you change attributes of this class, also change its CallbackAdapter and Utilities
 */
public final class User extends Model {
    private final SimpleField<String> nickName = new SimpleField<>("nickName");
    private final SimpleField<String> email = new SimpleField<>("email");
    private final SimpleField<String> firstName = new SimpleField<>("firstName");
    private final SimpleField<String> lastName = new SimpleField<>("lastName");
    private final SimpleField<String> phoneNumber = new SimpleField<>("phoneNumber");
    private final SimpleField<String> profilePicture = new SimpleField<>("profilePicture");
    private final SimpleField<ArrayList<String>> ownListings = new SimpleField<>("ownListings", new ArrayList<>());
    private final SimpleField<ArrayList<String>> favorites = new SimpleField<>("favorites", new ArrayList<>());
    private final SimpleField<String> token = new SimpleField<>("token");

    private final static String COLLECTION = "users";
    public final static String NO_PROFILE_PICTURE = "no_picture";

    public String getNickName() {
        return nickName.get();
    }

    public String getEmail() {
        return email.get();
    }

    public String getFirstName(){
        return firstName.get();
    }

    public String getLastName(){
        return lastName.get();
    }

    public String getPhoneNumber(){
        return phoneNumber.get();
    }

    public String getToken() {
        return token.get();
    }

    public String getProfilePicture(){
        if (profilePicture.get() != null) {
            return profilePicture.get();
        } else {
            return NO_PROFILE_PICTURE;
        }
    }

    // no-argument constructor so that instances can be created by ModelTransaction
    public User() {
        registerFields(nickName, email, firstName, lastName, phoneNumber, profilePicture, ownListings, favorites, token);
    }

    /**
     * User Constructor
     * @param nickName name displayed on listing
     * @param email email address, unique identifier (key)
     * @throws IllegalArgumentException
     */
    public User(String nickName, String email) {
        this();
        if (nameIsValid(nickName)) {
            this.nickName.set(nickName);
        } else {
            throw new IllegalArgumentException("nickName has invalid format");
        }
        if (emailIsValid(email)) {
            this.email.set(email);
        } else {
            throw new IllegalArgumentException("email has invalid format");
        }
        firstName.set(capitalize(email.substring(0, email.indexOf("."))));
        lastName.set(capitalize(email.substring(email.indexOf(".")+1, email.indexOf("@"))));
        phoneNumber.set("");
        profilePicture.set(NO_PROFILE_PICTURE);
        this.token.set(FirebaseInstanceId.getInstance().getToken());
    }

    /**
     * User contructor where names and phone number can be specified
     * @param nickName name displayed on listing
     * @param email email address, unique identifier (key)
     * @param firstName User's first name
     * @param lastName User's last name
     * @param phoneNumber User's phone number
     * @param profilePicture StringImage that is the user's profile Picture
     */
    public User(String nickName, String email, String firstName, String lastName, String phoneNumber,
                String profilePicture, ArrayList<String> ownListings, ArrayList<String> favorites){
        this(nickName, email);
        this.firstName.set(firstName);
        this.lastName.set(lastName);
        this.phoneNumber.set(phoneNumber);
        this.profilePicture.set(profilePicture);
        this.ownListings.set(ownListings);
        this.favorites.set(favorites);
        this.token.set(FirebaseInstanceId.getInstance().getToken());
    }

    @Override
    public String collectionName() {
        return COLLECTION;
    }

    @Override
    public String getId() {
        return email.get();
    }

    @Override
    public void setId(String id) {
        this.email.get();
    }

    public ArrayList<String> getFavorites() {
        return favorites.get();
    }

    public ArrayList<String> getOwnListings() {
        return ownListings.get();
    }

    public void addOwnListing(String liteListingId) {
        ownListings.get().add(liteListingId);
    }

    public void deleteOwnListing(String liteListingId) {
        ownListings.get().remove(liteListingId);
    }

    /**
     * Adds a listing to the user's favorites
     * @param listing listing to add
     */
    public void addFavorite(Listing listing) {
        favorites.get().add(listing.getId());
    }

    /**
     * Removes a listing from the user's favorites
     * @param listing listing to remove
     */
    public void removeFavorite(Listing listing) {
        favorites.get().remove(listing.getId());
    }

    public static <T> Task<Void> updateField(String field, String id, T updatedValue) {

        return ModelTransaction.updateField(COLLECTION, id, field, updatedValue);
    }


    public static Task<User> fetch(String email) {
        return ModelTransaction.fetch(COLLECTION, email, User.class);
    }

    private String capitalize(String str){
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}