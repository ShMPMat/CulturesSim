package simulation.space;

import extra.ShnyPair;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectTag;
import simulation.space.resource.Resource;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Set of tiles.
 */
public class Territory {
    private List<Tile> tiles;

    public Territory(Collection<Tile> tiles) {
        this.tiles = new ArrayList<>();
        this.tiles.addAll(tiles);
    }

    public Territory() {
        this(new ArrayList<>());
    }

    public List<Tile> getTiles() {
        return tiles;
    }

    public Tile getTileByNumber(int n) {
        return tiles.get(n);
    }

    public List<Tile> getBrinkWithCondition(Predicate<Tile> predicate) {
        return getBrinkWithImportance(predicate, tile -> 0).stream().map(pair -> pair.first)
                .collect(Collectors.toList());
    }

    public List<ShnyPair<Tile, Integer>> getBrinkWithImportance(Predicate<Tile> predicate, Function<Tile, Integer> mapper) {
        Set<ShnyPair<Tile, Integer>> goodTiles = new HashSet<>();
        for (Tile tile : tiles) {
            if (tile == null) {
                int i = 0;
            }
            try {
                goodTiles.addAll(tile.getNeighbours(tile1 -> predicate.test(tile1) && !tiles.contains(tile1)).stream()
                        .map(tile1 -> new ShnyPair<>(tile1, mapper.apply(tile1))).collect(Collectors.toList()));
            } catch (Throwable t) {
                int i = 0;
            }
        }

        return new ArrayList<>(goodTiles);
    }

    public List<Resource> getResourcesWithAspectTag(AspectTag aspectTag) {
        List<Resource> resources = new ArrayList<>();
        for (Tile tile : tiles) {
            for (Resource resource : tile.getResources()) {
                if (resource.getTags().contains(aspectTag) && !resources.contains(resource)) {
                    resources.add(resource);
                }
            }
        }
        if (resources.size() != 0) {
            return resources;
        }
        return null;
    }

    public Collection<Resource> getDifferentResources() {
        List<Resource> resources = new ArrayList<>();
        for (Tile tile : tiles) {
            for (Resource resource : tile.getResources()) {
                if (!resources.contains(resource)) {
                    resources.add(resource);
                }
            }
        }
        return resources;
    }

    public List<Resource> getResourcesWhichConverseToTag(Aspect aspect, AspectTag tag) {
        List<Resource> resources = new ArrayList<>();
        for (Resource resource : getDifferentResources()) {
            List<Resource> _l = resource.applyAspect(aspect);
            if (_l.stream().anyMatch(res -> res.getTags().contains(tag)) &&
                    !(_l.size() == 1 && _l.get(0).equals(resource))) {
                resources.add(resource);
            }
        }
        return resources;
    }

    public Collection<Resource> getResourceInstances(Resource resource) {
        List<Resource> list = new ArrayList<>();
        for (Tile tile : tiles) {
            tile.getResources().stream().filter(res -> res.equals(resource) && !res.hasMeaning()).forEach(list::add);
        }
        return list;
    }

    public int size() {
        return tiles.size();
    }

    public boolean contains(Tile tile) {
        return tiles.contains(tile);
    }

    public void addTile(Tile tile) {
        if (!tiles.contains(tile)) {
            tiles.add(tile);
        }
    }

    public void removeTile(Tile tile) {
        tiles.remove(tile);
        tile.group = null;
    }

    public Tile excludeMostUselessTileExcept(Collection<Tile> exceptions, Function<Tile, Integer> mapper) {
        Set<Tile> result = new HashSet<>(tiles);
        result.removeAll(exceptions);
        Optional<Tile> exclude = result.stream().min(Comparator.comparingInt(mapper::apply));
        if (exclude.isPresent()) {
            removeTile(exclude.get());
            return exclude.get();
        }
        return null;
    }

    public Tile includeMostUsefulTile(Predicate<Tile> predicate, Function<Tile, Integer> mapper) {
        Optional<ShnyPair<Tile, Integer>> _o = getBrinkWithImportance(predicate, mapper).stream()
                .max(Comparator.comparingInt(o -> o.second));
        if (_o.isPresent()) {
            return _o.get().first;
        }
        return null;
    }

    public List<Tile> getBorder() {
        return tiles.stream().filter(tile -> !tile.getNeighbours(t -> !this.contains(t)).isEmpty())
                .collect(Collectors.toList());
    }
}
