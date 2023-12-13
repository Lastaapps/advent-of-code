package y2023.day13

import InputLoader
import Year
import io.kotest.matchers.shouldBe

@JvmInline
value class BitSet(private val value: UInt = 0u) {
    operator fun get(index: Int) = (value and (1u shl index)) != 0u
    operator fun set(index: Int, bit: Boolean): BitSet {
        val bool =
            if (bit) {
                1u shl index
            } else {
                0u
            }

        val bits = (value and (1u shl index).inv()) or bool
        return BitSet(bits)
    }

    override fun toString(): String = value.toString(2)

    infix fun and(other: BitSet) = BitSet(value xor other.value)
    infix fun or(other: BitSet) = BitSet(value xor other.value)
    infix fun xor(other: BitSet) = BitSet(value xor other.value)
    fun inv() = BitSet(value.inv())

    fun countOneBits() = value.countOneBits()
}

private fun String.parseInput() =
    splitToSequence("\n\n".toRegex())
        .map { input ->
            val lines = input.lines()

            val rows = lines.map { line ->
                line.foldIndexed(BitSet()) { i, bitset, char ->
                    bitset.set(i, char == '#')
                }
            }

            val columns = lines.first().indices.map { j ->
                lines.foldIndexed(BitSet()) { i, bitset, line ->
                    bitset.set(i, line[j] == '#')
                }
            }

            rows to columns
        }

private fun List<BitSet>.findReflection(): Int {
    // This is really naive, I'm saving time for part 2
    (1..lastIndex).forEach { i ->
        val dist = kotlin.math.min(i, size - i)
        (0 until dist)
            .all { j ->
                this[i + j] == this[i - j - 1]
            }
            .takeIf { it }
            ?.let { return i }
    }

    return 0
}

private fun String.part01() =
    parseInput()
        .sumOf { (rows, columns) ->
            rows.findReflection().times(100) + columns.findReflection()
        }

private fun List<BitSet>.findReflectionWithSmudge(): Int {
    (1..lastIndex).forEach { i ->
        val dist = kotlin.math.min(i, size - i)
        var cleaningAllowed = true
        (0 until dist)
            .all { j ->
                val b1 = this[i + j]
                val b2 = this[i - j - 1]
                if (b1 == b2) {
                    true
                } else if ((b1 xor b2).countOneBits() == 1) {
                    cleaningAllowed.also { cleaningAllowed = false }
                } else {
                    false
                }

            }
            .takeIf { it && !cleaningAllowed }
            ?.let { return i }
    }

    return 0
}

private fun String.part02() =
    parseInput()
        .sumOf { (rows, columns) ->
            rows.findReflectionWithSmudge().times(100) +
                    columns.findReflectionWithSmudge()
        }

fun main() {
    testInput.part01() shouldBe PART_01_TEST
    testInput.part02() shouldBe PART_02_TEST

    val input = InputLoader.loadInput(Year.Y2023, "day13")
    input.part01()
        .also { println(it) }
        .also { it shouldBe PART_01_PROD }
    input.part02()
        .also { println(it) }
        .also { it shouldBe PART_02_PROD }
}

private val testInput = """
#.##..##.
..#.##.#.
##......#
##......#
..#.##.#.
..##..##.
#.#.##.#.

#...##..#
#....#..#
..##..###
#####.##.
#####.##.
..##..###
#....#..#
""".trimIndent()

private const val PART_01_TEST = 405
private const val PART_01_PROD = 33047
private const val PART_02_TEST = 400
private const val PART_02_PROD = 28806
