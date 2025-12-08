package com.example.eventfinder.api;

import com.example.eventfinder.models.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    @GET("api/suggest")
    Call<SuggestResponse> getSuggestions(@Query("keyword") String keyword);

    @GET("api/events/search")
    Call<EventSearchResponse> searchEvents(
            @Query("keyword") String keyword,
            @Query("segmentId") String segmentId,
            @Query("radius") String radius,
            @Query("unit") String unit,
            @Query("geoPoint") String geoPoint
    );

    @GET("api/events/{id}")
    Call<EventDetailsResponse> getEventDetails(@Path("id") String eventId);

    @GET("api/spotify/artist")
    Call<SpotifyArtistResponse> searchArtist(@Query("name") String artistName);

    @GET("api/spotify/artist/{id}/albums")
    Call<SpotifyAlbumsResponse> getArtistAlbums(@Path("id") String artistId);

    @GET("api/favorites")
    Call<List<FavoriteEvent>> getAllFavorites();

    @GET("api/favorites/{eventId}")
    Call<FavoriteResponse> checkFavorite(@Path("eventId") String eventId);

    @POST("api/favorites")
    Call<FavoriteResponse> addFavorite(@Body FavoriteEvent event);

    @DELETE("api/favorites/{eventId}")
    Call<FavoriteResponse> removeFavorite(@Path("eventId") String eventId);
}
