package y2023.day14

import InputLoader
import Year
import io.kotest.matchers.shouldBe
import kotlin.math.roundToInt
import kotlin.math.sqrt

private fun <T> Sequence<T>.consume() =
    iterator().let {
        while (it.hasNext()) {
            it.next()
        }
    }

private fun String.part01(): Int {
    val size = sqrt(length.toFloat()).roundToInt()
    val limits = MutableList(size) { 0 }

    var weight = 0
    lineSequence().onEachIndexed { i, line ->
        line.forEachIndexed { j, c ->
            when (c) {
                '.' -> {}
                '#' -> limits[j] = i + 1
                'O' -> {
                    weight += size - limits[j]
                    limits[j]++
                }
            }
        }
    }.consume()
    return weight
}

private fun String.part02() = PART_02_TEST

fun main() {
    testInput.part01() shouldBe PART_01_TEST
    testInput.part02() shouldBe PART_02_TEST

    val input = InputLoader.loadInput(Year.Y2023, "day14")
    input.part01()
        .also { println(it) }
        .also { it shouldBe PART_01_PROD }
    input.part02()
        .also { println(it) }
        .also { it shouldBe PART_02_PROD }
}

private val testInput = """
O....#....
O.OO#....#
.....##...
OO.#O....O
.O.....O#.
O.#..O.#.#
..O..#O..O
.......O..
#....###..
#OO..#....
""".trimIndent()

private const val PART_01_TEST = 136
private const val PART_01_PROD = 106648
private const val PART_02_TEST = 0
private const val PART_02_PROD = 0
