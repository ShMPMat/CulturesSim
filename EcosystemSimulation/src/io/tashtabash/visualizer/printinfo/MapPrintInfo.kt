package io.tashtabash.visualizer.printinfo

import io.tashtabash.sim.SimulationError
import io.tashtabash.sim.space.SpaceData.data
import io.tashtabash.sim.space.WorldMap
import io.tashtabash.sim.space.tile.Tile


class MapPrintInfo {
    var cut = 0 // Set in a way to minimize the number of land being divided by a map border in the view

    fun computeCut(map: WorldMap) {
        if (!data.yMapLooping) return

        var bestGapLand = Int.MAX_VALUE
        var bestGapStart = 0
        var bestGapWidth = 0

        var currentGapStart = 0
        var currentGapWidth = 0

        for (y in 0 until data.mapSizeY) {
            val landInThisLine = (0 until data.mapSizeX).count { x ->
                val tile = map[x, y]
                    ?: throw SimulationError("Incoherent map size")
                tile.type != Tile.Type.Water && tile.type != Tile.Type.Ice
            }

            if (landInThisLine < bestGapLand) {
                bestGapLand = landInThisLine
                currentGapStart = y
                currentGapWidth = 1
                bestGapStart = y
                bestGapWidth = 1
            } else if (landInThisLine == bestGapLand) {
                currentGapWidth++
                if (currentGapWidth > bestGapWidth) {
                    bestGapWidth = currentGapWidth
                    bestGapStart = currentGapStart
                }
            } else {
                // Reset current streak
                currentGapWidth = 0
                currentGapStart = y + 1
            }
        }

        cut = bestGapStart + (bestGapWidth / 2)
    }
}
