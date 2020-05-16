package ch.epfl.polybazaar.saledetails;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

import ch.epfl.polybazaar.R;
import ch.epfl.polybazaar.UI.SaleDetails;
import ch.epfl.polybazaar.UI.SalesOverview;
import ch.epfl.polybazaar.chat.ChatMessage;
import ch.epfl.polybazaar.filestorage.ImageTransaction;
import ch.epfl.polybazaar.listing.Listing;
import ch.epfl.polybazaar.listingImage.ListingImage;
import ch.epfl.polybazaar.litelisting.LiteListing;
import ch.epfl.polybazaar.login.Account;
import ch.epfl.polybazaar.login.Authenticator;
import ch.epfl.polybazaar.login.AuthenticatorFactory;
import ch.epfl.polybazaar.user.User;

import static ch.epfl.polybazaar.utilities.ImageUtilities.convertStringToBitmap;

public class ListingManager {

    SaleDetails activity;

    public ListingManager(SaleDetails activity) {
        this.activity = activity;
    }

    /**
     * Fill the UI with the Listing given in parameter
     * @param listing the listing
     */
    public void fillWithListing(final Listing listing) {
        if(listing == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(R.string.object_not_found)
                    .setNeutralButton(R.string.back, (dialog, which) -> {
                        Intent intent = new Intent(activity.getApplicationContext(), SalesOverview.class);
                        activity.startActivity(intent);
                    });
            builder.create().show();
        } else {
            activity.updateViews();
            activity.setMP();

            //Set the title
            TextView title_txt = activity.findViewById(R.id.title);
            title_txt.setText(listing.getTitle());
            title_txt.setVisibility(View.VISIBLE);

            //Set the description
            LinearLayout description = activity.findViewById(R.id.descriptionLayout);
            TextView description_txt = activity.findViewById(R.id.description);
            if (!(listing.getDescription().trim().length() == 0
                    || listing.getDescription().isEmpty()
                    || listing.getDescription() == null )) {
                description.setVisibility(View.VISIBLE);
                description_txt.setText(listing.getDescription());
            }

            //Set the price
            TextView price_txt = activity.findViewById(R.id.price);
            if (listing.getPrice().equals(Listing.SOLD)) {
                price_txt.setText(R.string.sold);
            } else {
                price_txt.setText(String.format("CHF %s", listing.getPrice()));
            }
            price_txt.setVisibility(View.VISIBLE);

            // Set seller information
            ImageView sellerPicture  = activity.findViewById(R.id.sellerProfilePicture);
            TextView sellerNickname  = activity.findViewById(R.id.sellerNickname);
            User.fetch(listing.getUserEmail()).addOnSuccessListener(result -> {
                if (!result.getProfilePictureRef().equals(User.NO_PROFILE_PICTURE)) {
                    ImageTransaction.fetch(result.getProfilePictureRef(), activity.getApplicationContext()).addOnSuccessListener(sellerPicture::setImageBitmap);
                }
                if (result.getNickName() != null) {
                    sellerNickname.setText(result.getNickName());
                }
            });

            // Enable logged in features
            Button contactSelButton = activity.findViewById(R.id.contactSel);
            Button makeOfferButton = activity.findViewById(R.id.makeOffer);
            Button buyNowButton = activity.findViewById(R.id.buyNow);
            RatingBar ratingBar = activity.findViewById(R.id.ratingBar);
            TextView viewsTextView = activity.findViewById(R.id.viewsLabel);
            TextView nbViewsTextView = activity.findViewById(R.id.nbViews);

            Account authUser = AuthenticatorFactory.getDependency().getCurrentUser();
            if(authUser != null) {
                // logged in, general:
                ratingBar.setVisibility(View.VISIBLE);
                String sellerEmail = listing.getUserEmail();
                authUser.getUserData().addOnSuccessListener(user -> {
                    List<String> favorites = user.getFavorites();
                    if (favorites.contains(listing.getId())){
                        ratingBar.setRating(1);
                    }
                });
                if(authUser.getEmail().equals(sellerEmail)){
                    // logged in and own listing:
                    activity.findViewById(R.id.editButtonsLayout).setVisibility(View.VISIBLE);
                    contactSelButton.setVisibility(View.GONE);
                    buyNowButton.setVisibility(View.GONE);
                    makeOfferButton.setVisibility(View.GONE);
                    activity.setupNbViews(listing);
                } else{
                    //logged in, but not own listing:
                    contactSelButton.setVisibility(View.VISIBLE);
                    if (listing.getListingActive()) {
                        makeOfferButton.setVisibility(View.VISIBLE);
                        buyNowButton.setVisibility(View.VISIBLE);
                    } else {
                        makeOfferButton.setVisibility(View.GONE);
                        buyNowButton.setVisibility(View.GONE);
                    }
                    activity.findViewById(R.id.editButtonsLayout).setVisibility(View.GONE);
                    viewsTextView.setVisibility(View.GONE);
                    nbViewsTextView.setVisibility(View.GONE);
                }
            } else {
                // not logged in:
                contactSelButton.setVisibility(View.VISIBLE);
                contactSelButton.setText(R.string.sign_in_to_contact);
                makeOfferButton.setVisibility(View.GONE);
                buyNowButton.setVisibility(View.GONE);
                ratingBar.setVisibility(View.INVISIBLE);
                ratingBar.setClickable(false);
                viewsTextView.setVisibility(View.GONE);
                nbViewsTextView.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Deletes the listing specified
     * @param listingID the listings Id
     */
    public void deleteCurrentListing(String listingID) {
        Listing.deleteWithLiteVersion(listingID).addOnSuccessListener(result -> {
            Toast toast = Toast.makeText(activity.getApplicationContext(),R.string.deleted_listing, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            Authenticator fbAuth = AuthenticatorFactory.getDependency();
            Account authAccount = fbAuth.getCurrentUser();
            authAccount.getUserData().addOnSuccessListener(user -> {
                user.deleteOwnListing(listingID);
                user.save();
            });
            Intent SalesOverviewIntent = new Intent(activity.getApplicationContext(), SalesOverview.class);
            activity.startActivity(SalesOverviewIntent);
        });
        Listing.fetch(listingID).addOnSuccessListener(listing -> {
            // Delete images:
            if (listing != null && listing.getImagesRefs() != null) {
                for (String ref : listing.getImagesRefs()) {
                    // todo: delete image at ref
                }
            }
            LiteListing.fetch(listingID).addOnSuccessListener(liteListing -> {
                // TODO: delete img at liteListing.getThumbnailRef()
            });
            // delete all messages
            ChatMessage.fetchConversation(listingID).addOnSuccessListener(chatMessages -> {
                for (ChatMessage message : chatMessages) {
                    message.delete();
                }
            });
            Listing.deleteWithLiteVersion(listingID).addOnSuccessListener(result -> {
                Toast toast = Toast.makeText(activity.getApplicationContext(),R.string.deleted_listing, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
                Intent SalesOverviewIntent = new Intent(activity.getApplicationContext(), SalesOverview.class);
                activity.startActivity(SalesOverviewIntent);
            });
        });

    }

    /**
     * Adds the listing to favorites, or removes it from the user's favorites if it is already
     * a favorite
     */
    public void favorite(Listing listing) {
        RatingBar ratingBar = activity.findViewById(R.id.ratingBar);
        Account authUser = AuthenticatorFactory.getDependency().getCurrentUser();
        //if it's 0 set 1 and vice versa
        ratingBar.setRating((ratingBar.getRating() + 1) % 2);
        //must be connected
        if (authUser != null) {
            authUser.getUserData().addOnSuccessListener(user -> {
                if (ratingBar.getRating() == 1) {
                    user.addFavorite(listing.getId());
                } else {
                    user.removeFavorite(listing.getId());
                }
                user.save();
            });
        }
    }

}
