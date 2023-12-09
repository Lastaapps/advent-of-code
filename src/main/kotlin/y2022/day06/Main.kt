package y2022.day06

import InputLoader
import Year
import kotlinx.collections.immutable.persistentListOf

private fun String.findMarker(length: Int) : Int {
    var queue = persistentListOf<Char>()

    forEachIndexed { index, _ ->
        val c = get(index)
        queue = queue.add(c)

        if (queue.toSet().size == length)
            return index + 1

        if (queue.size == length)
            queue = queue.removeAt(0)
    }

    return -1
}

private fun String.part01() : Int =
    findMarker(4)

private fun String.part02() : Int =
    findMarker(14)

fun main() {
    testInputs.forEach {
        require(it.first.part01() == it.second.first)
        require(it.first.part02() == it.second.second)
    }

    val input = InputLoader.loadInput(Year.Y2022, "day06")
    println(input.part01())
    println(input.part02())
}

private val testInputs = listOf(
    "mjqjpqmgbljsphdztnvjfqwrcgsmlb" to (7 to 19),
    "bvwbjplbgvbhsrlpgdmjqwftvncz" to (5 to 23),
    "nppdvjthqldpwncqszvftbrmjlhg" to (6 to 23),
    "nznrnfrfntjfmvfwmzdfjlvtqnbhcprsg" to (10 to 29),
    "zcfzfwzzqfrljwzlrfnpqdbhtmscgvjw" to (11 to 26),
)
