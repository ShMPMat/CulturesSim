package simulation.space.generator;

import extra.ProbFunc;
import simulation.World;
import simulation.culture.aspect.AspectTag;
import simulation.space.TectonicPlate;
import simulation.space.resource.Resource;
import simulation.space.Tile;
import simulation.space.WorldMap;
import simulation.space.resource.ResourceDependency;
import simulation.space.resource.ResourceIdeal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public static void fill(World world) {
//        createBlob(map, Tile.Type.Mountain, 30);
//        createBlob(map, Tile.Type.Water, 30);
        boolean sw = true, ssw = true;
        for (TectonicPlate plate: world.map.getTectonicPlates()) {
            if (sw) {
                plate.setType(TectonicPlate.Type.Terrain);
                sw = false;
            } else if (ssw) {
                plate.setType(TectonicPlate.Type.Oceanic);
                ssw = false;
            }
            plate.initialize();
        }
        world.map.movePlates();
    }

    @Deprecated
    private static void fillResourcePoolWithRandomResources(World world, int numberOrResources) {
        for (int i = 0; i < numberOrResources; i++) {
            List<String> tags = new ArrayList<>();
            tags.add("r" + i);
            tags.add(((Double) (Math.random() / 100)).toString());
            int n = ProbFunc.randomInt(aspectTagPool.size());
            for (int j = 0; j < n; j++) {
                while (true) {
                    String name = ProbFunc.randomElement(aspectTagPool).name;
                    if (!tags.contains("-" + name)) {
                        tags.add("-" + name);
                        break;
                    }
                }
            }
            world.resourcePool.add(new ResourceIdeal(tags.toArray(new String[n + 2]),
                    1 + ProbFunc.randomInt(5), world));
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

    public static void fillResources(World world) {
        for (Resource resource : world.map.resourcePool) {
            if (resource.getSpreadProbability() == 0 && !resource.getBaseName().matches("Clay") &&
                    !resource.getBaseName().matches("Stone")) {
                continue;
            }
            scatter(world, resource, 40 + ProbFunc.randomInt(30));
        }
    }


    private static void scatter(World world, Resource resource, int n) {
        for (int i = 0; i < n; i++) {
            Tile tile = ProbFunc.randomTile(world.map);
            while (!tile.canSettle(resource.getGenome())) {
                tile = ProbFunc.randomTile(world.map);
            }
            tile.addDelayedResource(resource.copy());
            addDependencies(resource, tile, world);
        }
    }

    private static void addDependencies(Resource resource, Tile tile, World world) {
        for (ResourceDependency dependency: resource.getGenome().getDependencies()) {
            if (dependency.getResourceNames().stream().anyMatch(s -> s.equals("Vapour"))) {
                return;
            }
            for (String name: dependency.getResourceNames()) {
                Resource dep = world.getResourceFromPoolByName(name);
                if (tile.canSettle(dep.getGenome())) {
                    tile.addDelayedResource(dep.copy());
                }
                addDependencies(dep, tile, world);
            }
            for (String name: dependency.getMaterialNames()) {
                for (Resource dep: world.resourcePool.stream().filter(r -> r.getSpreadProbability() > 0 &&
                        !r.getSimpleName().equals(resource.getSimpleName()) &&
                        r.getGenome().getPrimaryMaterial() != null &&
                        r.getGenome().getPrimaryMaterial().getName().equals(name)).collect(Collectors.toList())) {
                    if (tile.canSettle(dep.getGenome())) {
                        tile.addDelayedResource(dep.copy());
                    }
                    addDependencies(dep, tile, world);
                }
            }
        }
    }
}
