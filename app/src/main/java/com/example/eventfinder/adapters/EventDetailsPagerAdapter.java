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
                return DetailsFragment.newInstance(event);
            case 1:
                return ArtistsFragment.newInstance(event);
            case 2:
                return VenueFragment.newInstance(event);
            default:
                return DetailsFragment.newInstance(event);
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}