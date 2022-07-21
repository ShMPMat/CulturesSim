package shmp.simulation.space.generator

import shmp.random.randomTile
import shmp.random.singleton.randomElementOrNull
import shmp.simulation.space.WorldMap
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.ResourceType
import shmp.simulation.space.resource.container.ResourcePool
import shmp.simulation.space.resource.dependency.LabelerDependency
import shmp.simulation.space.tile.Tile
import kotlin.random.Random


class ResourcePlacer(
        val map: WorldMap,
        val resourcePool: ResourcePool,
        val supplement: MapGeneratorSupplement,
        val random: Random
) {
    fun placeResources() {
        val validTypes = listOf(ResourceType.Mineral, ResourceType.Animal, ResourceType.Plant)
        val resourcesToScatter = resourcePool.getAll {
            it.genome.spreadProbability != 0.0 || it.genome.type in validTypes
        }

        for (resource in resourcesToScatter)
            scatter(
                    resource,
                    random.nextInt(supplement.startResourceAmountRange.first, supplement.startResourceAmountRange.last)
            )
    }

    private fun scatter(resource: Resource, n: Int) {
        println("Start scatter ${resource.baseName}")
        val idealTiles = map.getTiles { resource.areNecessaryDependenciesSatisfied(it) }
        val goodTiles = map.getTiles { resource.isAcceptable(it) }
        println("Done tiles")

        for (i in 0 until n) {
            val tile: Tile = idealTiles.randomElementOrNull()
                    ?: goodTiles.randomElementOrNull()
                    ?: randomTile(map)

            tile.addDelayedResource(resource.copy())
            println("Start deps")
            //TODO precompute the whole tree
            addDependencies(listOf(), resource, tile)
            println("End deps")
        }
        println("End scatter ${resource.baseName}")
    }

    private fun addDependencies(resourceStack: List<Resource>, resource: Resource, tile: Tile) {
        val newStack = resourceStack + listOf(resource)
        for (dependency in resource.genome.dependencies) {
            if (!dependency.isPositive || !dependency.isResourceNeeded)
                continue
            if (dependency is LabelerDependency) {
                val suitableResources = resourcePool
                        .getAll { dependency.isResourceDependency(it) }
                        .filter { filterDependencyResources(it, newStack) }
                for (dependencyResource in suitableResources) {
                    if (dependencyResource.areNecessaryDependenciesSatisfied(tile)) {
                        tile.addDelayedResource(dependencyResource)
                        addDependencies(newStack, dependencyResource, tile)
                    }
                }
            }
        }
    }

    private fun filterDependencyResources(resource: Resource, previous: List<Resource>) =
            resource.genome.type in listOf(ResourceType.Plant, ResourceType.Animal)
                    && previous.none { resource.simpleName == it.simpleName }
                    && resource.genome.primaryMaterial != null

}
