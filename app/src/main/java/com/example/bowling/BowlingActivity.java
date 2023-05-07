package com.example.bowling;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BowlingActivity extends AppCompatActivity {
    private static final String LOG_TAG = BowlingActivity.class.getName();
    private FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bowling);

        FirebaseApp.initializeApp(this);
        FirebaseFirestore database  = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        CollectionReference bookingsRef = database.collection("bookings");


        if(user != null) {
            Log.d(LOG_TAG, "Authenticated user!");
        }else{
            Log.d(LOG_TAG, "Unauthenticated user!");
            finish();
        }

        String userEmail = user.getEmail();
        List<String> timeList = new ArrayList<>();

        Calendar currentTime = Calendar.getInstance();
        currentTime.add(Calendar.DAY_OF_MONTH,1);
        int day = currentTime.get(Calendar.DAY_OF_MONTH);
        int month = currentTime.get(Calendar.MONTH);
        int year = currentTime.get(Calendar.YEAR);
        int numOfDays = currentTime.getActualMaximum(Calendar.DAY_OF_MONTH);

        currentTime.set(year,month,day, 20, 0);


        for (int i = 0; i < 14; i++) {
            timeList.add(new SimpleDateFormat("yyyy.MM.dd. HH:mm").format(currentTime.getTime()));
            currentTime.add(Calendar.HOUR_OF_DAY, 1);
            timeList.add(new SimpleDateFormat("yyyy.MM.dd. HH:mm").format(currentTime.getTime()));
            currentTime.add(Calendar.HOUR_OF_DAY, 1);
            timeList.add(new SimpleDateFormat("yyyy.MM.dd. HH:mm").format(currentTime.getTime()));
            currentTime.add(Calendar.HOUR_OF_DAY, 1);
            timeList.add(new SimpleDateFormat("yyyy.MM.dd. HH:mm").format(currentTime.getTime()));
            currentTime.add(Calendar.DAY_OF_MONTH, 1);
            currentTime.set(currentTime.get(Calendar.YEAR),currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH), 20, 0);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, timeList);

        for (String time : timeList) {
            Log.d(LOG_TAG, time);
        }

        LinearLayout parentLayout = findViewById(R.id.linear_layout);
        for (int i = 0; i < timeList.size(); i++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);

            TextView textView = new TextView(this);
            String time = timeList.get(i);
            textView.setText(time);
            textView.setPadding(30, 30, 30, 30);
            textView.setBackgroundColor(Color.parseColor("#f8fafa"));
            textView.setId(i + 1);

            Button button = new Button(this);
            button.setText("Foglalás");
            button.setId(i + 1);

            // foglalás gomb eseménykezelője
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int id = view.getId();
                    String selectedTime = timeList.get(id - 1);

                    // ellenőrizze, hogy az időpont foglalt-e már
                    bookingsRef.whereEqualTo("time", selectedTime)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    QuerySnapshot querySnapshot = task.getResult();
                                    if (!querySnapshot.isEmpty()) {
                                        // az időpont már foglalt, a gombot letiltjuk
                                        button.setText("Foglalva");
                                        button.setEnabled(false);
                                        Log.d(LOG_TAG, "Time slot is already booked!");
                                    } else {
                                        // az időpont még nincs foglalva, mentse a Firebase adatbázisba
                                        Map<String, Object> booking = new HashMap<>();
                                        booking.put("time", selectedTime);
                                        booking.put("user", userEmail);
                                        bookingsRef.document("booking_" + id).set(booking)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Log.d(LOG_TAG, "Booking added to Firestore!");
                                                        // frissítse a gombot, hogy azonnal reagáljon a foglalásra
                                                        button.setText("Foglalva");
                                                        button.setEnabled(false);
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.e(LOG_TAG, "Error adding booking to Firestore!", e);
                                                    }
                                                });
                                    }
                                } else {
                                    Log.e(LOG_TAG, "Error getting bookings from Firestore!", task.getException());
                                }
                            });
                }
            });

            rowLayout.addView(textView);
            rowLayout.addView(button);

            parentLayout.addView(rowLayout);
        }
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