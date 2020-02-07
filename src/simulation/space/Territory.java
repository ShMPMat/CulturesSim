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
    private List<Tile> brink = new ArrayList<>();
    private Tile center;

    public Territory(Collection<Tile> tiles) {
        this.tiles = new ArrayList<>();
        addAll(tiles);

    }

    public Territory() {
        this(new ArrayList<>());
    }

    public List<Tile> getTiles() {
        return tiles;
    }

    public List<Tile> getTilesWithPredicate(Predicate<Tile> predicate) {
        return tiles.stream().filter(predicate).collect(Collectors.toList());
    }

    public Tile getCenter() {
        return center;
    }

    public void setCenter(Tile newCenter) {
        center = newCenter;
        add(center);
    }

    public List<Tile> getBrink() {
        return new ArrayList<>(brink);
    }

    public List<Tile> getBrinkWithCondition(Predicate<Tile> predicate) {
        List<Tile> collect = getBrinkWithImportance(predicate, tile -> 0).stream().map(pair -> pair.first)
                .collect(Collectors.toList());
        return collect;
    }

    public List<ShnyPair<Tile, Integer>> getBrinkWithImportance(Predicate<Tile> predicate, Function<Tile, Integer> mapper) {
        Set<ShnyPair<Tile, Integer>> goodTiles = new HashSet<>();
        goodTiles.addAll(brink.stream().filter(predicate).map(tile1 -> new ShnyPair<>(tile1, mapper.apply(tile1)))
                .collect(Collectors.toList()));
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

    public int getMinTemperature() {
        return tiles.stream().reduce(Integer.MAX_VALUE, (x, y) -> Math.min(x, y.getTemperature()), Integer::compareTo);
    }

    public int size() {
        return tiles.size();
    }

    public boolean contains(Tile tile) {
        return tiles.contains(tile);
    }

    public void addAll(Territory territory) {
        addAll(territory.tiles);
    }

    public void addAll(Collection<Tile> tiles) {
        tiles.forEach(this::add);
    }

    public void add(Tile tile) {
        if (tile == null) {
            return;
        }
        if (!tiles.contains(tile)) {
            tiles.add(tile);
            brink.remove(tile);
            tile.getNeighbours().forEach(this::addToBrink);
        }
        if (tiles.size() == 1) {
            center = tile;
        }
    }

    private void addToBrink(Tile tile) {
        if (!brink.contains(tile) && !tiles.contains(tile)) {
            brink.add(tile);
        }
    }

    public void removeTile(Tile tile) {
        if (tile == null) {
            return;
        }
        tile.group = null;
        if (!tiles.remove(tile)) {
            return;
        }
        tile.getNeighbours().forEach(t -> {
            if (t.getNeighbours().stream().noneMatch(this::contains)) {
                brink.remove(t);
            }
        });
    }

    public Tile excludeMostUselessTileExcept(Collection<Tile> exceptions, Function<Tile, Integer> mapper) {
        if (size() <= 1) {
            return null;
        }
        Set<Tile> result = new HashSet<>(tiles);
        result.removeAll(exceptions);
        Optional<Tile> exclude = result.stream().min(Comparator.comparingInt(mapper::apply));
        if (exclude.isPresent()) {
            removeTile(exclude.get());
            return exclude.get();
        }
        return null;
    }

    public Tile getMostUsefulTile(Predicate<Tile> predicate, Function<Tile, Integer> mapper) {
        Optional<ShnyPair<Tile, Integer>> _o = getBrinkWithImportance(predicate, mapper).stream()
                .max(Comparator.comparingInt(o -> o.second));
        return _o.map(tileIntegerShnyPair -> tileIntegerShnyPair.first).orElse(null);
    }

    public List<Tile> getBorder() {
        return tiles.stream().filter(tile -> !tile.getNeighbours(t -> !this.contains(t)).isEmpty())
                .collect(Collectors.toList());
    }

    public boolean isEmpty() {
        return size() == 0;
    }
}
