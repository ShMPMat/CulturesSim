package simulation.culture.group.process.behaviour

import shmp.random.randomElement
import simulation.Controller
import simulation.Event
import simulation.culture.aspect.hasMeaning
import simulation.culture.group.centers.Group
import simulation.culture.group.process.action.ReceiveGroupWideResourcesA
import simulation.culture.group.request.resourceToRequest


object RandomArtifactBehaviour : AbstractGroupBehaviour() {
    override fun run(group: Group): List<Event> {
        if (group.cultureCenter.memePool.isEmpty)
            return emptyList()

        val resourcesWithMeaning = group.cultureCenter.aspectCenter.aspectPool.producedResources
                .filter { it.first.hasMeaning }
                .map { it.first }
        if (resourcesWithMeaning.isEmpty())
            return emptyList()

        val chosen = randomElement(resourcesWithMeaning, Controller.session.random)
        val result = group.populationCenter.executeRequest(resourceToRequest(chosen, group, 1, 5)).pack

        val events = if (result.isNotEmpty)
            listOf(Event(Event.Type.Creation, "${group.name} created artifacts: $result"))
        else emptyList()

        ReceiveGroupWideResourcesA(group, result).run()

        return events
    }

    override fun toString() = "Make a random Resource with some meaning"
}
