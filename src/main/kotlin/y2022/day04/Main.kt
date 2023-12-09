package y2022.day04

import InputLoader
import Year

private infix fun IntRange.isIn(other: IntRange): Boolean =
    first in other && endInclusive in other

private infix fun IntRange.intersects(other: IntRange): Boolean =
    first in other || endInclusive in other || other.isIn(this)

private fun String.toRange() =
    split("-")
        .let { (from, to) ->
            from.toInt()..to.toInt()
        }

private fun String.processElves(process: (first: IntRange, second: IntRange) -> Boolean) =
    lines()
        .map { line ->
            line
                .split(",")
                .map { range ->
                    range.toRange()
                }
                .let { (first, second) ->
                    process(first, second)
                }
        }.count { it }

private fun String.part01(): Int =
    processElves { first, second ->
        first isIn second || second isIn first
    }

private fun String.part02(): Int =
    processElves { first, second ->
        first intersects second
        // first.intersect(second).isNotEmpty()
    }

fun main() {
    listOf(
        TEST_INPUT,
        InputLoader.loadInput(Year.Y2022, "day04"),
    ).forEach { input ->
        println(input.part01())
        println(input.part02())
    }
}

private val TEST_INPUT = """
    2-4,6-8
    2-3,4-5
    5-7,7-9
    2-8,3-7
    6-6,4-6
    2-6,4-8
""".trimIndent()
