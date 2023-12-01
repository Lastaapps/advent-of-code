package y2022.day02

import y2022.InputLoader

enum class Result(val score: Int, val hint: Char) {
    WIN(6, 'Z'),
    DRAW(3, 'Y'),
    LOSE(0, 'X'),
}

enum class Option(val elf: Char, val you: Char, val score: Int) {
    ROCK('A', 'X', 1),
    PAPER('B', 'Y', 2),
    SCISSORS('C', 'Z', 3), ;
}


private fun String.parseInputActual(): List<Pair<Option, Result>> =
    lines().map { line ->
        val (elf, you) = line.split(" ")
        Option.values().first { it.elf == elf[0] } to
                Result.values().first { it.hint == you[0] }
    }

private fun Option.withIntent(intent: Result) =
    when (intent) {
        Result.WIN -> when (this) {
            Option.ROCK -> Option.PAPER
            Option.PAPER -> Option.SCISSORS
            Option.SCISSORS -> Option.ROCK
        }

        Result.DRAW -> this

        Result.LOSE -> when (this) {
            Option.ROCK -> Option.SCISSORS
            Option.PAPER -> Option.ROCK
            Option.SCISSORS -> Option.PAPER
        }
    }

private fun Pair<Option, Result>.scoreActual(): Int =
    (first.withIntent(second).score + second.score)


private fun String.parseInputGuessed(): List<Pair<Option, Option>> =
    lines().map { line ->
        val (elf, you) = line.split(" ")
        Option.values().first { it.elf == elf[0] } to
                Option.values().first { it.you == you[0] }
    }

private fun Pair<Option, Option>.checkElfWin(): Boolean =
    first == second.withIntent(Result.WIN)

private fun Pair<Option, Option>.scoreGuessed(): Int =
    when {
        first == second -> Result.DRAW
        checkElfWin() -> Result.LOSE
        else -> Result.WIN
    }.score + second.score


fun main() {
    sequenceOf(
        testInput,
        InputLoader.loadInput("y2022/day02/day02"),
    )
        .forEach { input ->
            println(input.parseInputGuessed().sumOf { it.scoreGuessed() })
            println(input.parseInputActual().sumOf { it.scoreActual() })
        }
}

private val testInput = """
    A Y
    B X
    C Z
""".trimIndent()
