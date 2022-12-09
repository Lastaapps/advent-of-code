package day09

import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.*

private enum class Dir {
    LEFT, RIGHT, UP, DOWN,
    UL, UR, DL, DR,
    NONE;

    companion object {
        fun from(c: Char) = when (c) {
            'L' -> LEFT
            'R' -> RIGHT
            'U' -> UP
            'D' -> DOWN
            else -> error("Unknown character")
        }

        fun from(diff: Pair<Int, Int>) = when (diff) {
            zero -> NONE
            1 to 0 -> RIGHT
            1 to 1 -> DR
            0 to 1 -> DOWN
            -1 to 1 -> DL
            -1 to 0 -> LEFT
            -1 to -1 -> UL
            0 to -1 -> UP
            1 to -1 -> UR
            else -> error("Unsupported movement: $diff")
        }
    }
}

private val zero = 0 to 0

private fun String.parseInput(): Sequence<Dir> =
    lineSequence().map {
        val (dirRaw, countRaw) = it.split(" ")
        val dir = Dir.from(dirRaw.first())

        sequence {
            repeat(countRaw.toInt()) { yield(dir) }
        }
    }.flatMap { it }

private operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>): Pair<Int, Int> =
    Pair(first + other.first, second + other.second)

private operator fun Pair<Int, Int>.minus(other: Pair<Int, Int>): Pair<Int, Int> =
    Pair(first - other.first, second - other.second)

private fun Pair<Int, Int>.maxAbs(): Int =
    max(abs(first), abs(second))

private fun String.part01() =
    parseInput().let { data ->
        val positions = mutableSetOf<Pair<Int, Int>>()

        var head = Pair(0, 0)
        var tail = Pair(0, 0)

        data.forEach { dir ->
            val diff = head - tail

            when (dir) {
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

                else -> error("Not supported in part 01")
            }

            positions += tail
        }

        positions.size
    }

private fun Pair<Int, Int>.moveHead(dir: Dir): Pair<Int, Int> =
    this + when (dir) {
        Dir.LEFT -> -1 to 0
        Dir.RIGHT -> 1 to 0
        Dir.UP -> 0 to -1
        Dir.DOWN -> 0 to 1
        else -> error("Cannot move head in the $dir direction")
    }

private fun Pair<Int, Int>.follow(head: Pair<Int, Int>, dir: Dir): Pair<Pair<Int, Int>, Dir> {
    val diff = head - this

    when (dir) {
        Dir.LEFT -> {
            if (diff.first < 0) {
                head
            } else {
                this
            }
        }

        Dir.RIGHT -> {
            if (diff.first > 0) {
                head
            } else {
                this
            }
        }

        Dir.UP -> {
            if (diff.second < 0) {
                head
            } else {
                this
            }
        }

        Dir.DOWN -> {
            if (diff.second > 0) {
                head
            } else {
                this
            }
        }

        Dir.UL -> {
            if (diff.first < 0 || diff.second < 0) {
                this + (-1 to -1)
            } else {
                this
            }
        }
        Dir.UR -> {
            if (diff.first > 0 || diff.second < 0) {
                this + (1 to -1)
            } else {
                this
            }
        }
        Dir.DL -> {
            if (diff.first < 0 || diff.second > 0) {
                this + (-1 to 1)
            } else {
                this
            }
        }
        Dir.DR -> {
            if (diff.first > 0 || diff.second > 0) {
                this + (1 to 1)
            } else {
                this
            }
        }
        Dir.NONE -> this
    }.let { newPos ->
        return newPos to Dir.from(newPos - this)
    }
}

private fun List<Pair<Int, Int>>.printMap() {
    val toDraw = 12
    for (i in -toDraw..toDraw) {
        for (j in -toDraw..toDraw) {
            val pos = j to i
            when (val index = this.indexOf(pos)) {
                -1 -> if (pos == zero) 's' else '.'
                0 -> 'H'
                else -> index.toString().first()
            }.also { print(it) }
        }
        println()
    }
    println()
}

private fun String.part02(): Int =
    parseInput().let { data ->
        val positions = mutableSetOf<Pair<Int, Int>>()

        var knots = MutableList(10) { Pair(0, 0) }
        knots.printMap()

        data.forEach { headDir ->

            val newKnots = mutableListOf<Pair<Int, Int>>()
            newKnots += knots[0].moveHead(headDir)
            var lastDir = headDir
            for (i in 1 until 10) {
                val (newPos, dir) = knots[i].follow(knots[i - 1], lastDir)
                newKnots += newPos
                lastDir = dir
            }

            knots = newKnots
            knots.printMap()

            positions += knots.last()
        }

        positions.size
    }

fun main() {
    testInput01.part01() shouldBe PART_01_RES
    testInput01.part02() shouldBe PART_02_RES_A
    testInput02.part02() shouldBe PART_02_RES_B

    val input = InputLoader.loadInput("day09")
    println(input.part01())
    println(input.part02())
}

private val testInput01 = """
R 4
U 4
L 3
D 1
R 4
D 1
L 5
R 2
""".trimIndent()

private val testInput02 = """
R 5
U 8
L 8
D 3
R 17
D 10
L 25
U 20
""".trimIndent()

private const val PART_01_RES = 13
private const val PART_02_RES_A = 1
private const val PART_02_RES_B = 36
