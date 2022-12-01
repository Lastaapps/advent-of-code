package dec01

import InputLoader
import kotlinx.collections.immutable.persistentListOf

private fun handleInput(input: String): Int =
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
        .max()

fun main(args: Array<String>) {
    println(handleInput(TEST_INPUT))
    println(handleInput(InputLoader.loadInput("dec01")))
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
