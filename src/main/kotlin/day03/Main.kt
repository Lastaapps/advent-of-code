package day03

import InputLoader

private fun String.splitHalves() =
    (length / 2).let { l -> take(l) to drop(l) }

private fun Char.priority() =
    when(this) {
        in 'a' .. 'z' -> this - 'a'
        in 'A' .. 'Z' -> this - 'A' + ('z' - 'a' + 1)
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


fun main() {
    listOf(
        TEST_INPUT,
        InputLoader.loadInput("day03")
    ).forEach { input ->
        println(part01(input))
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