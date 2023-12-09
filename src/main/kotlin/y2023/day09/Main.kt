package y2023.day09

import InputLoader
import Year
import io.kotest.matchers.shouldBe

private fun String.parseInput() =
    lineSequence()
        .map { line ->
            line.split(" ")
                .map { it.toInt() }
        }

private fun resolveSequence(sequence: List<Int>): Int {
    val diffs = sequence.windowed(2, 1).map { (a, b) -> b - a }
    return if (diffs.all { it == 0 }) {
        0 // diffs.last()
    } else {
        diffs.last() + resolveSequence(diffs)
    }
}

private fun String.part01(): Int =
    parseInput()
        .map { line ->
            line.last() + resolveSequence(line)
        }
        .sum()

private fun String.part02(): Int =
    PART_02_RES

fun main() {
    testInput.part01() shouldBe PART_01_RES
    testInput.part02() shouldBe PART_02_RES

    val input = InputLoader.loadInput(Year.Y2023, "day09")
    println(input.part01())
    println(input.part02())
}

private val testInput = """
0 3 6 9 12 15
1 3 6 10 15 21
10 13 16 21 30 45
""".trimIndent()

private const val PART_01_RES = 114
private const val PART_02_RES = 0
