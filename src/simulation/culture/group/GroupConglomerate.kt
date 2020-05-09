package simulation.culture.group

import extra.OutputFunc
import simulation.Controller
import simulation.Event
import simulation.culture.aspect.Aspect
import simulation.culture.group.centers.Group
import simulation.culture.group.centers.PopulationCenter
import simulation.culture.group.centers.RelationCenter
import simulation.culture.group.centers.ResourceCenter
import simulation.culture.group.cultureaspect.CultureAspect
import simulation.culture.thinking.meaning.GroupMemes
import simulation.culture.thinking.meaning.Meme
import simulation.space.Territory
import simulation.space.resource.MutableResourcePack
import simulation.space.tile.Tile
import simulation.space.tile.getClosest
import java.util.*

class GroupConglomerate(var name: String, var population: Int, numberOfSubGroups: Int, root: Tile) {
    @JvmField
    var subgroups: MutableList<Group> = ArrayList()
    private val shuffledSubgroups: List<Group>
        get() = subgroups.shuffled(Controller.session.random)
    var state = State.Live

    val events: MutableList<Event> = ArrayList()

    /**
     * Overall Territory which is under the child Groups
     */
    val territory = Territory()
    private var namesScore = 0

    init {
        claimTile(root)
        for (i in 0 until numberOfSubGroups) {
            val name = name + "_" + i
            addGroup(Group(
                    ResourceCenter(MutableResourcePack(), center, name),
                    this,
                    name,
                    PopulationCenter(
                            population / numberOfSubGroups,
                            Controller.session.defaultGroupMaxPopulation,
                            Controller.session.defaultGroupMinPopulationPerTile
                    ),
                    RelationCenter {
                        val difference = getGroupsDifference(it.owner, it.other)
                        val weakRelationsMultiplier = difference * difference * difference / 10
                        (difference + it.positiveInteractions * weakRelationsMultiplier - 0.5) * 2
                    },
                    center,
                    ArrayList(),
                    GroupMemes(),
                    emptyList(),
                    Controller.session.defaultGroupSpreadability
            ))
        }
    }

    constructor(numberOfSubgroups: Int, root: Tile) : this(
            Controller.session.vacantGroupName,
            100 + Controller.session.random.nextInt(100),
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
                .map { it.cultureCenter.memePool.memes }
                .foldRight(mutableMapOf()) { x: MutableList<Meme>, y: MutableMap<Meme, Meme> ->
                    for (meme in x) {
                        if (meme !in y)
                            y[meme] = meme.copy()
                        else
                            y[meme]?.increaseImportance(meme.importance)
                    }
                    y
                }.values.toList()

    private fun overgroupDie() {
        state = State.Dead
        population = 0
    }

    val center: Tile
        get() = territory.center

    fun update() { //TODO simplify
        if (state == State.Dead) return
        val mainTime = System.nanoTime()
        subgroups.removeIf { it.state == Group.State.Dead }
        shuffledSubgroups.forEach { it.update() }
        Controller.session.groupMainTime += System.nanoTime() - mainTime
        val othersTime = System.nanoTime()
        shuffledSubgroups.mapNotNull { it.populationUpdate() }
                .forEach { it.execute(this) }
        updatePopulation()
        if (state == State.Dead) return
        shuffledSubgroups.forEach { it.diverge() }
        shuffledSubgroups.forEach { it.intergroupUpdate() }
        Controller.session.groupOthersTime += System.nanoTime() - othersTime
    }

    private fun updatePopulation() {
        computePopulation()
        if (population == 0) overgroupDie()
    }

    private fun computePopulation() {
        population = subgroups
                .map { it.populationCenter.population }
                .foldRight(0, Int::plus)
    }

    fun claimTile(tile: Tile?) = territory.add(tile)

    fun addGroup(group: Group) {
        subgroups.add(group)
        computePopulation()
        group.territoryCenter.territory.tiles.forEach { tile: Tile? -> claimTile(tile) }
        group.parentGroup = this
    }

    fun getClosestInnerGroupDistance(tile: Tile): Int {
        var d = Int.MAX_VALUE
        for (subgroup in subgroups) {
            d = Math.min(d, getClosest(tile, setOf(subgroup.territoryCenter.territory.center)).second)
        }
        return d
    }

    fun removeGroup(group: Group) {
        population -= group.populationCenter.population
        if (!subgroups.remove(group)) {
            throw RuntimeException("Trying to remove non-child subgroup ${group.name} from Group $name")
        }
        group.territoryCenter.territory.tiles.forEach { removeTile(it) }
    }

    fun finishUpdate() = subgroups.forEach { it.finishUpdate() }

    fun removeTile(tile: Tile?) {
        if (tile == null) return
        territory.remove(tile)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val group = other as GroupConglomerate
        return name == group.name
    }

    override fun hashCode() = Objects.hash(name)

    override fun toString(): String {
        var stringBuilder = StringBuilder()
        for (subgroup in subgroups.take(10))
            stringBuilder = OutputFunc.addToRight(stringBuilder.toString(), subgroup.toString(), false)
        if (subgroups.size > 10) stringBuilder.append(
                subgroups.joinToString(",", "\n all:") { it.name }
        )
        return stringBuilder.toString()
    }

    val newName: String
        get() {
            namesScore++
            return name + "_" + namesScore
        }

    enum class State {
        Live, Dead
    }
}