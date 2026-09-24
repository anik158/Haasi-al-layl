# `assets/hisn/` — Hisn al-Muslim data (generated)

`HaasiAlLaylViewModel.loadHisnAlMuslimData()` loads the *Hisn al-Muslim* (Fortress of the Muslim)
collection from this directory, one JSON file per language. **Do not edit these files by hand** —
they are written by `python3 tools/hisn/build.py` from:

| Source | What | Licence |
| --- | --- | --- |
| `tools/hisn/source/hisn.json` | Arabic text, repeat counts, hadith references — 133 chapters, 302 supplications | MIT, © 2021 Abdellah SELLAM ([asellam/HisnElMuslim](https://github.com/asellam/HisnElMuslim)); notice shipped here as `LICENSE-HisnElMuslim.md` |
| `tools/hisn/translations/<lang>.*.json` | Chapter titles and translations (en also transliteration) | The project's own translations of the Arabic above |

| File | Language | Role |
| --- | --- | --- |
| `ar.json` | Arabic | Defines the chapters and supplications, and the Arabic text |
| `en.json` | English | Chapter titles, transliteration, translation |
| `bn.json` / `es.json` / `pt.json` | Bengali / Spanish / Portuguese | Chapter titles and translation |

A missing language or field falls back to English.

## Rules

- **The Arabic and references are never written from memory.** Fix them in `tools/hisn/source/`
  (ideally upstream too) and rebuild.
- **Translations are made from the Arabic, never copied** from a published translation —
  sunnah.com's terms forbid reproducing whole collections and hisnulmuslim.org is all rights
  reserved. The current ones are machine-assisted and **not yet reviewed by a scholar**.
- **IDs are 1..N in the upstream file's order** and become `hisn_<ID>` bookmark keys. Never reorder
  the source or insert into its middle; only append.

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
