package shmp.simulation

import shmp.random.singleton.RandomSingleton
import shmp.random.singleton.randomTile
import shmp.simulation.CulturesController.session
import shmp.simulation.culture.aspect.*
import shmp.simulation.culture.aspect.dependency.AspectDependencies
import shmp.simulation.culture.aspect.labeler.AspectNameLabeler
import shmp.simulation.culture.group.GROUP_TAG_TYPE
import shmp.simulation.culture.group.GroupConglomerate
import shmp.simulation.culture.group.compulsoryAspects
import shmp.simulation.culture.group.place.StrayPlacesManager
import shmp.simulation.space.resource.Resources
import shmp.simulation.space.resource.action.ResourceAction
import shmp.simulation.space.resource.transformer.ConcatTransformer
import shmp.simulation.space.resource.transformer.NameTransformer
import shmp.simulation.space.resource.transformer.TagTransformer
import shmp.simulation.space.tile.Tile
import java.util.*


class CulturesWorld : World() {
    var groups: MutableList<GroupConglomerate> = ArrayList()

    val shuffledGroups: List<GroupConglomerate>
        get() = groups.shuffled(RandomSingleton.random)

    val strayPlacesManager = StrayPlacesManager()

    lateinit var aspectPool: AspectPool
        private set

    fun initializeMap(proportionCoefficient: Double) {
        val aspectUrls = this::class.java.classLoader.getResources("Aspects")
        val mutableAspectPool = AspectInstantiation(tags, actionTags).createPool(aspectUrls)
        aspectPool = mutableAspectPool

        val actions = aspectPool.all.map { it.core.resourceAction }
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
                        val actionName = a.name + postfix
                        val newAction = a.copy(actionName)
                        val newResources = rs.map {
                            if (it != building) it.exactCopy() else transformer.transform(it)
                        }

                        val oldAspectCore = aspectPool.get(a.name)!!.core
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
            groups.add(GroupConglomerate(1, tileForGroup))

        for (aspectName in compulsoryAspects)
            groups.forEach { c ->
                c.subgroups.forEach { it.cultureCenter.aspectCenter.addAspectTry(aspectPool.getValue(aspectName), it) }
            }

        groups.forEach { it.finishUpdate() }
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
        groups.add(groupConglomerate)
    }

    fun clearDeadConglomerates() {
        groups.removeIf { it.state == GroupConglomerate.State.Dead }
    }
}

private val improvedAspectNames = listOf("Trade", "ShapeMalleable", "Engrave", "Sculpt", "Carve", "Paint", "Incrust")
private val buildingsNames = listOf("House", "Wigwam", "Yurt")
