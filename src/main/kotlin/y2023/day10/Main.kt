package y2023.day10

import InputLoader
import Year
import io.kotest.matchers.shouldBe
import kotlin.math.absoluteValue

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
private operator fun Point.minus(other: Point): Point = Point(x - other.x, y - other.y)

private typealias PipeList = List<List<Pipe>>

private operator fun <T> List<List<T>>.get(point: Point) = this[point.x][point.y]
private operator fun <T> List<MutableList<T>>.set(point: Point, value: T) = (this[point.x].set(point.y, value))

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

private tailrec fun proceed(
    map: PipeList,
    point: Point,
    from: Dir,
    prevPoint: Point,
    length: Int = 1,
    used: MutableSet<Point> = HashSet(),
): Triple<Int, Set<Point>, Point>? {
    val pipe = map[point]
    used += point

    return if (pipe == Pipe.START) {
        return Triple(length, used, prevPoint)
    } else {
        val directions = pipe.directions()
        val inverse = from.inverse()
        if (inverse !in directions) {
            return null
        }

        val dir = directions.first { it != inverse }
        val nextPoint = point.move(dir, map) ?: return null
        proceed(
            map,
            nextPoint,
            dir,
            point,
            length + 1,
            used,
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
            proceed(input, it, dir, start)
        }
    }.first / 2
}

private fun replaceStart(start: Point, neighbours: Pair<Point, Point>): Pipe {
    val norm = neighbours.first - start
    val inverse = neighbours.second - start

    return when {
        norm.x == 0 && inverse.x == 0 -> Pipe.HORIZONTAL
        norm.x.absoluteValue == 1 && inverse.x.absoluteValue == 1 -> Pipe.VERTICAL
        norm.x == 1 || inverse.x == 1 ->
            if (norm.y == 1 || inverse.y == 1) {
                Pipe.SW
            } else {
                Pipe.SE
            }

        norm.x == -1 || inverse.x == -1 ->
            if (norm.y == 1 || inverse.y == 1) {
                Pipe.NW
            } else {
                Pipe.NE
            }

        else -> error("You fucked up, there are more cases: $norm, $inverse")
    }
}

private fun String.part02(): Int {
    val input = parseInput().toList()
    val start = input.findStart()
    val (used, startNeighbours) = Pipe.START.directions().firstNotNullOf { dir ->
        start.move(dir, input)?.let { point ->
            proceed(input, point, dir, start)
                ?.let {
                    it.second to (point to it.third)
                }
        }
    }

    val startPipe = replaceStart(start, startNeighbours)

    return input.indices.asSequence()
        .zip(input.asSequence())
        .sumOf { (i, pipes) ->

            var isInNest = false
            var startedTop = false
            var tilesCnt = 0

            pipes.indices.forEach { j ->
                val point = Point(i, j)
                if (point in used) {
                    val pipe = pipes[j].takeUnless { it == Pipe.START } ?: startPipe

                    fun resolveStarted(leavingTop: Boolean) {
                        if (startedTop != leavingTop) {
                            isInNest = !isInNest
                        }
                    }

                    when (pipe) {
                        Pipe.HORIZONTAL -> {}
                        Pipe.VERTICAL -> isInNest = !isInNest
                        Pipe.NE -> startedTop = true
                        Pipe.SE -> startedTop = false
                        Pipe.NW -> resolveStarted(true)
                        Pipe.SW -> resolveStarted(false)
                        Pipe.GROUND -> error("Invalid state - ground is a pipe")
                        Pipe.START -> error("Start is not replaced")
                    }
                } else {
                    if (isInNest) {
                        tilesCnt++
                    }
                }
            }
            tilesCnt
        }
}

fun main() {
    testInput01.part01() shouldBe PART_01_TEST
    testInput02.part02() shouldBe PART_02_TEST
    // 1603 to high

    val input = InputLoader.loadInput(Year.Y2023, "day10")
    input.part01()
        .also { println(it) }
        .also { it shouldBe PART_01_PROD }
    input.part02()
        .also { println(it) }
        .also { it shouldBe PART_02_PROD }
}

private val testInput01 = """
..F7.
.FJ|.
SJ.L7
|F--J
LJ...
""".trimIndent()

private val testInput02 = """
FF7FSF7F7F7F7F7F---7
L|LJ||||||||||||F--J
FL-7LJLJ||||||LJL-77
F--JF--7||LJLJ7F7FJ-
L---JF-JLJ.||-FJLJJ7
|F|F-JF---7F7-L7L|7|
|FFJF7L7F-JF7|JL---7
7-L-JL7||F7|L7F-7F7|
L.L7LFJ|||||FJL7||LJ
L7JLJL-JLJLJL--JLJ.L
""".trimIndent()

private const val PART_01_TEST = 8
private const val PART_01_PROD = 6725
private const val PART_02_TEST = 10
private const val PART_02_PROD = 383
