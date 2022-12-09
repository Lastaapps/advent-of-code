package day09

import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf

private enum class Dir {
    LEFT, RIGHT, UP, DOWN;

    companion object {
        fun from(c: Char) = when (c) {
            'L' -> LEFT
            'R' -> RIGHT
            'U' -> UP
            'D' -> DOWN
            else -> error("Unknown character")
        }
    }
}

private fun String.parseInput(): List<Dir> =
    persistentListOf<Dir>().mutate { out ->
        lines().onEach {
            val (dirRaw, countRaw) = it.split(" ")
            val dir = Dir.from(dirRaw.first())
            repeat(countRaw.toInt()) {
                out += dir
            }
        }
    }

private operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>): Pair<Int, Int> =
    Pair(first + other.first, second + other.second)

private operator fun Pair<Int, Int>.minus(other: Pair<Int, Int>): Pair<Int, Int> =
    Pair(first - other.first, second - other.second)

private fun String.part01() =
    parseInput().let { data ->
        val positions = mutableSetOf<Pair<Int, Int>>()

        var head = Pair(0, 0)
        var tail = Pair(0, 0)

        data.forEach { dir ->
            val diff = head - tail

            when(dir) {
                Dir.LEFT -> {
                    if (diff.first < 0) {
                        tail = head
                    }
                    head += -1 to 0
                }
                Dir.RIGHT -> {
                    if (diff.first > 0) {
                        tail = head
                    }
                    head += 1 to 0
                }
                Dir.UP -> {
                    if (diff.second < 0) {
                        tail = head
                    }
                    head += 0 to -1
                }
                Dir.DOWN -> {
                    if (diff.second > 0) {
                        tail = head
                    }
                    head += 0 to 1
                }
            }

            positions += tail
        }

        positions.size
    }

fun main() {
    testInput.part01() shouldBe PART_01_RES

    val input = InputLoader.loadInput("day09")
    println(input.part01())
}

private val testInput = """
R 4
U 4
L 3
D 1
R 4
D 1
L 5
R 2
""".trimIndent()

private const val PART_01_RES = 13
