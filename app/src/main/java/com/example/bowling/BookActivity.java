package com.example.bowling;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class BookActivity extends AppCompatActivity {

    private static final String LOG_TAG = BowlingActivity.class.getName();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference bookingsRef = db.collection("bookings");
    private FirebaseUser user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference datesRef = db.collection("dates");
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null) {
            Log.d(LOG_TAG, "Authenticated user!");
        }else{
            Log.d(LOG_TAG, "Unauthenticated user!");
            finish();
        }

        bookingsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.w(LOG_TAG, "Listen failed.", error);
                return;
            }
            LinearLayout linearLayout = findViewById(R.id.linear_layout2);
            linearLayout.removeAllViews();
            for (QueryDocumentSnapshot document : value) {
                if (document.contains("time")) {
                    String time = document.getString("time");
                    TextView textView = new TextView(this);
                    textView.setBackgroundColor(Color.parseColor("#f8fafa"));
                    textView.setText(time);
                    linearLayout.addView(textView);
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.billiard_menu, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.book_button:
                Log.d(LOG_TAG, "Book clicked!");
                Intent bookIntent = new Intent(this, BowlingActivity.class);
                startActivity(bookIntent);
                return true;
            case R.id.logout_button:
                Log.d(LOG_TAG, "Logout clicked!");
                FirebaseAuth.getInstance().signOut();
                Intent logoutIntent = new Intent(this, MainActivity.class);
                startActivity(logoutIntent);
                finish();
                return true;
            case R.id.contact_button:
                Log.d(LOG_TAG, "Contact clicked!");
                Intent contactIntent = new Intent(this, ContactActivity.class);
                startActivity(contactIntent);
                return true;
            case R.id.dates_button:
                Log.d(LOG_TAG, "Dates clicked!");
                Intent datesIntent = new Intent(this, BookActivity.class);
                startActivity(datesIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

}