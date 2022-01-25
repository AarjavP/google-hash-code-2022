package divine.brothers.pizza

import java.math.BigInteger
import java.util.*

class CandidateSetsApproach(input: Sequence<String>) : PizzaApproach(input) {
    data class CandidateSetsSolution(
        override val ingredients: BitSet,
        val customersSatisfied: Int,
        val customersSet: CustomersSet
    ): PizzaSolution

    class CustomersSet {
        val likes = BitSet()
        val dislikes = BitSet()
        val customers = BitSet()
        var size = 0

        fun offer(customer: Customer): Boolean {
            if (likes.intersects(customer.dislikes) || dislikes.intersects(customer.likes)) return false
            likes += customer.likes
            dislikes += customer.dislikes
            customers.set(customer.id.id)
            size++
            return true
        }
    }
    fun toValidCombinationOrNull(combination: BitSet): CustomersSet? {
        val currentSet = CustomersSet()
        for (index in combination.stream()) {
            val customer = customers[index]
            if (!currentSet.offer(customer)) return null
        }
        return currentSet
    }

    override fun findIngredients(): PizzaSolution {
        val topCombination = mostBitsSetOrder(customers.size).mapNotNull {
            val combination = it.toBitSet()
            toValidCombinationOrNull(combination)
        }.first()

        return CandidateSetsSolution(topCombination.likes, topCombination.size, topCombination)
    }

}

/**
 * Converts from BigInteger to BitSet. The BigInteger must be non-negative,
 * otherwise an IllegalArgumentException is thrown.
 */
fun BigInteger.toBitSet(): BitSet = BitSet.valueOf(toByteArray().apply { reverse() })

// http://graphics.stanford.edu/~seander/bithacks.html#NextBitPermutation
fun mostBitsSetOrder(count: Int): Sequence<BigInteger> = sequence {
    val maxNumber = BigInteger.ONE.shiftLeft(count)
    for (k in count downTo 1) {
        println("new k = $k")
        var currentNumber = BigInteger.ONE.shiftLeft(k) - BigInteger.ONE
        while(currentNumber < maxNumber) {
            yield(currentNumber)
            val temp = currentNumber.or(currentNumber - BigInteger.ONE)
            currentNumber = (temp + BigInteger.ONE).or(
                temp.not().and(temp.not().negate()).minus(BigInteger.ONE).shiftRight(currentNumber.lowestSetBit + 1)
            )
        }
    }
}

operator fun BitSet.plusAssign(other: BitSet) = this.or(other)
