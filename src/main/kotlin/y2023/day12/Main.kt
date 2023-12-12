package y2023.day12

import InputLoader
import Year
import io.kotest.matchers.shouldBe

private fun String.parseInput() =
    lineSequence()
        .map { line ->
            val (record, numbers) = line.split(' ', limit = 2)
            record.toList() to numbers.split(',').map { it.toInt() }
        }

private fun <T> List<T>.subFrom(fromIndex: Int) = subList(fromIndex, size)

// Yes, no cache (yet), not time for it now
private fun countOptions(data: Pair<List<Char>, List<Int>>): Int {
    val (text, numbers) = data

    if (text.isEmpty()) {
        return if (numbers.isEmpty()) 1 else 0
    }

    val first = text.first()
    var cnt = 0

    if (first != '#') { // '.', '?'
        cnt += countOptions(text.subFrom(1) to numbers)
    }

    if (first != '.') { // '#', '?'
        cnt += numbers.firstOrNull()?.let { required ->
            if (text.size >= required
                && text.subList(0, required).none { it == '.' }
                && text.getOrNull(required) != '#'
            ) {
                countOptions(text.subFrom((required + 1).coerceAtMost(text.size)) to numbers.subFrom(1))
            } else {
                0
            }
        } ?: 0
    }

    return cnt
}

private fun String.part01() =
    parseInput()
        .map { countOptions(it) }
        .sum()

private fun String.part02() = PART_02_TEST

fun main() {
    testInput.part01() shouldBe PART_01_TEST
    testInput.part02() shouldBe PART_02_TEST

    val input = InputLoader.loadInput(Year.Y2023, "day12")
    input.part01()
        .also { println(it) }
        .also { it shouldBe PART_01_PROD }
    input.part02()
        .also { println(it) }
        .also { it shouldBe PART_02_PROD }
}

private val testInput = """
???.### 1,1,3
.??..??...?##. 1,1,3
?#?#?#?#?#?#?#? 1,3,1,6
????.#...#... 4,1,1
????.######..#####. 1,6,5
?###???????? 3,2,1
""".trimIndent()

private const val PART_01_TEST = 21
private const val PART_01_PROD = 7173
private const val PART_02_TEST = 0
private const val PART_02_PROD = 0
