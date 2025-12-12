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
import com.example.eventfinder.utils.TimeUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;

import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int SEARCH_DELAY = 500;

    // UI Components
    private Toolbar toolbar;
    private LinearLayout searchFormContainer;
    private AutoCompleteTextView searchKeywordInput;
    private TextView errorText;
    private TextView distanceErrorText;
    private Spinner locationSpinner;
    private MaterialCardView manualLocationCard;
    private AutoCompleteTextView manualLocationInput;
    private EditText distanceInput;
    private ImageView backButton;
    private ImageView searchIcon;
    private ImageView clearIcon;
    private ProgressBar locationProgressBar;
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private ProgressBar progressBar;
    private MaterialCardView emptyViewCard;
    private TextView emptyView;
    private TextView poweredByText;
    private TextView currentDateText;
    private TabLayout categoryTabs;

    // Location
    private FusedLocationProviderClient fusedLocationClient;
    private Geocoder geocoder;
    private String currentLatLng = "34.0522,-118.2437";
    private boolean useCurrentLocation = true;

    // Data
    private boolean isShowingFavorites = true;
    private boolean isSearchMode = false;
    private List<Event> eventList = new ArrayList<>();
    private List<FavoriteEvent> favoritesList = new ArrayList<>();

    // Search
    private Handler searchHandler = new Handler();
    private Handler locationHandler = new Handler();
    private Runnable searchRunnable;
    private Runnable locationRunnable;
    private String lastSearchKeyword = "";

    // Location cache
    private Map<String, String> locationCache = new HashMap<>();
    private ArrayAdapter<String> locationAdapter;

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
        distanceErrorText = findViewById(R.id.distanceErrorText);
        locationSpinner = findViewById(R.id.locationSpinner);
        manualLocationCard = findViewById(R.id.manualLocationCard);
        manualLocationInput = findViewById(R.id.manualLocationInput);
        distanceInput = findViewById(R.id.distanceInput);
        backButton = findViewById(R.id.backButton);
        searchIcon = findViewById(R.id.searchIcon);
        clearIcon = findViewById(R.id.clearIcon);
        locationProgressBar = findViewById(R.id.locationProgressBar);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyViewCard = findViewById(R.id.emptyViewCard);
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

        // Initially hide manual location card
        if (manualLocationCard != null) {
            manualLocationCard.setVisibility(View.GONE);
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
        ArrayAdapter<String> locationSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, locationOptions);
        locationSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(locationSpinnerAdapter);

        // Location spinner listener
        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // Current Location
                    useCurrentLocation = true;
                    if (manualLocationCard != null) {
                        manualLocationCard.setVisibility(View.GONE);
                        if (manualLocationInput != null) {
                            manualLocationInput.setText("");
                        }
                    }
                } else {
                    // Other Location
                    useCurrentLocation = false;
                    if (manualLocationCard != null) {
                        manualLocationCard.setVisibility(View.VISIBLE);
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

        // Manual location autocomplete setup
        if (manualLocationInput != null) {
            locationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
            manualLocationInput.setAdapter(locationAdapter);
            manualLocationInput.setThreshold(1);
            manualLocationInput.setDropDownHeight(600);

            manualLocationInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (locationRunnable != null) {
                        locationHandler.removeCallbacks(locationRunnable);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String location = s.toString().trim();
                    if (location.length() >= 3) {
                        locationRunnable = () -> fetchLocationSuggestions(location);
                        locationHandler.postDelayed(locationRunnable, SEARCH_DELAY);
                    } else {
                        locationAdapter.clear();
                        locationAdapter.notifyDataSetChanged();
                    }
                }
            });

            manualLocationInput.setOnItemClickListener((parent, view, position, id) -> {
                String selectedLocation = (String) parent.getItemAtPosition(position);
                Log.d(TAG, "Location selected: " + selectedLocation);
                manualLocationInput.setText(selectedLocation);
                manualLocationInput.setSelection(selectedLocation.length());
                manualLocationInput.dismissDropDown();
            });

            manualLocationInput.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    manualLocationInput.dismissDropDown();
                }
            });
        }
    }

    private void fetchLocationSuggestions(String location) {
        if (geocoder == null || !Geocoder.isPresent()) {
            Log.e(TAG, "Geocoder not available");
            return;
        }

        locationProgressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                Log.d(TAG, "Geocoding location: " + location);

                // Add a small delay to avoid overwhelming the geocoder service
                Thread.sleep(100);

                List<Address> addresses = geocoder.getFromLocationName(location, 10);

                if (addresses != null && !addresses.isEmpty()) {
                    List<String> suggestions = new ArrayList<>();

                    for (Address address : addresses) {
                        StringBuilder addressString = new StringBuilder();

                        if (address.getLocality() != null) {
                            addressString.append(address.getLocality());
                        } else if (address.getSubAdminArea() != null) {
                            addressString.append(address.getSubAdminArea());
                        }

                        if (address.getAdminArea() != null) {
                            if (addressString.length() > 0) addressString.append(", ");
                            addressString.append(address.getAdminArea());
                        }

                        if (address.getCountryName() != null) {
                            if (addressString.length() > 0) addressString.append(", ");
                            addressString.append(address.getCountryName());
                        }

                        if (addressString.length() > 0) {
                            String displayAddress = addressString.toString();

                            if (!suggestions.contains(displayAddress)) {
                                suggestions.add(displayAddress);
                                String latLng = address.getLatitude() + "," + address.getLongitude();
                                locationCache.put(displayAddress, latLng);
                                Log.d(TAG, "Added suggestion: " + displayAddress + " -> " + latLng);
                            }
                        }
                    }

                    runOnUiThread(() -> {
                        locationProgressBar.setVisibility(View.GONE);

                        if (suggestions.isEmpty()) {
                            Log.d(TAG, "No suggestions to display");
                            return;
                        }

                        locationAdapter.clear();
                        locationAdapter.addAll(suggestions);
                        locationAdapter.notifyDataSetChanged();
                        Log.d(TAG, "Updated adapter with " + suggestions.size() + " suggestions");

                        if (manualLocationInput != null && !manualLocationInput.getText().toString().isEmpty()) {
                            manualLocationInput.showDropDown();
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        locationProgressBar.setVisibility(View.GONE);
                        Log.d(TAG, "No geocoding results found");
                    });
                }
            } catch (IOException e) {
                Log.e(TAG, "Geocoding error", e);
                runOnUiThread(() -> {
                    locationProgressBar.setVisibility(View.GONE);

                    // Check if it's a DeadObjectException
                    if (e.getMessage() != null && e.getMessage().contains("DeadObjectException")) {
                        // Recreate the Geocoder instance
                        geocoder = new Geocoder(MainActivity.this, Locale.US);
                        Log.w(TAG, "Geocoder service died, recreated instance");
                    }

                    // Don't show toast on every error, just log it
                    Log.w(TAG, "Location suggestions temporarily unavailable");
                });
            } catch (InterruptedException e) {
                Log.e(TAG, "Thread interrupted", e);
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void clearSearchForm() {
        searchKeywordInput.setText("");
        distanceInput.setText("10");
        locationSpinner.setSelection(0);
        if (manualLocationInput != null) {
            manualLocationInput.setText("");
            manualLocationInput.dismissDropDown();
        }
        if (manualLocationCard != null) {
            manualLocationCard.setVisibility(View.GONE);
        }
        locationCache.clear();
        if (locationAdapter != null) {
            locationAdapter.clear();
            locationAdapter.notifyDataSetChanged();
        }
        errorText.setVisibility(View.GONE);
        distanceErrorText.setVisibility(View.GONE);
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

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(eventAdapter);
    }

    private void switchToGridLayout() {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
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

                    String locationToUse = getSearchLocation();
                    performSearchWithCategory(lastSearchKeyword, categoryId, distance, locationToUse);
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
        geocoder = new Geocoder(this, Locale.US);

        if (!Geocoder.isPresent()) {
            Log.e(TAG, "Geocoder is not available on this device!");
            Toast.makeText(this, "Geocoding not available on this device", Toast.LENGTH_LONG).show();
        } else {
            Log.d(TAG, "Geocoder is available");
        }

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
                } else {
                    Log.w(TAG, "Location is null, using default Los Angeles coordinates");
                }
            }).addOnFailureListener(e -> {
                locationProgressBar.setVisibility(View.GONE);
                Log.e(TAG, "Failed to get location", e);
            });
        }
    }

    private String getSearchLocation() {
        if (useCurrentLocation) {
            return currentLatLng;
        } else if (manualLocationInput != null) {
            String manualLocation = manualLocationInput.getText().toString().trim();
            if (!manualLocation.isEmpty()) {
                if (locationCache.containsKey(manualLocation)) {
                    String cachedLatLng = locationCache.get(manualLocation);
                    Log.d(TAG, "Using cached location: " + manualLocation + " -> " + cachedLatLng);
                    return cachedLatLng;
                } else {
                    Log.d(TAG, "Geocoding manually entered location: " + manualLocation);
                    String geocodedLatLng = geocodeLocationSync(manualLocation);
                    return geocodedLatLng != null ? geocodedLatLng : currentLatLng;
                }
            }
        }
        return currentLatLng;
    }

    private String geocodeLocationSync(String locationName) {
        if (geocoder == null || !Geocoder.isPresent()) {
            return null;
        }

        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String latLng = address.getLatitude() + "," + address.getLongitude();
                Log.d(TAG, "Geocoded " + locationName + " to " + latLng);
                return latLng;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error geocoding location", e);
        }
        return null;
    }

    private void showSearchForm() {
        searchFormContainer.setVisibility(View.VISIBLE);
        poweredByText.setVisibility(View.GONE);
        currentDateText.setVisibility(View.GONE);
    }

    private void performSearch() {
        String keyword = searchKeywordInput.getText().toString().trim();
        String distance = distanceInput.getText().toString().trim();

        // Validate keyword
        if (keyword.isEmpty()) {
            errorText.setVisibility(View.VISIBLE);
            distanceErrorText.setVisibility(View.GONE);
            return;
        }
        errorText.setVisibility(View.GONE);

        // Validate distance
        if (distance.isEmpty()) {
            distance = "10";
        } else {
            try {
                int distanceValue = Integer.parseInt(distance);
                if (distanceValue <= 0) {
                    distanceErrorText.setText("Distance must be greater than 0");
                    distanceErrorText.setVisibility(View.VISIBLE);
                    return;
                }
            } catch (NumberFormatException e) {
                distanceErrorText.setText("Invalid distance value");
                distanceErrorText.setVisibility(View.VISIBLE);
                return;
            }
        }
        distanceErrorText.setVisibility(View.GONE);

        String locationToUse = getSearchLocation();
        lastSearchKeyword = keyword;
        performSearchWithCategory(keyword, "", distance, locationToUse);
    }

    private void performSearchWithCategory(String keyword, String segmentId, String radius, String geoPoint) {
        Log.d(TAG, "Performing search: " + keyword + " at " + geoPoint);
        showLoading(true);
        categoryTabs.setVisibility(View.VISIBLE);
        isSearchMode = true;
        isShowingFavorites = false;

        // FIXED: Switch to GRID layout for search results (2 columns)
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
            // CREATE COMPLETE FAVORITE EVENT OBJECT
            FavoriteEvent favorite = new FavoriteEvent();
            favorite.setEventId(event.getId());
            favorite.setName(event.getName());

            // ADD IMAGE URL
            if (event.getImages() != null && !event.getImages().isEmpty()) {
                favorite.setImageUrl(event.getImages().get(0).getUrl());
            }

            // ADD DATE
            if (event.getDates() != null && event.getDates().getStart() != null) {
                String date = event.getDates().getStart().getLocalDate();
                String time = event.getDates().getStart().getLocalTime();
                favorite.setDate(TimeUtils.formatEventDate(date, time));
            }

            // ADD VENUE
            if (event.getEmbedded() != null && event.getEmbedded().getVenues() != null && !event.getEmbedded().getVenues().isEmpty()) {
                favorite.setVenue(event.getEmbedded().getVenues().get(0).getName());
            }

            // ADD CATEGORY
            if (event.getClassifications() != null && !event.getClassifications().isEmpty()) {
                if (event.getClassifications().get(0).getSegment() != null) {
                    favorite.setCategory(event.getClassifications().get(0).getSegment().getName());
                }
            }

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
        emptyViewCard.setVisibility(View.GONE);
    }

    private void showEmptyView(String message) {
        emptyView.setText(message);
        emptyViewCard.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void hideEmptyView() {
        emptyViewCard.setVisibility(View.GONE);
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