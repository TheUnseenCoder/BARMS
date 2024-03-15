package com.capstone.barms;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class Profile extends AppCompatActivity {

    private ImageView profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        // Get the email from the intent extras
        String email = getIntent().getStringExtra("email");
        String fullname = getIntent().getStringExtra("fullname");

        // Initialize Views
        TextView emailTextView = findViewById(R.id.emailTextView);
        TextView fullNameTextView = findViewById(R.id.fullNameTextView);
        TextView addressTextView = findViewById(R.id.addressTextView);
        TextView mobileTextView = findViewById(R.id.mobileTextView);
        profileImageView = findViewById(R.id.profileImageView);

        // EditText fields for address and mobile number
        EditText addressEditText = findViewById(R.id.addressEditText);
        EditText mobileEditText = findViewById(R.id.mobileEditText);

        Button btnedit = findViewById(R.id.btnEdit);
        Button btnhistory = findViewById(R.id.btnHistory);
        Button btnEditMobileAddress = findViewById(R.id.btnEditmobile_address);
        Button btnSave = findViewById(R.id.btnSave);

        btnhistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start SignUpChoices activity
                Intent intent = new Intent(Profile.this, ComplaintHistory.class);
                intent.putExtra("email", email);
                intent.putExtra("fullname", fullname);
                startActivity(intent);
            }
        });

        // Initialize the logout button
        Button btnLogout = findViewById(R.id.btnLogout);

        // Set onClickListener for the logout button
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Profile.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Set click listener for profile image to open image selection dialog
        btnedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        // Set click listener for Edit Address and Mobile button
        btnEditMobileAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show EditText fields for address and mobile number
                addressEditText.setVisibility(View.VISIBLE);
                mobileEditText.setVisibility(View.VISIBLE);

                // Hide TextViews for address and mobile number
                addressTextView.setVisibility(View.GONE);
                mobileTextView.setVisibility(View.GONE);

                // Show Save button and hide Edit button
                btnSave.setVisibility(View.VISIBLE);
                btnEditMobileAddress.setVisibility(View.GONE);

                // Populate EditText fields with current address and mobile number
                addressEditText.setText(addressTextView.getText().toString().replace("Address: ", ""));
                mobileEditText.setText(mobileTextView.getText().toString().replace("Mobile: ", ""));
            }
        });

        // Save button click listener
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get updated address and mobile number from EditText fields
                String updatedAddress = addressEditText.getText().toString().trim();
                String updatedMobile = mobileEditText.getText().toString().trim();

                // Update the address and mobile number in the database
                updateProfile(email, updatedAddress, updatedMobile);
            }
        });

        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://capstone-it4b.com/epcr/barms/includes/updatechecker.php";

        // Request a string response from the provided URL
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getString("status").equals("success")) {
                                emailTextView.setText("Email: " + jsonObject.getString("email"));
                                fullNameTextView.setText(jsonObject.getString("fullname"));
                                addressTextView.setText("Address: " + jsonObject.getString("address"));
                                mobileTextView.setText("Mobile: " + jsonObject.getString("mobile"));

                                // Decode and set profile image
                                String profileBase64 = jsonObject.getString("profile");
                                if (profileBase64 != null && !profileBase64.isEmpty()) {
                                    // Decode Base64-encoded image data
                                    byte[] profileBytes = Base64.decode(profileBase64, Base64.DEFAULT);

                                    // Convert the byte array into a Bitmap
                                    Bitmap profileBitmap = BitmapFactory.decodeByteArray(profileBytes, 0, profileBytes.length);

                                    // Set the decoded Bitmap as the image for the ImageView
                                    profileImageView.setImageBitmap(profileBitmap);
                                } else {
                                    // Set default profile image
                                    profileImageView.setImageResource(R.drawable.default_profile);
                                }
                            } else {
                                Toast.makeText(Profile.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(Profile.this, "JSON Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(Profile.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                return params;
            }
        };

        // Add the request to the RequestQueue
        queue.add(stringRequest);

    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                // Get the image URI
                Uri selectedImageUri = data.getData();
                // Set the selected image to the ImageView
                profileImageView.setImageURI(selectedImageUri);
                // Upload the image to the server and update the database
                uploadImageToServer(selectedImageUri);
            }
        }
    }

    private void uploadImageToServer(Uri imageUri) {
        try {
            // Get the bitmap from the URI
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            // Get the user's email
            String email = getIntent().getStringExtra("email");

            // Instantiate the RequestQueue
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "https://capstone-it4b.com/epcr/barms/includes/updateprofile.php";

            // Request a string response from the provided URL
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Toast.makeText(Profile.this, response, Toast.LENGTH_SHORT).show();
                            // Handle response
                            if (response.equals("success")) {
                                // Profile image updated successfully
                                Toast.makeText(Profile.this, "Image Update Success", Toast.LENGTH_SHORT).show();
                            } else {
                                // Error updating profile image
                                Toast.makeText(Profile.this, "Error updating profile image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Handle error
                    Toast.makeText(Profile.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("email", email);
                    params.put("profile", imageString);
                    return params;
                }
            };

            // Add the request to the RequestQueue
            queue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Method to update the profile with new address and mobile number
    // Method to update the profile with new address and mobile number
    private void updateProfile(String email, String address, String mobile) {
        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://capstone-it4b.com/epcr/barms/includes/updateaddressmob.php";

        // Request a string response from the provided URL
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle response
                        if (response.equals("success")) {
                            // Profile updated successfully
                            Toast.makeText(Profile.this, "Profile Update Success", Toast.LENGTH_SHORT).show();
                            TextView addressTextView = findViewById(R.id.addressTextView);
                            TextView mobileTextView = findViewById(R.id.mobileTextView);
                            EditText addressEditText = findViewById(R.id.addressEditText);
                            EditText mobileEditText = findViewById(R.id.mobileEditText);
                            Button btnSave = findViewById(R.id.btnSave);
                            Button btnEditMobileAddress = findViewById(R.id.btnEditmobile_address);

                            // Ensure views are initialized before updating them
                            if (addressTextView != null && mobileTextView != null &&
                                    addressEditText != null && mobileEditText != null &&
                                    btnSave != null && btnEditMobileAddress != null) {
                                addressTextView.setText("Address: " + address);
                                mobileTextView.setText("Mobile: " + mobile);
                                addressTextView.setVisibility(View.VISIBLE);
                                mobileTextView.setVisibility(View.VISIBLE);
                                addressEditText.setVisibility(View.GONE);
                                mobileEditText.setVisibility(View.GONE);

                                btnSave.setVisibility(View.GONE);
                                btnEditMobileAddress.setVisibility(View.VISIBLE);
                            } else {
                                String errorMessage = "Error: Some views are null";
                                Toast.makeText(Profile.this, errorMessage, Toast.LENGTH_SHORT).show();
                                Log.e("Profile", errorMessage);
                            }
                        } else {
                            // Error updating profile
                            Toast.makeText(Profile.this, "Error updating profile: " + response, Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error
                String errorMessage = "Error: " + error.getMessage();
                Toast.makeText(Profile.this, errorMessage, Toast.LENGTH_SHORT).show();
                Log.e("Profile", errorMessage, error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("address", address);
                params.put("mobile", mobile);
                return params;
            }
        };

        // Add the request to the RequestQueue
        queue.add(stringRequest);
    }

}
