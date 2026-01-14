package io.tashtabash.sim

import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.sim.culture.aspect.*
import io.tashtabash.sim.culture.aspect.dependency.AspectDependencies
import io.tashtabash.sim.culture.aspect.labeler.AspectNameLabeler
import io.tashtabash.sim.culture.group.GroupConglomerate
import io.tashtabash.sim.culture.group.compulsoryAspects
import io.tashtabash.sim.culture.group.place.StrayPlacesManager
import io.tashtabash.sim.space.resource.Resources
import io.tashtabash.sim.space.resource.action.ResourceAction
import io.tashtabash.sim.space.resource.transformer.ConcatTransformer
import io.tashtabash.sim.space.resource.transformer.NameTransformer
import io.tashtabash.sim.space.resource.transformer.TagTransformer
import io.tashtabash.sim.space.tile.Tile
import java.util.*


open class CulturesWorld(ecosystemWorld: EcosystemWorld) : World by ecosystemWorld {
    var conglomerates: MutableList<GroupConglomerate> = ArrayList()

    val shuffledConglomerates: List<GroupConglomerate>
        get() = conglomerates.shuffled(RandomSingleton.random)

    val strayPlacesManager = StrayPlacesManager()

    lateinit var aspectPool: AspectPool
        private set

    fun initializeMap(proportionCoefficient: Double) {
        val classLoader = Thread.currentThread().contextClassLoader
        val aspectUrls = classLoader.getResources("Aspects")
        val mutableAspectPool = AspectInstantiation(tags, actionTags).createPool(aspectUrls)
        aspectPool = mutableAspectPool

        val actions = aspectPool.all
            .associate { it.core.resourceAction to it.core.matchers }
        val resourceActionInjectors = listOf(
            fun(a: ResourceAction, rs: Resources): List<Pair<ResourceAction, Resources>> {
                val building = rs.firstOrNull { it.simpleName in buildingsNames }
                    ?: return listOf()

                val transformers = improvedAspectNames.map { n ->
                    n to ConcatTransformer(
                        TagTransformer(AspectImprovementTag(AspectNameLabeler(n), 0.5)),
                        NameTransformer { n + it }
                    )
                }

                return transformers.map { (postfix, transformer) ->
                    val actionName = a.technicalName + postfix
                    val newAction = a.copy(actionName)
                    val newResources = rs.map {
                        if (it != building) it.exactCopy() else transformer.transform(it)
                    }

                    val oldAspectCore = aspectPool.get(a.technicalName)!!.core
                    mutableAspectPool.add(
                        Aspect(
                            oldAspectCore.copy(name = actionName, resourceAction = newAction),
                            AspectDependencies(mutableMapOf())
                        )
                    )

                    newAction to newResources
                }
            }
        )

        initializeMap(actions, AspectResourceTagParser(tags), resourceActionInjectors, proportionCoefficient)
    }

    fun addConglomerate(tile: Tile): GroupConglomerate {
        val conglomerate = GroupConglomerate(1, tile)
        conglomerates += conglomerate

        for (aspectName in compulsoryAspects)
            for (group in conglomerate.subgroups)
                group.cultureCenter
                    .aspectCenter
                    .tryAddingAspect(aspectPool.getValue(aspectName), group)

        conglomerate.finishUpdate()

        return conglomerate
    }

    fun addGroupConglomerate(groupConglomerate: GroupConglomerate) {
        conglomerates.add(groupConglomerate)
    }

    fun clearDeadConglomerates() {
        conglomerates.removeIf { it.state == GroupConglomerate.State.Dead }
    }
}

private val improvedAspectNames = listOf("Trade", "ShapeMalleable", "Engrave", "Sculpt", "Carve", "Paint", "Encrust")
private val buildingsNames = listOf("House", "Wigwam", "Yurt")
