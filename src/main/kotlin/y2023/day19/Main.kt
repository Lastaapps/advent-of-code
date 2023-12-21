package y2023.day19

import InputLoader
import Year
import io.kotest.matchers.shouldBe

private data class Command(
    val index: Int,
    val gt: Boolean,
    val threshold: Int,
    val goto: String,
)

private data class Input(
    val commands: List<List<Command>>,
    val translate: Map<String, Int>,
    val items: Sequence<List<Int>>,
)

private const val ACCEPTED = -1
private const val REJECTED = -2

private fun String.parseInput(): Input {
    val (conditions, items) = split("\n\n".toRegex(), limit = 2)

    val translate = hashMapOf(
        "A" to ACCEPTED,
        "R" to REJECTED,
    )

    val commands = conditions.lineSequence().mapIndexed() { index, line ->
        val split = line.indexOfFirst { it == '{' }

        line.substring(0, split).let { id ->
            translate[id] = index
        }

        line.substring(split + 1, line.length - 1)
            .splitToSequence(',').map { condition ->
                val parts = condition.split(':')
                if (parts.size == 1) {
                    Command(0, true, -1, condition)
                } else {
                    val (text, id) = parts
                    val type = arrayOf('x', 'm', 'a', 's').indexOf(text.first())
                    val gt = text[1] == '>'
                    val threshold = text.substring(2).toInt()
                    Command(type, gt, threshold, id)
                }
            }.toList()
    }.toList()

    val details = items.lineSequence().map { line ->
        """^\{x=(\d+),m=(\d+),a=(\d+),s=(\d+)}$""".toRegex()
            .find(line)!!.groupValues
            .asSequence()
            .drop(1)
            .map { it.toInt() }
            .toList()
    }

    return Input(commands, translate, details)
}

private fun String.part01(): Int {
    val (commandsMap, translate, items) = parseInput()

    val start = translate["in"]!!
    return items.sumOf { item ->
        var line = start
        do {
            val commands = commandsMap[line]
            val applies = commands.first {
                val diff = item[it.index] - it.threshold
                diff != 0 && diff > 0 == it.gt
            }

            line = translate[applies.goto]!!
        } while (line >= 0)

        when (line) {
            ACCEPTED -> item.sum()
            REJECTED -> 0
            else -> error("Wrong calculations")
        }
    }
}

private fun String.part02(): Int = PART_02_TEST

fun main() {
    testInput.part01() shouldBe PART_01_TEST
    testInput.part02() shouldBe PART_02_TEST

    val input = InputLoader.loadInput(Year.Y2023, "day19")
    input.part01()
        .also { println(it) }
        .also { it shouldBe PART_01_PROD }
    input.part02()
        .also { println(it) }
        .also { it shouldBe PART_02_PROD }
}

private val testInput = """
px{a<2006:qkq,m>2090:A,rfg}
pv{a>1716:R,A}
lnx{m>1548:A,A}
rfg{s<537:gd,x>2440:R,A}
qs{s>3448:A,lnx}
qkq{x<1416:A,crn}
crn{x>2662:A,R}
in{s<1351:px,qqz}
qqz{s>2770:qs,m<1801:hdj,R}
gd{a>3333:R,R}
hdj{m>838:A,pv}

{x=787,m=2655,a=1222,s=2876}
{x=1679,m=44,a=2067,s=496}
{x=2036,m=264,a=79,s=2244}
{x=2461,m=1339,a=466,s=291}
{x=2127,m=1623,a=2188,s=1013}
""".trimIndent()

private const val PART_01_TEST = 19114
private const val PART_01_PROD = 449531
private const val PART_02_TEST = 0
private const val PART_02_PROD = 0
