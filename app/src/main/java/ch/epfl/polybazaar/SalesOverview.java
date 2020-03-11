package ch.epfl.polybazaar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import ch.epfl.polybazaar.database.callback.LiteListingCallback;
import ch.epfl.polybazaar.database.callback.LiteListingListCallback;
import ch.epfl.polybazaar.litelisting.LiteListing;

import static ch.epfl.polybazaar.litelisting.LiteListingDatabase.fetchLiteListing;
import static ch.epfl.polybazaar.litelisting.LiteListingDatabase.fetchLiteListingList;

public class SalesOverview extends AppCompatActivity {

    // TODO: add tests for error cases (ex empty list on database)

    // TODO: test listings, to delete
    LiteListing l1 = new LiteListing("titre1", "Algebre Linéaire by David C. Lay", "CHF 13");
    LiteListing l2 = new LiteListing("titre2", "Guitare folk", "CHF 125");
    LiteListing l3 = new LiteListing("titre3", "Equipement de ski de randonnée", "CHF 300");
    LiteListing l4 = new LiteListing("titre4", "Fer à repasser", "CHF 5");
    LiteListing l5 = new LiteListing("titre5", "Livre de cuisine asiatique", "CHF 7");
    LiteListing l6 = new LiteListing("titre6", "Table de salle à manger", "CHF 70");
    LiteListing l7 = new LiteListing("titre7", "Vaccin Coronavirus", "CHF 999999");
    LiteListing l8 = new LiteListing("titre8", "Nintendo Switch neuve", "CHF 250");


    private final AtomicInteger counter = new AtomicInteger(0);
    private List<String> IDList;
    private List<LiteListing> liteListingList;
    ScrollView scroll;
    LinearLayout linearLayout;

    // Create an anonymous implementation of OnClickListener
    private View.OnClickListener titleClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            TextView itemName = findViewById(v.getId());
            Intent intent = new Intent(SalesOverview.this, SaleDetails.class);
            intent.putExtra("title", itemName.getText().toString());
            intent.putExtra("description", "Never used");
            intent.putExtra("price", "18 CHF");
            startActivity(intent);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_overview2);

        IDList = new ArrayList<>();
        liteListingList = new ArrayList<>();
        scroll = new ScrollView(this);
        linearLayout = new LinearLayout(this);

        SetScrollView();

        // attach scrollView to ConstraintLayout
        ConstraintLayout constraintLayout = findViewById(R.id.rootContainer);
        if (constraintLayout != null) {
            constraintLayout.addView(scroll);
        }

        // create list of all LiteListing IDs in database
        createLiteListingIDList();
        // create list of all LiteListings in database
        createLiteListingList();

        // add all LiteListings into activity scrollview
        for(LiteListing l : liteListingList) {
            addListingView(l);
        }
    }


    /**
     * Set view skeleton of activity screen:
     * a general scrollview with a linear layout as single child
     */
    public void SetScrollView() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        scroll.setLayoutParams(layoutParams);

        LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.BOTTOM);
        linearLayout.setLayoutParams(linearParams);

        scroll.addView(linearLayout);
    }

    /**
     * create a TextView displaying the LiteListing title and inserts it into the ScrollView
     * @param l The LiteListing whose data need to be displayed
     */
    public void addListingView(LiteListing l) {
        // create a horizontal linear layout that will display title and price on one line
        LinearLayout linelinearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        linelinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linelinearLayout.setLayoutParams(linearParams);

        // create TextView for listing title
        int boxWidth = (int)(getResources().getDimension(R.dimen.title_box_length));
        int boxHeight = TableRow.LayoutParams.WRAP_CONTENT;
        int boxWeight = 1;
        float textSize = getResources().getDimension(R.dimen.overview_text_size);
        TextView textView = createView(boxWidth, boxHeight, boxWeight, textSize, true, true, l.getTitle());

        // create TextView for listing price
        boxWidth = (int)(getResources().getDimension(R.dimen.price_box_length));
        TextView priceView = createView(boxWidth, boxHeight, boxWeight, textSize, true, false, l.getPrice());

        // add title and price to horizontal layout
        linelinearLayout.addView(textView);
        linelinearLayout.addView(priceView);

        // add horizontal layout to vertical layout
        linearLayout.addView(linelinearLayout);
    }

    /**
     * Helper method to create a TextView
     * @param boxWidth width of TextView
     * @param boxHeight height of TextView
     * @param boxWeight weight of TextView
     * @param textSize size of text
     * @param ellipsize boolean indicating if text must be truncated
     * @param clickable boolean indicating if the TextView is clickable
     * @param content string for text content of TextView
     * @return the newly created TextView
     */
    public TextView createView(int boxWidth, int boxHeight, int boxWeight, float textSize, boolean ellipsize, boolean clickable, String content) {
        TextView textView = new TextView(this);
        textView.setId(View.generateViewId());
        TableRow.LayoutParams boxParams = new TableRow.LayoutParams(boxWidth, boxHeight, boxWeight);
        textView.setLayoutParams(boxParams);

        textView.setText(content);
        textView.setTextSize(textSize);
        textView.setMaxLines(1);

        if(ellipsize) textView.setEllipsize(TextUtils.TruncateAt.END);

        // set TextView as clickable and call onClick method when clicked
        if(clickable) {
            textView.setClickable(true);
            textView.setOnClickListener(titleClickListener);
        }
        return textView;
    }


    /**
     * create a list of LiteListingIDs
     */
    public void createLiteListingIDList() {
        LiteListingListCallback callbackLiteListingList = new LiteListingListCallback() {

            @Override
            public void onCallback(List<String> result) {
                for(String id : result) {
                    IDList.add(id);      // create deep copy of ID list
                }
            }
        };
        fetchLiteListingList(callbackLiteListingList);
    }

    /**
     * create a list of LiteListings
     */
    public void createLiteListingList() {
        LiteListingCallback callbackLiteListing = new LiteListingCallback() {
            @Override
            public void onCallback(LiteListing result) {
                liteListingList.add(result);
            }
        };
        int size = IDList.size();
        for(int i = 0; 0 < size; ++i) {
            fetchLiteListing(IDList.get(i), callbackLiteListing);
        }
    }
    

    // TODO: delete
    public void createLiteListingMap() {
        LiteListingCallback callbackLiteListing = new LiteListingCallback() {
            @Override
            public void onCallback(LiteListing result) {
                int i = 0;
                while(true) {
                    i = counter.get();
                    if(counter.compareAndSet(i, i+1)) break;
                }
                // LiteListingMap.put(viewTitle + i, result);    // TextView numbering will start at 1
                // viewTitle += i;
                // TextView viewTitle = (TextView)findViewById(R.id.viewTitle);
                // see how we can create textviews in java
            }
        };
        for(int i = 0; i < 6; i++) {
            // fetchLiteListing(IDList.get(i), callbackLiteListing);
        }
    }

}
