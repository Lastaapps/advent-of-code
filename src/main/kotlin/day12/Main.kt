package day12

import InputLoader
import io.kotest.matchers.shouldBe

private data class Point(val x: Int, val y: Int)

private fun String.findChar(char: Char): Point {
    lines()
        .forEachIndexed { x, line ->
            line.forEachIndexed { y, c ->
                if (c == char) return Point(x, y)
            }
        }
    error("Char $char not found")
}

private typealias HeatMap = List<String>

@Suppress("NOTHING_TO_INLINE")
private inline operator fun HeatMap.get(point: Point) =
    this[point.x][point.y]

private fun Point.neighborhood(map: HeatMap): List<Point> =
    listOf(
        Point(x - 1, y),
        Point(x + 1, y),
        Point(x, y - 1),
        Point(x, y + 1),
    ).filter {
        it.x in (map.indices) && it.y in map.first().indices
    }

private fun String.part01(): Int {
    val start = findChar('S')
    val end = findChar('E')

    replace('S', 'a')
        .replace('E', 'z')
        .lines()
        .let { map ->

            val queue = mutableListOf(start)
            val visited = mutableSetOf<Point>(start)
            val distances = mutableMapOf<Point, Int>(start to 0)

            while (queue.isNotEmpty()) {
                val item = queue.removeAt(0)
                val char = map[item]
                val dist = distances[item]!!

                if (item == end) {
                    return dist
                }

                item.neighborhood(map).forEach { point ->
                    if (point in visited)
                        return@forEach

                    val desChar = map[point]
                    if (char + 1 < desChar) return@forEach

                    visited += point
                    distances[point] = dist + 1
                    queue.add(point)
                }
            }

            error("Path not found")
        }
}

private fun String.part02(): Int {
    val start = findChar('E')

    replace('S', 'a')
        .replace('E', 'z')
        .lines()
        .let { map ->

            val queue = mutableListOf(start)
            val visited = mutableSetOf<Point>(start)
            val distances = mutableMapOf<Point, Int>(start to 0)

            while (queue.isNotEmpty()) {
                val item = queue.removeAt(0)
                val char = map[item]
                val dist = distances[item]!!

                if (char == 'a') {
                    return dist
                }

                item.neighborhood(map).forEach { point ->
                    if (point in visited)
                        return@forEach

                    val desChar = map[point]
                    if (char - 1 > desChar) return@forEach

                    visited += point
                    distances[point] = dist + 1
                    queue.add(point)
                }
            }

            error("Path not found")
        }
}

fun main() {
    testInput.part01() shouldBe PART_01_RES
    testInput.part02() shouldBe PART_02_RES

    val input = InputLoader.loadInput("day12")
    println(input.part01())
    println(input.part02())
}

private val testInput = """
Sabqponm
abcryxxl
accszExk
acctuvwj
abdefghi
""".trimIndent()

private const val PART_01_RES = 31
private const val PART_02_RES = 29
