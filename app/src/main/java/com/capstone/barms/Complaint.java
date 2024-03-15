package com.capstone.barms;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Complaint extends AppCompatActivity {

    EditText complaint;
    TextView tvPhoto, tvVideo;
    Button btnPhoto, btnVideo, btnSubmit;
    String email, fullname;

    int categoryId;
    private static final int PHOTO_PICKER_REQUEST_CODE = 1;
    private static final int VIDEO_PICKER_REQUEST_CODE = 2;
    private byte[] photoBytes;
    private byte[] videoBytes;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.complaint);

        complaint = findViewById(R.id.editTextTextMultiLine2);
        tvPhoto = findViewById(R.id.tvPhoto);
        tvVideo = findViewById(R.id.tvVideo);
        btnPhoto = findViewById(R.id.btnPhoto);
        btnVideo = findViewById(R.id.btnVideo);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progress);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("email") && intent.hasExtra("category_id")) {
            email = intent.getStringExtra("email");
            fullname = intent.getStringExtra("full_name");
            categoryId = intent.getIntExtra("category_id", 0); // Default value 0
        }

        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                photoPickerIntent.setType("image/*");
                startActivityForResult(Intent.createChooser(photoPickerIntent, "Select Picture"), PHOTO_PICKER_REQUEST_CODE);
            }
        });

        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent videoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                videoPickerIntent.setType("video/*");
                startActivityForResult(Intent.createChooser(videoPickerIntent, "Select Video"), VIDEO_PICKER_REQUEST_CODE);
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE); // Show ProgressBar when submitting complaint
                submitComplaint();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PHOTO_PICKER_REQUEST_CODE) {
                if (data != null && data.getData() != null) {
                    tvPhoto.setText("                                    Photo Chosen"); // Update hint text
                    Uri selectedImageUri = data.getData();
                    try {
                        photoBytes = getBytes(selectedImageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("Complaint", "No data or URI received for photo");
                }
            } else if (requestCode == VIDEO_PICKER_REQUEST_CODE) {
                if (data != null && data.getData() != null) {
                    tvVideo.setText("                                    Video Chosen"); // Update hint text
                    Uri selectedVideoUri = data.getData();
                    uploadVideo(selectedVideoUri);
                } else {
                    Log.e("Complaint", "No data or URI received for video");
                }
            }
        } else {
            Log.e("Complaint", "Result code not OK: " + resultCode);
        }
    }

    private void uploadVideo(Uri videoUri) {
        try {
            videoBytes = getBytes(videoUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private byte[] getBytes(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        return outputStream.toByteArray();
    }

    private void submitComplaint() {
        String url = "https://capstone-it4b.com/epcr/barms/includes/submit_complaint.php";

        // Create a new StringRequest to handle POST request
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(View.GONE);
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            if (status.equals("success")) {
                                Intent intent = new Intent(Complaint.this, Home.class);
                                intent.putExtra("email", email);
                                intent.putExtra("fullname", fullname);
                                startActivity(intent);
                                Toast.makeText(Complaint.this, "Complaint submitted successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Complaint.this, "Error: " + status, Toast.LENGTH_SHORT).show();
                                Log.e("ComplaintResponse", "Error: " + status);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(Complaint.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                            Log.e("ComplaintResponse", "Error parsing response", e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Complaint.this, "Error submitting complaint", Toast.LENGTH_SHORT).show();
                        Log.e("ComplaintError", "Error submitting complaint", error);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("category_id", String.valueOf(categoryId));
                params.put("complaints", complaint.getText().toString().trim());

                // Convert photo and video bytes to Base64 strings
                String photoString = (photoBytes != null) ? Base64.encodeToString(photoBytes, Base64.DEFAULT) : "";
                String videoString = (videoBytes != null) ? Base64.encodeToString(videoBytes, Base64.DEFAULT) : "";
                params.put("photo", photoString);
                params.put("video", videoString);

                return params;
            }
        };

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(request);

        // Show ProgressBar when submitting complaint
        progressBar.setVisibility(View.VISIBLE);
    }

}
