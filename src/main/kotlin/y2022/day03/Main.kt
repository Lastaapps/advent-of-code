package y2022.day03

import InputLoader
import Year

private fun String.splitHalves() =
    (length / 2).let { l -> take(l) to drop(l) }

private fun Char.priority() =
    when (this) {
        in 'a'..'z' -> this - 'a'
        in 'A'..'Z' -> this - 'A' + ('z' - 'a' + 1)
        else -> 0
    } + 1

private fun part01(input: String): Int =
    input.lines().map {
        it.splitHalves()
    }.map {
        it.first.toSet() to it.second.toSet()
    }.map {
        it.first.intersect(it.second).first()
    }.sumOf {
        it.priority()
    }

private fun part02(input: String): Int =
    input.lines()
        .chunked(3)
        .map { chunk ->
            chunk
                .map { it.toSet() }
                .reduce { i1, i2 -> i1.intersect(i2) }
                .first()
        }
        .sumOf {
            it.priority()
        }

fun main() {
    listOf(
        TEST_INPUT,
        InputLoader.loadInput(Year.Y2022, "day03")
    ).forEach { input ->
        println(part01(input))
        println(part02(input))
    }
}

private val TEST_INPUT = """
    vJrwpWtwJgWrhcsFMMfFFhFp
    jqHRNqRjqzjGDLGLrsFMfFZSrLrFZsSL
    PmmdzqPrVvPwwTWBwg
    wMqvLMZHhHMvwLHjbvcjnnSBnvTQFn
    ttgJtRGJQctTZtZT
    CrZsJsPPZsGzwwsLwLmpwMDw
""".trimIndent()