package com.example.android.cs496_fp4;

import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONObject;
import org.w3c.dom.Text;

public class LogActivity extends AppCompatActivity implements View.OnClickListener {
    private FusedLocationProviderClient mFusedLocationClient;
    private static String loc;
    private static FirebaseFirestore db;
    private static DocumentReference user;

    private static EditText editTextId;
    private static EditText editTextName;
    private static EditText editTextAge;
    private static TextView textViewDbLoc;
    private static TextView textViewMyLoc;
    private static Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        TextView textViewUser = findViewById(R.id.textView2);
        textViewUser.setText(message);

        Button signOutButton = findViewById(R.id.sign_out_button);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                finish();
            }
        });

        editTextId = findViewById(R.id.editTextId);
        editTextName = findViewById(R.id.editTextName);
        editTextAge = findViewById(R.id.editTextAge);
        textViewDbLoc = findViewById(R.id.textViewDbLoc);
        textViewMyLoc = findViewById(R.id.textViewMyLoc);
        saveButton = findViewById(R.id.buttonSubmit);

        saveButton.setOnClickListener(this);

        // get location client
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // get Firebase instance
        db = FirebaseFirestore.getInstance();
        user = db.collection("users").document("JSPYVHttcrA3LeUUGoCR");

        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("fire", document.getId() + " => " + document.getData());
                                JSONObject data = new JSONObject(document.getData());
                                try {
                                    editTextName.setText(data.getString("name"));
                                    editTextId.setText(data.getString("id"));
                                    editTextAge.setText(data.getString("age"));
                                    textViewDbLoc.setText("DB location: " + data.getString("location").toString());
                                }
                                catch (Exception e){
                                    Log.d("json", e.getMessage());
                                }
                            }
                        } else {
                            Log.w("fire", "Error getting documents.", task.getException());
                        }
                    }
                });

        //user.update("name", "brock");


        // HTTP requests
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://jsonplaceholder.typicode.com/todos/1";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject reader = new JSONObject(response);
                            String userId = reader.getString("userId");
                            String id = reader.getString("id");
                            String title = reader.getString("title");

                            EditText editTextUserId = findViewById(R.id.editTextId);
                            editTextUserId.setText(userId);
                        }
                        catch (Exception e){
                            Toast.makeText(getApplicationContext(), e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "API ERROR", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        queue.add(stringRequest);
    }

    private String convertLocation(Location location){
        // Logic to handle location object
        String lat = Double.toString(location.getLatitude());
        String lon = Double.toString(location.getLongitude());
        return lat + ", " + lon;
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case (R.id.buttonSubmit):
                updateDB();
            default:
                break;
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        // get location
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                loc = convertLocation(location);
                                textViewMyLoc.setText("My location: " + loc);
                            }
                        }
                    });
        }
        catch (SecurityException e){
            Toast.makeText(this, "Location permissions must be enabled", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDB(){
        Toast.makeText(this, "SENDING LOCATION", Toast.LENGTH_SHORT).show();
        user.update("id", editTextId.getText().toString());
        user.update("age", editTextAge.getText().toString());
        user.update("name", editTextName.getText().toString());
        user.update("location", loc);
    }
}
