package shmp.simulation.culture.thinking.meaning

import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors


open class Meme(observerWord: String, var predicates: MutableList<Meme> = mutableListOf(), open var importance: Int = 1) {
    var observerWord: String = observerWord.toLowerCase()

    val isSimple: Boolean
        get() = predicates.isEmpty()

    fun increaseImportance(delta: Int) {
        importance += delta

        if (importance < 0)
            importance = Int.MAX_VALUE
    }

    fun addPredicate(predicate: Meme): Meme {
        predicates.add(predicate)
        return this
    }

    fun splitOn(splitters: Collection<String?>): List<Meme> {
        val memes: MutableList<Meme> = ArrayList()
        val newMemes: Queue<Meme> = ArrayDeque()
        val copy = copy()
        memes.add(copy)
        newMemes.add(copy)
        while (!newMemes.isEmpty()) {
            val current = newMemes.poll()
            if (splitters.contains(current.observerWord)) {
                memes.addAll(current.predicates)
                continue
            }
            val children: MutableList<Meme> = current.predicates
            var i = 0
            while (i < children.size) {
                val child = children[i]
                if (splitters.contains(child.observerWord)) {
                    children.removeAt(i)
                    i--
                    memes.addAll(child.predicates)
                }
                newMemes.addAll(child.predicates)
                i++
            }
        }
        return memes.stream()
                .distinct()
                .collect(Collectors.toList())
    }

    fun refactor(mapper: Function<Meme, Meme>): Meme {
        val dummy = Meme("dummy").addPredicate(copy())
        val queue: Queue<Meme> = ArrayDeque()
        queue.add(dummy)
        while (!queue.isEmpty()) {
            val current = queue.poll()
            val predicates = current.predicates
            for (i in predicates.indices) {
                val child = predicates[i]
                val substitution = mapper.apply(child)
                child.predicates.forEach(Consumer { predicate: Meme -> substitution.addPredicate(predicate) })
                predicates[i] = substitution
            }
            queue.addAll(predicates)
        }
        return dummy.predicates[0]
    }

    fun anyMatch(predicate: Predicate<Meme>): Boolean {
        val queue: Queue<Meme> = ArrayDeque()
        queue.add(this)
        while (!queue.isEmpty()) {
            val current = queue.poll()
            if (predicate.test(current)) {
                return true
            }
            queue.addAll(current.predicates)
        }
        return false
    }

    fun hasPart(that: Meme, splitters: Collection<String?>): Boolean {
        val thatMemes: Collection<Meme> = that.splitOn(splitters)
        return splitOn(splitters).stream().anyMatch { thatMemes.contains(it) }
    }

    override fun toString() = observerWord +
            if (predicates.isNotEmpty())
                predicates.joinToString(" ", " ")
            else ""

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val meme = other as Meme
        return toString() == meme.toString()
    }

    override fun hashCode() = Objects.hash(toString())

    open fun copy(): Meme {
        return Meme(
                observerWord,
                predicates.map { it.copy() }.toMutableList(),
                importance
        )
    }

    open fun topMemeCopy() = Meme(observerWord)
}
