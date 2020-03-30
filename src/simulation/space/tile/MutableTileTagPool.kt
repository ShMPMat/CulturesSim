package simulation.space.tile

class MutableTileTagPool(private val tags: MutableSet<TileTag> = mutableSetOf()) {
    val size: Int
        get() = tags.size

    fun contains(tag: TileTag) = tags.contains(tag)

    fun getByType(type: String) = tags.filter { it.type == type }

    fun add(tag: TileTag) = tags.add(tag)

    fun remove(tag: TileTag) = tags.remove(tag)
}