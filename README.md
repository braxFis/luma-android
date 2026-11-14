# Luma

A calm, Swedish-language Android app with original landscape illustrations and offline synthesized soundscapes.

## Features
- Four soundscapes: ocean, rain, forest and evening calm.
- Play/pause, volume, saved favorites and a 15/30/60 minute sleep timer.
- All artwork is drawn locally; audio is synthesized on the device. No accounts, tracking, network permissions or external media licenses.
- Playback pauses when the app leaves the foreground and on audio-focus loss.

## Build
Open in Android Studio with JDK 17 and Android SDK 35, or install Gradle 8.9 and run `gradle assembleDebug lintDebug`.
GitHub Actions builds an installable debug APK under Actions → Android APK → Luma-Android-APK.

## Scope
First native Android release, implemented with Java and Android SDK widgets. The soundscapes are synthetic ambient textures, not field recordings. No background playback. Device and emulator verification should be completed before production release.
