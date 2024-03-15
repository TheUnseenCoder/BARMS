package com.capstone.barms;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class Meeting extends AppCompatActivity {
    private static final String TAG = Meeting.class.getSimpleName();
    private static final String FETCH_MEETING_URL = "https://capstone-it4b.com/epcr/barms/includes/fetch_meeting.php";
    private TextView descriptionTextView, dateTimeTextView, setterTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meeting);

        // Initialize TextViews
        descriptionTextView = findViewById(R.id.descriptionTextView);
        dateTimeTextView = findViewById(R.id.dateTimeTextView);
        setterTextView = findViewById(R.id.Meetingsetter);

        // Get the email from the intent extras
        String email = getIntent().getStringExtra("email");
        Log.d(TAG, "Email: " + email);

        // Fetch meeting details
        fetchMeetingDetails(email);
    }

    private void fetchMeetingDetails(String email) {
        // Make Volley String Request
        StringRequest request = new StringRequest(Request.Method.GET, FETCH_MEETING_URL + "?email=" + email,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Check if the response is not empty and contains meeting details
                        if (!response.isEmpty() && !response.equals("No meeting details found")) {
                            // Split the response string into individual meeting details
                            String[] meetingDetails = response.split("\\|");

                            // Check if the array has the expected number of elements
                            if (meetingDetails.length == 3) {
                                // Extract meeting details
                                String description = meetingDetails[0];
                                String dateTime = meetingDetails[1];
                                String setter = meetingDetails[2];

                                // Assuming description, dateTime, and setter are strings containing the content
                                String formattedDescription = "  Description: " + description;
                                String formattedDateTime = "  Date and Time: " + dateTime;
                                String formattedSetter = "  Meeting Setter: " + setter;

                                descriptionTextView.setText(formattedDescription);
                                dateTimeTextView.setText(formattedDateTime);
                                setterTextView.setText(formattedSetter);
                            } else {
                                // Handle incorrect response format
                                Toast.makeText(getApplicationContext(), "Error: Incorrect response format", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // No meeting details found
                            Toast.makeText(getApplicationContext(), "No meeting details found", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Volley Error: " + error.getMessage());
                        Toast.makeText(getApplicationContext(), "Error fetching meeting details", Toast.LENGTH_SHORT).show();
                    }
                });

        // Add request to Volley request queue
        Volley.newRequestQueue(this).add(request);
    }
}
