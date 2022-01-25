package divine.brothers

import java.io.BufferedReader
import java.math.BigInteger
import java.security.MessageDigest

/**
 * Opens a BufferedReader for resource on classpath at the given path.
 * The caller is responsible for closing the reader.
 */
fun readFromClasspath(path: String): BufferedReader {
    val stream = Thread.currentThread().contextClassLoader.getResourceAsStream(path)
    if (stream == null) error("Requested resource not found on classpath: $path")
    return stream.bufferedReader()
}

/**
 * Converts string to md5 hash.
 */
fun String.md5(): String = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray())).toString(16)

/**
 * Split an iterable into sublists kind of like the split method on String.
 */
fun <T> Sequence<T>.split(omitEmpty: Boolean = true, delimit: (T) -> Boolean): Sequence<List<T>> {
    return sequence {
        val iterator = iterator()
        var currentRun = mutableListOf<T>()
        while (iterator.hasNext()) {
            val next = iterator.next()
            val isDelimiter = delimit(next)
            if (isDelimiter) {
                if (omitEmpty && currentRun.isEmpty()) continue
                val saved = currentRun
                currentRun = mutableListOf()
                yield(saved)
            } else {
                currentRun.add(next)
            }
        }
        if (currentRun.isNotEmpty()) yield(currentRun)
    }
}

fun <T> MutableMap<T, Int>.increment(key: T) {
    merge(key, 1, Int::plus)
}

fun <T> Set<T>.containsAny(iterable: Iterable<T>): Boolean {
    return iterable.any { it in this }
}

fun <T, E> Sequence<T>.takeUntilChanged(selector: T.() -> E): Sequence<T> = sequence {
    val iterator = iterator()
    if (!iterator.hasNext()) {
        return@sequence
    }
    val first = iterator.next()
    val firstValue = first.selector()
    yield(first)
    while (iterator().hasNext()) {
        val next = iterator.next()
        if (firstValue == next) {
            yield(next)
        } else break
    }
}

