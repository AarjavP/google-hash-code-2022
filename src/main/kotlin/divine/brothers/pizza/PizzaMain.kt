package divine.brothers.pizza

import divine.brothers.readFromClasspath
import java.io.File

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
        val approach = readFromClasspath(file).useLines(::GraphApproach)
        try {
            val solution = approach.findIngredients()
            val ingredients = mutableListOf<String>()
            val idToIngredient = approach.ingredientAliases.inverse()
            for (ingredientId in solution.ingredients.stream()) {
                ingredients += idToIngredient[Ingredient(ingredientId)]!!
            }

            val outFile = File(file.replace(".in.txt", ".out.txt"))
            val output = "${ingredients.size} ${ingredients.joinToString(" ")}"
            outFile.writeText(output)
        } catch (e: Exception) {
            println("error: ${e.message}")
        }
        println("---------------------------------")
    }
}


