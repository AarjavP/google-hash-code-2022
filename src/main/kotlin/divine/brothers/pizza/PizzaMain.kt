package divine.brothers.pizza

import divine.brothers.readFromClasspath

fun main() {
    val files = listOf(
        "a_an_example.in.txt",
        "b_basic.in.txt",
        "c_coarse.in.txt",
        "d_difficult.in.txt",
        "e_elaborate.in.txt",
    )

    for (file in files) {
        println("Attempting Solution for $file...")
        val solution = readFromClasspath(file).useLines(::BruteForceSolution)
        solution.findIngredients()
        println("---------------------------------")
    }
}


