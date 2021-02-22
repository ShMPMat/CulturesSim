package shmp.utils.labeler


class ConcatLabeler<T>(val labelers: List<Labeler<T>>) : AbstractLabeler<T>() {
    constructor(vararg labelers: Labeler<T>): this(labelers.toList())

    override fun matches(sample: T) = labelers.all { it.matches(sample) }

    override fun explanation() = labelers.joinToString(", and ")
}
