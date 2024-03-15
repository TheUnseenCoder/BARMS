package com.capstone.barms;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ComplaintHistory extends AppCompatActivity {
    private static final String TAG = ComplaintHistory.class.getSimpleName();
    private static final String FETCH_COMPLAINT_URL = "https://capstone-it4b.com/epcr/barms/includes/fetchcomplaint.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.complainthistory);

        // Get email from intent
        String email = getIntent().getStringExtra("email");

        fetchComplaints(email);
    }

    private void fetchComplaints(String email) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, FETCH_COMPLAINT_URL + "?email=" + email,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            populateTable(jsonArray);
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error: " + e.getMessage());
                            Toast.makeText(ComplaintHistory.this, "No complaints found", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching complaints: " + error.getMessage());
                        Toast.makeText(ComplaintHistory.this, "Error fetching complaints: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    // Helper method to create TextViews for column headers
    private TextView createHeaderTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(getResources().getColor(R.color.white)); // Set text color to white
        textView.setBackgroundColor(getResources().getColor(R.color.black)); // Set background color to black
        textView.setMaxWidth(200); // Limit maximum width to ensure content fits on smaller screens
        textView.setTextSize(8); // Set text size to 8px
        textView.setTypeface(null, Typeface.BOLD); // Make text bold
        textView.setGravity(Gravity.CENTER);
        return textView;
    }

    // Inside populateTable() method
    private void populateTable(JSONArray jsonArray) throws JSONException {
        TableLayout tableLayout = findViewById(R.id.tableLayout);

        // Create a header row
        TableRow headerRow = new TableRow(this);

        // Column headers
        String[] headers = {"No", "Category", "Description", "Status", "Date"};

        // Create TextViews for each column header and add them to the header row
        for (String header : headers) {
            TextView headerTextView = createHeaderTextView(header);
            headerRow.addView(headerTextView);
        }

        // Add the header row to the table
        tableLayout.addView(headerRow);

        // Iterate over JSON array and populate data rows
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            // Extract complaint details from JSON object
            String category = getCategory(jsonObject.getInt("category_id"));
            String description = jsonObject.getString("complaint");
            String status = jsonObject.getString("status");
            String date = jsonObject.getString("date_submitted");

            // Create a new row and add TextViews for each column
            TableRow dataRow = new TableRow(this);

            // Create TextView for the row number
            TextView textViewNo = createTextView(String.valueOf(i + 1));
            dataRow.addView(textViewNo); // Add row number to the data row

            // Create TextViews for other columns
            TextView textViewCategory = createTextView(category);
            TextView textViewDescription = createTextView(description);
            TextView textViewStatus = createTextView(status);
            TextView textViewDate = createTextView(date);

            // Add TextViews to the TableRow
            dataRow.addView(textViewCategory);
            dataRow.addView(textViewDescription);
            dataRow.addView(textViewStatus);
            dataRow.addView(textViewDate);

            // Add the data row to the table
            tableLayout.addView(dataRow);
        }
    }


    // Helper method to create TextViews for data rows
    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(getResources().getColor(R.color.black)); // Set text color to black
        textView.setPadding(8, 8, 8, 8); // Add padding to the text view
        textView.setTextSize(8); // Set text size to 12px
        textView.setMaxWidth(200); // Limit maximum width to ensure content fits on smaller screens
        return textView;
    }


    private String getCategory(int categoryId) {
        switch (categoryId) {
            case 1:
                return "Sanitation";
            case 2:
                return "Security";
            case 3:
                return "Infrastructure";
            case 4:
                return "Neighbor Concern";
            case 5:
                return "Other Concern";
            default:
                return "Unknown";
        }
    }
}
