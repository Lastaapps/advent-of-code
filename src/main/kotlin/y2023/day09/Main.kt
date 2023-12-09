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

private fun resolveSequenceBack(sequence: List<Int>): Int {
    val diffs = sequence.windowed(2, 1).map { (a, b) -> b - a }
    return if (diffs.all { it == 0 }) {
        0 // diffs.last()
    } else {
        diffs.last() + resolveSequenceBack(diffs)
    }
}

private fun String.part01(): Int =
    parseInput()
        .map { line ->
            line.last() + resolveSequenceBack(line)
        }
        .sum()

private fun resolveSequenceFront(sequence: List<Int>): Int {
    val diffs = sequence.windowed(2, 1).map { (a, b) -> b - a }
    return if (diffs.all { it == 0 }) {
        0 // diffs.last()
    } else {
        diffs.first() - resolveSequenceFront(diffs)
    }
}

private fun String.part02(): Int =
    parseInput()
        .map { line ->
            line.first() - resolveSequenceFront(line)
        }
        .sum()

fun main() {
    testInput.part01() shouldBe PART_01_TEST
    testInput.part02() shouldBe PART_02_TEST

    val input = InputLoader.loadInput(Year.Y2023, "day09")
    input.part01()
        .also { it shouldBe PART_01_PROD }
        .also { println(it) }
    input.part02()
        .also { it shouldBe PART_02_PROD }
        .also { println(it) }
}

private val testInput = """
0 3 6 9 12 15
1 3 6 10 15 21
10 13 16 21 30 45
""".trimIndent()

private const val PART_01_TEST = 114
private const val PART_01_PROD = 1762065988
private const val PART_02_TEST = 2
private const val PART_02_PROD = 1066
