package y2022.day13

import io.kotest.matchers.shouldBe
import y2022.InputLoader
import kotlin.math.min

private sealed interface Elem : Comparable<Elem> {
    @JvmInline
    value class EList(val value: List<Elem>) : Elem {
        override fun compareTo(other: Elem): Int {
            return when (other) {
                is EInt -> compareTo(EList(other))
                is EList -> {
                    for (i in 0 until min(value.size, other.value.size)) {
                        value[i].compareTo(other.value[i]).let {
                            if (it != 0) return it
                        }
                    }
                    value.size.compareTo(other.value.size)
                }
            }
        }

        constructor(value: Elem) : this(listOf(value))
    }

    @JvmInline
    value class EInt(val value: Int) : Elem {
        override fun compareTo(other: Elem): Int =
            when (other) {
                is EInt -> value.compareTo(other.value)
                is EList -> EList(this).compareTo(other)
            }
    }
}

private fun ListIterator<Char>.parseDigit(): Elem.EInt =
    buildString {
        while (true) {
            when (val char = next()) {
                in '0'..'9' -> append(char)
                else -> break
            }
        }
    }.let {
        previous()
        return Elem.EInt(it.toInt())
    }

private fun ListIterator<Char>.parseList(): Elem.EList {
    val items = mutableListOf<Elem>()
    next() shouldBe '['

    while (true) {
        when (val char = next()) {
            '[' -> {
                previous()
                items.add(parseList())
            }

            ']' -> return Elem.EList(items)
            ',' -> {}
            in '0'..'9' -> {
                previous()
                items.add(parseDigit())
            }

            else -> error("Unexpected character $char")
        }
    }
}

private fun String.parseInputPairs() =
    lines()
        .chunked(3)
        .map { chunk ->
            chunk[0].toList().listIterator().parseList() to
                    chunk[1].toList().listIterator().parseList()
        }

private fun String.part01(): Int =
    parseInputPairs()
        .mapIndexed { index, (left, right) ->
            if (left.compareTo(right) != 1) index + 1
            else 0
        }.sum()


private fun String.parseInputLines() =
    lineSequence()
        .filter { it.isNotBlank() }
        .map { line ->
            line.toList().listIterator().parseList()
        }

private fun String.part02(): Int =
    parseInputLines().let { input ->
        val marks = listOf(
            Elem.EList(Elem.EList(Elem.EInt(2))),
            Elem.EList(Elem.EList(Elem.EInt(6))),
        )
        (input + marks).sorted().toList().let { sorted ->
            marks.map { mark ->
                sorted.binarySearch(mark) + 1
            }.let { (f, s) -> f * s }
        }
    }

fun main() {
    testInput.part01() shouldBe PART_01_RES
    testInput.part02() shouldBe PART_02_RES

    val input = InputLoader.loadInput("y2022/day13/day13")
    println(input.part01())
    println(input.part02())
}

private val testInput = """
[1,1,3,1,1]
[1,1,5,1,1]

[[1],[2,3,4]]
[[1],4]

[9]
[[8,7,6]]

[[4,4],4,4]
[[4,4],4,4,4]

[7,7,7,7]
[7,7,7]

[]
[3]

[[[]]]
[[]]

[1,[2,[3,[4,[5,6,7]]]],8,9]
[1,[2,[3,[4,[5,6,0]]]],8,9]
""".trimIndent()

private const val PART_01_RES = 13
private const val PART_02_RES = 140
