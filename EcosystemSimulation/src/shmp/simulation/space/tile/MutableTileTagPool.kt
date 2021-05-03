package shmp.simulation.space.tile


class MutableTileTagPool(private val tags: MutableSet<TileTag> = mutableSetOf()) {
    val size: Int
        get() = tags.size

    val all: Set<TileTag>
        get() = tags

    fun contains(tag: TileTag) = tags.contains(tag)

    fun getByType(type: String) = tags.filter { it.type == type }

    fun getByTypeSubstring(substring: String) = tags.filter { it.type.contains(substring) }

    fun add(tag: TileTag) = tags.add(tag)

    fun remove(tag: TileTag) = tags.remove(tag)
}
