package day06

import InputLoader
import kotlinx.collections.immutable.persistentListOf

private const val TAG_LENGTH = 4

private fun String.part01() : Int {
    var queue = persistentListOf<Char>()

    forEachIndexed { index, _ ->
        val c = get(index)
        queue = queue.add(c)

        if (queue.toSet().size == TAG_LENGTH)
            return index + 1

        if (queue.size == 4)
            queue = queue.removeAt(0)
    }

    return -1
}

fun main() {
    testInputs.forEach {
        require(it.first.part01() == it.second)
    }
    val input = InputLoader.loadInput("day06")
    println(input.part01())
}

private val testInputs = listOf(
    "mjqjpqmgbljsphdztnvjfqwrcgsmlb" to 7,
    "bvwbjplbgvbhsrlpgdmjqwftvncz" to 5,
    "nppdvjthqldpwncqszvftbrmjlhg" to 6,
    "nznrnfrfntjfmvfwmzdfjlvtqnbhcprsg" to 10,
    "zcfzfwzzqfrljwzlrfnpqdbhtmscgvjw" to 11,
)
