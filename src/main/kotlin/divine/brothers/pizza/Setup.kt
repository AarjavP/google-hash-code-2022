package divine.brothers.pizza

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import divine.brothers.containsAny

@JvmInline
value class Ingredient(val id: Int) {
    override fun toString(): String = id.toString()
}

fun Set<Ingredient>.isCompatibleWith(customer: Customer): Boolean {
    return containsAll(customer.likes) && customer.dislikes.none { it in this }
}

@JvmInline
value class CustomerId(val id: Int)

data class Customer(
    val id: CustomerId,
    val likes: Set<Ingredient>,
    val dislikes: Set<Ingredient>,
) {
    fun isCompatibleWith(other: Customer): Boolean {
        return !likes.containsAny(other.dislikes) && !other.likes.containsAny(dislikes)
    }
}

abstract class PizzaSolution(input: Sequence<String>) {

    val ingredientAliases: BiMap<String, Ingredient> = HashBiMap.create()
    val customers: List<Customer>
    init {
        var aliasCounter = 1
        fun registerIngredient(original: String): Ingredient {
            return ingredientAliases.computeIfAbsent(original) { Ingredient(aliasCounter++) }
        }
        fun String.parseIngredients(): Set<Ingredient> {
            return substring(2).splitToSequence(' ').mapTo(mutableSetOf()) {
                registerIngredient(it)
            }
        }

        val customers = mutableListOf<Customer>()
        for ((index, customerPrefs) in input.drop(1).chunked(2).withIndex()) {
            val likes = customerPrefs[0].parseIngredients()
            val dislikes = customerPrefs[1].takeIf { it != "0" }?.parseIngredients() ?: emptySet()

            customers += Customer(id = CustomerId(index), likes = likes, dislikes = dislikes)
        }
        this.customers = customers
    }

    abstract fun findIngredients(): Set<Ingredient>
}
