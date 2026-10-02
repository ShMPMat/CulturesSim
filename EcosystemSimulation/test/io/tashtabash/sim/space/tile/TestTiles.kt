package io.tashtabash.sim.space.tile

import io.tashtabash.sim.space.Scale
import io.tashtabash.sim.space.SpaceData.data
import java.util.concurrent.atomic.AtomicInteger


private val nextTestTileId = AtomicInteger()

fun createTestTile(
    x: Int = 0,
    y: Int = 0,
    scale: Scale = data.defaultScale,
    area: Double = scale.tileAreaKm2,
    id: Int = nextTestTileId.getAndIncrement()
) = Tile(id, x, y, scale = scale, area = area)
