package core.util

fun <T, R, V> Collection<Triple<T, R, V>>.unzip(): Triple<List<T>, List<R>, List<V>> {
    val expectedSize = size
    val listT = ArrayList<T>(expectedSize)
    val listR = ArrayList<R>(expectedSize)
    val listV = ArrayList<V>(expectedSize)
    for (triple in this) {
        listT.add(triple.first)
        listR.add(triple.second)
        listV.add(triple.third)
    }
    return Triple(listT, listR, listV)
}