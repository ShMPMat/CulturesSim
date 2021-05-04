package shmp.simulation.culture.group

import shmp.utils.addToRight
import shmp.simulation.CulturesController
import shmp.simulation.culture.aspect.Aspect
import shmp.simulation.culture.group.centers.*
import shmp.simulation.culture.group.cultureaspect.CultureAspect
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonField
import shmp.simulation.culture.thinking.meaning.GroupMemes
import shmp.simulation.culture.thinking.meaning.Meme
import shmp.simulation.event.EventLog
import shmp.simulation.space.resource.container.MutableResourcePack
import shmp.simulation.space.resource.container.ResourcePack
import shmp.simulation.space.territory.BrinkInvariantTerritory
import shmp.simulation.space.territory.MutableTerritory
import shmp.simulation.space.tile.Tile
import shmp.simulation.space.tile.getClosest
import java.util.*
import kotlin.math.max


class GroupConglomerate(val name: String, var population: Int, numberOfSubGroups: Int, root: Tile) {
    private val _subgroups = mutableListOf<Group>()
    val subgroups: List<Group>
        get() = _subgroups

    private val shuffledSubgroups: List<Group>
        get() = subgroups.shuffled(CulturesController.session.random)
    var state = State.Live

    var events = EventLog()

    //Overall Territory under of the child Groups
    val territory: MutableTerritory = BrinkInvariantTerritory()
    private var namesScore = 0

    init {
        claimTile(root)
        for (i in 0 until numberOfSubGroups) {
            val name = name + "_$i"
            val memoryCenter = MemoryCenter()
            val aspectCenter = AspectCenter(emptyList())
            val populationCenter = PopulationCenter(
                    population / numberOfSubGroups,
                    CulturesController.session.defaultGroupMaxPopulation,
                    CulturesController.session.defaultGroupMinPopulationPerTile,
                    root,
                    ResourcePack()
            )

            addGroup(Group(
                    ProcessCenter(AdministrationType.Main),
                    ResourceCenter(MutableResourcePack(), root, name),
                    this,
                    name,
                    populationCenter,
                    RelationCenter {
                        val difference = getGroupsDifference(it.owner, it.other)
                        val weakRelationsMultiplier = difference * difference * difference / 10
                        (difference + it.positiveInteractions * weakRelationsMultiplier - 0.5) * 2
                    },
                    CultureAspectCenter(
                            ReasonField(),
                            cultureConversions(memoryCenter, aspectCenter, populationCenter.stratumCenter)
                    ),
                    TraitCenter(),
                    root,
                    aspectCenter,
                    memoryCenter,
                    GroupMemes(),
                    emptyList(),
                    CulturesController.session.defaultGroupSpreadability
            ))
        }
    }

    constructor(numberOfSubgroups: Int, root: Tile) : this(
            CulturesController.session.vacantGroupName,
            100 + CulturesController.session.random.nextInt(100),
            numberOfSubgroups,
            root
    )

    /**
     * @return All aspects which exist in any of Groups.
     * Each of them belongs to one of the Groups, it is not guarantied to which one.
     */
    val aspects: Set<Aspect>
        get() = subgroups
                .flatMap { it.cultureCenter.aspectCenter.aspectPool.all }
                .toSet()

    /**
     * @return all CultureAspects of child Groups.
     */
    val cultureAspects: Set<CultureAspect>
        get() = subgroups
                .flatMap { it.cultureCenter.cultureAspectCenter.aspectPool.all }
                .toSet()

    /**
     * @return all Memes of child Groups.
     */
    val memes: List<Meme>
        get() = subgroups
                .map { it.cultureCenter.memePool.all }
                .foldRight(mutableMapOf()) { x: List<Meme>, y: MutableMap<Meme, Meme> ->
                    for (meme in x) {
                        if (meme !in y)
                            y[meme] = meme.copy()
                        else
                            y[meme]?.increaseImportance(max(meme.importance - y.getValue(meme).importance, 0))
                    }
                    y
                }.values.toList()

    private fun die() {
        state = State.Dead
        population = 0
    }

    fun update() {
        if (state == State.Dead)
            return
        val mainTime = System.nanoTime()
        _subgroups.removeIf { it.state == Group.State.Dead }
        shuffledSubgroups.forEach { it.update() }
        CulturesController.session.groupMainTime += System.nanoTime() - mainTime
        val othersTime = System.nanoTime()
        updatePopulation()
        if (state == State.Dead)
            return
        shuffledSubgroups.forEach { it.intergroupUpdate() }
        CulturesController.session.groupOthersTime += System.nanoTime() - othersTime
    }

    private fun updatePopulation() {
        computePopulation()
        if (population == 0)
            die()
    }

    private fun computePopulation() {
        population = subgroups
                .map { it.populationCenter.population }
                .foldRight(0, Int::plus)
    }

    fun claimTile(tile: Tile?) = territory.add(tile)

    fun addGroup(group: Group) {
        _subgroups.add(group)
        computePopulation()
        group.territoryCenter.territory.tiles.forEach { claimTile(it) }
        group.parentGroup = this
    }

    fun getClosestInnerGroupDistance(tile: Tile) = subgroups
            .map { getClosest(tile, setOf(it.territoryCenter.center)).second }
            .min()
            ?: Int.MAX_VALUE

    fun removeGroup(group: Group) {
        population -= group.populationCenter.population
        if (!_subgroups.remove(group))
            throw RuntimeException("Trying to remove non-child subgroup ${group.name} from Group $name")

        group.territoryCenter.territory.tiles.forEach { removeTile(it) }
    }

    fun finishUpdate() {
        subgroups.forEach { it.finishUpdate() }
        subgroups.forEach { events.joinNewEvents(it.cultureCenter.events) }
    }

    fun removeTile(tile: Tile?) = territory.remove(tile)

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null || javaClass != other.javaClass)
            return false
        val group = other as GroupConglomerate
        return name == group.name
    }

    override fun hashCode() = Objects.hash(name)

    override fun toString(): String {
        var stringBuilder = StringBuilder()
        for (subgroup in subgroups.take(10))
            stringBuilder = addToRight(stringBuilder.toString(), subgroup.toString(), false)
        if (subgroups.size > 10) stringBuilder.append(
                subgroups.joinToString(",", "\n all:") { it.name }
        )
        return stringBuilder.toString()
    }

    val newName: String
        get() {
            namesScore++
            return name + "_$namesScore"
        }

    enum class State {
        Live, Dead
    }
}
