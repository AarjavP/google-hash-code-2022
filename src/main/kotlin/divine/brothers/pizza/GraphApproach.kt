package divine.brothers.pizza

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jgrapht.Graph
import org.jgrapht.GraphType
import org.jgrapht.alg.util.Pair
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DefaultGraphType
import org.jgrapht.graph.SimpleGraph
import org.jgrapht.sux4j.SuccinctUndirectedGraph
import java.time.Duration
import java.util.*
import kotlin.system.measureTimeMillis

class GraphApproach(input: Sequence<String>): PizzaApproach(input) {

    data class GraphSolution(
        override val ingredients: BitSet,
        val customersSatisfied: Int,
        val customers: Collection<Customer>
    ): PizzaSolution

    // nodes are customer (id), and there exists an edge between nodes if the two are 'compatible'
    fun createCustomerGraph(): SuccinctUndirectedGraph? {
        val customerGraph: SuccinctUndirectedGraph
        val graphCreationTime = measureTimeMillis {
            val edges = LinkedList<Pair<Int, Int>>()
            runBlocking {
                val mutex = Mutex()
                var edgesAdded = 0
                for ((index, customer) in customers.withIndex()) {
                    launch(context = Dispatchers.Default) {
                        for (other in customers.subList(index + 1, customers.size)) {
                            if (!customer.isCompatibleWith(other)) {
                                val otherId = other.id.id
                                mutex.withLock {
                                    edgesAdded++
                                    if (edgesAdded % 100_000 == 0) {
                                        println("added $edgesAdded edges on ${Thread.currentThread().name}")
                                    }
                                    edges.add(Pair(index, otherId))
                                }
                            }
                        }
                    }
                }

            }
            if (edges.isEmpty()) return null
            customerGraph = object : SuccinctUndirectedGraph(customers.size, edges) {
                override fun getType(): GraphType? {
                    return DefaultGraphType.Builder()
                        .undirected().weighted(false).modifiable(false).allowMultipleEdges(false)
                        .allowSelfLoops(true).build()
                }
            }
        }.let { Duration.ofMillis(it) }

        println("graph created in $graphCreationTime")
        return customerGraph
    }

    fun createCustomerGraph2(): Graph<Int, DefaultEdge> {
        val customerGraph = SimpleGraph<Int, DefaultEdge>(DefaultEdge::class.java)
        val graphCreationTime = measureTimeMillis {
            runBlocking {
                for (index in customers.indices) {
                    customerGraph.addVertex(index)
                }
                val mutex = Mutex()
                var edgesAdded = 0
                for ((index, customer) in customers.withIndex()) {
                    launch(context = Dispatchers.Default) {
                        for (other in customers.subList(index + 1, customers.size)) {
                            if (!customer.isCompatibleWith(other)) {
                                val otherId = other.id.id
                                mutex.withLock {
                                    edgesAdded++
                                    if (edgesAdded % 100_000 == 0) {
                                        println("added $edgesAdded edges on ${Thread.currentThread().name}")
                                    }
                                    customerGraph.addEdge(index, otherId)
                                }
                            }
                        }
                    }
                }
            }
        }.let { Duration.ofMillis(it) }

        println("graph created in $graphCreationTime")
        return customerGraph
    }

    override fun findIngredients(): PizzaSolution {
        val customerGraph = createCustomerGraph() ?: return GraphSolution(
            ingredients = BitSet().apply { set(0, ingredientAliases.size) },
            customersSatisfied = customers.size,
            customers = customers
        )

        val finalCustomers = run {
            val independentSet = BitSet()
            val seenCustomers = BitSet()
            val customersByDegree = customers.indices.map { it to customerGraph.degreeOf(it) }.sortedBy { it.second }
            for ((customerId, _) in customersByDegree) {
                if (seenCustomers.get(customerId)) continue
                independentSet.set(customerId)
                seenCustomers.set(customerId)
                for (edge in customerGraph.edgesOf(customerId)) {
                    val other = if (edge.firstInt() == customerId) edge.secondInt() else edge.firstInt()
                    seenCustomers.set(other)
                }
            }

            independentSet.toCustomers()
        }

        val finalIngredients = BitSet()
        for (customer in finalCustomers) {
            finalIngredients += customer.likes
        }

        return GraphSolution(finalIngredients, finalCustomers.size, finalCustomers)
    }

}

operator fun BitSet.plusAssign(indexes: Iterable<Int>) {
    for (i in indexes) set(i)
}
operator fun BitSet.plusAssign(i: Int) = set(i)
operator fun BitSet.contains(i: Int) = get(i)
