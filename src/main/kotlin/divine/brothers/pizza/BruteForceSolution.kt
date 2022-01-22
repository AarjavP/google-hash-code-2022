package divine.brothers.pizza

import com.google.common.collect.Sets

class BruteForceSolution(input: Sequence<String>) : PizzaSolution(input) {
    override fun findIngredients(): Set<Ingredient> {
        val results = Sets.powerSet(ingredientAliases.values).asSequence().map {
            it to customers.count { customer -> it.isCompatibleWith(customer) }
        }.filter { it.second > 1 }.sortedByDescending { it.second }

        val topIngredients = results.takeWhile { it.second == results.first().second }

        for (result in topIngredients) {
            println("customers: ${result.second}  ingredients: ${result.first}")
        }

        return topIngredients.first().first
    }
}
