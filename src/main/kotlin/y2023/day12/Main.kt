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

private fun Pair<List<Char>, List<Int>>.unfold(scale: Int = 5, fill: Char = '?'): Pair<List<Char>, List<Int>> =
    Pair(
        ArrayList<Char>(first.size * scale + scale - 1).also { list ->
            repeat(scale) { i ->
                if (i != 0) {
                    list.add(fill)
                }
                list.addAll(first)
            }
        },
        ArrayList<Int>(second.size * scale).also { list ->
            repeat(scale) {
                list.addAll(second)
            }
        },
    )

private fun <T> List<T>.subFrom(fromIndex: Int) = subList(fromIndex, size)

private fun countOptions(
    text: List<Char>,
    numbers: List<Int>,
    cache: MutableList<MutableList<Long>> = MutableList(text.size + 1) { MutableList(numbers.size + 1) { -1L } },
): Long {
    if (text.isEmpty()) {
        return if (numbers.isEmpty()) 1 else 0
    }

    val inCache = cache[text.size][numbers.size]
    if (inCache != -1L) {
        return inCache
    }

    val first = text.first()
    var cnt = 0L

    if (first != '#') { // '.', '?'
        cnt += countOptions(text.subFrom(1), numbers, cache)
    }

    if (first != '.') { // '#', '?'
        cnt += numbers.firstOrNull()?.let { required ->
            if (text.size >= required
                && text.subList(0, required).none { it == '.' }
                && text.getOrNull(required) != '#'
            ) {
                countOptions(
                    text.subFrom((required + 1).coerceAtMost(text.size)),
                    numbers.subFrom(1),
                    cache,
                )
            } else {
                0
            }
        } ?: 0
    }

    cache[text.size][numbers.size] = cnt
    return cnt
}

private fun String.part01() =
    parseInput()
        .map { countOptions(it.first, it.second) }
        .sum()

private fun String.part02() =
    parseInput()
        .map { it.unfold() }
        .map { countOptions(it.first, it.second) }
        .sum()

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
private const val PART_02_TEST = 525152
private const val PART_02_PROD = 29826669191291
