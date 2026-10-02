package io.tashtabash.sim.space.tile


// Where a neighbour lies relative to a Tile
enum class Direction(val dx: Int, val dy: Int) {
    XPlus(1, 0),
    XMinus(-1, 0),
    YPlus(0, 1),
    YMinus(0, -1),
    Here(0, 0);

    val opposite: Direction
        get() = when (this) {
            XPlus -> XMinus
            XMinus -> XPlus
            YPlus -> YMinus
            YMinus -> YPlus
            Here -> Here
        }

    companion object {
        // W/o Here
        val sides = arrayOf(XPlus, XMinus, YPlus, YMinus)

        fun of(dx: Int, dy: Int): Direction? = when {
            dx == 1 && dy == 0 -> XPlus
            dx == -1 && dy == 0 -> XMinus
            dx == 0 && dy == 1 -> YPlus
            dx == 0 && dy == -1 -> YMinus
            dx == 0 && dy == 0 -> Here
            else -> null
        }
    }
}
