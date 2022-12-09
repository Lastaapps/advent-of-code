package day09

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import kotlin.math.sign

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

private data class Vector(val x: Int, val y: Int) {
    companion object {
        val zero = Vector(0, 0)
    }
}

private operator fun Vector.plus(other: Vector): Vector =
    Vector(x + other.x, y + other.y)

private operator fun Vector.minus(other: Vector): Vector =
    Vector(x - other.x, y - other.y)


private fun String.parseInput(): Sequence<Dir> =
    lineSequence().map {
        val (dirRaw, countRaw) = it.split(" ")
        val dir = Dir.from(dirRaw.first())

        sequence {
            repeat(countRaw.toInt()) { yield(dir) }
        }
    }.flatMap { it }


private fun List<Vector>.printMap() {
    val toDraw = 12
    for (i in -toDraw..toDraw) {
        for (j in -toDraw..toDraw) {
            val pos = Vector(j, i)
            when (val index = this.indexOf(pos)) {
                -1 -> if (pos == Vector.zero) 's' else '.'
                0 -> 'H'
                else -> index.toString().first()
            }.also { print(it) }
        }
        println()
    }
    println()
}

private fun Dir.toMovement() =
    when (this) {
        Dir.LEFT -> Vector(-1, 0)
        Dir.RIGHT -> Vector(1, 0)
        Dir.UP -> Vector(0, -1)
        Dir.DOWN -> Vector(0, 1)
    }

private fun Vector.areNeighbors(head: Vector): Boolean =
    (this - head).let { it.x in -1..1 && it.y in -1..1 }

private fun Vector.follow(head: Vector): Vector =
    if (areNeighbors(head)) {
        Vector.zero
    } else {
        (head - this).let { diff ->
            Vector(diff.x.sign, diff.y.sign)
        }
    }

private fun String.part01(): Int =
    parseInput().let { input ->
        val visited = mutableSetOf<Vector>()

        var head = Vector.zero
        var tail = Vector.zero

        input.forEach { dir ->
            head += dir.toMovement()
            tail += tail.follow(head)

            visited += tail
        }

        visited.size
    }

private fun String.generalSolve(length: Int): Int =
    parseInput().let { input ->
        mutableSetOf<Vector>().also { visited ->
            MutableList(length) { Vector.zero }.let { rope ->
                input.forEach { dir ->
                    rope[0] += dir.toMovement()

                    for (i in 1 until rope.size) {
                        rope[i] += rope[i].follow(rope[i - 1])
                    }

                    visited += rope.last()
                }
            }
        }.size
    }

private fun String.part015(): Int = generalSolve(2)

private fun String.part02(): Int = generalSolve(10)

private class NeighbourMather(val expected: Vector) : Matcher<Vector> {
    override fun test(value: Vector): MatcherResult =
        MatcherResult(
            expected.areNeighbors(value),
            { "$expected is not neighbour of $value" },
            { "$expected is neighbour of $value" },
        )
}

private infix fun Vector.shouldNeighbour(value: Vector) =
    this.should(NeighbourMather(value))

fun main() {
    for (i in -2..2) {
        for (j in -2..2) {
            val toFollow = Vector(i, j)
            Vector.zero.follow(toFollow) shouldNeighbour toFollow
        }
    }

    testInput01.part01() shouldBe PART_01_RES
    testInput01.part015() shouldBe PART_01_RES
    testInput01.part02() shouldBe PART_02_RES_A
    testInput02.part02() shouldBe PART_02_RES_B

    val input = InputLoader.loadInput("day09")
    println(input.part01())
    println(input.part015())
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
