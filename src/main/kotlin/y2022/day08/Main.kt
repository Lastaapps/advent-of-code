package y2022.day08

import InputLoader
import Year
import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf

private fun String.parseInput() =
    lines().map { line -> line.map { it.digitToInt() } }

private fun List<Int>.visibilityInLine(): List<Boolean> =
    persistentListOf<Boolean>().mutate { out ->
        var min = -1

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
    }

private fun <T> List<List<T>>.transpose(): List<List<T>> =
    MutableList(get(0).size) { mutableListOf<T>() }.also { out ->
        forEach { line ->
            line.forEachIndexed { j, tree ->
                out[j] += tree
            }
        }
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

private fun List<List<List<Boolean>>>.lor(): List<List<Boolean>> =
    MutableList(get(0).size) { mutableListOf<Boolean>() }.also { out ->
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

// Warning - naive implementation
private fun List<List<Int>>.neighborhood(x: Int, y: Int): Int {
    val tree = this[x][y]

    var top = 0
    var bottom = 0
    var left = 0
    var right = 0

    for (i in (y - 1) downTo 0) {
        ++top
        val local = this[x][i]
        if (local >= tree) {
            break
        }
    }
    for (i in (y + 1) until size) {
        ++bottom
        val local = this[x][i]
        if (local >= tree) {
            break
        }
    }
    for (i in (x - 1) downTo 0) {
        ++left
        val local = this[i][y]
        if (local >= tree) {
            break
        }
    }
    for (i in (x + 1) until size) {
        ++right
        val local = this[i][y]
        if (local >= tree) {
            break
        }
    }
    return top * bottom * left * right
}

// Warning - this solution is really stupid, but I had not time for a better one
private fun String.part02(): Int {
    val input = parseInput()

    return input.mapIndexed { x, line ->
        List(line.size) { y ->
            input.neighborhood(x, y)
        }
    }
        .maxOf { it.max() }
}

private fun <T> List<List<T>>.printMatrix() =
    println(this.joinToString("\n"))

fun main() {
    testInput.part01() shouldBe PART_01_RES
    testInput.part02() shouldBe PART_02_RES

    val input = InputLoader.loadInput(Year.Y2022, "day08")
    println(input.part01())
    println(input.part02())
}

private val testInput = """
30373
25512
65332
33549
35390""".trimIndent()

private const val PART_01_RES = 21
private const val PART_02_RES = 8
