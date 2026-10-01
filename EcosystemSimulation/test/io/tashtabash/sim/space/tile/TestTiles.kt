package io.tashtabash.sim.space.tile

import io.tashtabash.sim.space.Scale
import io.tashtabash.sim.space.SpaceData.data


fun createTestTile(x: Int = 0, y: Int = 0, scale: Scale = data.defaultScale) =
    Tile(x, y, scale = scale)
