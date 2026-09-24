#!/usr/bin/env python3
"""Builds app/src/main/assets/hisn/<lang>.json from the vendored upstream collection.

  source/hisn.json         Arabic text, counts and references, from github.com/asellam/HisnElMuslim
                           (MIT, see source/LICENSE.md). Keyed by chapter title, no IDs.
  translations/<lang>.json Our own translations of that Arabic:
                           {"chapters": ["title", ...], "entries": {"<ID>": {"text": "...", "translit": "..."}}}
                           "chapters" is by position; "translit" is only read from en.json.

IDs are assigned 1..N in the upstream file's order. They are bookmark keys, so never reorder the
source or insert into the middle of it: append new entries only, and bump nothing that exists.

  python3 tools/hisn/build.py           write the asset files
  python3 tools/hisn/build.py --chunks N DIR   also split the Arabic into N work files for translating
"""
import json
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
ASSETS = HERE.parents[1] / "app/src/main/assets/hisn"
LANGS = {"en": "English", "bn": "Bengali", "es": "Spanish", "pt": "Portuguese"}


def load_source():
    raw = json.loads((HERE / "source/hisn.json").read_text(encoding="utf-8"))
    chapters, next_id = [], 1
    for title, chapter in raw.items():
        entries = []
        for item in chapter["Adhkar"]:
            entries.append({"id": next_id, "arabic": item["Text"].strip(),
                            "repeat": int(item.get("Count") or 1), "reference": item.get("Reference", "").strip()})
            next_id += 1
        chapters.append({"title": title.strip(), "entries": entries})
    return chapters


def load_translation(code):
    """Merges translations/<code>.json and any <code>.part*.json. Chapter titles are keyed by index."""
    titles, entries = {}, {}
    for path in sorted((HERE / "translations").glob(f"{code}.*json")):
        tr = json.loads(path.read_text(encoding="utf-8"))
        chapters = tr.get("chapters", {})
        titles.update({str(k): v for k, v in (chapters.items() if isinstance(chapters, dict) else enumerate(chapters))})
        entries.update(tr.get("entries", {}))
    return titles, entries


def write(path, root_key, chapters):
    path.write_text(json.dumps({root_key: chapters}, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")


def build(chapters):
    ASSETS.mkdir(parents=True, exist_ok=True)
    write(ASSETS / "ar.json", "Arabic", [
        {"TITLE": c["title"], "TEXT": [
            {"ID": e["id"], "ARABIC_TEXT": e["arabic"], "REPEAT": e["repeat"], "REFERENCE": e["reference"]}
            for e in c["entries"]]}
        for c in chapters])
    total = sum(len(c["entries"]) for c in chapters)
    print(f"ar: {len(chapters)} chapters, {total} supplications")

    for code, name in LANGS.items():
        titles, entries = load_translation(code)
        if not entries:
            print(f"{code}: no translation file, skipped")
            continue
        out, missing = [], []
        for index, c in enumerate(chapters):
            text = []
            for e in c["entries"]:
                t = entries.get(str(e["id"]))
                if not t or not t.get("text"):
                    missing.append(e["id"])
                    continue
                item = {"ID": e["id"], "ARABIC_TEXT": e["arabic"], "TRANSLATED_TEXT": t["text"].strip(),
                        "REPEAT": e["repeat"], "REFERENCE": e["reference"]}
                if code == "en" and t.get("translit"):
                    item["LANGUAGE_ARABIC_TRANSLATED_TEXT"] = t["translit"].strip()
                text.append(item)
            out.append({"TITLE": titles.get(str(index), ""), "TEXT": text})
        write(ASSETS / f"{code}.json", name, out)
        print(f"{code}: {total - len(missing)}/{total} translated, {len(titles)}/{len(chapters)} titles"
              + (f"; missing IDs {missing}" if missing else ""))


def chunks(chapters, n, directory):
    """Splits whole chapters into n files of roughly equal Arabic length, for translating."""
    directory.mkdir(parents=True, exist_ok=True)
    size = sum(len(e["arabic"]) for c in chapters for e in c["entries"]) / n
    parts, current, length = [], [], 0
    for index, c in enumerate(chapters):
        current.append({"chapter": index, "title": c["title"],
                        "entries": [{"id": e["id"], "arabic": e["arabic"]} for e in c["entries"]]})
        length += sum(len(e["arabic"]) for e in c["entries"])
        if length >= size * (len(parts) + 1) and len(parts) < n - 1:
            parts.append(current)
            current = []
    parts.append(current)
    for i, part in enumerate(parts, 1):
        (directory / f"chunk{i}.json").write_text(json.dumps(part, ensure_ascii=False, indent=1), encoding="utf-8")
        print(f"chunk{i}: chapters {part[0]['chapter']}-{part[-1]['chapter']}")


if __name__ == "__main__":
    source = load_source()
    if "--chunks" in sys.argv:
        i = sys.argv.index("--chunks")
        chunks(source, int(sys.argv[i + 1]), Path(sys.argv[i + 2]))
    build(source)
