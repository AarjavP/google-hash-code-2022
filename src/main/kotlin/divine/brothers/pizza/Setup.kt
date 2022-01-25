package divine.brothers.pizza

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import java.util.*
import java.util.stream.Collectors

@JvmInline
value class Ingredient(val id: Int) {
    override fun toString(): String = id.toString()
}

@JvmInline
value class CustomerId(val id: Int)

data class Customer(
    val id: CustomerId,
    val likes: BitSet,
    val dislikes: BitSet,
) {
    fun isCompatibleWith(other: Customer): Boolean {
        return !(likes.intersects(other.dislikes) || other.likes.intersects(dislikes))
    }
}

abstract class PizzaApproach(input: Sequence<String>) {

    interface PizzaSolution {
        val ingredients: BitSet
    }

    val ingredientAliases: BiMap<String, Ingredient> = HashBiMap.create()
    val customers: List<Customer>

    init {
        var aliasCounter = 0
        fun registerIngredient(original: String): Ingredient {
            return ingredientAliases.computeIfAbsent(original) { Ingredient(aliasCounter++) }
        }
        fun String.parseIngredients(): BitSet {
            return substring(2).splitToSequence(' ').fold(BitSet()) { set, ingredient ->
                set.set(registerIngredient(ingredient).id)
                set
            }
        }

        val customers = mutableListOf<Customer>()
        for ((index, customerPrefs) in input.drop(1).chunked(2).withIndex()) {
            val likes = customerPrefs[0].parseIngredients()
            val dislikes = customerPrefs[1].takeIf { it != "0" }?.parseIngredients() ?: BitSet(0)
            customers += Customer(id = CustomerId(index), likes = likes, dislikes = dislikes)
        }
        this.customers = customers
    }

    fun BitSet.toIngredients(): Set<Ingredient> = stream().mapToObj { Ingredient(it) }.collect(Collectors.toSet())
    fun BitSet.toCustomers(): List<Customer> = mutableListOf<Customer>().also { for (i in stream()) it.add(customers[i]) }

    abstract fun findIngredients(): PizzaSolution
}
