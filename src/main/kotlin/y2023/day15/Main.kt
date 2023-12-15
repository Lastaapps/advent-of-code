package y2023.day15

import InputLoader
import Year
import io.kotest.matchers.shouldBe

private fun String.hash() =
    fold(0) { acu, char ->
        acu.plus(char.code).times(17)
    }.mod(256)

private fun String.part01(): Int =
    splitToSequence(',')
        .map { it.hash() }
        .sum()


private fun String.part02(): Int =
    splitToSequence(',')
        .fold(List(256) { ArrayDeque<Pair<String, Int>>() }) { acu, command ->
            val id: String
            val focalLength: Int

            if (command.last().isDigit()) {
                id = command.substring(0, command.length - 2)
                focalLength = command.last().digitToInt()
            } else {
                id = command.substring(0, command.length - 1)
                focalLength = 0
            }

            val list = acu[id.hash()]
            list.indexOfFirst { it.first == id }
                .takeIf { it >= 0 }
                .let { index ->
                    if (focalLength == 0) {
                        index?.let { list.removeAt(index) }
                    } else {
                        val item = id to focalLength
                        index?.let { list.set(index, item) } ?: list.add(item)
                    }
                }

            acu
        }
        .asSequence()
        .mapIndexed { boxNum, list ->
            list
                .asSequence()
                .mapIndexed { order, lens ->
                    (boxNum + 1) * (order + 1) * lens.second
                }.sum()
        }.sum()


fun main() {
    testInput.part01() shouldBe PART_01_TEST
    testInput.part02() shouldBe PART_02_TEST

    val input = InputLoader.loadInput(Year.Y2023, "day15")
    input.part01()
        .also { println(it) }
        .also { it shouldBe PART_01_PROD }
    input.part02()
        .also { println(it) }
        .also { it shouldBe PART_02_PROD }
}

private val testInput = """
rn=1,cm-,qp=3,cm=2,qp-,pc=4,ot=9,ab=5,pc-,pc=6,ot=7
""".trimIndent()

private const val PART_01_TEST = 1320
private const val PART_01_PROD = 519041
private const val PART_02_TEST = 145
private const val PART_02_PROD = 260530
