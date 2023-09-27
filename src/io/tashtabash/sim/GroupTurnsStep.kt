package io.tashtabash.sim

import io.tashtabash.random.singleton.randomTile
import io.tashtabash.sim.culture.group.GROUP_TAG_TYPE
import io.tashtabash.sim.interactionmodel.InteractionModel
import io.tashtabash.sim.space.tile.Tile


class GroupTurnsStep<E : CulturesWorld>(
        private val turnNumber: Int,
        private val debugPrint: Boolean,
) : ControllerInitStep<E> {
    override fun run(world: E, interactionModel: InteractionModel<E>) {
        for (i in 0 until CulturesController.session.startGroupAmount)
            world.addConglomerate(chooseTileForGroup(world))

        for (i in 1..turnNumber) {
            interactionModel.turn(world)
            if (debugPrint)
                Controller.visualizer.print()
        }
    }

    private fun chooseTileForGroup(world: E): Tile {
        while (true) {
            val tile = world.map.randomTile()
            if (tile.tagPool.getByType(GROUP_TAG_TYPE).isEmpty() && tile.type !in deprecatedTileTypes)
                return tile
        }
    }

    private val deprecatedTileTypes = listOf(Tile.Type.Water, Tile.Type.Mountain)
}
