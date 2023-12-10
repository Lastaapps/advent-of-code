package y2023.day10

import InputLoader
import Year
import io.kotest.matchers.shouldBe

private enum class Pipe(private val letter: Char) {
    GROUND('.'),
    START('S'),
    HORIZONTAL('-'),
    VERTICAL('|'),
    NW('J'),
    NE('L'),
    SW('7'),
    SE('F'),
    ;

    companion object {
        fun fromChar(letter: Char): Pipe = entries.first { it.letter == letter }
    }
}

private data class Point(val x: Int, val y: Int)

private operator fun Point.plus(other: Point): Point = Point(x + other.x, y + other.y)

private typealias PipeList = List<List<Pipe>>

private data class PointInMap(val map: PipeList, val point: Point)

private fun PipeList.at(point: Point) = this[point.x][point.y]

enum class Dir {
    SOUTH, NORTH, EAST, WEST, ;
}

private fun Dir.inverse() = when (this) {
    Dir.SOUTH -> Dir.NORTH
    Dir.NORTH -> Dir.SOUTH
    Dir.EAST -> Dir.WEST
    Dir.WEST -> Dir.EAST
}

private fun Point.move(dir: Dir, map: PipeList) =
    when (dir) {
        Dir.SOUTH -> Point(1, 0)
        Dir.NORTH -> Point(-1, 0)
        Dir.EAST -> Point(0, 1)
        Dir.WEST -> Point(0, -1)
    }.plus(this).takeIf {
        it.x in map.indices && it.y in map.first().indices
    }

private fun Pipe.directions() = when (this) {
    Pipe.GROUND -> emptyList()
    Pipe.START -> listOf(Dir.EAST, Dir.SOUTH, Dir.WEST, Dir.NORTH)
    Pipe.HORIZONTAL -> listOf(Dir.WEST, Dir.EAST)
    Pipe.VERTICAL -> listOf(Dir.NORTH, Dir.SOUTH)
    Pipe.NW -> listOf(Dir.NORTH, Dir.WEST)
    Pipe.NE -> listOf(Dir.NORTH, Dir.EAST)
    Pipe.SW -> listOf(Dir.SOUTH, Dir.WEST)
    Pipe.SE -> listOf(Dir.SOUTH, Dir.EAST)
}


private tailrec fun proceed(pos: PointInMap, from: Dir, length: Int = 1): Int? {
    val pipe = pos.map.at(pos.point)
    return if (pipe == Pipe.START) {
        length
    } else {
        val directions = pipe.directions()
        val inverse = from.inverse()
        if (inverse !in directions) {
            return null
        }

        val dir = directions.first { it != inverse }
        val nextPoint = pos.point.move(dir, pos.map) ?: return null
        proceed(
            pos.copy(point = nextPoint),
            dir,
            length + 1,
        )
    }
}

private fun String.parseInput() =
    lineSequence()
        .map { line ->
            line.map(Pipe.Companion::fromChar)
        }

// Yes, I know this requires one more iteration through the whole map
// and that this should be done few lines above... but I'm lazy
private fun PipeList.findStart(): Point {
    this.indices.forEach { i ->
        val line = this[i]
        line.indices.forEach { j ->
            if (line[j] == Pipe.START) {
                return Point(i, j)
            }
        }
    }
    error("Start not found")
}

private fun String.part01(): Int {
    val input = parseInput().toList()
    val start = input.findStart()
    return Pipe.START.directions().firstNotNullOf { dir ->
        start.move(dir, input)?.let {
            proceed(PointInMap(input, it), dir)
        }
    } / 2
}

private fun String.part02(): Int =
    PART_02_TEST

fun main() {
    testInput.part01() shouldBe PART_01_TEST
    testInput.part02() shouldBe PART_02_TEST

    val input = InputLoader.loadInput(Year.Y2023, "day10")
    input.part01()
        .also { println(it) }
        .also { it shouldBe PART_01_PROD }
    input.part02()
        .also { println(it) }
        .also { it shouldBe PART_02_PROD }
}

private val testInput = """
..F7.
.FJ|.
SJ.L7
|F--J
LJ...
""".trimIndent()

private const val PART_01_TEST = 8
private const val PART_01_PROD = 6725
private const val PART_02_TEST = 0
private const val PART_02_PROD = 0
