package y2022.day07

import InputLoader
import Year
import io.kotest.matchers.shouldBe

private const val SMALL_DIR_THRESHOLD = 100_000
private const val DISK_SIZE = 70_000_000
private const val FREE_REQUIRED = 30_000_000

sealed interface DirType {
    object Root : DirType
    object Parent : DirType
    data class Child(val name: String) : DirType
}

sealed interface TerminalOut {
    object LS : TerminalOut
    data class CD(val type: DirType) : TerminalOut
    data class File(val name: String, val size: Long) : TerminalOut
    data class Dir(val name: String) : TerminalOut
}

private fun String.toDirType(): DirType =
    when (this) {
        "/" -> DirType.Root
        ".." -> DirType.Parent
        else -> DirType.Child(this)
    }


private fun String.parseToCommands(): List<TerminalOut> =
    lines().map { line ->
        when {
            line.startsWith("\$ ls") -> TerminalOut.LS
            line.startsWith("\$ cd") -> TerminalOut.CD(line.drop(5).toDirType())
            line.startsWith("dir ") -> TerminalOut.Dir(line.drop(4))
            else -> line.split(" ").let { (size, name) ->
                TerminalOut.File(name, size.toLong())
            }
        }
    }

sealed interface Node {
    val name: String

    data class File(override val name: String, val size: Long) : Node
    data class Dir(override val name: String, val items: List<Node>) : Node
}

private fun ListIterator<TerminalOut>.readFiles(): List<Node> {
    val list = mutableListOf<Node>()
    while (true) {
        if (!hasNext()) return list

        when (val item = next()) {
            is TerminalOut.CD,
            is TerminalOut.LS,
            -> {
                previous()
                return list
            }

            is TerminalOut.Dir -> {
                if (list.none { item.name == it.name }) {
                    list += Node.Dir(item.name, emptyList())
                }
            }

            is TerminalOut.File -> {
                list += Node.File(item.name, item.size)
            }
        }
    }
}

private fun ListIterator<TerminalOut>.processCommands(
    starting: List<Node>,
    isRoot: Boolean,
): List<Node> {
    val nodes = starting.toMutableList()

    while (hasNext()) {

        when (val item = next()) {
            is TerminalOut.CD -> {
                when (val type = item.type) {
                    is DirType.Child -> {
                        val name = type.name
                        val prev = (nodes.firstOrNull { it.name == name } as? Node.Dir)?.items
                        val childes = processCommands(prev ?: emptyList(), false)
                        val index = nodes.indexOfFirst { it.name == name }
                        nodes[index] = Node.Dir(name, childes)
                    }

                    DirType.Parent -> {
                        return nodes
                    }

                    DirType.Root -> {
                        if (!isRoot) {
                            previous()
                            return nodes
                        }
                    }
                }
            }

            TerminalOut.LS -> {
                val children = readFiles()
                nodes.addAll(children)
            }

            is TerminalOut.Dir,
            is TerminalOut.File,
            -> error("Unexpected file detected!")
        }
    }

    return nodes
}

private fun List<TerminalOut>.processCommands() =
    Node.Dir("/", listIterator().processCommands(emptyList(), true))

private fun Node.Dir.filterSmallDirsSumImpl(acu: Array<Long>) : Long {
    val res = items.fold(0L) { a, item ->
        a + when(item) {
            is Node.Dir ->
                item.filterSmallDirsSumImpl(acu)

            is Node.File ->
                item.size
        }
    }
    if (res <= SMALL_DIR_THRESHOLD) {
        acu[0] += res
    }
    return res
}

private fun Node.Dir.filterSmallDirsSum() : Long =
    arrayOf(0L).also { filterSmallDirsSumImpl(it) }[0]

private fun String.part01(): Long =
    this
        .parseToCommands()
        .processCommands()
        // .also { it.printTree() }
        .filterSmallDirsSum()




private fun Node.Dir.dirSize() : Long =
    items.fold(0L) { a, item ->
        a + when(item) {
            is Node.Dir ->
                item.dirSize()

            is Node.File ->
                item.size
        }
    }

private fun Node.Dir.findToDelete(threshold: Long, acu: Array<Long>) : Long {
    val res = items.fold(0L) { a, item ->
        a + when(item) {
            is Node.Dir ->
                item.findToDelete(threshold, acu)

            is Node.File ->
                item.size
        }
    }
    if (res >= threshold && res < acu[0]) {
        acu[0] = res
    }
    return res
}

private fun Node.Dir.findToDelete(threshold: Long) : Long =
    arrayOf(Long.MAX_VALUE).also { findToDelete(threshold, it) }[0]


private fun String.part02(): Long =
    this
        .parseToCommands()
        .processCommands()
        .let { commands ->
            val threshold = commands.dirSize()
            .let { used -> FREE_REQUIRED - (DISK_SIZE - used)}

            commands.findToDelete(threshold)
        }


private fun Node.printTree(offset: Int = 0) {
    print("".padStart(offset) + "- ")
    when(this) {
        is Node.Dir -> {
            println("$name (dir)")
            items.forEach { it.printTree(offset + 2) }
        }
        is Node.File -> {
            println("$name (file, size=$size)")
        }
    }
}

fun main() {
    testInput.part01() shouldBe TEST_RES_PART_01
    testInput.part02() shouldBe TEST_RES_PART_02

    val input = InputLoader.loadInput(Year.Y2022, "day07")
    println(input.part01())
    println(input.part02())
}

private val testInput = """
${'$'} cd /
${'$'} ls
dir a
14848514 b.txt
8504156 c.dat
dir d
${'$'} cd a
${'$'} ls
dir e
29116 f
2557 g
62596 h.lst
${'$'} cd e
${'$'} ls
584 i
${'$'} cd ..
${'$'} cd ..
${'$'} cd d
${'$'} ls
4060174 j
8033020 d.log
5626152 d.ext
7214296 k   
""".trimIndent()

private const val TEST_RES_PART_01 = 95437L
private const val TEST_RES_PART_02 = 24933642L
