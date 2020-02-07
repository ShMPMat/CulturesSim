package simulation.space.generator;

import extra.ProbabilityFuncs;
import simulation.World;
import simulation.culture.aspect.AspectTag;
import simulation.space.TectonicPlate;
import simulation.space.resource.Genome;
import simulation.space.resource.Resource;
import simulation.space.Tile;
import simulation.space.resource.dependency.ResourceDependency;
import simulation.space.resource.dependency.ResourceDependencyDep;
import simulation.space.resource.ResourceIdeal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static simulation.Controller.*;

/**
 * Basic map generator.
 */
public class RandomMapGenerator {
    public static List<AspectTag> aspectTagPool;

    @Deprecated
    public static void createResources(World world, int numberOrResources) {
        fillAspectTagPool();
        //fillResourcePoolWithRandomResources(world, numberOrResources);
    }

    public static void fill() {
//        createBlob(map, Tile.Type.Mountain, 30);
//        createBlob(map, Tile.Type.Water, 30);
        boolean sw = true, ssw = true;
        for (TectonicPlate plate: session.world.map.getTectonicPlates()) {
            if (sw) {
                plate.setType(TectonicPlate.Type.Terrain);
                sw = false;
            } else if (ssw) {
                plate.setType(TectonicPlate.Type.Oceanic);
                ssw = false;
            }
            plate.initialize();
        }
        session.world.map.movePlates();
    }

    @Deprecated
    private static void fillResourcePoolWithRandomResources(World world, int numberOrResources) {
        for (int i = 0; i < numberOrResources; i++) {
            List<String> tags = new ArrayList<>();
            tags.add("r" + i);
            tags.add(((Double) (Math.random() / 100)).toString());
            int n = ProbabilityFuncs.randomInt(aspectTagPool.size());
            for (int j = 0; j < n; j++) {
                while (true) {
                    String name = ProbabilityFuncs.randomElement(aspectTagPool).name;
                    if (!tags.contains("-" + name)) {
                        tags.add("-" + name);
                        break;
                    }
                }
            }
            world.resourcePool.add(new ResourceIdeal(tags.toArray(new String[n + 2])));
        }
    }

    private static void fillAspectTagPool() {
        aspectTagPool = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("SupplementFiles/ResourceTags"))) {
            String line;
            String[] tags;
            while (true) {
                line = br.readLine();
                if (line == null) {
                    break;
                }
                aspectTagPool.add(new AspectTag(line));
            }
        } catch (Throwable t) {
            System.err.println(t.toString());
        }
    }

    public static void fillResources() {
        for (Resource resource : session.world.map.resourcePool) {
            if (resource.getSpreadProbability() == 0 && !resource.getGenome().getType().equals(Genome.Type.Mineral)) {
                continue;
            }
            scatter(resource, session.startResourceAmountMin +
                    ProbabilityFuncs.randomInt(session.startResourceAmountMax -
                            session.startResourceAmountMin));
        }
    }


    private static void scatter(Resource resource, int n) {
        List<Tile> goodTiles = session.world.map.getTilesWithPredicate(t -> resource.getGenome().isOptimal(t));
        for (int i = 0; i < n; i++) {
            Tile tile;
            if (goodTiles.isEmpty()) {
                tile = ProbabilityFuncs.randomTile(session.world.map);
                while (!resource.getGenome().isAcceptable(tile)) {
                    tile = ProbabilityFuncs.randomTile(session.world.map);
                }
            } else {
                tile = ProbabilityFuncs.randomElement(goodTiles);
            }
            tile.addDelayedResource(resource.copy());
            addDependencies(resource, tile);
        }
    }

    private static void addDependencies(Resource resource, Tile tile) {
        for (ResourceDependency dependency: resource.getGenome().getDependencies()) {
            if (!dependency.isPositive() || !dependency.isResourceNeeded()) {
                continue;
            }
            if (dependency instanceof ResourceDependencyDep && ((ResourceDependencyDep) dependency).getResourceNames()
                    .stream().anyMatch(s -> s.equals("Vapour"))) {
                return;
            }
            if (dependency instanceof ResourceDependencyDep) {
                for (String name : ((ResourceDependencyDep) dependency).getResourceNames()) {
                    Resource dep = session.world.getPoolResource(name);
                    if (dep.getGenome().isAcceptable(tile)) {
                        tile.addDelayedResource(dep.copy());
                    }
                    addDependencies(dep, tile);
                }
                for (String name : ((ResourceDependencyDep) dependency).getMaterialNames()) {
                    for (Resource dep : session.world.resourcePool.stream().filter(r -> r.getSpreadProbability() > 0 &&
                            !r.getSimpleName().equals(resource.getSimpleName()) &&
                            r.getGenome().getPrimaryMaterial() != null &&
                            r.getGenome().getPrimaryMaterial().getName().equals(name)).collect(Collectors.toList())) {
                        if (dep.getGenome().isAcceptable(tile)) {
                            tile.addDelayedResource(dep.copy());
                            addDependencies(dep, tile);
                        }
                    }
                }
            }
        }
    }
}
