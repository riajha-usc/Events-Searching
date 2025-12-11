package com.example.eventfinder.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventfinder.R;
import com.example.eventfinder.models.SpotifyAlbumsResponse;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.AlbumViewHolder> {

    private List<SpotifyAlbumsResponse.Album> albums;

    public AlbumsAdapter(List<SpotifyAlbumsResponse.Album> albums) {
        this.albums = albums;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        SpotifyAlbumsResponse.Album album = albums.get(position);
        holder.bind(album);
    }

    @Override
    public int getItemCount() {
        return albums != null ? albums.size() : 0;
    }

    static class AlbumViewHolder extends RecyclerView.ViewHolder {
        ImageView albumImage;
        TextView albumName;
        TextView albumDate;

        AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            albumImage = itemView.findViewById(R.id.albumImage);
            albumName = itemView.findViewById(R.id.albumName);
            albumDate = itemView.findViewById(R.id.albumDate);
        }

        void bind(SpotifyAlbumsResponse.Album album) {
            albumName.setText(album.getName());
            albumDate.setText(album.getReleaseDate());

            if (album.getImages() != null && !album.getImages().isEmpty()) {
                Picasso.get().load(album.getImages().get(0).getUrl()).into(albumImage);
            }

            itemView.setOnClickListener(v -> {
                if (album.getExternalUrls() != null && album.getExternalUrls().getSpotify() != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(album.getExternalUrls().getSpotify()));
                    v.getContext().startActivity(intent);
                }
            });
        }
    }
}