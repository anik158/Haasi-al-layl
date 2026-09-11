import math

def sunAngleTime(angle, lat, declination):
    cosHourAngle = (math.cos(math.radians(angle)) -
      math.sin(math.radians(lat)) * math.sin(math.radians(declination))) / \
      (math.cos(math.radians(lat)) * math.cos(math.radians(declination)))
    clamped = max(-1.0, min(1.0, cosHourAngle))
    return math.degrees(math.acos(clamped)) / 15.0

lat = 23.8
declination = -5.0 # Example
shadowFactor = 1.0

# Old bug
asrAngle_old = -math.degrees(math.atan(1.0 / (shadowFactor + math.tan(math.radians(abs(lat - declination))))))
asrDiff_old = sunAngleTime(90.0 - asrAngle_old, lat, declination)
print(f"Old Diff (hours): {asrDiff_old}")

# New fixed
asrAngle_new = math.degrees(math.atan(1.0 / (shadowFactor + math.tan(math.radians(abs(lat - declination))))))
asrDiff_new = sunAngleTime(90.0 - asrAngle_new, lat, declination)
print(f"New Diff (hours): {asrDiff_new}")
