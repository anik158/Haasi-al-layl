# Getting a signed `.aab` for Google Play — step by step

Google Play does not accept the APK you install for testing. It wants an **Android App Bundle
(`.aab`)** signed with **your upload key**. GitHub builds the `.aab` for you on every push to
`build-apk-release` — but only once it has your key. You set that up **once**.

## What the pieces are

| Thing | What it is | Where it lives |
| --- | --- | --- |
| `my-upload-key.jks` | Your upload key: a file that proves an upload comes from you | On your computer, **plus two backups** (e.g. cloud drive + USB) |
| Keystore password | The password you choose while creating the key | In your head / password manager |
| Three GitHub secrets | The key and its password, stored encrypted so GitHub can sign | Repository → Settings → Secrets and variables → Actions |

Never put the key or the password in the code, in a chat, or in a commit.

## Step 1 — create the key (once, on your computer)

In a terminal, in any folder you will remember:

```bash
keytool -genkeypair -v -keystore my-upload-key.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000
```

It asks:
1. **"Enter keystore password"** — choose a strong password, type it twice. Write it down safely.
2. Name, organisation, city, country — answer anything sensible (e.g. your name, BD).
3. It may ask for a separate key password — if so, press **Enter** to use the same one.

You now have `my-upload-key.jks`. **Back it up now, in two places.** If it is lost you must ask
Google support to reset it, which takes days.

## Step 2 — turn the key into text

```bash
base64 -w0 my-upload-key.jks
```

It prints one very long line of letters. That line is the key, as text. Copy all of it.

## Step 3 — give it to GitHub (once)

1. Open **github.com/anik158/Haasi-al-layl** (the repository, not your profile).
2. Click the **Settings** tab at the top of the repository.
3. Left menu: **Secrets and variables → Actions**.
4. Click **New repository secret** three times, once for each:

| Name (type exactly) | Secret (paste) |
| --- | --- |
| `UPLOAD_KEYSTORE_BASE64` | the long line from step 2 |
| `UPLOAD_STORE_PASSWORD` | your keystore password from step 1 |
| `UPLOAD_KEY_PASSWORD` | the **same** password again (unless you chose a separate key password) |

## Step 4 — get the `.aab`

Push anything to `build-apk-release` (or run the workflow from the **Actions** tab → *Build
Release APK* → *Run workflow*). When the run is green:

- Open the repository's **Releases** (right side of the repository page) → the newest release →
  download the file ending in **`.aab`**. The APK is next to it, as before.
- Or: **Actions** → the run → **Artifacts** → `release-aab`.

If the run log says *"No upload key in the repository secrets"*, step 3 is incomplete — check the
three names are spelled exactly as above.

## Step 5 — upload to Play Console

1. Play Console → your app → **Test and release → Testing → Closed testing** (or Internal testing
   first) → **Create new release**.
2. Accept **Play App Signing** when asked (Google keeps the real signing key; yours is only the
   upload key — this is what makes a lost upload key recoverable).
3. Upload the `.aab`, add release notes, save, review, roll out to testers.

Each new upload needs a higher version number. This is automatic: CI sets `versionCode` to the run
number, so every build is higher than the last.

## One rule: one way of uploading

Upload either the `.aab` from GitHub **or** through AI Studio's Publish — not both. Whichever reaches
Play first fixes the upload key, and Play rejects bundles signed with the other one until the key
is reset through Google support.
