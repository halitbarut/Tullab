# Branding & Resource Contract: Application Rebranding to "Tullab"

**Feature**: `006-rename-app-tullab` | **Date**: 2026-09-05

## 1. Localized String Resource Contract

The following string keys MUST maintain linguistic and semantic parity across all three supported locales:

| String Resource Key | English (`values/strings.xml`) | Turkish (`values-tr/strings.xml`) | German (`values-de/strings.xml`) |
|---|---|---|---|
| `app_name` | `Tullab` | `Tullab` | `Tullab` |
| `onboarding_welcome_title` | `Welcome to Tullab` | `Tullab’a Hoş Geldiniz` | `Willkommen bei Tullab` |
| `notification_default_message` | `You have a new reminder from Tullab.` | `Tullab’dan yeni bir hatırlatmanız var.` | `Du hast eine neue Erinnerung von Tullab.` |

---

## 2. Android Manifest Contract

The root application manifest (`app/src/main/AndroidManifest.xml`) must fulfill the following contract:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:name=".TullabApp"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Tullab">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.Tullab">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

---

## 3. Adaptive Launcher Icon Contract

The launcher icon mipmap definitions must bind the white background with the inset Tullab vector emblem:

### `res/mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_tullab_foreground" />
    <monochrome android:drawable="@drawable/ic_launcher_tullab_foreground" />
</adaptive-icon>
```

### `res/drawable/ic_launcher_background.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M0,0h108v108h-108z" />
</vector>
```

### `res/drawable/ic_launcher_tullab_foreground.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<inset xmlns:android="http://schemas.android.com/apk/res/android"
    android:drawable="@drawable/tullab"
    android:inset="18%" />
```
