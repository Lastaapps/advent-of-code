package y2022.day05

import y2022.InputLoader
import java.util.*

data class Operation(
    val count: Int,
    val from: Int,
    val to: Int,
)

typealias InputRow = List<Char?>

private fun String.parseInput(): Pair<List<InputRow>, List<Operation>> {
    val separator = lineSequence().indexOfFirst { it.isBlank() }

    val stacks = lineSequence().take(separator - 1)
        .map { line ->
            line.chunked(4)
        }
        .map { chunks ->
            chunks.map { chunk ->
                chunk[1].takeIf { it.isLetter() }
            }
        }.toList()

    val instructions = lineSequence().drop(separator + 1)
        .map { line ->
            line.split(" ").filter { it.first().isDigit() }.map { it.toInt() }
        }
        .map { (count, from, to) ->
            Operation(count, from - 1, to - 1)
        }.toList()

    return stacks to instructions
}

fun List<InputRow>.toStacks(): List<Stack<Char>> {
    val stacks = last().map { Stack<Char>() }

    reversed().forEach { row ->
        row.forEachIndexed { index, payload ->
            payload?.let {
                stacks[index] += it
            }
        }
    }

    return stacks
}

fun List<Stack<Char>>.applyOperation(operation: Operation) {
    repeat(operation.count) {
        this[operation.to] += this[operation.from].peek()
        this[operation.from].pop()
    }
}

fun String.part01() : String {
    val (stackInput, operations) = parseInput()
    val stacks = stackInput.toStacks()

    operations.forEach {
        stacks.applyOperation(it)
    }

    return stacks.map { it.peek() }.joinToString("")
}

fun List<Stack<Char>>.applyAdvancedOperation(operation: Operation) {
    val tmpStack = Stack<Char>()
    repeat(operation.count) {
        tmpStack += this[operation.from].peek()
        this[operation.from].pop()
    }
    repeat(operation.count) {
        this[operation.to] += tmpStack.peek()
        tmpStack.pop()
    }
}

fun String.part02() : String {
    val (stackInput, operations) = parseInput()
    val stacks = stackInput.toStacks()

    operations.forEach {
        stacks.applyAdvancedOperation(it)
    }

    return stacks.map { it.peek() }.joinToString("")
}

fun main() {
    listOf(
        TEST_INPUT,
        InputLoader.loadInput("y2022/day05/day05"),
    ).forEach { input ->
        println(input.part01())
        println(input.part02())
    }
}

private val TEST_INPUT = """
        [D]    
    [N] [C]    
    [Z] [M] [P]
     1   2   3 
    
    move 1 from 2 to 1
    move 3 from 1 to 3
    move 2 from 2 to 1
    move 1 from 1 to 2""".trimIndent()
