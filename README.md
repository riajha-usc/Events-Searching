# Events-Searching

A full-stack event discovery application that lets users search for live events near them, explore artist and venue details, and save favorites - powered by the Ticketmaster Discovery API, Spotify API, and MongoDB Atlas.

---

## Overview

Events-Searching consists of three layers:

- **Backend** - Node.js/Express REST API that proxies Ticketmaster and Spotify requests (keeping API keys server-side) and persists favorites in MongoDB Atlas.
- **Web Frontend** - Angular + Tailwind CSS single-page app deployed to Google App Engine.
- **Android App** - Native Android application (Java) that consumes the same backend API.

- [Live deployment](https://search-event-svc-98723.uc.r.appspot.com/)
- [Github Repo(Backend server)](https://github.com/riajha-usc/event-search-app)
---

## Features

- **Event Search** - Search by keyword, category, distance (in miles), and location (auto-detected via IP or entered manually).
- **Autocomplete** - Keyword suggestions powered by the Ticketmaster Suggest endpoint.
- **Category Filtering** - Filter results by Music, Sports, Arts & Theatre, Film, or Miscellaneous.
- **Event Details** - Three-tab detail view covering event info, artist/Spotify data, and venue information.
- **Artist Info** - Spotify integration showing artist profile, follower count, popularity, and recent albums.
- **Favorites** - Add or remove favorites with undo support; persisted to MongoDB Atlas.
- **Social Sharing** - Share events via Facebook, X (Twitter), or the native Android share sheet.
- **Dark Mode** - Android app supports system light/dark themes.
- **Seat Maps** - Displays Ticketmaster seat map images on the event detail screen.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Node.js, Express, MongoDB Atlas |
| Web Frontend | Angular 20, Tailwind CSS, TypeScript |
| Android | Java (Android SDK 26+), Retrofit, Picasso |
| APIs | Ticketmaster Discovery API, Spotify Web API, Google Geocoding API, IPInfo |
| Hosting | Google App Engine |

---

## Backend API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/suggest?keyword=` | Autocomplete keyword suggestions |
| `GET` | `/api/events/search` | Search events by keyword, location, category, radius |
| `GET` | `/api/events/:id` | Event details by Ticketmaster event ID |
| `GET` | `/api/spotify/artist?name=` | Search Spotify for an artist |
| `GET` | `/api/spotify/artist/:id/albums` | Get an artist's albums |
| `GET` | `/api/favorites` | List all saved favorites |
| `GET` | `/api/favorites/:eventId` | Check if an event is a favorite |
| `POST` | `/api/favorites` | Add an event to favorites |
| `DELETE` | `/api/favorites/:eventId` | Remove an event from favorites |
| `GET` | `/api/health` | Health check |

---

## Local Development

### Prerequisites

- Node.js 18+
- npm
- Android Studio (for the Android app)
- MongoDB Atlas account (optional for favorites persistence)
---

## Environment Variables

### Backend (`backend/.env`)

```env
TICKETMASTER_API_KEY=your_ticketmaster_api_key
MONGODB_URI=your_mongodb_atlas_connection_string
SPOTIFY_CLIENT_ID=your_spotify_client_id
SPOTIFY_CLIENT_SECRET=your_spotify_client_secret
PORT=8080
```

### Frontend (`frontend/src/environments/environment.ts`)

Copy from the template and fill in your keys:

```bash
cp frontend/src/environments/environment.template.ts frontend/src/environments/environment.ts
```

```typescript
export const environment = {
  production: false,
  ipinfoToken: 'your_ipinfo_token',
  googleMapsApiKey: 'your_google_maps_api_key'
};
```
---

## License

MIT
