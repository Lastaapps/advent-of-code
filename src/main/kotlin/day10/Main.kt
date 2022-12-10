package day10

import InputLoader
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe

private sealed interface Inst {
    data class AddX(val value: Int) : Inst
    object Noop : Inst
}

private fun String.parseInput() =
    lineSequence()
        .map { line ->
            when (line) {
                "noop" -> Inst.Noop
                else -> Inst.AddX(line.split(" ")[1].toInt())
            }
        }

private fun Sequence<Inst>.simplify() = sequence {
    val src = this@simplify
    src.forEach {
        when (it) {
            is Inst.AddX -> {
                yield(Inst.Noop)
                yield(it)
            }

            Inst.Noop ->
                yield(it)
        }
    }
}

private operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>): Pair<Int, Int> =
    (first + other.first) to (second + other.second)

private fun String.part01(): Int =
    parseInput()
        .simplify()
        .let { src ->
            fun Int.shouldCompute() =
                (this - 20 + 1) % 40 == 0

            src.foldIndexed(1 to 0) { index, (acc, total), inst ->
                when (inst) {
                    is Inst.AddX -> inst.value
                    Inst.Noop -> 0
                }.let { newAcc ->
                    (if (index.shouldCompute()) acc * (index + 1) else 0).let { newTotal ->
                        (acc to total) + (newAcc to newTotal)
                    }
                }
            }
        }.second

private fun String.part02(): Int = 0

fun main() {
    testInput.part01() shouldBe PART_01_RES

    InputLoader.loadInput("day10").let { input ->
        println(input.part01())
        println(input.part02())
    }
}


private val testInput = """
addx 15
addx -11
addx 6
addx -3
addx 5
addx -1
addx -8
addx 13
addx 4
noop
addx -1
addx 5
addx -1
addx 5
addx -1
addx 5
addx -1
addx 5
addx -1
addx -35
addx 1
addx 24
addx -19
addx 1
addx 16
addx -11
noop
noop
addx 21
addx -15
noop
noop
addx -3
addx 9
addx 1
addx -3
addx 8
addx 1
addx 5
noop
noop
noop
noop
noop
addx -36
noop
addx 1
addx 7
noop
noop
noop
addx 2
addx 6
noop
noop
noop
noop
noop
addx 1
noop
noop
addx 7
addx 1
noop
addx -13
addx 13
addx 7
noop
addx 1
addx -33
noop
noop
noop
addx 2
noop
noop
noop
addx 8
noop
addx -1
addx 2
addx 1
noop
addx 17
addx -9
addx 1
addx 1
addx -3
addx 11
noop
noop
addx 1
noop
addx 1
noop
noop
addx -13
addx -19
addx 1
addx 3
addx 26
addx -30
addx 12
addx -1
addx 3
addx 1
noop
noop
noop
addx -9
addx 18
addx 1
addx 2
noop
noop
addx 9
noop
noop
noop
addx -1
addx 2
addx -37
addx 1
addx 3
noop
addx 15
addx -21
addx 22
addx -6
addx 1
noop
addx 2
addx 1
noop
addx -10
noop
noop
addx 20
addx 1
addx 2
addx 2
addx -6
addx -11
noop
noop
noop
""".trimIndent()

private const val PART_01_RES = 13140
