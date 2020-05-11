package visualizer

import simulation.space.SpaceData
import simulation.space.SpaceData.data
import simulation.space.WorldMap
import simulation.space.tile.Tile

data class MapPrintInfo(val map: WorldMap) {
    var cut = 0

    fun computeCut() {
        if (!data.yMapLooping) return
        var gapStart = 0
        var gapFinish = 0
        var start = -1
        var finish = 0
        for (y in 0 until data.mapSizeY) {
            var isLineClear = true
            for (x in 0 until data.mapSizeX) {
                val tile: Tile = map.get(x, y)
                if (tile.type != Tile.Type.Water && tile.type != Tile.Type.Ice) {
                    isLineClear = false
                    break
                }
            }
            if (isLineClear) {
                if (start == -1) {
                    start = y
                }
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