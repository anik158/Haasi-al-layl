import math
import datetime

def toJulianDay(year, month, day):
    y = year
    m = month
    if m <= 2:
        y -= 1
        m += 12
    a = math.floor(y / 100.0)
    b = 2.0 - a + math.floor(a / 4.0)
    jd = math.floor(365.25 * (y + 4716.0)) + math.floor(30.6001 * (m + 1.0)) + day + b - 1524.5
    return jd

def fixAngle(angle):
    a = angle - 360.0 * math.floor(angle / 360.0)
    if a < 0.0:
        a += 360.0
    return a

def sunAngleTime(angle, lat, declination):
    cosHourAngle = (math.cos(math.radians(angle)) -
      math.sin(math.radians(lat)) * math.sin(math.radians(declination))) / \
      (math.cos(math.radians(lat)) * math.cos(math.radians(declination)))
    clamped = max(-1.0, min(1.0, cosHourAngle))
    return math.degrees(math.acos(clamped)) / 15.0

lat = 23.8
lng = 90.4
offsetHours = 6.0 # Asia/Dhaka

# Let's say date is Sept 11, 2026
jd = toJulianDay(2026, 9, 11)
d = jd - 2451545.0
g = fixAngle(357.529 + 0.98560028 * d)
q = fixAngle(280.459 + 0.98564736 * d)
l = fixAngle(q + 1.915 * math.sin(math.radians(g)) + 0.020 * math.sin(math.radians(2 * g)))
e = 23.439 - 0.00000036 * d
declination = math.degrees(math.asin(math.sin(math.radians(e)) * math.sin(math.radians(l))))

eqTime = q / 15.0 - (math.degrees(math.atan2(math.cos(math.radians(e)) * math.sin(math.radians(l)), math.cos(math.radians(l)))) / 15.0)
eqTimeHours = eqTime
if eqTimeHours > 12.0: eqTimeHours -= 24.0
if eqTimeHours < -12.0: eqTimeHours += 24.0

noon = 12.0 + offsetHours - (lng / 15.0) - eqTimeHours
print(f"Noon: {noon}")

# Sunrise
sunriseAngle = 90.8333
sunriseDiff = sunAngleTime(sunriseAngle, lat, declination)
sunrise = noon - sunriseDiff
sunset = noon + sunriseDiff
print(f"Sunrise: {sunrise}, Sunset: {sunset}")

# Asr
shadowFactor = 1.0
asrAngle = math.degrees(math.atan(1.0 / (shadowFactor + math.tan(math.radians(abs(lat - declination))))))
asrDiff = sunAngleTime(90.0 + asrAngle, lat, declination)
asr = noon + asrDiff
print(f"AsrAngle (above horizon): {asrAngle}")
print(f"AsrDiff (using 90+asrAngle): {asrDiff}")
print(f"Asr Time: {asr}")

# Wait, previously I had `90.0 - asrAngle`.
asrDiff_old = sunAngleTime(90.0 - asrAngle, lat, declination)
asr_old = noon + asrDiff_old
print(f"AsrDiff (using 90-asrAngle): {asrDiff_old}")
print(f"Asr Time (using 90-asrAngle): {asr_old}")

