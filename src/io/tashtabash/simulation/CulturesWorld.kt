package io.tashtabash.simulation

import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.random.singleton.randomTile
import io.tashtabash.simulation.CulturesController.Companion.session
import io.tashtabash.simulation.culture.aspect.*
import io.tashtabash.simulation.culture.aspect.dependency.AspectDependencies
import io.tashtabash.simulation.culture.aspect.labeler.AspectNameLabeler
import io.tashtabash.simulation.culture.group.GROUP_TAG_TYPE
import io.tashtabash.simulation.culture.group.GroupConglomerate
import io.tashtabash.simulation.culture.group.compulsoryAspects
import io.tashtabash.simulation.culture.group.place.StrayPlacesManager
import io.tashtabash.simulation.space.resource.Resources
import io.tashtabash.simulation.space.resource.action.ResourceAction
import io.tashtabash.simulation.space.resource.transformer.ConcatTransformer
import io.tashtabash.simulation.space.resource.transformer.NameTransformer
import io.tashtabash.simulation.space.resource.transformer.TagTransformer
import io.tashtabash.simulation.space.tile.Tile
import java.util.*


open class CulturesWorld : World() {
    var conglomerates: MutableList<GroupConglomerate> = ArrayList()

    val shuffledConglomerates: List<GroupConglomerate>
        get() = conglomerates.shuffled(RandomSingleton.random)

    val strayPlacesManager = StrayPlacesManager()

    lateinit var aspectPool: AspectPool
        private set

    fun initializeMap(proportionCoefficient: Double) {
        val aspectUrls = this::class.java.classLoader.getResources("Aspects")
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

    fun initializeGroups() {
        for (i in 0 until session.startGroupAmount)
            conglomerates.add(GroupConglomerate(1, tileForGroup))

        for (aspectName in compulsoryAspects)
            for (c in conglomerates)
                for (it in c.subgroups)
                    it.cultureCenter
                            .aspectCenter
                            .addAspectTry(aspectPool.getValue(aspectName), it)

        conglomerates.forEach { it.finishUpdate() }
    }

    private val tileForGroup: Tile
        get() {
            while (true) {
                val tile = map.randomTile()
                if (tile.tagPool.getByType(GROUP_TAG_TYPE).isEmpty()
                        && tile.type != Tile.Type.Water && tile.type != Tile.Type.Mountain) {
                    return tile
                }
            }
        }

    fun addGroupConglomerate(groupConglomerate: GroupConglomerate) {
        conglomerates.add(groupConglomerate)
    }

    fun clearDeadConglomerates() {
        conglomerates.removeIf { it.state == GroupConglomerate.State.Dead }
    }
}

private val improvedAspectNames = listOf("Trade", "ShapeMalleable", "Engrave", "Sculpt", "Carve", "Paint", "Incrust")
private val buildingsNames = listOf("House", "Wigwam", "Yurt")
