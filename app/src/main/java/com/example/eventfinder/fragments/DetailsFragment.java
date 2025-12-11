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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.eventfinder.R;
import com.example.eventfinder.models.Attraction;
import com.example.eventfinder.models.Classification;
import com.example.eventfinder.models.Event;
import com.example.eventfinder.models.PriceRange;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetailsFragment extends Fragment {

    private static final String ARG_EVENT = "event";
    private Event event;

    public static DetailsFragment newInstance(Event event) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT, (java.io.Serializable) event);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Note: For proper serialization, consider using Parcelable or passing event ID
            // For now, we'll handle this through a workaround
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);

        TextView dateText = view.findViewById(R.id.dateText);
        TextView artistsText = view.findViewById(R.id.artistsText);
        TextView venueText = view.findViewById(R.id.venueText);
        TextView genresText = view.findViewById(R.id.genresText);
        TextView priceRangeText = view.findViewById(R.id.priceRangeText);
        TextView ticketStatusText = view.findViewById(R.id.ticketStatusText);
        ImageView seatmapImage = view.findViewById(R.id.seatmapImage);
        ImageView buyTicketsIcon = view.findViewById(R.id.buyTicketsIcon);

        if (event != null) {
            populateDetails(dateText, artistsText, venueText, genresText, priceRangeText, ticketStatusText, seatmapImage, buyTicketsIcon);
        }

        return view;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    private void populateDetails(TextView dateText, TextView artistsText, TextView venueText, TextView genresText, TextView priceRangeText, TextView ticketStatusText, ImageView seatmapImage, ImageView buyTicketsIcon) {

        // Date and Time
        if (event.getDates() != null && event.getDates().getStart() != null) {
            String date = event.getDates().getStart().getLocalDate();
            String time = event.getDates().getStart().getLocalTime();

            String formattedDate = formatDate(date, time);
            dateText.setText(formattedDate);
        }

        // Artists/Teams
        if (event.getEmbedded() != null && event.getEmbedded().getAttractions() != null) {
            StringBuilder artists = new StringBuilder();
            for (Attraction attraction : event.getEmbedded().getAttractions()) {
                if (artists.length() > 0) artists.append(", ");
                artists.append(attraction.getName());
            }
            artistsText.setText(artists.toString());
        }

        // Venue
        if (event.getEmbedded() != null && event.getEmbedded().getVenues() != null && !event.getEmbedded().getVenues().isEmpty()) {
            venueText.setText(event.getEmbedded().getVenues().get(0).getName());
        }

        // Genres
        if (event.getClassifications() != null && !event.getClassifications().isEmpty()) {
            Classification classification = event.getClassifications().get(0);
            StringBuilder genres = new StringBuilder();

            if (classification.getSegment() != null)
                genres.append(classification.getSegment().getName());
            if (classification.getGenre() != null && classification.getGenre().getName() != null) {
                if (genres.length() > 0) genres.append(" | ");
                genres.append(classification.getGenre().getName());
            }
            if (classification.getSubGenre() != null && classification.getSubGenre().getName() != null) {
                if (genres.length() > 0) genres.append(" | ");
                genres.append(classification.getSubGenre().getName());
            }
            if (classification.getType() != null && classification.getType().getName() != null) {
                if (genres.length() > 0) genres.append(" | ");
                genres.append(classification.getType().getName());
            }
            if (classification.getSubType() != null && classification.getSubType().getName() != null) {
                if (genres.length() > 0) genres.append(" | ");
                genres.append(classification.getSubType().getName());
            }

            genresText.setText(genres.toString());
        }

        // Price Range
        if (event.getPriceRanges() != null && !event.getPriceRanges().isEmpty()) {
            PriceRange priceRange = event.getPriceRanges().get(0);
            String price = String.format(Locale.US, "$%.2f - $%.2f", priceRange.getMin(), priceRange.getMax());
            priceRangeText.setText(price);
        }

        // Ticket Status
        if (event.getDates() != null && event.getDates().getStatus() != null) {
            String status = event.getDates().getStatus().getCode();
            ticketStatusText.setText(status);

            // Set color based on status
            int color;
            if ("onsale".equalsIgnoreCase(status)) {
                color = ContextCompat.getColor(requireContext(), R.color.purple_500);
            } else if ("offsale".equalsIgnoreCase(status)) {
                color = ContextCompat.getColor(requireContext(), R.color.teal_200);
            } else {
                color = ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark);
            }
            ticketStatusText.setTextColor(color);
        }

        // Seatmap
        if (event.getSeatmap() != null && event.getSeatmap().getStaticUrl() != null) {
            Picasso.get().load(event.getSeatmap().getStaticUrl()).into(seatmapImage);
        }

        // Buy Tickets
        buyTicketsIcon.setOnClickListener(v -> {
            if (event.getUrl() != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(event.getUrl()));
                startActivity(intent);
            }
        });
    }

    private String formatDate(String date, String time) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date parsedDate = inputFormat.parse(date);

            SimpleDateFormat outputFormat;
            SimpleDateFormat currentYear = new SimpleDateFormat("yyyy", Locale.US);
            String eventYear = date.substring(0, 4);
            String currentYearStr = currentYear.format(new Date());

            if (eventYear.equals(currentYearStr)) {
                outputFormat = new SimpleDateFormat("MMM d", Locale.US);
            } else {
                outputFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
            }

            String formattedDate = outputFormat.format(parsedDate);

            if (time != null && !time.isEmpty()) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
                Date parsedTime = timeFormat.parse(time);
                SimpleDateFormat timeOutputFormat = new SimpleDateFormat("h:mm a", Locale.US);
                formattedDate += ", " + timeOutputFormat.format(parsedTime);
            }

            return formattedDate;
        } catch (Exception e) {
            return date + (time != null ? " " + time : "");
        }
    }
}