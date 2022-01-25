package divine.brothers.pizza

import com.google.common.collect.Sets
import divine.brothers.takeUntilChanged
import java.util.*

class BruteForceApproach(input: Sequence<String>) : PizzaApproach(input) {

    data class BruteForceSolution(
        override val ingredients: BitSet,
        val topCombinations: List<Combination>
    ): PizzaSolution

    fun BitSet.isCompatibleWith(customer: Customer): Boolean {
        return !intersects(customer.dislikes) && customer.likes.stream().allMatch { contains(it) }
    }

    data class Combination(val ingredients: BitSet, val customersSatisfied: Int)
    override fun findIngredients(): PizzaSolution {
        val topCombinations = Sets.powerSet(ingredientAliases.values).asSequence().mapNotNull { combo ->
            val set = BitSet()
            for (ingredient in combo) {
                set.set(ingredient.id)
            }
            Combination(set, customers.count { customer -> set.isCompatibleWith(customer) }).takeIf {
                it.customersSatisfied > 0
            }
        }.sortedByDescending { it.customersSatisfied }.takeUntilChanged { customersSatisfied }.toList()

        return BruteForceSolution(topCombinations.first().ingredients, topCombinations)
    }
}
