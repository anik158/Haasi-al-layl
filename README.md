# Ḥaasi al-Layl (حَاسِي اللَّيْل)

> *"The one who sips the night" — A serene, mindful Islamic daily companion featuring astronomical horizon celestial tracking, local multi-madhab prayer schedules, offline prophetic supplications, and tranquil weather guidance.*

---

## 🌙 Name Details & Poetic Origin

### **Ḥaasi al-Layl (حَاسِي اللَّيْل)**
- **Meaning:** "The one who sips the night" or "The night-sipper."
- **Language Breakdown:**
  - **Ḥaasi (حَاسِي)**: Derived from the Arabic root and verb *ḥasā* (حَسَا), which means to sip, sup, or drink a liquid slowly and mindfully.
  - **Al-Layl (اللَّيْل)**: Means "the night."
- **Poetic Context:**
  This is a highly metaphorical and poetic Arabic expression. It traditionally depicts a traveler, a seeker of night prayers (*Qiyam al-Layl* / *Tahajjud*), a sleepless contemplator, or a poet who "absorbs" or slowly endures the long hours of darkness, as if savoring every drop of a soothing beverage under the celestial canopy.

---

## ✨ Key Features & Architecture

### 1. ⚖️ Four Sunni Madhabs Jurisprudence (Default: Imam Shafi'i)
Users can seamlessly select their juristic school (*Madhab*) to calculate the exact Asr prayer time:
- **Imam Shafi'i (الإمام الشافعي) — [Default]**: Asr begins when the shadow of an object equals its length ($1\times$ shadow factor).
- **Imam Abu Hanifa (الإمام أبو حنيفة)**: Asr begins when the shadow of an object equals twice its length ($2\times$ shadow factor).
- **Imam Malik (الإمام مالك)**: Standard Asr calculation ($1\times$ shadow factor).
- **Imam Ahmad ibn Hanbal (الإمام أحمد بن حنبل)**: Standard Asr calculation ($1\times$ shadow factor).

Calculations are computed 100% locally and instantaneously using astronomical solar positioning formulas, adjusting dynamically whenever a location or madhab is updated.

---

### 2. 📖 Comprehensive Hisn al-Muslim (Fortress of the Muslim)
The application's **Duas** tab now features the complete **Hisn al-Muslim** database (132 chapters, 267 supplications), operating completely offline. Every entry includes:
- **Authentic Arabic Text with Full Tashkeel**
- **Phonetic Transliteration**
- **Verified Translations in 5 Languages**:
  - 🇬🇧 English
  - 🇧🇩 Bengali (বাংলা)
  - 🇪🇸 Spanish (Español)
  - 🇵🇹 Portuguese (Português)
  - 🇸🇦 Arabic (عربي)
- **Categorized Sections**: Weather & Nature, Morning Adhkar, Evening Adhkar, Prayer & Mosque, Daily Life.
- **Bookmarks & Instant Search** across titles and translations in all languages.

---

### 3. ⚙️ Centralized Settings & Customization
- **Unified Settings Bottom Sheet**: Quickly configure Prayer Start Alerts, Forbidden Time Warnings, App Language, and Madhab selection from a single intuitive menu in the Top Bar.
- **Offline Weather Graceful Degradation**: Real-time weather and temperature widgets cleanly handle network loss with an offline state banner instead of endless loading loops.
- **Dynamic Top Bar**: Displays a minimal logo, app branding, an actionable City Changer, and the Settings hub to maximize screen real estate.

---

### 3. 🎨 Color Depth & Visual Aesthetic
- **Warm Parchment Canvas (`#F4EFE6`)**: Natural unbleached manuscript backdrop that reduces eye strain and honors Islamic scholarly manuscripts.
- **Deep Forest Emerald (`#1C3D2B`)**: Rich, saturated ink tones for typography, iconography, and active highlights.
- **Pale Water-Wash Celadon (`#D1E2D3`) & Warm Gold (`#D4A373`)**: Soft accents and badges for next prayer status, celestial horizons, and madhab indicators.

---

### 4. 🌅 Celestial Horizon & Real-Time Tracking
- **Proportional Curved Horizon**: Visually tracks the real-time position of the sun and moon across the sky dome based on the user's selected geographic coordinates and local timezone.
- **Animated Moon Phase Indicator**: Displays the lunar phase (New Moon, Waxing Crescent, First Quarter, Waxing Gibbous, Full Moon, Waning Gibbous, Last Quarter, Waning Crescent) with live illumination percentage.
- **Triple Calendar Integration**: Hijri lunar date, Bengali solar calendar (বঙ্গাব্দ), and Gregorian calendar displayed concurrently.

---

### 5. 🧭 Precision Qibla Compass & Digital Tasbih
- **True Direction to the Kaaba**: Real-time sensor-fused compass heading with alignment haptics.
- **Digital Counter (Tasbih)**: Offline Room-persisted counts with haptic feedback at milestone targets (33, 99, 100).

---

## 🛠️ Tech Stack & Engineering
- **Platform:** Android 14+ (Jetpack Compose, Material 3)
- **Language:** Kotlin 2.0+
- **Architecture:** Clean MVVM with `StateFlow`, Coroutines, and unidirectional data flow
- **Local Persistence:** Room Database with KSP (Prayer logs, Tasbih counters, Dua bookmarks)
- **Hardware Integration:** Android `SensorManager` (Magnetometer & Accelerometer)
- **Network Layer:** Standard Ktor / Retrofit client for live weather (Open-Meteo free public API, zero API key required)
