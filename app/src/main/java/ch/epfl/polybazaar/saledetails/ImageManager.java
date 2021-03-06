package ch.epfl.polybazaar.saledetails;


import android.content.Context;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.polybazaar.R;
import ch.epfl.polybazaar.UI.SaleDetails;
import ch.epfl.polybazaar.UI.SliderAdapter;
import ch.epfl.polybazaar.UI.SliderItem;

import static ch.epfl.polybazaar.utilities.ImageUtilities.convertDrawableToBitmap;
import static ch.epfl.polybazaar.utilities.ImageUtilities.cropToSize;
import static ch.epfl.polybazaar.utilities.ImageUtilities.resizeBitmap;
import static ch.epfl.polybazaar.utilities.ImageUtilities.scaleBitmap;

public class ImageManager {

    SaleDetails activity;

    public ImageManager(SaleDetails activity) {
        this.activity = activity;
    }

    /**
     * Displays the images on the ViewPager
     * @param listImage list of images to Display
     */
    public void drawImages(List<Bitmap> listImage) {
        ViewPager2 viewPager = activity.findViewById(R.id.viewPagerImageSlider);
        activity.runOnUiThread (()-> {
            List<SliderItem> sliderItems = new ArrayList<>();
            List<SliderItem> sliderItemsZoom = new ArrayList<>();

            if (listImage.isEmpty()) {
                listImage.add(convertDrawableToBitmap(activity.getResources().getDrawable(R.drawable.no_image_thumbnail, activity.getTheme())));
            } else {
                activity.findViewById(R.id.pageNumber).setVisibility(View.VISIBLE);
            }
            viewPager.setVisibility(View.VISIBLE);
            activity.findViewById(R.id.loadingImage).setVisibility(View.GONE);

            for (Bitmap bm : listImage) {
                sliderItems.add(new SliderItem(scaleBitmap(cropToSize(bm, viewPager.getWidth(), viewPager.getHeight()), viewPager.getWidth())));
                sliderItemsZoom.add(new SliderItem(scaleBitmap(bm, viewPager.getWidth())));
            }

            viewPager.setAdapter(new SliderAdapter(sliderItems, viewPager));

            viewPager.setClipToPadding(false);
            viewPager.setClipChildren(false);
            viewPager.setOffscreenPageLimit(3);
            viewPager.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

            CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
            compositePageTransformer.addTransformer((page, position) -> {
                float r = 1 - Math.abs(position);
                page.setScaleY(0.85f + r * 0.15f);
            });
            viewPager.setPageTransformer(compositePageTransformer);

            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    TextView textPageNumber = activity.findViewById(R.id.pageNumber);
                    textPageNumber.setText(String.format("%s/%s", Integer.toString(viewPager.getCurrentItem() + 1), Integer.toString(listImage.size())));
                    textPageNumber.setGravity(Gravity.CENTER);
                }
            });

            // inflate the layout of the popup window
            LayoutInflater inflater = (LayoutInflater) activity.getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.popup_window_images, null);
            ViewPager2 zoomViewPager = popupView.findViewById(R.id.viewPagerZoom);
            zoomViewPager.setAdapter(new SliderAdapter(sliderItemsZoom, zoomViewPager));

            final PopupWindow popupWindow = new PopupWindow(
                    popupView,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    true);

            viewPager.setOnClickListener(v -> {
                zoomViewPager.setCurrentItem(((ViewPager2)v).getCurrentItem(), false);
                popupWindow.showAtLocation(v, Gravity.NO_GRAVITY, 0, 0);
            });

        });
    }
}
