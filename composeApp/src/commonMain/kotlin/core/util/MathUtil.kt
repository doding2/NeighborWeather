package core.util

import kotlin.math.round


inline fun roundToSecond(value: Double) = round(value * 100) / 100

inline fun roundToSecond(value: Float) = round(value * 100) / 100