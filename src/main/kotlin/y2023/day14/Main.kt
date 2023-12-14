package y2023.day14

import InputLoader
import Year
import io.kotest.matchers.shouldBe
import kotlin.math.roundToInt
import kotlin.math.sqrt

private fun String.part01(): Int {
    val size = sqrt(length.toFloat()).roundToInt()
    val limits = MutableList(size) { 0 }

    var weight = 0
    lineSequence().forEachIndexed { i, line ->
        line.forEachIndexed { j, c ->
            when (c) {
                '.' -> {}
                '#' -> limits[j] = i + 1
                'O' -> {
                    weight += size - limits[j]
                    limits[j]++
                }
            }
        }
    }
    return weight
}

private fun <T> List<T>.reverseIf(cond: Boolean) = if (cond) this.asReversed() else this

private fun List<List<Int>>.roll(stones: List<List<Int>>, normalDirection: Boolean): List<List<Int>> {
    val limits = MutableList(size) { 0 }
    val nextStones = MutableList(size) { ArrayList<Int>(size / 3) }

    reverseIf(!normalDirection).asSequence()
        .zip(stones.reverseIf(!normalDirection).asSequence())
        .forEachIndexed { i, (squares, stones) ->
            squares.forEach { j ->
                limits[j] = i + 1
            }
            stones.forEach { j ->
                nextStones[j].add(limits[j].let { if (normalDirection) it else lastIndex - it })
                limits[j]++
            }
        }

    return nextStones
}

private fun List<List<Int>>.weight() =
    asReversed().foldIndexed(0) { index, acc, stones ->
        acc + stones.size * (index + 1)
    }

private fun String.part02(): Int {
    val size = sqrt(length.toFloat()).roundToInt()
    val mapOrig = MutableList(size) { ArrayList<Int>(size / 3) }
    val mapTrans = MutableList(size) { ArrayList<Int>(size / 3) }

    val stonesOrig = MutableList(size) { ArrayList<Int>(size / 3) }

    lineSequence().forEachIndexed { i, line ->
        line.forEachIndexed { j, c ->
            when (c) {
                '#' -> {
                    mapOrig[i].add(j)
                    mapTrans[j].add(i)
                }

                'O' -> {
                    stonesOrig[i].add(j)
                }
            }
        }
    }

    var stones = stonesOrig as List<List<Int>>
    val stonesSeen = hashMapOf(stones to 0)

    repeat(1_000_000_000) { iteration ->
        stones = mapOrig.roll(stones, true)
        stones = mapTrans.roll(stones, true)
        stones = mapOrig.roll(stones, false)
        stones = mapTrans.roll(stones, false)
        stones = stones.map { it.sorted() }

        stonesSeen[stones]
            ?.let { index ->
                val cycleLen = iteration + 1 - index
                val positionInCycle = (1_000_000_000 - index) % cycleLen
                val absoluteIndex = index + positionInCycle

                return stonesSeen.entries.first { it.value == absoluteIndex }.key.weight()
            }
            ?: stonesSeen.set(stones, iteration + 1)
    }

    return stones.weight()
}

fun main() {
    testInput.part01() shouldBe PART_01_TEST
    testInput.part02() shouldBe PART_02_TEST

    val input = InputLoader.loadInput(Year.Y2023, "day14")
    input.part01()
        .also { println(it) }
        .also { it shouldBe PART_01_PROD }
    input.part02()
        .also { println(it) }
        .also { it shouldBe PART_02_PROD }
}

private val testInput = """
O....#....
O.OO#....#
.....##...
OO.#O....O
.O.....O#.
O.#..O.#.#
..O..#O..O
.......O..
#....###..
#OO..#....
""".trimIndent()

private const val PART_01_TEST = 136
private const val PART_01_PROD = 106648
private const val PART_02_TEST = 64
private const val PART_02_PROD = 87700
