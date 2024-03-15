package com.capstone.barms;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Home extends AppCompatActivity {
    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 100;

    private WindowManager windowManager;
    private ImageView floatingImage;

    ImageButton profile, meeting;
    private static final int[] categoryButtonIds = {R.id.ivSanitation, R.id.ivSecurity, R.id.ibInfrastructure, R.id.ibNeighbor, R.id.ibOthers};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        // Set onClick listener for each category button

                for (int i = 0; i < categoryButtonIds.length; i++) {
                    final int categoryId = i + 1; // Category ID starts from 1
                    ImageButton categoryButton = findViewById(categoryButtonIds[i]);
                    categoryButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Start Complaint activity and pass email and category ID as extras
                            Intent intent = new Intent(Home.this, Complaint.class);
                            intent.putExtra("email", getIntent().getStringExtra("email"));
                            intent.putExtra("category_id", categoryId);
                            startActivity(intent);
                        }
                    });
                }

        profile = findViewById(R.id.btnprofile);
        // Set click listeners for buttons
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start SignUpChoices activity
                Intent intent = new Intent(Home.this, Profile.class);
                intent.putExtra("email", getIntent().getStringExtra("email"));
                intent.putExtra("fullname", getIntent().getStringExtra("fullname"));
                startActivity(intent);
            }
        });

        meeting = findViewById(R.id.btnmeeting);
        // Set click listeners for buttons
        meeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start SignUpChoices activity
                Intent intent = new Intent(Home.this, Meeting.class);
                intent.putExtra("email", getIntent().getStringExtra("email"));
                intent.putExtra("fullname", getIntent().getStringExtra("fullname"));
                startActivity(intent);
            }
        });

        // Get all the button IDs
        int[] buttonIds = {R.id.btnHazardous, R.id.btnDefacation, R.id.btnCanal, R.id.btnContaminated,
                R.id.btnImproper, R.id.btnThreats, R.id.btnTheft, R.id.btnCollapse, R.id.btnDamage,
                R.id.btnOtherDamage, R.id.btnVandalism, R.id.btnProperty, R.id.btnParking, R.id.btnLoud};

        for (int buttonId : buttonIds) {
            Button button = findViewById(buttonId);
            button.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // Get the image resource ID based on the button's ID
                    String imageName = getResources().getResourceEntryName(buttonId).toLowerCase();
                    int imageResource = getResources().getIdentifier(imageName, "drawable", getPackageName());
                    if (imageResource != 0) {
                        showFloatingImage(imageResource);
                    } else {
                        Log.e("FloatingImage", "Resource not found for image.");
                    }
                    return true;
                }
            });

            button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        removeFloatingImage();
                    }
                    return false;
                }
            });
        }
    }

    private void showFloatingImage(int imageResource) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // Request the permission dynamically
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
            return;
        }

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View floatingView = inflater.inflate(R.layout.floating_image_layout, null);
        floatingImage = floatingView.findViewById(R.id.floating_image);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.CENTER | Gravity.START;
        params.x = 0;
        params.y = 0;

        if (windowManager == null) {
            windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        }

        windowManager.addView(floatingView, params);

        floatingImage.setImageResource(imageResource);
        floatingImage.setVisibility(View.VISIBLE);
    }

    private void removeFloatingImage() {
        if (floatingImage != null && floatingImage.getParent() != null && floatingImage.getParent() instanceof View) {
            windowManager.removeView((View) floatingImage.getParent());
            floatingImage = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                // Permission granted, show the floating image
                // Replace 0 with the image resource ID you want to show initially
                showFloatingImage(0);
            } else {
                // Permission not granted
                Log.e("FloatingImage", "Overlay permission not granted.");
            }
        }
    }
}
