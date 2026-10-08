package io.tashtabash.sim.space


class Region(val x: Int, val y: Int, val sizeX: Int, val sizeY: Int) {
    init {
        require(sizeX > 0 && sizeY > 0) { "Region must have cells, got ${sizeX}x$sizeY" }
    }

    operator fun div(factor: Int) =
        Region(x / factor, y / factor, sizeX / factor, sizeY / factor)

    override fun toString() =
        "$x $y ${sizeX}x$sizeY"
}
