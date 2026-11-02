"""Renders the Play Store icon (512x512) and feature graphic (1024x500) from the launcher's layers.

Run from the repository root: python3 docs/play-listing/make_images.py
"""
from PIL import Image, ImageDraw, ImageFont

LOGO = "app/src/main/res/drawable-xxxhdpi/ic_haasi_logo.png"
OUT = "docs/play-listing/"
SERIF_BOLD = "/usr/share/fonts/truetype/noto/NotoSerif-Bold.ttf"
SANS = "/usr/share/fonts/truetype/noto/NotoSans-Regular.ttf"

# The stops of drawable/ic_launcher_background.xml.
STOPS = [(0.0, (0x16, 0x20, 0x4A)), (0.55, (0x09, 0x0F, 0x28)), (1.0, (0x02, 0x04, 0x0E))]
GOLD = (0xE2, 0xC0, 0x7A)
MOON_WHITE = (0xF3, 0xEC, 0xE0)


def night_sky(w, h, cx, cy, radius):
  img = Image.new("RGB", (w, h))
  px = img.load()
  for y in range(h):
    for x in range(w):
      t = min(((x - cx) ** 2 + (y - cy) ** 2) ** 0.5 / radius, 1.0)
      for (o0, c0), (o1, c1) in zip(STOPS, STOPS[1:]):
        if t <= o1:
          f = (t - o0) / (o1 - o0)
          px[x, y] = tuple(round(a + (b - a) * f) for a, b in zip(c0, c1))
          break
  return img


def icon():
  size = 512
  # Same proportions as the adaptive icon: 108dp canvas, gradient centred at (54, 46), r = 74.
  img = night_sky(size, size, size / 2, size * 46 / 108, size * 74 / 108)
  logo_px = round(size * 69 / 108)
  logo = Image.open(LOGO).convert("RGBA").resize((logo_px, logo_px), Image.LANCZOS)
  img.paste(logo, ((size - logo_px) // 2, (size - logo_px) // 2), logo)
  img.save(OUT + "icon-512.png")


def feature_graphic():
  w, h = 1024, 500
  img = night_sky(w, h, 260, 220, 900)
  logo_px = 400
  logo = Image.open(LOGO).convert("RGBA").resize((logo_px, logo_px), Image.LANCZOS)
  img.paste(logo, (60, (h - logo_px) // 2), logo)

  draw = ImageDraw.Draw(img)
  title = ImageFont.truetype(SERIF_BOLD, 68)
  tagline = ImageFont.truetype(SANS, 30)
  x = 500
  draw.text((x, 150), "Ḥaasi al-Layl", font=title, fill=GOLD)
  draw.text((x, 252), "Prayer times · Azan alarm · Qibla", font=tagline, fill=MOON_WHITE)
  draw.text((x, 296), "Tasbih · Hisn al-Muslim · 99 Names", font=tagline, fill=MOON_WHITE)
  img.save(OUT + "feature-graphic-1024x500.png")


# Play rejects a screenshot whose long side is more than twice the short one, and the phone's
# captures are 1084x2412, so each goes into a 1080x2160 frame with a caption.
SCREENSHOTS = [
  ("02-today", "Prayer times for where you are"),
  ("03-alarm", "An alarm that rings on the minute"),
  ("02b-prayers", "Mark each prayer as prayed"),
  ("05-qibla", "Qibla from where you stand"),
  ("08-tasbih", "Tasbih with a vibration per count"),
  ("06-duas", "The complete Hisn al-Muslim"),
  ("07-names", "The 99 Names of Allah"),
  ("04-reliability", "Checks that alarms will ring"),
]


def rounded(img, radius):
  mask = Image.new("L", img.size, 0)
  ImageDraw.Draw(mask).rounded_rectangle((0, 0, *img.size), radius, fill=255)
  img.putalpha(mask)
  return img


def screenshots():
  w, h = 1080, 2160
  background = night_sky(w, h, w / 2, h * 0.3, h * 0.9)
  for index, (name, text) in enumerate(SCREENSHOTS, start=1):
    img = background.copy()
    shot_h = 1780
    shot = Image.open(f"{OUT}screenshots/raw/{name}.png").convert("RGBA")
    shot = rounded(shot.resize((round(shot.width * shot_h / shot.height), shot_h), Image.LANCZOS), 48)
    x, y = (w - shot.width) // 2, h - shot_h - 70
    img.paste(shot, (x, y), shot)
    draw = ImageDraw.Draw(img)
    # The alarm screen is as dark as the background; the outline keeps the phone's edge visible.
    draw.rounded_rectangle((x - 3, y - 3, x + shot.width + 2, y + shot_h + 2), 51, outline=GOLD, width=3)
    size = 64
    while draw.textlength(text, font=ImageFont.truetype(SERIF_BOLD, size)) > w - 100:
      size -= 2
    caption = ImageFont.truetype(SERIF_BOLD, size)
    text_w = draw.textlength(text, font=caption)
    draw.text(((w - text_w) / 2, 150), text, font=caption, fill=GOLD)
    img.save(f"{OUT}screenshots/{index:02d}-{name}.png")


if __name__ == "__main__":
  icon()
  feature_graphic()
  screenshots()
