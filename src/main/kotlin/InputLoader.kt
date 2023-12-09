enum class Year(val year: Int) {
    Y2022(2022),
    Y2023(2023),
}

object InputLoader {
    fun loadInput(year: Year, day: String): String = run {
        InputLoader.javaClass.getResource("/${year.year}/$day.txt")!!.readText()
    }
}