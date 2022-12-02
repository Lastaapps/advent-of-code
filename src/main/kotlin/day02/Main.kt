package day02

import InputLoader

object Score {
    const val WIN = 6
    const val DRAW = 3
    const val LOSE = 0
}

enum class Option(val op: Char, val you: Char, val score: Int) {
    ROCK('A', 'X', 1),
    PAPER('B', 'Y', 2),
    SCISSORS('C', 'Z', 3), ;
}

private fun String.parseInput() =
    lines().map { line ->
        val (op, you) = line.split(" ")
        Option.values().first { it.op == op[0] } to
                Option.values().first { it.you == you[0] }
    }

private fun Pair<Option, Option>.checkElfWin(): Boolean =
    first == Option.ROCK && second == Option.SCISSORS
            || first == Option.PAPER && second == Option.ROCK
            || first == Option.SCISSORS && second == Option.PAPER


private fun Pair<Option, Option>.score(): Int {
    val (f, s) = this
    if (f == s) return Score.DRAW + s.score

    return if (checkElfWin()) {
        s.score + Score.LOSE
    } else
        s.score + Score.WIN
}

fun main() {
    sequenceOf(
        testInput,
        InputLoader.loadInput("day02"),
    )
        .map { it.parseInput() }
        .forEach { input ->
            println(input.sumOf { it.score() })
        }
}

private val testInput = """
    A Y
    B X
    C Z
""".trimIndent()