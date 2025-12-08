package com.example.eventfinder.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventfinder.R;
import com.example.eventfinder.models.Event;
import com.example.eventfinder.models.FavoriteEvent;
import com.squareup.picasso.Picasso;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private Context context;
    private List<Event> events;
    private List<FavoriteEvent> favorites;
    private OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
        void onFavoriteClick(Event event, boolean isFavorite);
    }

    public EventAdapter(Context context, List<Event> events,
                        List<FavoriteEvent> favorites, OnEventClickListener listener) {
        this.context = context;
        this.events = events;
        this.favorites = favorites;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void updateEvents(List<Event> newEvents, List<FavoriteEvent> newFavorites) {
        this.events = newEvents;
        this.favorites = newFavorites;
        notifyDataSetChanged();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryIcon, favoriteIcon;
        TextView eventName, venueName, eventDate;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryIcon = itemView.findViewById(R.id.categoryIcon);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
            eventName = itemView.findViewById(R.id.eventName);
            venueName = itemView.findViewById(R.id.venueName);
            eventDate = itemView.findViewById(R.id.eventDate);
        }

        @SuppressLint("SetTextI18n")
        void bind(Event event) {
            eventName.setText(event.getName());

            // Set venue name
            if (event.getEmbedded() != null &&
                    event.getEmbedded().getVenues() != null &&
                    !event.getEmbedded().getVenues().isEmpty()) {
                venueName.setText(event.getEmbedded().getVenues().get(0).getName());
            }

            // Set date
            if (event.getDates() != null && event.getDates().getStart() != null) {
                String date = event.getDates().getStart().getLocalDate();
                String time = event.getDates().getStart().getLocalTime();
                eventDate.setText(date + (time != null ? " " + time : ""));
            }

            // Check if favorite
            boolean isFavorite = isEventFavorite(event.getId());
            favoriteIcon.setImageResource(isFavorite ?
                    R.drawable.ic_favorite : R.drawable.ic_favorite_border);

            // Click listeners
            itemView.setOnClickListener(v -> listener.onEventClick(event));
            favoriteIcon.setOnClickListener(v -> listener.onFavoriteClick(event, isFavorite));

            // Enable marquee
            eventName.setSelected(true);
        }

        private boolean isEventFavorite(String eventId) {
            for (FavoriteEvent fav : favorites) {
                if (fav.getEventId().equals(eventId)) {
                    return true;
                }
            }
            return false;
        }
    }
}