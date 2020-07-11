package simulation.culture.group.process.action

import simulation.culture.group.centers.Group
import simulation.space.tile.getDistance


class ComputeTravelTime(group: Group, private val target: Group): AbstractGroupAction(group) {
    override fun run() = getDistance(group.territoryCenter.center, target.territoryCenter.center) /
            group.territoryCenter.reachDistance

    override val internalToString = "Compute the travel time from ${group.name} to ${target.name}"
}
