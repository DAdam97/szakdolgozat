def percentToPWMVal(percent):
    return int((255 / 100) * percent)

def percentToAngle(percent):
    return int((180 / 100) * percent)

def distanceToVolume(val, inMin, inMax, outMin, outMax):
    return 1 - ((val - inMin) * (outMax - outMin) / (inMax - inMin) + outMin);
