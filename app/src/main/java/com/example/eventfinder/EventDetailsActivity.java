package com.example.eventfinder;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.eventfinder.adapters.EventDetailsPagerAdapter;
import com.example.eventfinder.api.RetrofitClient;
import com.example.eventfinder.models.Event;
import com.example.eventfinder.models.EventDetailsResponse;
import com.example.eventfinder.models.FavoriteEvent;
import com.example.eventfinder.models.FavoriteResponse;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventDetailsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView toolbarTitle;
    private ImageView favoriteIcon;
    private ImageView shareIcon;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ProgressBar progressBar;

    private String eventId;
    private Event eventDetails;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        eventId = getIntent().getStringExtra("EVENT_ID");
        if (eventId == null) {
            finish();
            return;
        }

        initializeViews();
        setupToolbar();
        checkFavoriteStatus();
        loadEventDetails();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        favoriteIcon = findViewById(R.id.favoriteIcon);
        shareIcon = findViewById(R.id.shareIcon);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        favoriteIcon.setOnClickListener(v -> toggleFavorite());
        shareIcon.setOnClickListener(v -> shareEvent());
    }

    private void checkFavoriteStatus() {
        RetrofitClient.getApiService().checkFavorite(eventId).enqueue(new Callback<FavoriteResponse>() {
            @Override
            public void onResponse(Call<FavoriteResponse> call, Response<FavoriteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    isFavorite = response.body().isFavorite();
                    updateFavoriteIcon();
                }
            }

            @Override
            public void onFailure(Call<FavoriteResponse> call, Throwable t) {
                // Silent fail
            }
        });
    }

    private void loadEventDetails() {
        progressBar.setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.GONE);

        RetrofitClient.getApiService().getEventDetails(eventId).enqueue(new Callback<EventDetailsResponse>() {
            @Override
            public void onResponse(Call<EventDetailsResponse> call, Response<EventDetailsResponse> response) {
                progressBar.setVisibility(View.GONE);
                viewPager.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    eventDetails = response.body();
                    setupEventDetails();
                } else {
                    Toast.makeText(EventDetailsActivity.this, "Failed to load event details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<EventDetailsResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EventDetailsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupEventDetails() {
        toolbarTitle.setText(eventDetails.getName());
        toolbarTitle.setSelected(true); // Enable marquee

        // Setup ViewPager with tabs
        EventDetailsPagerAdapter adapter = new EventDetailsPagerAdapter(this, eventDetails);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Details");
                    break;
                case 1:
                    tab.setText("Artists");
                    break;
                case 2:
                    tab.setText("Venue");
                    break;
            }
        }).attach();
    }

    private void toggleFavorite() {
        if (eventDetails == null) return;

        if (isFavorite) {
            // ... remove favorite code stays the same
        } else {
            FavoriteEvent favorite = new FavoriteEvent();
            favorite.setEventId(eventId);
            favorite.setName(eventDetails.getName());

            // ADD IMAGE URL
            if (eventDetails.getImages() != null && !eventDetails.getImages().isEmpty()) {
                favorite.setImageUrl(eventDetails.getImages().get(0).getUrl());
            }

            // ADD DATE
            if (eventDetails.getDates() != null && eventDetails.getDates().getStart() != null) {
                String date = eventDetails.getDates().getStart().getLocalDate();
                String time = eventDetails.getDates().getStart().getLocalTime();

                // Format the date properly
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    Date parsedDate = inputFormat.parse(date);
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
                    String formattedDate = outputFormat.format(parsedDate);

                    if (time != null && !time.isEmpty()) {
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
                        Date parsedTime = timeFormat.parse(time);
                        SimpleDateFormat timeOutputFormat = new SimpleDateFormat("h:mm a", Locale.US);
                        formattedDate += ", " + timeOutputFormat.format(parsedTime);
                    }
                    favorite.setDate(formattedDate);
                } catch (Exception e) {
                    favorite.setDate(date + (time != null ? " " + time : ""));
                }
            }

            // ADD VENUE
            if (eventDetails.getEmbedded() != null && eventDetails.getEmbedded().getVenues() != null
                    && !eventDetails.getEmbedded().getVenues().isEmpty()) {
                favorite.setVenue(eventDetails.getEmbedded().getVenues().get(0).getName());
            }

            // ADD CATEGORY
            if (eventDetails.getClassifications() != null && !eventDetails.getClassifications().isEmpty()) {
                if (eventDetails.getClassifications().get(0).getSegment() != null) {
                    favorite.setCategory(eventDetails.getClassifications().get(0).getSegment().getName());
                }
            }

            RetrofitClient.getApiService().addFavorite(favorite).enqueue(new Callback<FavoriteResponse>() {
                @Override
                public void onResponse(Call<FavoriteResponse> call, Response<FavoriteResponse> response) {
                    isFavorite = true;
                    updateFavoriteIcon();
                    Toast.makeText(EventDetailsActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<FavoriteResponse> call, Throwable t) {
                    Toast.makeText(EventDetailsActivity.this, "Error adding favorite", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateFavoriteIcon() {
        favoriteIcon.setImageResource(isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
    }

    private void shareEvent() {
        if (eventDetails == null) return;

        // Create the share text with event details
        String shareText = "Check out " + eventDetails.getName() + " on Ticketmaster: " + eventDetails.getUrl();

        // Create the share intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        // Add a subject line (optional but good practice)
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, eventDetails.getName());

        // Create chooser to show all sharing options
        Intent chooserIntent = Intent.createChooser(shareIntent, "Share event via");

        // Verify that the intent can be resolved
        if (shareIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooserIntent);
        } else {
            Toast.makeText(this, "No apps available to share", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}