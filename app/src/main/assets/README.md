# `assets/`

## `hisn_al_muslim.json` — not in this repository

`HaasiAlLaylViewModel.loadHisnAlMuslimData()` reads this file at startup. It is **absent**, so a
clean checkout ships only the 14 built-in supplications from `DuaData.kt` rather than the full
Hisn al-Muslim collection. On a device that shows up as "not all duas are available", which is
accurate.

Until it was made to log, the failure was swallowed by a bare `printStackTrace()` on a background
thread — so the app quietly shipped a fraction of its advertised content with nothing to say about
it. That is the part worth not repeating; the missing file itself is just a missing file.

### Why it is not generated

Hisn al-Muslim is a published collection of supplications with a fixed text and fixed references.
Its contents cannot be written from memory or approximated: a supplication with an invented word,
or a correct supplication attributed to the wrong hadith, is worse than an absent one. Drop in a
real copy of the data, or leave the built-in set.

### Expected shape

The loader reads the `English` array; each entry is a category with a `TEXT` array of
supplications. Only the fields below are read — anything else is ignored.

```json
{
  "English": [
    {
      "TITLE": "When waking up",
      "TEXT": [
        {
          "ID": "1",
          "ARABIC_TEXT": "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
          "LANGUAGE_ARABIC_TRANSLATED_TEXT": "Alhamdu lillahil-lathee ahyana ba'da ma amatana wa ilayhin-nushoor",
          "TRANSLATED_TEXT": "Praise is to Allah who gives us life after He has caused us to die and to Him is the return.",
          "REPEAT": 1
        }
      ]
    }
  ]
}
```

| Field | Used for |
| --- | --- |
| `TITLE` | the category heading, and the title shown on each supplication in it |
| `ID` | uniqueness only — ids become `hisn_<categoryIndex>_<ID>` |
| `ARABIC_TEXT` | the Arabic |
| `LANGUAGE_ARABIC_TRANSLATED_TEXT` | the transliteration |
| `TRANSLATED_TEXT` | the English translation |
| `REPEAT` | rendered as `"3x"` |

Entries loaded this way land in `DuaCategory.ALL` and are **English-only** — the `DuaItem`
translation fields for Bengali, Spanish and Portuguese stay blank and fall back to English. The 14
built-in supplications in `DuaData.kt` are translated into all five languages; that asymmetry is
worth knowing before assuming the loader is broken.
