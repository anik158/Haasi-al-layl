# `assets/hisn/` — Hisn al-Muslim data (not in this repository)

`HaasiAlLaylViewModel.loadHisnAlMuslimData()` loads the *Hisn al-Muslim* (Fortress of the Muslim)
collection from this directory, one JSON file per language. **None are checked in**, so a clean
checkout shows the 14 built-in supplications from `DuaData.kt` and the Hisn entry in the Duas filter
menu stays hidden. Dropping files in here is all it takes to switch it on — no code change.

| File | Language | Role |
| --- | --- | --- |
| `ar.json` | Arabic | Defines the chapters and supplications, and the Arabic text |
| `en.json` | English | Chapter titles, transliteration, translation — and the definition if `ar.json` is absent |
| `bn.json` | Bengali | Chapter titles and translation |
| `es.json` | Spanish | Chapter titles and translation |
| `pt.json` | Portuguese | Chapter titles and translation |

Any subset works. A missing language falls back to English field by field.

## Why the text is not generated or scraped

Hisn al-Muslim is a published collection by Saʿīd al-Qaḥṭānī with fixed wording and fixed hadith
references. A supplication with an invented word, or a correct one attributed to the wrong hadith,
is worse than an absent one — so it cannot be written from memory.

It also cannot be copied from a website that has not licensed it. The translations are modern
copyrighted works: sunnah.com's terms prohibit reproducing whole collections, and hisnulmuslim.org
is marked *All Rights Reserved* (and offers no Portuguese). Obtain the files from a source that
permits app use — hisnmuslim.com-style exports, or a publisher's written permission — and keep that
permission on record.

## Shape

The same shape hisnmuslim.com-style exports use. The root key is ignored, so any language's export
works unchanged:

```json
{
  "English": [
    {
      "TITLE": "When waking up",
      "TEXT": [
        {
          "ID": 1,
          "ARABIC_TEXT": "…",
          "LANGUAGE_ARABIC_TRANSLATED_TEXT": "…",
          "TRANSLATED_TEXT": "…",
          "REPEAT": 1,
          "REFERENCE": "Al-Bukhari 6312"
        }
      ]
    }
  ]
}
```

| Field | Used for | Notes |
| --- | --- | --- |
| `TITLE` | the chapter title, per language | chapters are matched across languages **by position** |
| `ID` | the supplication's identity | **required**; entries without one are skipped. Ids become `hisn_<ID>` and are bookmark keys, so they must stay stable between releases |
| `ARABIC_TEXT` | the Arabic | taken from `ar.json`, else `en.json` |
| `LANGUAGE_ARABIC_TRANSLATED_TEXT` | the transliteration | taken from `en.json` |
| `TRANSLATED_TEXT` | the translation for that file's language | matched across languages **by `ID`** |
| `REPEAT` | shown as `3x` | defaults to 1 |
| `REFERENCE` | the hadith source | optional; falls back to "Hisn al-Muslim" |

The loader logs what it found — `Hisn al-Muslim: N chapters, M supplications from ar, en, …` — or
a single warning when the directory holds no data.
