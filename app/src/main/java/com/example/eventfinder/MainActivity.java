package com.example.eventfinder;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventfinder.adapters.EventAdapter;
import com.example.eventfinder.api.RetrofitClient;
import com.example.eventfinder.models.Event;
import com.example.eventfinder.models.EventSearchResponse;
import com.example.eventfinder.models.FavoriteEvent;
import com.example.eventfinder.models.FavoriteResponse;
import com.example.eventfinder.models.SuggestResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    // UI Components
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private ProgressBar progressBar;
    private TextView emptyView;
    private TextView poweredByText;
    private TextView currentDateText;
    private TabLayout categoryTabs;

    // Location
    private FusedLocationProviderClient fusedLocationClient;
    private String currentLatLng = "34.0522,-118.2437"; // Default LA

    // Data
    private boolean isShowingFavorites = true;
    private boolean isSearchMode = false;
    private List<Event> eventList = new ArrayList<>();
    private List<FavoriteEvent> favoritesList = new ArrayList<>();

    // Autocomplete
    private Handler searchHandler = new Handler();
    private Runnable searchRunnable;
    private static final int SEARCH_DELAY = 300;

    private String lastSearchKeyword = "";
    private String lastSelectedCategory = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupCategoryTabs();
        setupLocation();
        loadFavorites();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        poweredByText = findViewById(R.id.poweredByText);
        currentDateText = findViewById(R.id.currentDateText);
        categoryTabs = findViewById(R.id.categoryTabs);

        // Set current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d, yyyy", Locale.US);
        currentDateText.setText(dateFormat.format(new Date()));

        // Powered by Ticketmaster clickable
        poweredByText.setOnClickListener(v -> {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
            Uri.parse("https://www.ticketmaster.com"));
        startActivity(browserIntent);
    });

        // Hide category tabs initially
        categoryTabs.setVisibility(View.GONE);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Event Finder");
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(this, eventList, favoritesList,
        new EventAdapter.OnEventClickListener() {
            @Override
            public void onEventClick(Event event) {
                openEventDetails(event.getId());
            }

            @Override
            public void onFavoriteClick(Event event, boolean isFavorite) {
                toggleFavorite(event, isFavorite);
            }
        });
        recyclerView.setAdapter(eventAdapter);
    }

    private void setupCategoryTabs() {
        categoryTabs.addTab(categoryTabs.newTab().setText("All").setTag(""));
        categoryTabs.addTab(categoryTabs.newTab().setText("Music").setTag("KZFzniwnSyZfZ7v7nJ"));
        categoryTabs.addTab(categoryTabs.newTab().setText("Sports").setTag("KZFzniwnSyZfZ7v7nE"));
        categoryTabs.addTab(categoryTabs.newTab().setText("Arts & Theatre").setTag("KZFzniwnSyZfZ7v7na"));
        categoryTabs.addTab(categoryTabs.newTab().setText("Film").setTag("KZFzniwnSyZfZ7v7nn"));
        categoryTabs.addTab(categoryTabs.newTab().setText("Miscellaneous").setTag("KZFzniwnSyZfZ7v7n1"));

        categoryTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (isSearchMode && !lastSearchKeyword.isEmpty()) {
                    String categoryId = (String) tab.getTag();
                    performSearch(lastSearchKeyword, categoryId, "10", currentLatLng);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocationPermission();
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                if (location != null) {
                    currentLatLng = location.getLatitude() + "," + location.getLongitude();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }

    private void loadFavorites() {
        showLoading(true);
        isShowingFavorites = true;
        isSearchMode = false;
        categoryTabs.setVisibility(View.GONE);

        RetrofitClient.getApiService().getAllFavorites()
            .enqueue(new Callback<List<FavoriteEvent>>() {
                @Override
                public void onResponse(Call<List<FavoriteEvent>> call,
                    Response<List<FavoriteEvent>> response) {
                    showLoading(false);
                    if (response.isSuccessful() && response.body() != null) {
                        favoritesList.clear();
                        favoritesList.addAll(response.body());
                        updateFavoritesView();
                    } else {
                        showEmptyView("No Favorites");
                    }
                }

                @Override
                public void onFailure(Call<List<FavoriteEvent>> call, Throwable t) {
                    showLoading(false);
                    showEmptyView("No Favorites");
                }
            });
    }

    private void updateFavoritesView() {
        if (favoritesList.isEmpty()) {
            showEmptyView("No Favorites");
        } else {
            hideEmptyView();
            eventList.clear();
            for (FavoriteEvent fav : favoritesList) {
                Event event = new Event();
                event.setId(fav.getEventId());
                event.setName(fav.getName());
                eventList.add(event);
            }
            eventAdapter.updateEvents(eventList, favoritesList);
        }
    }

    private void showSearchDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_search);
        dialog.getWindow().setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );

        AutoCompleteTextView searchInput = dialog.findViewById(R.id.searchKeywordInput);
        TextView errorText = dialog.findViewById(R.id.errorText);
        RadioGroup locationRadioGroup = dialog.findViewById(R.id.locationRadioGroup);
        EditText distanceInput = dialog.findViewById(R.id.distanceInput);
        Button searchButton = dialog.findViewById(R.id.searchButton);
        Button clearButton = dialog.findViewById(R.id.clearButton);

        setupAutocomplete(searchInput);

        searchButton.setOnClickListener(v -> {
        String keyword = searchInput.getText().toString().trim();

        if (keyword.isEmpty()) {
            errorText.setText("Keyword is required");
            errorText.setVisibility(View.VISIBLE);
            return;
        }

        errorText.setVisibility(View.GONE);
        String distance = distanceInput.getText().toString();
        if (distance.isEmpty()) distance = "10";

        boolean useCurrentLocation =
        locationRadioGroup.getCheckedRadioButtonId() == R.id.currentLocationRadio;
        String geoPoint = useCurrentLocation ? currentLatLng : "34.0522,-118.2437";

        lastSearchKeyword = keyword;
        performSearch(keyword, "", distance, geoPoint);
        dialog.dismiss();
    });

        clearButton.setOnClickListener(v -> {
        searchInput.setText("");
        distanceInput.setText("10");
        errorText.setVisibility(View.GONE);
    });

        dialog.show();
    }

    private void setupAutocomplete(AutoCompleteTextView autoCompleteTextView) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            new ArrayList<>()
        );
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setThreshold(1);

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String keyword = s.toString().trim();
                if (keyword.length() > 0) {
                    searchRunnable = () -> fetchSuggestions(keyword, adapter);
                    searchHandler.postDelayed(searchRunnable, SEARCH_DELAY);
                }
            }
        });
    }

    private void fetchSuggestions(String keyword, ArrayAdapter<String> adapter) {
        RetrofitClient.getApiService().getSuggestions(keyword)
            .enqueue(new Callback<SuggestResponse>() {
                @Override
                public void onResponse(Call<SuggestResponse> call,
                    Response<SuggestResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<String> suggestions = new ArrayList<>();
                        SuggestResponse suggestResponse = response.body();

                        if (suggestResponse.getEmbedded() != null &&
                            suggestResponse.getEmbedded().getAttractions() != null) {
                            for (SuggestResponse.Attraction attraction :
                            suggestResponse.getEmbedded().getAttractions()) {
                                suggestions.add(attraction.getName());
                            }
                        }

                        runOnUiThread(() -> {
                            adapter.clear();
                            adapter.addAll(suggestions);
                            adapter.notifyDataSetChanged();
                        });
                    }
                }

                @Override
                public void onFailure(Call<SuggestResponse> call, Throwable t) {
                    // Silent fail for autocomplete
                }
            });
    }

    private void performSearch(String keyword, String segmentId,
        String radius, String geoPoint) {
        showLoading(true);
        categoryTabs.setVisibility(View.VISIBLE);
        isSearchMode = true;
        isShowingFavorites = false;

        RetrofitClient.getApiService().searchEvents(keyword, segmentId, radius, "miles", geoPoint)
            .enqueue(new Callback<EventSearchResponse>() {
                @Override
                public void onResponse(Call<EventSearchResponse> call,
                    Response<EventSearchResponse> response) {
                    showLoading(false);

                    if (response.isSuccessful() && response.body() != null) {
                        EventSearchResponse searchResponse = response.body();

                        if (searchResponse.getEmbedded() != null &&
                            searchResponse.getEmbedded().getEvents() != null) {
                            eventList.clear();
                            eventList.addAll(searchResponse.getEmbedded().getEvents());

                            if (eventList.isEmpty()) {
                                showEmptyView("No events found");
                            } else {
                                hideEmptyView();
                                eventAdapter.updateEvents(eventList, favoritesList);
                            }
                        } else {
                            showEmptyView("No events found");
                        }
                    } else {
                        showEmptyView("Search failed");
                    }
                }

                @Override
                public void onFailure(Call<EventSearchResponse> call, Throwable t) {
                    showLoading(false);
                    showEmptyView("Error: " + t.getMessage());
                }
            });
    }

    private void openEventDetails(String eventId) {
        Intent intent = new Intent(this, EventDetailsActivity.class);
        intent.putExtra("EVENT_ID", eventId);
        startActivity(intent);
    }

    private void toggleFavorite(Event event, boolean isFavorite) {
        if (isFavorite) {
            RetrofitClient.getApiService().removeFavorite(event.getId())
                .enqueue(new Callback<FavoriteResponse>() {
                    @Override
                    public void onResponse(Call<FavoriteResponse> call,
                        Response<FavoriteResponse> response) {
                        Toast.makeText(MainActivity.this,
                            "Removed from favorites", Toast.LENGTH_SHORT).show();
                        loadFavorites();
                    }

                    @Override
                    public void onFailure(Call<FavoriteResponse> call, Throwable t) {
                        Toast.makeText(MainActivity.this,
                            "Error removing favorite", Toast.LENGTH_SHORT).show();
                    }
                });
        } else {
            FavoriteEvent favorite = new FavoriteEvent();
            favorite.setEventId(event.getId());
            favorite.setName(event.getName());

            RetrofitClient.getApiService().addFavorite(favorite)
                .enqueue(new Callback<FavoriteResponse>() {
                    @Override
                    public void onResponse(Call<FavoriteResponse> call,
                        Response<FavoriteResponse> response) {
                        Toast.makeText(MainActivity.this,
                            "Added to favorites", Toast.LENGTH_SHORT).show();
                        loadFavorites();
                    }

                    @Override
                    public void onFailure(Call<FavoriteResponse> call, Throwable t) {
                        Toast.makeText(MainActivity.this,
                            "Error adding favorite", Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyView(String message) {
        emptyView.setText(message);
        emptyView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void hideEmptyView() {
        emptyView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            showSearchDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isShowingFavorites) {
            loadFavorites();
        }
    }
}