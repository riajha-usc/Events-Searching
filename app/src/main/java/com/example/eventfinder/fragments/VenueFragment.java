package com.example.eventfinder.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventfinder.R;
import com.example.eventfinder.models.Event;
import com.example.eventfinder.models.Venue;
import com.squareup.picasso.Picasso;

public class VenueFragment extends Fragment {

    private Event event;

    public static VenueFragment newInstance(Event event) {
        VenueFragment fragment = new VenueFragment();
        fragment.setEvent(event);
        return fragment;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_venue, container, false);

        if (event != null && event.getEmbedded() != null && event.getEmbedded().getVenues() != null && !event.getEmbedded().getVenues().isEmpty()) {

            Venue venue = event.getEmbedded().getVenues().get(0);
            populateVenueInfo(view, venue);
        }

        return view;
    }

    private void populateVenueInfo(View view, Venue venue) {
        TextView venueNameText = view.findViewById(R.id.venueNameText);
        TextView addressText = view.findViewById(R.id.addressText);
        ImageView venueImage = view.findViewById(R.id.venueImage);
        ImageView ticketmasterIcon = view.findViewById(R.id.ticketmasterIcon);

        venueNameText.setText(venue.getName());

        if (venue.getAddress() != null && venue.getAddress().getLine1() != null) {
            addressText.setText(venue.getAddress().getLine1());
        }

        if (venue.getImages() != null && !venue.getImages().isEmpty()) {
            Picasso.get().load(venue.getImages().get(0).getUrl()).into(venueImage);
        }

        ticketmasterIcon.setOnClickListener(v -> {
            if (venue.getUrl() != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(venue.getUrl()));
                startActivity(intent);
            }
        });
    }
}