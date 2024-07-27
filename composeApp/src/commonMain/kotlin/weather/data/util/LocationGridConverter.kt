package weather.data.util

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

// https://gist.github.com/fronteer-kr/14d7f779d52a21ac2f16
class LocationGridConverter {
    companion object {
        fun convertToGrid(latitude: Double, longitude: Double): Pair<Double, Double> {
            val RE = 6371.00877 // 지구 반경(km)
            val GRID = 5.0 // 격자 간격(km)
            val SLAT1 = 30.0 // 투영 위도1(degree)
            val SLAT2 = 60.0 // 투영 위도2(degree)
            val OLON = 126.0 // 기준점 경도(degree)
            val OLAT = 38.0 // 기준점 위도(degree)
            val XO = 43.0 // 기준점 X좌표(GRID)
            val YO = 136.0 // 기1준점 Y좌표(GRID)

            //
            // LCC DFS 좌표변환 ( code : "TO_GRID"(위경도->좌표, lat_X:위도,  lng_Y:경도), "TO_GPS"(좌표->위경도,  lat_X:x, lng_Y:y) )
            //
            val DEGRAD: Double = PI / 180.0
            val RADDEG: Double = 180.0 / PI

            val re = RE / GRID
            val slat1 = SLAT1 * DEGRAD
            val slat2 = SLAT2 * DEGRAD
            val olon = OLON * DEGRAD
            val olat = OLAT * DEGRAD

            var sn =
                tan(PI * 0.25 + slat2 * 0.5) / tan(PI * 0.25 + slat1 * 0.5)
            sn = ln(cos(slat1) / cos(slat2)) / ln(sn)
            var sf = tan(PI * 0.25 + slat1 * 0.5)
            sf = sf.pow(sn) * cos(slat1) / sn
            var ro = tan(PI * 0.25 + olat * 0.5)
            ro = re * sf / ro.pow(sn)

            var ra = tan(PI * 0.25 + (latitude) * DEGRAD * 0.5)
            ra = re * sf / ra.pow(sn)
            var theta = longitude * DEGRAD - olon
            if (theta > PI) theta -= 2.0 * PI
            if (theta < -PI) theta += 2.0 * PI
            theta *= sn
            val x = floor(ra * sin(theta) + XO + 0.5)
            val y = floor(ro - ra * cos(theta) + YO + 0.5)

            return x to y
        }

        fun convertToLocation(x: Double, y: Double): Pair<Double, Double> {
            val RE = 6371.00877 // 지구 반경(km)
            val GRID = 5.0 // 격자 간격(km)
            val SLAT1 = 30.0 // 투영 위도1(degree)
            val SLAT2 = 60.0 // 투영 위도2(degree)
            val OLON = 126.0 // 기준점 경도(degree)
            val OLAT = 38.0 // 기준점 위도(degree)
            val XO = 43.0 // 기준점 X좌표(GRID)
            val YO = 136.0 // 기1준점 Y좌표(GRID)

            //
            // LCC DFS 좌표변환 ( code : "TO_GRID"(위경도->좌표, lat_X:위도,  lng_Y:경도), "TO_GPS"(좌표->위경도,  lat_X:x, lng_Y:y) )
            //
            val DEGRAD: Double = PI / 180.0
            val RADDEG: Double = 180.0 / PI

            val re = RE / GRID
            val slat1 = SLAT1 * DEGRAD
            val slat2 = SLAT2 * DEGRAD
            val olon = OLON * DEGRAD
            val olat = OLAT * DEGRAD

            var sn =
                tan(PI * 0.25 + slat2 * 0.5) / tan(PI * 0.25 + slat1 * 0.5)
            sn = ln(cos(slat1) / cos(slat2)) / ln(sn)
            var sf = tan(PI * 0.25 + slat1 * 0.5)
            sf = sf.pow(sn) * cos(slat1) / sn
            var ro = tan(PI * 0.25 + olat * 0.5)
            ro = re * sf / ro.pow(sn)

            val xn = x - XO
            val yn = ro - y + YO
            var ra = sqrt(xn * xn + yn * yn)
            if (sn < 0.0) {
                ra = -ra
            }
            var alat: Double = (re * sf / ra).pow((1.0 / sn))
            alat = 2.0 * atan(alat) - PI * 0.5

            var theta: Double
            if (abs(xn) <= 0.0) {
                theta = 0.0
            } else {
                if (abs(yn) <= 0.0) {
                    theta = PI * 0.5
                    if (xn < 0.0) {
                        theta = -theta
                    }
                } else theta = atan2(xn, yn)
            }
            val alon = theta / sn + olon

            val latitude = alat * RADDEG
            val longitude = alon * RADDEG

            return latitude to longitude
        }
    }
}