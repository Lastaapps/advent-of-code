package y2023.day11

import InputLoader
import Year
import io.kotest.matchers.shouldBe
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.sqrt

private const val GALAXY = '#'
private const val SPACE = '.'

private data class Point(val x: Int, val y: Int)

private operator fun Point.plus(other: Point): Point = Point(x + other.x, y + other.y)
private operator fun Point.minus(other: Point): Point = Point(x - other.x, y - other.y)
private operator fun Point.times(scale: Int): Point = Point(x * scale, y * scale)
private fun Point.norm() = x.absoluteValue + y.absoluteValue

private data class ExpandedGalaxy(
    val galaxies: List<Point>,
    val unusedRows: List<Int>,
    val unusedColumns: List<Int>,
) {
    private fun transform(point: Point, expansionFactor: Int): Point {
        val indexRows = -unusedRows.binarySearch(point.x) - 1
        val indexColumns = -unusedColumns.binarySearch(point.y) - 1

        return point + Point(indexRows, indexColumns) * (expansionFactor - 1)
    }

    fun transformGalaxy(expansionFactor: Int) = galaxies.map { transform(it, expansionFactor) }
}

private fun String.parseAndFind(): ExpandedGalaxy {
    val size = sqrt(length.toFloat()).roundToInt()
    val usedRows = MutableList(size) { false }
    val usedColumns = MutableList(size) { false }
    val galaxies = mutableListOf<Point>()

    lineSequence().forEachIndexed { i, line ->
        line.forEachIndexed { j, char ->
            if (char == GALAXY) {
                galaxies.add(Point(i, j))
                usedRows[i] = true
                usedColumns[j] = true
            }
        }
    }

    fun List<Boolean>.filterToIndexes() =
        this.asSequence()
            .mapIndexed { index, b -> if (b) -1 else index }
            .filter { it >= 0 }
            .toList()

    val unusedRows = usedRows.filterToIndexes()
    val unusedColumns = usedColumns.filterToIndexes()

    return ExpandedGalaxy(galaxies, unusedRows, unusedColumns)
}

private fun String.solve(expansionFactor: Int): Long =
    parseAndFind()
        .transformGalaxy(expansionFactor)
        .let { galaxies ->
            galaxies
                .asSequence()
                .mapIndexed { index, galaxy ->
                    galaxies.subList(index + 1, galaxies.size).sumOf { other ->
                        (other - galaxy).norm().toLong()
                    }
                }
        }.sum()

fun main() {
    testInput.solve(2) shouldBe PART_01_TEST
    testInput.solve(10) shouldBe PART_02_TEST_10
    testInput.solve(100) shouldBe PART_02_TEST_100

    val input = InputLoader.loadInput(Year.Y2023, "day11")
    input.solve(2)
        .also { println(it) }
        .also { it shouldBe PART_01_PROD }
    input.solve(1_000_000)
        .also { println(it) }
        .also { it shouldBe PART_02_PROD_1000000 }
}

private val testInput = """
...#......
.......#..
#.........
..........
......#...
.#........
.........#
..........
.......#..
#...#.....
""".trimIndent()

private const val PART_01_TEST = 374
private const val PART_01_PROD = 10292708
private const val PART_02_TEST_10 = 1030
private const val PART_02_TEST_100 = 8410
private const val PART_02_PROD_1000000 = 790194712336
