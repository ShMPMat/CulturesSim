package simulation.space.generator;

import kotlin.random.Random;
import simulation.space.ResourcePool;
import simulation.space.TectonicPlate;
import simulation.space.WorldMap;
import simulation.space.resource.Genome;
import simulation.space.resource.Resource;
import simulation.space.Tile;
import simulation.space.resource.dependency.ResourceDependency;
import simulation.space.resource.dependency.ResourceDependencyDep;

import java.util.List;
import java.util.stream.Collectors;

import static shmp.random.RandomCollectionsKt.*;
import static shmp.random.RandomSpaceKt.randomTile;

/**
 * Basic map generator.
 */
public class RandomMapGenerator {
    public static void fill(WorldMap map) {
        boolean sw = true, ssw = true;
        for (TectonicPlate plate : map.getTectonicPlates()) {
            if (sw) {
                plate.setType(TectonicPlate.Type.Terrain);
                sw = false;
            } else if (ssw) {
                plate.setType(TectonicPlate.Type.Oceanic);
                ssw = false;
            }
            plate.initialize();
        }
        map.movePlates();
    }

    public static void fillResources(
            WorldMap map,
            ResourcePool resourcePool,
            MapGeneratorSupplement supplement,
            Random random
    ) {
        for (Resource resource : resourcePool.getResourcesWithPredicate(r ->
                r.getSpreadProbability() != 0 || r.getGenome().getType().equals(Genome.Type.Mineral))) {
            scatter(
                    map,
                    resourcePool,
                    resource,
                    random.nextInt(
                            supplement.getStartResourceAmountRange().getStart(),
                            supplement.getStartResourceAmountRange().getEndInclusive()
                    ),
                    random
            );
        }
    }

    private static void scatter(WorldMap map, ResourcePool resourcePool, Resource resource, int n, Random random) {
        List<Tile> goodTiles = map.getTilesWithPredicate(t -> resource.getGenome().isOptimal(t));
        for (int i = 0; i < n; i++) {
            Tile tile;
            if (goodTiles.isEmpty()) {
                tile = randomTile(map, random);
                while (!resource.getGenome().isAcceptable(tile)) {
                    tile = randomTile(map, random);
                }
            } else {
                tile = randomElement(goodTiles, random);
            }
            tile.addDelayedResource(resource.copy());
            addDependencies(resource, tile, resourcePool);
        }
    }

    private static void addDependencies(Resource resource, Tile tile, ResourcePool resourcePool) {
        for (ResourceDependency dependency : resource.getGenome().getDependencies()) {
            if (!dependency.isPositive() || !dependency.isResourceNeeded()) {
                continue;
            }
            if (dependency instanceof ResourceDependencyDep && ((ResourceDependencyDep) dependency).getResourceNames()
                    .stream().anyMatch(s -> s.equals("Vapour"))) {
                return;
            }
            if (dependency instanceof ResourceDependencyDep) {
                for (String name : ((ResourceDependencyDep) dependency).getResourceNames()) {
                    Resource dep = resourcePool.getResource(name);
                    if (dep.getGenome().isAcceptable(tile)) {
                        tile.addDelayedResource(dep);
                    }
                    addDependencies(dep, tile, resourcePool);
                }
                for (String name : ((ResourceDependencyDep) dependency).getMaterialNames()) {
                    for (Resource dep : resourcePool.getResourcesWithPredicate(r ->
                            r.getSpreadProbability() > 0
                                    && !r.getSimpleName().equals(resource.getSimpleName())
                                    && r.getGenome().getPrimaryMaterial() != null
                                    && r.getGenome().getPrimaryMaterial().getName().equals(name))) {
                        if (dep.getGenome().isAcceptable(tile)) {
                            tile.addDelayedResource(dep.copy());
                            addDependencies(dep, tile, resourcePool);
                        }
                    }
                }
            }
        }
    }
}
