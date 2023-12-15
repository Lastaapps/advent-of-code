package y2023.day15

import InputLoader
import Year
import io.kotest.matchers.shouldBe

private fun String.part01(): Int =
    splitToSequence(',')
        .map {
            it.fold(0) { acu, char ->
                acu.plus(char.code).times(17).mod(256)
            }
        }
        .sum()


private fun String.part02(): Int = PART_02_TEST


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
private const val PART_02_TEST = 0
private const val PART_02_PROD = 0
