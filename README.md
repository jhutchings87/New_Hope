# Jame360

A personal Android client for a self-hosted download/media-automation stack,
inspired by nzb360. Built for one user, one device, no accounts, no cloud
backend — the app talks directly to servers you already run on your own
network.

## What it does today

Four tabs along the bottom:

- **Downloads**: the live NZBGet queue with per-item progress, pause/resume/
  delete, global pause/resume, speed, remaining size, and free disk space.
  Refreshes every 5 seconds. A chip row appears to switch servers if more
  than one NZBGet server is configured.
- **Search**: type a show or movie title, and every configured Sonarr and
  Radarr server is searched at once. Results show posters and mark what's
  already in the library. Tapping **Add** lets you pick a quality profile and
  root folder, then adds it and kicks off a search for it on the server.
- **Upcoming**: merged, read-only calendar of episodes and movies airing or
  releasing over the next couple of weeks, with a check mark for what's
  already downloaded.
- **Servers**: add/edit/delete NZBGet, Sonarr, and Radarr connections, each
  with a "Test connection" button. Credentials live in an AES-256-GCM
  `EncryptedSharedPreferences` file backed by the Android Keystore, never in
  plaintext.

Not implemented yet: Sonarr/Radarr activity queues, manual release search
and grabbing (picking a specific NZB), RSS monitoring, push notifications,
torrent clients (qBittorrent/Transmission), SABnzbd. The architecture
(`ServerProfile` + per-service client) extends to those without a rewrite.

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
    arr/         ArrApiFactory + ArrRepository (search/add across *arr servers)
  di/            Hilt modules (OkHttpClient, Json, SecurePrefs)
  ui/
    downloads/   Downloads tab (NZBGet server picker)
    dashboard/   NZBGet queue body
    search/      Sonarr/Radarr search + add-to-library sheet
    calendar/    Upcoming tab
    servers/     Server list + add/edit form
    navigation/  Bottom-nav shell, NavHost + routes
    theme/       Material 3 theme
```

## Building

This was written in a sandbox without the Android SDK and without network
access to Google's Maven repo, so it has not actually been compiled here —
the Gradle wrapper (`./gradlew`) is checked in and correct, but dependency
resolution and a real build were never run against it. To build it:

1. Open the project root in Android Studio (Koala or newer), or run
   `./gradlew assembleDebug` from a machine with the Android SDK installed.
2. Sync Gradle, then run the `app` module on a device or emulator running
   Android 8.0+.

## Shipping a build to a phone

Plain-English version, no Android experience assumed.

**What's actually happening:** an Android app ships as a single `.apk` file.
Google Play is just one way to deliver that file. You can hand someone the
file directly instead (called "sideloading"), which is what this setup does.
GitHub builds the APK for you in the cloud and posts it on a download page.
You send that page's link. The phone downloads and installs it. That's the
whole system.

**A note on "signing":** every APK is stamped with a cryptographic key. The
phone remembers the key an app was installed with. If a future APK carries
the same key, Android treats it as an update and installs it right over the
top, keeping all settings. If the key is different, Android refuses and the
app has to be uninstalled first (losing the configured servers). So: the
keystore file must never be lost or replaced. That's the only truly
irreversible thing here.

### Part 1: one-time setup (do this once, ~5 minutes)

You need to give GitHub the signing key so it can stamp the APKs. GitHub
stores these as "secrets" — encrypted values only the build can read.

1. Open `https://github.com/jhutchings87/New_Hope` in a browser.
2. Click the **Settings** tab (top of the page, on the far right).
3. In the left sidebar, click **Secrets and variables**, then **Actions**.
4. Click the green **New repository secret** button. You'll do this four
   times, once per row in the table below. Each time: type the Name exactly
   as shown, paste the Value, click **Add secret**.

   | Name | Value to paste |
   |---|---|
   | `RELEASE_KEYSTORE_BASE64` | The entire contents of the `jame360-release.jks.b64` file. Open it in a text editor, select all, copy. It's one enormous line of gibberish — that's correct. |
   | `RELEASE_KEYSTORE_PASSWORD` | The `KEYSTORE_PASSWORD=` value from `jame360-signing-secrets.txt` (just the part after the `=`). |
   | `RELEASE_KEY_ALIAS` | `jame360` |
   | `RELEASE_KEY_PASSWORD` | The same password as `RELEASE_KEYSTORE_PASSWORD`. Yes, the same value twice — this keystore format uses one password for both. |

5. Put `jame360-release.jks` and `jame360-signing-secrets.txt` somewhere you
   won't lose them (password manager, or an encrypted note). You do not need
   them again for normal releases, only if you ever rebuild this setup.

That's it. You never touch this part again.

### Part 2: shipping a version (do this every time, ~2 minutes of your time)

1. In a terminal, from the project folder, run these two lines. The only
   thing that changes each release is the number.

   ```
   git tag v0.2.0
   git push origin v0.2.0
   ```

   Version numbers just need to go up: `v0.2.0`, then `v0.3.0`, and so on.
   Reusing a number that already exists will fail, so always pick a new one.

2. Go to `https://github.com/jhutchings87/New_Hope/actions`. You'll see a
   run named "Build signed release APK" with a yellow dot (running). Wait
   for it to turn into a green check. It takes about 5 minutes. You can
   close the tab and come back.

   - **Green check:** it worked, continue to step 3.
   - **Red X:** the build broke. Click the run, click the failed step, and
     read the last ~20 lines of red text. That's the error. Nothing was
     shipped, so nothing is broken on anyone's phone.

3. Go to `https://github.com/jhutchings87/New_Hope/releases`. The newest
   entry is your version. Under **Assets** there's a file named something
   like `Jame360-v0.2.0.apk`.

4. Copy the link to that release page and send it to her (text, email,
   whatever). The page is public-readable if the repo is public; if the repo
   is private, she can't open it — in that case download the `.apk` yourself
   and send her the file directly instead.

5. **On her phone**, the first time only:
   - She taps the link, then taps the `.apk` file to download it.
   - She taps the downloaded file to open it.
   - Android says something like *"For your security, your phone isn't
     allowed to install unknown apps from this source."* Tap **Settings**,
     flip the toggle on, press back. This is a per-app permission — she's
     allowing whichever app she used to open it (Chrome, Files, Messages),
     not the whole phone.
   - Tap **Install**. Done.
   - If Play Protect shows an "unsafe app" style warning, that's the generic
     warning for any app not from the Play Store, not a virus detection.
     Choose the "install anyway" option.

6. **Every release after that:** she just taps the new link and installs. No
   toggles, no uninstall, and her configured servers survive the update.

### Quick reference

| I want to... | Do this |
|---|---|
| Ship a new version | `git tag v0.3.0 && git push origin v0.3.0`, wait for green check, send release link |
| See if the build finished | The Actions tab |
| Find the APK file | The Releases page, under Assets |
| Build one without tagging | Actions tab → "Build signed release APK" → "Run workflow". The APK lands under the run's Artifacts instead of a Release. |

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
