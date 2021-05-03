package shmp.utils.labeler


interface Labeler<T> {
    fun matches(sample: T): Boolean

    fun takeIfMatches(sample: T) = sample.takeIf { matches(it) }

    fun explanation(): String
}

abstract class AbstractLabeler<T>: Labeler<T> {
    override fun toString() = explanation()
}

