package day01

import InputLoader
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

private fun elvesBadges(input: String): ImmutableList<Int> =
    input
        .lines()
        .fold(
            persistentListOf<Int>() to 0
        ) { acc, s ->
            if (s.isBlank()) {
                acc.first.add(acc.second) to 0
            } else {
                val toAdd = s.toInt()
                acc.first to acc.second + toAdd
            }
        }
        .let { acc ->
            acc.first.add(acc.second)
        }

private fun List<Int>.top3Sum() =
    sortedDescending()
        .take(3)
        .sumOf { it }

fun main(args: Array<String>) {
    listOf(
        TEST_INPUT, InputLoader.loadInput("day01")
    ).forEach { input ->
        println(elvesBadges(input).max())
        println(elvesBadges(input).top3Sum())
        println()
    }
}

private const val TEST_INPUT = """
1000
2000
3000

4000

5000
6000

7000
8000
9000

10000"""
