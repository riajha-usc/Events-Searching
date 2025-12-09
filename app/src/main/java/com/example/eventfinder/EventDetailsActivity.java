package com.example.eventfinder;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class EventDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: Implement event details with 3 tabs
        setContentView(R.layout.activity_main);

        String eventId = getIntent().getStringExtra("EVENT_ID");
        // Load event details
    }
}
