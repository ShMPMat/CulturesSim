package shmp.utils.labeler


class DisjunctionLabeler<T>(val labelers: List<Labeler<T>>) : AbstractLabeler<T>() {
    constructor(vararg labelers: Labeler<T>): this(labelers.toList())

    override fun matches(sample: T) = labelers.any { it.matches(sample) }

    override fun explanation() = labelers.joinToString(", or ", "(", ")")
}
