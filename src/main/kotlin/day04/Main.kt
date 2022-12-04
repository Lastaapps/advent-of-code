package day04

private infix fun IntRange.isIn(other: IntRange): Boolean =
    first in other && endInclusive in other

private fun String.toRange() =
    split("-")
        .let { (from, to) ->
            from.toInt() .. to.toInt()
        }

private fun String.part01() : Int =
    lines()
        .map { line ->
            line
                .split(",")
                .map { range -> range.toRange() }
                .let { (first, second) ->
                    first isIn second || second isIn first
                }
        }.sumOf {
            (if (it) 1 else 0) as Int
        }

fun main() {
    listOf(
        TEST_INPUT,
        InputLoader.loadInput("day04"),
    ).forEach { input ->
        println(input.part01())
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
