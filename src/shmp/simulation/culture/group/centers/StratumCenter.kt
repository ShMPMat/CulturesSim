package shmp.simulation.culture.group.centers

import shmp.random.testProbability
import shmp.simulation.CulturesController
import shmp.simulation.culture.aspect.ConverseWrapper
import shmp.simulation.culture.group.GroupError
import shmp.simulation.culture.group.request.Request
import shmp.simulation.culture.group.stratum.*
import shmp.simulation.space.territory.Territory
import shmp.simulation.space.resource.container.MutableResourcePack
import shmp.simulation.space.tile.Tile

class StratumCenter(initTile: Tile) {
    private val _strata = mutableListOf<Stratum>()
    val strata: List<Stratum> = _strata

    init {
        _strata.add(WarriorStratum(initTile))
        _strata.add(TraderStratum(initTile))
    }

    val traderStratum: TraderStratum = strata
            .filterIsInstance<TraderStratum>()
            .first()
    val warriorStratum: WarriorStratum = strata
            .filterIsInstance<WarriorStratum>()
            .first()

    private val cultStrata = mutableMapOf<String, CultStratum>()
    private val aspectStrata = mutableMapOf<ConverseWrapper, AspectStratum>()

    fun getByCultNameOrNull(name: String) = cultStrata[name]

    fun getByCultName(name: String) = getByCultNameOrNull(name)
            ?: throw GroupError("No Stratum for a Cult $name")

    fun getByAspectOrNull(aspect: ConverseWrapper) = aspectStrata[aspect]

    fun getByAspect(aspect: ConverseWrapper) = getByAspectOrNull(aspect)
            ?: throw GroupError("No Stratum for an Aspect ${aspect.name}")

    fun getStrataForRequest(request: Request): List<AspectStratum> = _strata
            .filterIsInstance<AspectStratum>()
            .filter { request.isAcceptable(it) }
            .sortedByDescending { request.satisfactionLevel(it) }

    fun addStratum(stratum: Stratum) {
        when (stratum) {
            is TraderStratum -> mergeStrata(traderStratum, stratum)
            is WarriorStratum -> mergeStrata(warriorStratum, stratum)
            else -> {
                when (stratum) {
                    is CultStratum -> {
                        val innerStratum = cultStrata[stratum.cultName]
                        if (innerStratum != null)
                            mergeStrata(innerStratum, stratum)
                        else
                            cultStrata[stratum.cultName] = stratum
                    }
                    is AspectStratum -> {
                        val innerStratum = aspectStrata[stratum.aspect]
                        if (innerStratum != null)
                            throw GroupError("$stratum is already present in the Center")
                        aspectStrata[stratum.aspect] = stratum
                    }
                }
                _strata.add(stratum)
            }
        }
    }

    private fun mergeStrata(internal: NonAspectStratum, external: NonAspectStratum) {
        internal.population += external.population
        external.places.forEach { internal.addPlace(it) }
    }

    internal fun update(accessibleTerritory: Territory, group: Group, turnResources: MutableResourcePack) {
        if (testProbability(CulturesController.session.egoRenewalProb, CulturesController.session.random)) {
            val mostImportantStratum = strata
                    .filter { it.population > 0 }
                    .maxBy { it.importance }
            if (mostImportantStratum != null)
                if (mostImportantStratum.importance > 0)
                    mostImportantStratum.ego.isActive = true
        }
        _strata.forEach { it.update(turnResources, accessibleTerritory, group) }
    }

    fun finishUpdate(group: Group) = _strata.forEach { it.finishUpdate(group) }

    fun movePopulation(tile: Tile) = _strata.forEach { it.ego.place.move(tile) }

    fun die() = _strata.forEach { it.die() }

    override fun toString() = _strata
            .filter { it.population != 0 || it.ego.isActive }
            .joinToString("\n") { "    $it" }
}
