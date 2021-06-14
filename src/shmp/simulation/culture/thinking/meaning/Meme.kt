package shmp.simulation.culture.thinking.meaning

import java.util.*


open class Meme(observerWord: String, var predicates: List<Meme> = listOf(), open var importance: Int = 1) {
    var observerWord: String = observerWord.toLowerCase()

    val isSimple: Boolean
        get() = predicates.isEmpty()

    fun increaseImportance(delta: Int) {
        importance += delta

        if (importance < 0)
            importance = Int.MAX_VALUE
    }

    fun addPredicate(predicate: Meme) = Meme(observerWord, predicates + listOf(predicate), importance)

    fun refactor(mapper: (Meme) -> Meme): Meme {
        var newMeme = mapper(this)
        if (newMeme.predicates.isNotEmpty()) {
            newMeme = newMeme.refactor(mapper)
        }

        predicates.forEach {
            val newPredicate = it.refactor(mapper)
            newMeme = newMeme.addPredicate(newPredicate)
        }

        return newMeme
    }

    fun anyMatch(predicate: (Meme) -> Boolean): Boolean {
        val queue: Queue<Meme> = ArrayDeque()
        queue.add(this)
        while (!queue.isEmpty()) {
            val current = queue.poll()
            if (predicate(current))
                return true
            queue.addAll(current.predicates)
        }
        return false
    }

    fun contains(string: String) = toString().contains(string.toLowerCase())

    override fun toString() = string

    private val string = this.observerWord +
            if (predicates.isNotEmpty())
                predicates.joinToString(" ", " ")
            else ""

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is Meme) return false


        return toString() == other.toString()
    }

    override fun hashCode() = Objects.hash(toString())

    open fun copy(): Meme = Meme(
            observerWord,
            predicates.map { it.copy() }.toMutableList(),
            importance
    )

    open fun topMemeCopy() = Meme(observerWord)
}
