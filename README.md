# Free Firewall for Android

A no-root, local per-app blocking firewall. The app routes only selected blocked applications into Android VpnService and deliberately discards their IPv4/IPv6 packets. Other applications use the normal network.

## Important behavior
- No remote VPN server, account, analytics, or traffic upload.
- Only launcher-visible apps are listed, avoiding broad package visibility permission.
- Switch firewall OFF and ON after changing rules.
- Android allows only one VPN service at a time.
- This first version blocks complete network access per app. It does not yet provide separate Wi-Fi/mobile rules, traffic forwarding, IP/port rules, or detailed logs.

## Build online with GitHub Actions
1. Create a free GitHub repository.
2. Upload the contents of this folder, preserving `.github/workflows/build-apk.yml`.
3. Open Actions > Build Android APK > Run workflow.
4. Open the completed run and download `FreeFirewall-debug-apk` from Artifacts.
5. Extract the downloaded artifact and install `app-debug.apk` on Android.

## Local build (optional)
Use JDK 17 and Gradle 8.9, then run `gradle assembleDebug`.

## Security note
This implementation is intentionally a deny-only local VPN. It does not inspect, decrypt, store, or forward content. Review the source before installing. Sideloading requires enabling installation from the browser/file manager used to open the APK.
