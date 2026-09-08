# Jame360

A personal Android client for a self-hosted download/media-automation stack,
inspired by nzb360. Built for one user, one device, no accounts, no cloud
backend — the app talks directly to servers you already run on your own
network.

## MVP scope (this build)

- **Server profiles**: add/edit/delete NZBGet, Sonarr, and Radarr connections.
  Credentials are stored in an AES-256-GCM `EncryptedSharedPreferences` file
  backed by the Android Keystore, not plaintext.
- **Dashboard (NZBGet)**: live queue with per-item progress, pause/resume/
  delete, global pause/resume, download speed, remaining size, free disk
  space. Polls every 5 seconds while the screen is open.
- **Upcoming (Sonarr/Radarr)**: merged, read-only calendar of episodes/movies
  airing or releasing in the next couple of weeks.

Not implemented yet: NZB/torrent search & add-to-queue, RSS monitoring, push
notifications, torrent clients (qBittorrent/Transmission), SABnzbd. The
architecture (`ServerProfile` + per-service client) is set up to extend to
those later without a rewrite.

## Tech stack

- Kotlin + Jetpack Compose (Material 3), single-activity + Navigation Compose
- Hilt for DI
- Retrofit + kotlinx.serialization for Sonarr/Radarr's REST APIs
- Hand-rolled OkHttp + kotlinx.serialization client for NZBGet's JSON-RPC API
  (a single `/jsonrpc` endpoint doesn't map cleanly to Retrofit's per-method
  interfaces)
- `androidx.security:security-crypto` for encrypted local storage

minSdk 26 (Android 8.0), targetSdk/compileSdk 34.

## Project layout

```
app/src/main/java/com/jhutchings87/jame360/
  data/
    model/       ServerProfile, ServiceType
    security/    SecurePrefs (EncryptedSharedPreferences wrapper)
    repository/  ServerRepository (stores server profiles)
    nzbget/      NzbGetClient (JSON-RPC) + models
    sonarr/      SonarrApi (Retrofit) + models
    radarr/      RadarrApi (Retrofit) + models
    arr/         ArrApiFactory (shared Retrofit builder for Sonarr/Radarr)
  di/            Hilt modules (OkHttpClient, Json, SecurePrefs)
  ui/
    servers/     Server list + add/edit form
    dashboard/   NZBGet queue screen
    calendar/    Sonarr/Radarr upcoming screen
    navigation/  NavHost + routes
    theme/       Material 3 theme
```

## Building

This was written in a sandbox without the Android SDK, so it has not been
compiled here — the Gradle wrapper jar isn't checked in for that reason.
To build it:

1. Open the project root in Android Studio (Koala or newer). It will offer
   to regenerate the Gradle wrapper automatically, or run:
   ```
   gradle wrapper --gradle-version 8.7
   ```
   once if you have a local Gradle install, then use `./gradlew` from then on.
2. Sync Gradle, then run the `app` module on a device or emulator running
   Android 8.0+.

## Connecting your servers

- **NZBGet**: Settings → Security in the NZBGet web UI for the
  ControlUsername/ControlPassword to use here. Default port is `6789`.
- **Sonarr**: Settings → General → Security for the API key. Default port
  `8989`.
- **Radarr**: Settings → General → Security for the API key. Default port
  `7878`.

Cleartext HTTP is allowed for all hosts (see
`app/src/main/res/xml/network_security_config.xml`) since these are almost
always LAN or VPN-only servers without a TLS certificate in front of them.
If yours sits behind a reverse proxy with HTTPS, turn on "Use HTTPS" in the
add-server form instead.

## Personal use

This is a single-user app with no license/analytics/telemetry and no plans
to be published — it's scoped and named for one person's home server setup.
