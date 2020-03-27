package simulation.culture.group

import simulation.space.tile.Tile

/**
 * This operation is not commutative.
 * @return double from 0 to 1.
 */
fun getGroupsDifference(g1: Group, g2: Group): Double {
    var matched = 1.0
    var overall = 1.0
    for (aspect in g1.cultureCenter.aspectCenter.aspectPool.getAll()) {
        if (g2.cultureCenter.aspectCenter.aspectPool.contains(aspect)) {
            matched++
        }
        overall++
    }
    for (aspect in g1.cultureCenter.cultureAspectCenter.aspectPool.getAll()) {
        if (g2.cultureCenter.cultureAspectCenter.aspectPool.contains(aspect)) {
            matched++
        }
        overall++
    }
    return matched / overall
}

