package com.example.eventfinder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int SEARCH_DELAY = 300;

    // UI Components
    private Toolbar toolbar;
    private LinearLayout searchFormContainer;
    private AutoCompleteTextView searchKeywordInput;
    private TextView errorText;
    private Spinner locationSpinner;
    private AutoCompleteTextView manualLocationInput;
    private EditText distanceInput;
    private ImageView backButton;
    private ImageView searchIcon;
    private ImageView clearIcon;
    private ProgressBar locationProgressBar;
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private ProgressBar progressBar;
    private TextView emptyView;
    private TextView poweredByText;
    private TextView currentDateText;
    private TabLayout categoryTabs;

    // Location
    private FusedLocationProviderClient fusedLocationClient;
    private String currentLatLng = "34.0522,-118.2437";
    private boolean useCurrentLocation = true;

    // Data
    private boolean isShowingFavorites = true;
    private boolean isSearchMode = false;
    private List<Event> eventList = new ArrayList<>();
    private List<FavoriteEvent> favoritesList = new ArrayList<>();

    // Search
    private Handler searchHandler = new Handler();
    private Runnable searchRunnable;
    private String lastSearchKeyword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);
            initializeViews();
            setupToolbar();
            setupSearchForm();
            setupRecyclerView();
            setupCategoryTabs();
            setupLocation();
            loadFavorites();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        searchFormContainer = findViewById(R.id.searchFormContainer);
        searchKeywordInput = findViewById(R.id.searchKeywordInput);
        errorText = findViewById(R.id.errorText);
        locationSpinner = findViewById(R.id.locationSpinner);
        manualLocationInput = findViewById(R.id.manualLocationInput);
        distanceInput = findViewById(R.id.distanceInput);
        backButton = findViewById(R.id.backButton);
        searchIcon = findViewById(R.id.searchIcon);
        clearIcon = findViewById(R.id.clearIcon);
        locationProgressBar = findViewById(R.id.locationProgressBar);
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
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.ticketmaster.com"));
            startActivity(browserIntent);
        });

        // Hide category tabs and search form initially
        categoryTabs.setVisibility(View.GONE);
        searchFormContainer.setVisibility(View.GONE);

        // Initially hide manual location input
        if (manualLocationInput != null) {
            manualLocationInput.setVisibility(View.GONE);
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Event Finder");
        }
    }

    private void setupSearchForm() {
        // Setup location spinner
        List<String> locationOptions = Arrays.asList("Current Location", "Other Location");
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, locationOptions);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(locationAdapter);

        // Location spinner listener
        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // Current Location
                    useCurrentLocation = true;
                    if (manualLocationInput != null) {
                        manualLocationInput.setVisibility(View.GONE);
                        manualLocationInput.setText("");
                    }
                } else {
                    // Other Location
                    useCurrentLocation = false;
                    if (manualLocationInput != null) {
                        manualLocationInput.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Back button
        backButton.setOnClickListener(v -> {
            searchFormContainer.setVisibility(View.GONE);
            categoryTabs.setVisibility(View.GONE);
            loadFavorites();
        });

        // Search icon
        searchIcon.setOnClickListener(v -> performSearch());

        // Clear icon
        if (clearIcon != null) {
            clearIcon.setOnClickListener(v -> clearSearchForm());
        }

        // Keyword autocomplete
        ArrayAdapter<String> keywordAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        searchKeywordInput.setAdapter(keywordAdapter);
        searchKeywordInput.setThreshold(1);

        searchKeywordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

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
                    searchRunnable = () -> fetchSuggestions(keyword, keywordAdapter);
                    searchHandler.postDelayed(searchRunnable, SEARCH_DELAY);
                }
            }
        });

        // Enter key to search
        searchKeywordInput.setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });

        // Manual location autocomplete (if you want to add geocoding suggestions later)
        if (manualLocationInput != null) {
            manualLocationInput.setThreshold(1);
        }
    }

    private void clearSearchForm() {
        searchKeywordInput.setText("");
        distanceInput.setText("10");
        locationSpinner.setSelection(0);
        if (manualLocationInput != null) {
            manualLocationInput.setText("");
            manualLocationInput.setVisibility(View.GONE);
        }
        errorText.setVisibility(View.GONE);
        useCurrentLocation = true;
    }

    private void fetchSuggestions(String keyword, ArrayAdapter<String> adapter) {
        RetrofitClient.getApiService().getSuggestions(keyword).enqueue(new Callback<SuggestResponse>() {
            @Override
            public void onResponse(Call<SuggestResponse> call, Response<SuggestResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> suggestions = new ArrayList<>();
                    SuggestResponse suggestResponse = response.body();

                    if (suggestResponse.getEmbedded() != null && suggestResponse.getEmbedded().getAttractions() != null) {
                        for (SuggestResponse.Attraction attraction : suggestResponse.getEmbedded().getAttractions()) {
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
                // Silent fail
            }
        });
    }

    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(this, eventList, favoritesList, new EventAdapter.OnEventClickListener() {
            @Override
            public void onEventClick(Event event) {
                openEventDetails(event.getId());
            }

            @Override
            public void onFavoriteClick(Event event, boolean isFavorite) {
                toggleFavorite(event, isFavorite);
            }
        });

        // Start with LinearLayout for favorites
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(eventAdapter);
    }

    private void switchToGridLayout() {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
    }

    private void switchToListLayout() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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
                    String distance = distanceInput.getText().toString();
                    if (distance.isEmpty()) distance = "10";
                    performSearchWithCategory(lastSearchKeyword, categoryId, distance, currentLatLng);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void setupLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocationPermission();
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationProgressBar.setVisibility(View.VISIBLE);
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                locationProgressBar.setVisibility(View.GONE);
                if (location != null) {
                    currentLatLng = location.getLatitude() + "," + location.getLongitude();
                    Log.d(TAG, "Got location: " + currentLatLng);
                }
            }).addOnFailureListener(e -> {
                locationProgressBar.setVisibility(View.GONE);
            });
        }
    }

    private void showSearchForm() {
        searchFormContainer.setVisibility(View.VISIBLE);
        poweredByText.setVisibility(View.GONE);
        currentDateText.setVisibility(View.GONE);
    }

    private void performSearch() {
        String keyword = searchKeywordInput.getText().toString().trim();

        if (keyword.isEmpty()) {
            errorText.setVisibility(View.VISIBLE);
            return;
        }

        errorText.setVisibility(View.GONE);
        String distance = distanceInput.getText().toString().trim();
        if (distance.isEmpty()) distance = "10";

        // Determine location to use
        String locationToUse = currentLatLng;
        if (!useCurrentLocation && manualLocationInput != null) {
            String manualLocation = manualLocationInput.getText().toString().trim();
            if (!manualLocation.isEmpty()) {
                // TODO: Geocode manual location - for now use current location
                // You would need to call a geocoding API here
                locationToUse = currentLatLng;
            }
        }

        lastSearchKeyword = keyword;
        performSearchWithCategory(keyword, "", distance, locationToUse);
    }

    private void performSearchWithCategory(String keyword, String segmentId, String radius, String geoPoint) {
        Log.d(TAG, "Performing search: " + keyword);
        showLoading(true);
        categoryTabs.setVisibility(View.VISIBLE);
        isSearchMode = true;
        isShowingFavorites = false;

        // Switch to grid layout for search results
        switchToGridLayout();

        RetrofitClient.getApiService().searchEvents(keyword, segmentId, radius, "miles", geoPoint).enqueue(new Callback<EventSearchResponse>() {
            @Override
            public void onResponse(Call<EventSearchResponse> call, Response<EventSearchResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    EventSearchResponse searchResponse = response.body();

                    if (searchResponse.getEmbedded() != null && searchResponse.getEmbedded().getEvents() != null) {
                        eventList.clear();
                        eventList.addAll(searchResponse.getEmbedded().getEvents());

                        if (eventList.isEmpty()) {
                            showEmptyView("No events found");
                        } else {
                            hideEmptyView();
                            eventAdapter.updateEvents(eventList, favoritesList, false);
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

    private void loadFavorites() {
        showLoading(true);
        isShowingFavorites = true;
        isSearchMode = false;
        categoryTabs.setVisibility(View.GONE);
        searchFormContainer.setVisibility(View.GONE);
        poweredByText.setVisibility(View.VISIBLE);
        currentDateText.setVisibility(View.VISIBLE);

        // Switch to list layout for favorites
        switchToListLayout();

        RetrofitClient.getApiService().getAllFavorites().enqueue(new Callback<List<FavoriteEvent>>() {
            @Override
            public void onResponse(Call<List<FavoriteEvent>> call, Response<List<FavoriteEvent>> response) {
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
            eventAdapter.updateEvents(eventList, favoritesList, true);
        }
    }

    private void openEventDetails(String eventId) {
        Intent intent = new Intent(this, EventDetailsActivity.class);
        intent.putExtra("EVENT_ID", eventId);
        startActivity(intent);
    }

    private void toggleFavorite(Event event, boolean isFavorite) {
        if (isFavorite) {
            RetrofitClient.getApiService().removeFavorite(event.getId()).enqueue(new Callback<FavoriteResponse>() {
                @Override
                public void onResponse(Call<FavoriteResponse> call, Response<FavoriteResponse> response) {
                    Toast.makeText(MainActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                    reloadFavoritesList();
                }

                @Override
                public void onFailure(Call<FavoriteResponse> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Error removing favorite", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            FavoriteEvent favorite = new FavoriteEvent();
            favorite.setEventId(event.getId());
            favorite.setName(event.getName());

            RetrofitClient.getApiService().addFavorite(favorite).enqueue(new Callback<FavoriteResponse>() {
                @Override
                public void onResponse(Call<FavoriteResponse> call, Response<FavoriteResponse> response) {
                    Toast.makeText(MainActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                    reloadFavoritesList();
                }

                @Override
                public void onFailure(Call<FavoriteResponse> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Error adding favorite", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void reloadFavoritesList() {
        RetrofitClient.getApiService().getAllFavorites().enqueue(new Callback<List<FavoriteEvent>>() {
            @Override
            public void onResponse(Call<List<FavoriteEvent>> call, Response<List<FavoriteEvent>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    favoritesList.clear();
                    favoritesList.addAll(response.body());
                    if (isShowingFavorites) {
                        updateFavoritesView();
                    } else {
                        eventAdapter.updateEvents(eventList, favoritesList, false);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<FavoriteEvent>> call, Throwable t) {
            }
        });
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
            showSearchForm();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadFavoritesList();
    }
}