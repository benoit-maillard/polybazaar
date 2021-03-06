package ch.epfl.polybazaar.UI;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.polybazaar.R;
import ch.epfl.polybazaar.category.Category;
import ch.epfl.polybazaar.category.CategoryFragment;
import ch.epfl.polybazaar.category.NodeCategory;
import ch.epfl.polybazaar.category.RootCategoryFactory;
import ch.epfl.polybazaar.filllisting.ImageManager;
import ch.epfl.polybazaar.filllisting.ListingManager;
import ch.epfl.polybazaar.listing.Listing;
import ch.epfl.polybazaar.map.MapsActivity;
import ch.epfl.polybazaar.utilities.ImageTaker;
import ch.epfl.polybazaar.widgets.AddImageDialog;
import ch.epfl.polybazaar.widgets.NoConnectionForListingDialog;
import ch.epfl.polybazaar.widgets.NoticeDialogListener;

import static ch.epfl.polybazaar.chat.ChatActivity.removeBottomBarWhenKeyboardUp;
import static ch.epfl.polybazaar.map.MapsActivity.GIVE_LAT_LNG;
import static ch.epfl.polybazaar.map.MapsActivity.LAT;
import static ch.epfl.polybazaar.map.MapsActivity.LNG;
import static ch.epfl.polybazaar.map.MapsActivity.NOLAT;
import static ch.epfl.polybazaar.map.MapsActivity.NOLNG;
import static ch.epfl.polybazaar.map.MapsActivity.VALID;
import static ch.epfl.polybazaar.utilities.ImageTaker.CODE;
import static ch.epfl.polybazaar.utilities.ImageTaker.IMAGE_AVAILABLE;
import static ch.epfl.polybazaar.utilities.ImageTaker.LOAD_IMAGE;
import static ch.epfl.polybazaar.utilities.ImageTaker.PICTURE_PREFS;
import static ch.epfl.polybazaar.utilities.ImageTaker.STRING_IMAGE;
import static ch.epfl.polybazaar.utilities.ImageTaker.TAKE_IMAGE;
import static ch.epfl.polybazaar.utilities.ImageUtilities.convertStringToBitmap;

public class FillListing extends AppCompatActivity implements NoticeDialogListener, CategoryFragment.CategoryFragmentListener {


    public static final int ADD_MP = 3;
    public static final String LISTING_ID = "listingID";
    public static final String LISTING = "listing";

    private Button setMainImage;
    private Button rotateImage;
    private Button deleteImage;
    private ImageManager imageManager;
    private ListingManager listingManager;
    private Button addImages;
    private Button selectCategory;
    private Button submitListing;
    private Button addMP;
    private ImageView pictureView;
    private TextView titleSelector;
    private EditText descriptionSelector;
    private EditText priceSelector;
    private List<Bitmap> listImage;
    private Category selectedCategory;
    private double lat = NOLAT;
    private double lng = NOLNG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_listing);
        imageManager = new ImageManager(this);
        listingManager = new ListingManager(this);
        setMainImage = findViewById(R.id.setMain);
        rotateImage = findViewById(R.id.rotate);
        deleteImage = findViewById(R.id.deleteImage);
        addImages = findViewById(R.id.addImage);
        submitListing = findViewById(R.id.submitListing);
        titleSelector = findViewById(R.id.titleSelector);
        descriptionSelector = findViewById(R.id.descriptionSelector);
        priceSelector = findViewById(R.id.priceSelector);
        selectCategory = findViewById(R.id.selectCategory);
        addMP = findViewById(R.id.addMP);
        pictureView = findViewById(R.id.picturePreview);

        RootCategoryFactory.useJSONCategory(this);
        selectedCategory = RootCategoryFactory.getDependency();


        listImage = new ArrayList<>();
        boolean edit = fillFieldsIfEdit();
        addListeners(edit);

        BottomNavigationView bottomNavigationView = findViewById(R.id.activity_main_bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.action_add_item);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> bottomBar.updateActivity(item.getItemId(), FillListing.this));

        removeBottomBarWhenKeyboardUp(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Tags are set only for testing purposes using Espresso
        if(resultCode != Activity.RESULT_OK){
            pictureView.setTag(-1);
        }
        else if (requestCode == LOAD_IMAGE){
            if(data == null){
                pictureView.setTag(-1);
                return;
            }
            getNewImage(data);
        }
        else if (requestCode == TAKE_IMAGE){
            if (data != null) {
                getNewImage(data);
            }
        }
        if (resultCode == Activity.RESULT_OK && ADD_MP == requestCode) {
            if (data.getBooleanExtra(VALID, false)) {
                lng = data.getDoubleExtra(LNG, NOLNG);
                lat = data.getDoubleExtra(LAT, NOLAT);
                addMP.setText(R.string.change_MP);
            } else {
                lng = NOLNG;
                lat = NOLAT;
                addMP.setText(R.string.add_MP);
            }
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        intent.putExtra(CODE, requestCode);
        super.startActivityForResult(intent, requestCode);
    }

    private void getNewImage(Intent data) {
        boolean bitmapOK = data.getBooleanExtra(IMAGE_AVAILABLE, false);
        if (bitmapOK) {
            Bitmap image = convertStringToBitmap(this.getSharedPreferences(PICTURE_PREFS, MODE_PRIVATE).getString(STRING_IMAGE, null));
            imageManager.addImage(listImage, image);
            imageManager.updateViewPagerVisibility(listImage);
        }
    }

    private void addListeners(boolean edit){
        addImages.setOnClickListener(v -> {
            AddImageDialog dialog = new AddImageDialog();
            dialog.show(getSupportFragmentManager(), "select image import");
        });
        selectCategory.setOnClickListener(v -> {
            RootCategoryFactory.useJSONCategory(getApplicationContext());
            FragmentManager fragmentManager = getSupportFragmentManager();
            CategoryFragment categoryFragment = CategoryFragment.newInstance(RootCategoryFactory.getDependency(),
                   R.id.fillListing_fragment_container,fragmentManager.getBackStackEntryCount());
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.addToBackStack(null)
                    .add(R.id.fillListing_fragment_container,categoryFragment).commit();

        });
        addMP.setOnClickListener(v -> {
            Intent defineMP = new Intent(this, MapsActivity.class);
            defineMP.putExtra(GIVE_LAT_LNG, false);
            defineMP.putExtra(LAT, lat);
            defineMP.putExtra(LNG, lng);
            startActivityForResult(defineMP, ADD_MP);
        });
        titleSelector.setOnFocusChangeListener((v, hasFocus) -> {
            if (!titleSelector.getText().toString().equals("")) titleSelector.setBackground(getResources().getDrawable(R.drawable.boxed, getTheme()));
        });
        priceSelector.setOnFocusChangeListener((v, hasFocus) -> {
            if (!priceSelector.getText().toString().equals("")) priceSelector.setBackground(getResources().getDrawable(R.drawable.boxed, getTheme()));
        });
        setMainImage.setOnClickListener(v -> imageManager.setFirst(listImage));
        rotateImage.setOnClickListener(v -> imageManager.rotateLeft(listImage));
        deleteImage.setOnClickListener(v -> imageManager.deleteImage(listImage));
        submitListing.setOnClickListener(v -> {
            if (!listingManager.submit(selectedCategory, listImage, lat, lng)) {
                NoConnectionForListingDialog dialog = new NoConnectionForListingDialog();
                dialog.show(getSupportFragmentManager(), "noConnectionDialog");
            }
        });
        if(edit) {
            submitListing.setText(R.string.save);
            submitListing.setOnClickListener(v ->
                    listingManager.deleteOldListingAndSubmitNewOne(selectedCategory, listImage, lat, lng));
        }

        /**
         * FOR TESTING PURPOSES ONLY:
         */
        findViewById(R.id.addImageFromCamera).setOnClickListener(v -> {
            startActivityForResult(new Intent(this, ImageTaker.class), TAKE_IMAGE);
        });
        findViewById(R.id.addImageFromLibrary).setOnClickListener(v -> {
            startActivityForResult(new Intent(this, ImageTaker.class), LOAD_IMAGE);
        });
        /**
         * ==========================
         */
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        if(dialog instanceof NoConnectionForListingDialog){
            listingManager.submit(selectedCategory, listImage, lat, lng);
            Intent SalesOverviewIntent = new Intent(FillListing.this, SalesOverview.class);
            startActivity(SalesOverviewIntent);
        }
        if (dialog instanceof AddImageDialog) {
            startActivityForResult(new Intent(this, ImageTaker.class), TAKE_IMAGE);
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        if(dialog instanceof NoConnectionForListingDialog){
            //do nothing
        }
        if (dialog instanceof AddImageDialog) {
            startActivityForResult(new Intent(this, ImageTaker.class), LOAD_IMAGE);
        }
    }

    @Override
    public void onCategoryFragmentInteraction(Category category) {

        selectedCategory = category;
        selectCategory.setText(category.toString());

    }

    private boolean fillFieldsIfEdit() {
        Bundle bundle = getIntent().getExtras();
        if(bundle == null){
            return false;
        }
        String listingID = bundle.getString(LISTING_ID, "-1");
        if (listingID.equals("-1")){
            return false;
        }
        Listing listing = (Listing)bundle.get(LISTING);
        if(listing == null) {
            return false;
        }
        imageManager.retrieveAllImages(listingID, listImage);
        titleSelector.setText(listing.getTitle());
        descriptionSelector.setText(listing.getDescription());
        priceSelector.setText(listing.getPrice());
        selectCategory.setText(listing.getCategory());
        Category cat = new NodeCategory(listing.getCategory());
        RootCategoryFactory.useJSONCategory(getApplicationContext());
        Category parentCategory = RootCategoryFactory.getDependency().getSubCategoryContaining(cat);
        if (parentCategory != null) {
            for (Category c : parentCategory.subCategories()) {
                if (c.equals(cat)) {
                    selectedCategory = c;
                }
            }
        }
        lat = listing.getLatitude();
        lng = listing.getLongitude();
        if (lat != NOLAT && lng != NOLNG) {
            addMP.setText(R.string.change_MP);
        }
        return true;
    }



    /**
     * FOR TESTING PURPOSES ONLY:
     */
    // @return the current StringImage displayed or null if there is no image
    public Bitmap getCurrentImage() {
        if(listImage.size() > 0) {
            return listImage.get(((ViewPager2)findViewById(R.id.viewPager)).getCurrentItem());
        } else {
            return null;
        }
    }




}
