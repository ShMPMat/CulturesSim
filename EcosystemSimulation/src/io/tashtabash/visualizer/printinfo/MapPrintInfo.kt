package io.tashtabash.visualizer.printinfo

import io.tashtabash.simulation.SimulationError
import io.tashtabash.simulation.space.SpaceData.data
import io.tashtabash.simulation.space.WorldMap
import io.tashtabash.simulation.space.tile.Tile


class MapPrintInfo {
    var cut = 0

    fun computeCut(map: WorldMap) {
        if (!data.yMapLooping) return
        var gapStart = 0
        var gapFinish = 0
        var start = -1
        var finish = 0

        for (y in 0 until data.mapSizeY) {
            var isLineClear = true
            for (x in 0 until data.mapSizeX) {
                val tile = map[x, y]
                        ?: throw SimulationError("Incoherent map size")
                if (tile.type != Tile.Type.Water && tile.type != Tile.Type.Ice) {
                    isLineClear = false
                    break
                }
            }
            if (isLineClear) {
                if (start == -1)
                    start = y
                finish = y
            } else if (start != -1) {
                if (finish - start > gapFinish - gapStart) {
                    gapStart = start
                    gapFinish = finish
                }
                start = -1
            }
        }

        cut = (gapStart + gapFinish) / 2
    }
}
