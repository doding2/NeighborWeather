package core.util

import kotlin.math.pow
import kotlin.math.round


inline fun roundByDigit(value: Double, digit: Int): Double = round(value * 10.0.pow(digit)) / 10.0.pow(digit)

inline fun roundToFirst(value: Double) = round(value * 10) / 10

inline fun roundToFirst(value: Float) = round(value * 10) / 10

inline fun roundToSecond(value: Double) = round(value * 100) / 100

inline fun roundToSecond(value: Float) = round(value * 100) / 100