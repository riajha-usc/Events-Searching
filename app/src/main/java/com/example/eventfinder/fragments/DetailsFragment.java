package com.example.eventfinder.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

    private Event event;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);

        TextView dateText = view.findViewById(R.id.dateText);
        TextView artistsText = view.findViewById(R.id.artistsText);
        TextView venueText = view.findViewById(R.id.venueText);
        LinearLayout genresContainer = view.findViewById(R.id.genresContainer);
        TextView priceRangeText = view.findViewById(R.id.priceRangeText);
        TextView ticketStatusText = view.findViewById(R.id.ticketStatusText);
        ImageView seatmapImage = view.findViewById(R.id.seatmapImage);
        ImageView buyTicketsIcon = view.findViewById(R.id.buyTicketsIcon);
        ImageView shareIcon = view.findViewById(R.id.shareIcon); // ADD THIS LINE

        if (event != null) {
            populateDetails(dateText, artistsText, venueText, genresContainer, priceRangeText, ticketStatusText, seatmapImage, buyTicketsIcon, shareIcon); // UPDATE THIS LINE
        }

        return view;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    private void populateDetails(TextView dateText, TextView artistsText, TextView venueText, LinearLayout genresContainer, TextView priceRangeText, TextView ticketStatusText, ImageView seatmapImage, ImageView buyTicketsIcon, ImageView shareIcon) { // ADD shareIcon PARAMETER

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

        // Genres - populate the existing genre TextViews
        if (event.getClassifications() != null && !event.getClassifications().isEmpty()) {
            Classification classification = event.getClassifications().get(0);

            TextView genre1 = genresContainer.findViewById(R.id.genre1);
            TextView genre2 = genresContainer.findViewById(R.id.genre2);

            // Clear existing genres
            genre1.setVisibility(View.GONE);
            genre2.setVisibility(View.GONE);

            // Set segment (e.g., "Music")
            if (classification.getSegment() != null && classification.getSegment().getName() != null) {
                genre1.setText(classification.getSegment().getName());
                genre1.setVisibility(View.VISIBLE);
            }

            // Set genre (e.g., "Pop")
            if (classification.getGenre() != null && classification.getGenre().getName() != null) {
                genre2.setText(classification.getGenre().getName());
                genre2.setVisibility(View.VISIBLE);
            }
        }

        // Price Range
        if (event.getPriceRanges() != null && !event.getPriceRanges().isEmpty()) {
            PriceRange priceRange = event.getPriceRanges().get(0);
            String price = String.format(Locale.US, "$%.2f - $%.2f", priceRange.getMin(), priceRange.getMax());
            priceRangeText.setText(price);
            priceRangeText.setVisibility(View.VISIBLE);
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

        // ADD THIS: Share Icon Click Listener
        shareIcon.setOnClickListener(v -> shareEvent());
    }

    // ADD THIS METHOD
    private void shareEvent() {
        if (event == null) {
            Toast.makeText(getContext(), "Event details not available", Toast.LENGTH_SHORT).show();
            return;
        }

        if (event.getUrl() == null) {
            Toast.makeText(getContext(), "Event URL not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create share text
        String shareText = "Check out " + event.getName() + " on Ticketmaster: " + event.getUrl();

        // Create share intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, event.getName());

        // Create chooser to show all sharing options
        Intent chooserIntent = Intent.createChooser(shareIntent, "Share event via");

        // Verify that the intent can be resolved and start it
        if (getActivity() != null && shareIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(chooserIntent);
        } else {
            Toast.makeText(getContext(), "No apps available to share", Toast.LENGTH_SHORT).show();
        }
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