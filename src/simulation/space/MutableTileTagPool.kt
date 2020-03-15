package simulation.space

class MutableTileTagPool(private val tags: MutableSet<TileTag> = mutableSetOf()) {
    fun contains(tag: TileTag) = tags.contains(tag)

    fun remove(tag: TileTag) = tags.remove(tag)
}