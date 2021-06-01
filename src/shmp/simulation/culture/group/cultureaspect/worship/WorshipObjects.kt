package shmp.simulation.culture.group.cultureaspect.worship

import shmp.random.singleton.RandomSingleton
import shmp.random.singleton.randomElement
import shmp.random.singleton.randomElementOrNull
import shmp.simulation.culture.aspect.MeaningResourceFeature
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.centers.util.ArbitraryResource
import shmp.simulation.culture.group.cultureaspect.worship.CreationPath.*
import shmp.simulation.culture.group.request.resourceToRequest
import shmp.simulation.space.resource.Resource


fun makeWorshipObject(worship: Worship, group: Group): Resource? {//TODO more complex (resources tangent to worship)
    val paths = values().toList().shuffled(RandomSingleton.random)

    for (path in paths) {
        val resource = when(path) {
            InsertMeaning -> makeInsertMeaningResource(worship, group)
            ConnotationResource -> makeConnotationResource(worship, group)
        }

        resource?.let {
            return it
        }
    }

    return null
}

private fun makeInsertMeaningResource(worship: Worship, group: Group): Resource? {
    val meaningAspects = group.cultureCenter.aspectCenter.aspectPool.getMeaningAspects()
    val memes = worship.reasonComplex.reasonings.map { it.meme }

    if (meaningAspects.isNotEmpty() && memes.isNotEmpty()) {
        val meme = memes.randomElement()
        val aspect = meaningAspects.randomElement()

        val resource = aspect.resource.copyWithExternalFeatures(listOf(MeaningResourceFeature(meme)))
        val hasNeededStrata = group.populationCenter.stratumCenter
                .getStrataForRequest(resourceToRequest(resource, group))
                .any { it.population != 0 }

        if (!hasNeededStrata && group.populationCenter.freePopulation == 0)
            return null

        return resource
    }

    return null
}

private fun makeConnotationResource(worship: Worship, group: Group): Resource? {
    val resources = worship.worshipObject.concepts
            .filterIsInstance<ArbitraryResource>()
            .map { it.resource }

    val resource = resources.randomElementOrNull()
            ?: return null

    val hasNeededStrata = group.populationCenter.stratumCenter
            .getStrataForRequest(resourceToRequest(resource, group))
            .any { it.population != 0 }

    if (!hasNeededStrata && group.populationCenter.freePopulation == 0)
        return null

    return resource
}

private enum class CreationPath {
    InsertMeaning,
    ConnotationResource
}
