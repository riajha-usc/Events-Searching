package com.example.eventfinder.adapters;

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
import com.example.eventfinder.utils.TimeUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_EVENT = 0;
    private static final int VIEW_TYPE_FAVORITE = 1;

    private Context context;
    private List<Event> events;
    private List<FavoriteEvent> favorites;
    private OnEventClickListener listener;
    private boolean isShowingFavorites;

    public interface OnEventClickListener {
        void onEventClick(Event event);

        void onFavoriteClick(Event event, boolean isFavorite);
    }

    public EventAdapter(Context context, List<Event> events, List<FavoriteEvent> favorites, OnEventClickListener listener) {
        this.context = context;
        this.events = events;
        this.favorites = favorites;
        this.listener = listener;
        this.isShowingFavorites = false;
    }

    public void setShowingFavorites(boolean showingFavorites) {
        this.isShowingFavorites = showingFavorites;
    }

    @Override
    public int getItemViewType(int position) {
        return isShowingFavorites ? VIEW_TYPE_FAVORITE : VIEW_TYPE_EVENT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_FAVORITE) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_favorite, parent, false);
            return new FavoriteViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
            return new EventViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof EventViewHolder) {
            Event event = events.get(position);
            ((EventViewHolder) holder).bind(event);
        } else if (holder instanceof FavoriteViewHolder) {
            Event event = events.get(position);
            FavoriteEvent favorite = findFavorite(event.getId());
            ((FavoriteViewHolder) holder).bind(event, favorite);
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void updateEvents(List<Event> newEvents, List<FavoriteEvent> newFavorites, boolean showingFavorites) {
        this.events = newEvents;
        this.favorites = newFavorites;
        this.isShowingFavorites = showingFavorites;
        notifyDataSetChanged();
    }

    private FavoriteEvent findFavorite(String eventId) {
        for (FavoriteEvent fav : favorites) {
            if (fav.getEventId().equals(eventId)) {
                return fav;
            }
        }
        return null;
    }

    private boolean isEventFavorite(String eventId) {
        return findFavorite(eventId) != null;
    }

    private String getCategoryName(Event event) {
        if (event.getClassifications() != null && !event.getClassifications().isEmpty()) {
            if (event.getClassifications().get(0).getSegment() != null) {
                return event.getClassifications().get(0).getSegment().getName();
            }
        }
        return "Unknown";
    }

    // Event Card ViewHolder (for search results)
    class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView eventImage, favoriteIcon;
        TextView categoryBadge, dateBadge, eventName, venueName;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.eventImage);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
            categoryBadge = itemView.findViewById(R.id.categoryBadge);
            dateBadge = itemView.findViewById(R.id.dateBadge);
            eventName = itemView.findViewById(R.id.eventName);
            venueName = itemView.findViewById(R.id.venueName);
        }

        void bind(Event event) {
            // Event name
            eventName.setText(event.getName());
            eventName.setSelected(true);

            // Venue name
            if (event.getEmbedded() != null && event.getEmbedded().getVenues() != null && !event.getEmbedded().getVenues().isEmpty()) {
                venueName.setText(event.getEmbedded().getVenues().get(0).getName());
            }

            // Category badge
            categoryBadge.setText(getCategoryName(event));

            // Date badge
            if (event.getDates() != null && event.getDates().getStart() != null) {
                String date = event.getDates().getStart().getLocalDate();
                String time = event.getDates().getStart().getLocalTime();
                dateBadge.setText(TimeUtils.formatEventDate(date, time));
            }

            // Event image
            if (event.getImages() != null && !event.getImages().isEmpty()) {
                String imageUrl = event.getImages().get(0).getUrl();
                Picasso.get().load(imageUrl).into(eventImage);
            } else {
                eventImage.setImageResource(android.R.color.darker_gray);
            }

            // Favorite icon
            boolean isFavorite = isEventFavorite(event.getId());
            favoriteIcon.setImageResource(isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);

            // Click listeners
            itemView.setOnClickListener(v -> listener.onEventClick(event));
            favoriteIcon.setOnClickListener(v -> listener.onFavoriteClick(event, isFavorite));
        }
    }

    // Favorite List Item ViewHolder
    class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView eventThumbnail;
        TextView eventName, eventDate, relativeTime;

        FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            eventThumbnail = itemView.findViewById(R.id.eventThumbnail);
            eventName = itemView.findViewById(R.id.eventName);
            eventDate = itemView.findViewById(R.id.eventDate);
            relativeTime = itemView.findViewById(R.id.relativeTime);
        }

        void bind(Event event, FavoriteEvent favorite) {
            // Event name
            eventName.setText(event.getName());
            eventName.setSelected(true);

            // Event date
            if (favorite != null && favorite.getDate() != null) {
                eventDate.setText(favorite.getDate());
            }

            // Relative time
            if (favorite != null && favorite.getAddedAt() != null) {
                relativeTime.setText(TimeUtils.getRelativeTime(favorite.getAddedAt()));
            }

            // Thumbnail
            if (favorite != null && favorite.getImageUrl() != null) {
                Picasso.get().load(favorite.getImageUrl()).into(eventThumbnail);
            } else if (event.getImages() != null && !event.getImages().isEmpty()) {
                Picasso.get().load(event.getImages().get(0).getUrl()).into(eventThumbnail);
            }

            // Click listener
            itemView.setOnClickListener(v -> listener.onEventClick(event));
        }
    }
}