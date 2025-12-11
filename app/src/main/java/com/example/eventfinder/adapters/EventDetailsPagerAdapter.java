package com.example.eventfinder.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.eventfinder.fragments.ArtistsFragment;
import com.example.eventfinder.fragments.DetailsFragment;
import com.example.eventfinder.fragments.VenueFragment;
import com.example.eventfinder.models.Event;

public class EventDetailsPagerAdapter extends FragmentStateAdapter {

    private Event event;

    public EventDetailsPagerAdapter(@NonNull FragmentActivity fragmentActivity, Event event) {
        super(fragmentActivity);
        this.event = event;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                DetailsFragment detailsFragment = new DetailsFragment();
                detailsFragment.setEvent(event);
                return detailsFragment;
            case 1:
                ArtistsFragment artistsFragment = new ArtistsFragment();
                artistsFragment.setEvent(event);
                return artistsFragment;
            case 2:
                VenueFragment venueFragment = new VenueFragment();
                venueFragment.setEvent(event);
                return venueFragment;
            default:
                DetailsFragment defaultFragment = new DetailsFragment();
                defaultFragment.setEvent(event);
                return defaultFragment;
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}