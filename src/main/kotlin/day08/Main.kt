package day08

import InputLoader
import io.kotest.matchers.shouldBe

private fun String.parseInput() =
    lines().map { line -> line.map { it.digitToInt() } }

private fun List<Int>.visibilityInLine(): List<Boolean> {
    var min = -1
    val out = mutableListOf<Boolean>()
    forEachIndexed { index, tree ->
        if (min < tree) {
            out.add(true)
            min = tree
        } else {
            out.add(false)
        }

        if (min == 9) {
            out.addAll(((index + 1) until size).map { false })
            return out
        }
    }
    return out
}

private fun <T> List<List<T>>.transpose(): List<List<T>> {
    val out = MutableList(get(0).size) { mutableListOf<T>() }

    forEachIndexed { i, line ->
        line.forEachIndexed { j, tree ->
            out[j] += tree
        }
    }
    return out
}

private fun List<List<Int>>.handleHorizontalDirection(fromLeft: Boolean) =
    map {
        if (fromLeft)
            it.visibilityInLine()
        else
            it.reversed().visibilityInLine().reversed()
    }

private fun List<List<Int>>.handleVerticalDirection(fromTop: Boolean) =
    handleHorizontalDirection(fromTop).transpose()

private fun List<List<List<Boolean>>>.lor() : List<List<Boolean>> =
    MutableList(get(0).size) { mutableListOf<Boolean>() }.also {out ->
        repeat(get(0).size) { i ->
            repeat(get(0)[0].size) { j ->
                out[i] += fold(false) { acu, list -> acu || list[i][j] }
            }
        }
    }

private fun String.part01(): Int {
    val data = parseInput()
    val transposed = data.transpose()

    return listOf(
        data.handleHorizontalDirection(true),
        data.handleHorizontalDirection(false),
        transposed.handleVerticalDirection(true),
        transposed.handleVerticalDirection(false),
    )
        .lor()
        .sumOf { line -> line.count { it } }
}

private fun <T> List<List<T>>.printMatrix() =
    println(this.joinToString("\n"))

fun main() {
    testInput.part01() shouldBe PART_01_RES

    val input = InputLoader.loadInput("day08")
    println(input.part01())
}

private val testInput = """
30373
25512
65332
33549
35390""".trimIndent()

private const val PART_01_RES = 21
