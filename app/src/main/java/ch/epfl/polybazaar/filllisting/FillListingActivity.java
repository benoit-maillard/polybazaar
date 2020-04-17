package ch.epfl.polybazaar.filllisting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ch.epfl.polybazaar.MainActivity;
import ch.epfl.polybazaar.R;
import ch.epfl.polybazaar.UI.SalesOverview;
import ch.epfl.polybazaar.category.Category;
import ch.epfl.polybazaar.category.CategoryRepository;
import ch.epfl.polybazaar.category.StringCategory;
import ch.epfl.polybazaar.listing.Listing;
import ch.epfl.polybazaar.listingImage.ListingImage;
import ch.epfl.polybazaar.litelisting.LiteListing;
import ch.epfl.polybazaar.login.AppUser;
import ch.epfl.polybazaar.login.Authenticator;
import ch.epfl.polybazaar.login.AuthenticatorFactory;
import ch.epfl.polybazaar.map.MapsActivity;
import ch.epfl.polybazaar.widgets.NoConnectionForListingDialog;
import ch.epfl.polybazaar.widgets.NoticeDialogListener;
import ch.epfl.polybazaar.widgets.permissions.PermissionRequest;

import static ch.epfl.polybazaar.Utilities.convertBitmapToStringWithQuality;
import static ch.epfl.polybazaar.Utilities.convertFileToStringWithQuality;
import static ch.epfl.polybazaar.Utilities.getUser;
import static ch.epfl.polybazaar.Utilities.resizeStringImageThumbnail;
import static ch.epfl.polybazaar.filllisting.ImageManager.*;
import static ch.epfl.polybazaar.map.MapsActivity.GIVE_LAT_LNG;
import static ch.epfl.polybazaar.map.MapsActivity.LAT;
import static ch.epfl.polybazaar.map.MapsActivity.LNG;
import static ch.epfl.polybazaar.map.MapsActivity.NOLAT;
import static ch.epfl.polybazaar.map.MapsActivity.NOLNG;
import static ch.epfl.polybazaar.map.MapsActivity.VALID;
import static ch.epfl.polybazaar.network.InternetCheckerFactory.isInternetAvailable;
import static ch.epfl.polybazaar.user.User.editUser;
import static java.util.UUID.randomUUID;

public class FillListingActivity extends AppCompatActivity implements NoticeDialogListener {

    public static final int RESULT_LOAD_IMAGE = 1;
    public static final int RESULT_TAKE_PICTURE = 2;
    public static final int RESULT_ADD_MP = 3;
    public static final String INCORRECT_FIELDS_TEXT = "One or more required fields are incorrect or uncompleted";
    private final int QUALITY = 10;
    private final String DEFAULT_SPINNER_TEXT = "Select category...";

    private Button setImageFirst;
    private Button rotateImageLeft;
    private Button deleteImage;
    private Button modifyImage;
    private ImageManager imageManager;
    private Button uploadImage;
    private Button camera;
    private Button submitListing;
    private Button addMP;
    private ImageView pictureView;
    private ViewPager2 viewPager;
    private Switch freeSwitch;
    private TextView titleSelector;
    private TextView meetingPointStatus;
    private EditText descriptionSelector;
    private EditText priceSelector;
    private Spinner categorySelector;
    private List<Spinner> spinnerList;
    private LinearLayout linearLayout;
    private String oldPrice;
    private String currentPhotoPath;
    private File photoFile;
    private List<String> listStringImage;
    //only used for edit to delete all images
    private List<String> listImageID;
    private String stringImage = "";
    private Category traversingCategory;
    private String stringThumbnail = "";
    private double lat = NOLAT;
    private double lng = NOLNG;

    private PermissionRequest cameraPermissionRequest;
    private AppUser authAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_listing);
        imageManager = new ImageManager(this);

        setImageFirst = findViewById(R.id.setFirst);
        rotateImageLeft = findViewById(R.id.rotateLeft);
        deleteImage = findViewById(R.id.deleteImage);
        modifyImage = findViewById(R.id.modifyImage);

        camera = findViewById(R.id.camera);
        freeSwitch = findViewById(R.id.freeSwitch);
        uploadImage = findViewById(R.id.uploadImage);
        submitListing = findViewById(R.id.submitListing);

        titleSelector = findViewById(R.id.titleSelector);
        descriptionSelector = findViewById(R.id.descriptionSelector);
        priceSelector = findViewById(R.id.priceSelector);
        linearLayout = findViewById(R.id.fillListingLinearLayout);
        addMP = findViewById(R.id.addMP);
        meetingPointStatus = findViewById(R.id.MPStatus);
        pictureView = findViewById(R.id.picturePreview);

        categorySelector = findViewById(R.id.categorySelector);
        spinnerList = new ArrayList<>();
        spinnerList.add(categorySelector);
        setupSpinner(categorySelector, categoriesWithDefaultText(CategoryRepository.categories));
        listStringImage = new ArrayList<>();
        listImageID = new ArrayList<>();
        boolean edit = fillFieldsIfEdit();
        addListeners(edit);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Tags are set only for testing purposes using Espresso
        if(resultCode != Activity.RESULT_OK){
            pictureView.setTag(-1);
        }
        else if (requestCode == RESULT_LOAD_IMAGE){
            if(data == null){
                pictureView.setTag(-1);
                return;
            }
            Uri selectedImage = data.getData();
            Bitmap bitmap;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            stringImage = convertBitmapToStringWithQuality(bitmap, QUALITY);
            imageManager.addImage(listStringImage, stringImage);
        }
        else if (requestCode == RESULT_TAKE_PICTURE){
           stringImage = convertFileToStringWithQuality(imageManager.getPhotoFile(), QUALITY);
           imageManager.addImage(listStringImage, stringImage);
        }
        else if (requestCode == RESULT_ADD_MP) {
            if (data != null) {
                if (data.getBooleanExtra(VALID, false)) {
                    lng = data.getDoubleExtra(LNG, NOLNG);
                    lat = data.getDoubleExtra(LAT, NOLAT);
                    addMP.setText(R.string.change_MP);
                    meetingPointStatus.setText(R.string.MP_ok);
                } else {
                    lng = NOLNG;
                    lat = NOLAT;
                    addMP.setText(R.string.add_MP);
                    meetingPointStatus.setText(R.string.MP_nok);
                }
            }
        }
    }


    private void addListeners(boolean edit){
        camera.setOnClickListener(v -> checkCameraPermission());
        freeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> freezePriceSelector(isChecked));
        uploadImage.setOnClickListener(v -> imageManager.uploadImage());
        addMP.setOnClickListener(v -> {
            Intent defineMP = new Intent(this, MapsActivity.class);
            defineMP.putExtra(GIVE_LAT_LNG, false);
            defineMP.putExtra(LAT, lat);
            defineMP.putExtra(LNG, lng);
            startActivityForResult(defineMP, RESULT_ADD_MP);
        });

        setImageFirst.setOnClickListener(v -> imageManager.setFirst(listStringImage));
        rotateImageLeft.setOnClickListener(v -> imageManager.rotateLeft(listStringImage));
        deleteImage.setOnClickListener(v -> imageManager.deleteImage(listStringImage));
        modifyImage.setOnClickListener(v -> {
            if (setImageFirst.getVisibility() == View.INVISIBLE) {
                showImagesButtons();
            } else {
                hideImagesButtons();
            }
        });

        if(!edit){
            submitListing.setOnClickListener(v -> submit());
        }
        else{
            submitListing.setText(R.string.edit);
            submitListing.setOnClickListener(v -> deleteOldListingAndSubmitNewOne());
        }

    }

    private void setupSpinner(Spinner spinner, List<Category> categories){
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                removeSpinnersBelow(spinner);
                Category selectedCategory = (Category)parent.getItemAtPosition(position);
                popsUpSubCategorySpinner(selectedCategory);
                linearLayout.invalidate();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Do nothing
            }
        });
    }

    private void removeSpinnersBelow(Spinner spinner){
        if(!spinner.equals(spinnerList.get(spinnerList.size()-1))){
            int numbersOfSpinnersToRemove = spinnerList.size() - spinnerList.indexOf(spinner) -1;
            int indexOfFirstSpinnerToRemove = linearLayout.indexOfChild(categorySelector) + spinnerList.indexOf(spinner) + 1;
            linearLayout.removeViews(indexOfFirstSpinnerToRemove, numbersOfSpinnersToRemove);
            spinnerList = spinnerList.subList(0, spinnerList.indexOf(spinner)+1);
        }
    }

    private void popsUpSubCategorySpinner(Category selectedCategory){
        if (selectedCategory.hasSubCategories()){
            Spinner subSpinner = new Spinner(getApplicationContext());
            spinnerList.add(subSpinner);
            linearLayout.addView(subSpinner, linearLayout.indexOfChild(categorySelector)+spinnerList.size()-1);
            setupSpinner(subSpinner, categoriesWithDefaultText(selectedCategory.subCategories()));

            Bundle bundle = getIntent().getExtras();
            if(bundle != null && traversingCategory != null){
                Category editedCategory = new StringCategory(((Listing)bundle.get("listing")).getCategory());
                subSpinner.setSelection(traversingCategory.indexOf(traversingCategory.getSubCategoryContaining(editedCategory))+1);
                traversingCategory = traversingCategory.getSubCategoryContaining(editedCategory);
            }


        }
    }

    private List<Category> categoriesWithDefaultText(List<Category> categories){
        List<Category> categoriesWithDefText = new ArrayList<>(categories);
        categoriesWithDefText.add(0, new StringCategory(DEFAULT_SPINNER_TEXT));
        return categoriesWithDefText;
    }


    private void freezePriceSelector(boolean isChecked){
        if(isChecked){
            if(priceSelector.getText().length() > 0) {
                oldPrice = priceSelector.getText().toString();
            }
            priceSelector.setFocusable(false);
            priceSelector.setText(Double.toString(0.00));
        }
        else{
            priceSelector.setFocusableInTouchMode(true);
            priceSelector.setText(oldPrice);
        }
    }

    private boolean checkFields(){
        return checkTitle() && checkPrice() && checkCategory();
    }

    private boolean checkCategory() {
        return !spinnerList.get(spinnerList.size()-1).getSelectedItem().toString().equals(DEFAULT_SPINNER_TEXT);
    }

    private boolean checkPrice() {
        return !priceSelector.getText().toString().isEmpty();
    }

    private boolean checkTitle() {
        return !titleSelector.getText().toString().isEmpty();
    }

    private void submit() {
        if(!listStringImage.isEmpty()) {
            stringThumbnail = resizeStringImageThumbnail(listStringImage.get(0));
        }
        Context context = getApplicationContext();
        if (!checkFields()) {
            Toast.makeText(context, INCORRECT_FIELDS_TEXT, Toast.LENGTH_SHORT).show();
        }
        else {
                if(isInternetAvailable(context)){
                    createAndSendListing();
                    Intent SalesOverviewIntent = new Intent(FillListingActivity.this, SalesOverview.class);
                    startActivity(SalesOverviewIntent);
                }else{
                    NoConnectionForListingDialog dialog = new NoConnectionForListingDialog();
                    dialog.show(getSupportFragmentManager(),"noConnectionDialog");
                }
        }
    }


    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        if(dialog instanceof NoConnectionForListingDialog){
            createAndSendListing();
            Intent SalesOverviewIntent = new Intent(FillListingActivity.this, SalesOverview.class);
            startActivity(SalesOverviewIntent);

        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        if(dialog instanceof NoConnectionForListingDialog){
            //do nothing
        }

    }
    private void createAndSendListing() {
            final String newListingID = randomUUID().toString();
            String category = spinnerList.get(spinnerList.size()-1).getSelectedItem().toString();
            Authenticator fbAuth = AuthenticatorFactory.getDependency();

            if(fbAuth.getCurrentUser() == null) {
                Toast.makeText(getApplicationContext(), R.string.sign_in_required, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                return;
            }
            String userEmail = fbAuth.getCurrentUser().getEmail();

            Listing newListing = new Listing(titleSelector.getText().toString(), descriptionSelector.getText().toString(),
                    priceSelector.getText().toString(), userEmail, "", category, lat, lng);
            newListing.setId(newListingID);
            LiteListing newLiteListing = new LiteListing(newListingID, titleSelector.getText().toString(), priceSelector.getText().toString(), category, stringThumbnail);
            newLiteListing.setId(newListingID);

            newListing.save().addOnSuccessListener(result -> {
                Toast toast = Toast.makeText(getApplicationContext(),"Offer successfully sent!",Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
            });

            //store images (current has a ref to the next)
            if(listStringImage.size() > 0) {
                String currentId = newListingID;
                String nextId;
                for(int i = 0; i < (listStringImage.size() - 1); i++) {
                    nextId = randomUUID().toString();
                    ListingImage newListingImage = new ListingImage(listStringImage.get(i), nextId);

                    newListingImage.setId(currentId);
                    newListingImage.save()
                            .addOnSuccessListener(result -> Log.d("FirebaseDataStore", "successfully stored image"))
                            .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Failed to send image", Toast.LENGTH_LONG).show());

                    currentId = nextId;
                }
                //store the last without refNextImg
                ListingImage newListingImage = new ListingImage(listStringImage.get(listStringImage.size() - 1), "");

                newListingImage.setId(currentId);
                newListingImage.save();
            }

        newLiteListing.save()
                .addOnSuccessListener(result -> Log.d("FirebaseDataStore", "successfully stored data"))
                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Failed to send listing", Toast.LENGTH_LONG).show());

            authAccount = getUser();
            // update own listings of (logged) user
            if(authAccount != null) {
                authAccount.getUserData().addOnSuccessListener(user -> {
                    if (user != null) {
                        user.addOwnListing(newListingID);
                        editUser(user);
                    }
                });
            }
    }

    private boolean fillFieldsIfEdit() {
        Bundle bundle = getIntent().getExtras();
        if(bundle == null){
            return false;
        }
        String listingID = bundle.getString("listingID", "-1");
        if (listingID.equals("-1")){
            return false;
        }
        Listing listing = (Listing)bundle.get("listing");
        if(listing == null) {
            return false;
        }
        imageManager.retrieveAllImages(listStringImage, listImageID, listingID);

        titleSelector.setText(listing.getTitle());
        descriptionSelector.setText(listing.getDescription());
        freeSwitch.setChecked(listing.getPrice().equals("0.0"));
        priceSelector.setText(listing.getPrice());
        Category editedCategory = new StringCategory(listing.getCategory());
        traversingCategory = CategoryRepository.getCategoryContaining(editedCategory);
        categorySelector.setSelection(CategoryRepository.indexOf(traversingCategory)+1);
        lat = listing.getLatitude();
        lng = listing.getLongitude();
        if (lat != NOLAT && lng != NOLNG) {
            addMP.setText(R.string.change_MP);
            meetingPointStatus.setText(R.string.MP_ok);
        }
        return true;
    }


    private void deleteOldListingAndSubmitNewOne() {
        if (!checkFields()) {
            Toast.makeText(getApplicationContext(), INCORRECT_FIELDS_TEXT, Toast.LENGTH_SHORT).show();
        }
        else{
            Bundle bundle = getIntent().getExtras();
            if(bundle == null){
                return;
            }
            String listingID = bundle.getString("listingID");

            Listing.deleteWithLiteVersion(listingID)
                    .addOnSuccessListener((v) -> submit());

            for(String id: listImageID) {
                ListingImage.delete(id);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        cameraPermissionRequest.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }




    private void checkCameraPermission(){
        PermissionRequest cameraPermissionRequest = new PermissionRequest(this, "CAMERA", "Camera access is required to take pictures", null, result -> {
            if (result) imageManager.takePicture();
        });
        cameraPermissionRequest.assertPermission();
    }

    private void hideImagesButtons() {
        setImageFirst.setVisibility(View.INVISIBLE);
        rotateImageLeft.setVisibility(View.INVISIBLE);
        deleteImage.setVisibility(View.INVISIBLE);
    }

    private void showImagesButtons() {
        setImageFirst.setVisibility(View.VISIBLE);
        rotateImageLeft.setVisibility(View.VISIBLE);
        deleteImage.setVisibility(View.VISIBLE);
    }



}
