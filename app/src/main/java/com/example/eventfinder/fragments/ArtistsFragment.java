package com.example.eventfinder.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventfinder.R;
import com.example.eventfinder.adapters.AlbumsAdapter;
import com.example.eventfinder.api.RetrofitClient;
import com.example.eventfinder.models.Attraction;
import com.example.eventfinder.models.Event;
import com.example.eventfinder.models.SpotifyAlbumsResponse;
import com.example.eventfinder.models.SpotifyArtistResponse;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArtistsFragment extends Fragment {

    private Event event;
    private View contentView;
    private View noDataView;
    private ProgressBar progressBar;

    public static ArtistsFragment newInstance(Event event) {
        ArtistsFragment fragment = new ArtistsFragment();
        fragment.setEvent(event);
        return fragment;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artists, container, false);

        contentView = view.findViewById(R.id.contentView);
        noDataView = view.findViewById(R.id.noDataView);
        progressBar = view.findViewById(R.id.progressBar);

        checkIfMusicEvent();

        return view;
    }

    private void checkIfMusicEvent() {
        if (event == null || event.getClassifications() == null || event.getClassifications().isEmpty()) {
            showNoData();
            return;
        }

        String segmentId = event.getClassifications().get(0).getSegment().getId();
        if (!"KZFzniwnSyZfZ7v7nJ".equals(segmentId)) {
            showNoData();
            return;
        }

        loadArtistData();
    }

    private void loadArtistData() {
        if (event.getEmbedded() == null || event.getEmbedded().getAttractions() == null || event.getEmbedded().getAttractions().isEmpty()) {
            showNoData();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        contentView.setVisibility(View.GONE);

        String artistName = event.getEmbedded().getAttractions().get(0).getName();

        RetrofitClient.getApiService().searchArtist(artistName).enqueue(new Callback<SpotifyArtistResponse>() {
            @Override
            public void onResponse(Call<SpotifyArtistResponse> call, Response<SpotifyArtistResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getArtists() != null && response.body().getArtists().getItems() != null && !response.body().getArtists().getItems().isEmpty()) {

                    SpotifyArtistResponse.Artist artist = response.body().getArtists().getItems().get(0);
                    displayArtistInfo(artist);
                    loadAlbums(artist.getId());
                } else {
                    progressBar.setVisibility(View.GONE);
                    showNoData();
                }
            }

            @Override
            public void onFailure(Call<SpotifyArtistResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showNoData();
            }
        });
    }

    private void displayArtistInfo(SpotifyArtistResponse.Artist artist) {
        View view = getView();
        if (view == null) return;

        ImageView artistImage = view.findViewById(R.id.artistImage);
        TextView artistName = view.findViewById(R.id.artistName);
        TextView followersText = view.findViewById(R.id.followersText);
        TextView popularityText = view.findViewById(R.id.popularityText);
        ImageView spotifyIcon = view.findViewById(R.id.spotifyIcon);

        if (artist.getImages() != null && !artist.getImages().isEmpty()) {
            Picasso.get().load(artist.getImages().get(0).getUrl()).into(artistImage);
        }

        artistName.setText(artist.getName());
        followersText.setText(formatNumber(artist.getFollowers().getTotal()) + " Followers");
        popularityText.setText("Popularity: " + artist.getPopularity() + "%");

        spotifyIcon.setOnClickListener(v -> {
            if (artist.getExternalUrls() != null && artist.getExternalUrls().getSpotify() != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(artist.getExternalUrls().getSpotify()));
                startActivity(intent);
            }
        });
    }

    private void loadAlbums(String artistId) {
        RetrofitClient.getApiService().getArtistAlbums(artistId).enqueue(new Callback<SpotifyAlbumsResponse>() {
            @Override
            public void onResponse(Call<SpotifyAlbumsResponse> call, Response<SpotifyAlbumsResponse> response) {
                progressBar.setVisibility(View.GONE);
                contentView.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null && response.body().getItems() != null) {
                    displayAlbums(response.body());
                }
            }

            @Override
            public void onFailure(Call<SpotifyAlbumsResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                contentView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void displayAlbums(SpotifyAlbumsResponse albumsResponse) {
        View view = getView();
        if (view == null) return;

        RecyclerView albumsRecyclerView = view.findViewById(R.id.albumsRecyclerView);
        albumsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        AlbumsAdapter adapter = new AlbumsAdapter(albumsResponse.getItems());
        albumsRecyclerView.setAdapter(adapter);
    }

    private void showNoData() {
        progressBar.setVisibility(View.GONE);
        contentView.setVisibility(View.GONE);
        noDataView.setVisibility(View.VISIBLE);
    }

    private String formatNumber(int number) {
        if (number >= 1000000) {
            return String.format(Locale.US, "%.1fM", number / 1000000.0);
        } else if (number >= 1000) {
            return String.format(Locale.US, "%.1fK", number / 1000.0);
        }
        return String.valueOf(number);
    }
}