package com.capstone.barms;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class CreateAccount extends AppCompatActivity {

    EditText user_name, full_name, password, confirm_password, mobile_number;
    Button signUp;
    ProgressBar progressBar;
    ImageButton btnback;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createaccount);
        signUp = findViewById(R.id.btnSignUp);
        btnback = findViewById(R.id.btnBack);
        user_name = findViewById(R.id.cetemail);
        full_name = findViewById(R.id.cetfullname);
        password = findViewById(R.id.cetpassword);
        confirm_password = findViewById(R.id.cetconfirm_password);
        mobile_number = findViewById(R.id.cetmobile);
        progressBar = findViewById(R.id.progress);

        // Set click listeners for buttons
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start SignUpChoices activity
                Intent intent = new Intent(CreateAccount.this, MainActivity.class);
                startActivity(intent);
            }
        });
        // Set click listeners for buttons
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = "https://capstone-it4b.com/epcr/barms/includes/createaccount.php";

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                progressBar.setVisibility(View.GONE);
                                Log.d("Response", response); // Add logging statement for debugging
                                if (response.equals("success")){
                                    Toast.makeText(getApplicationContext(), "Account Created Successfully!", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                }
                                else{
                                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        error.printStackTrace();
                    }
                }){
                    protected Map<String, String> getParams(){
                        Map<String, String> paramV = new HashMap<>();
                        paramV.put("user_name", user_name.getText().toString());
                        paramV.put("fullname", full_name.getText().toString());
                        paramV.put("password", password.getText().toString());
                        paramV.put("confirm_password", confirm_password.getText().toString());
                        paramV.put("mobile_number", mobile_number.getText().toString());
                        return paramV;
                    }
                };
                queue.add(stringRequest);
            }
        });
    }
}
