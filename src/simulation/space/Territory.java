package simulation.space;

import kotlin.Pair;
import simulation.culture.aspect.Aspect;
import simulation.space.resource.tag.ResourceTag;
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

    public List<Tile> getTiles(Predicate<Tile> predicate) {
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

    public List<Tile> getBrink(Predicate<Tile> predicate) {
        return getBrink().stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public List<Pair<Tile, Integer>> getBrinkWithImportance(Predicate<Tile> predicate, Function<Tile, Integer> mapper) {
        return getBrink(predicate).stream()
                .map(t -> new Pair<>(t, mapper.apply(t)))
                .distinct()
                .collect(Collectors.toList());
    }

    public List<Resource> getResourcesWithAspectTag(ResourceTag resourceTag) {
        List<Resource> resources = new ArrayList<>();
        for (Tile tile : tiles) {
            for (Resource resource : tile.getResources()) {
                if (resource.getTags().contains(resourceTag) && !resources.contains(resource)) {
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

    public List<Resource> getResourcesWhichConverseToTag(Aspect aspect, ResourceTag tag) {
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
        Optional<Pair<Tile, Integer>> _o = getBrinkWithImportance(predicate, mapper).stream()
                .max(Comparator.comparingInt(Pair::getSecond));
        return _o.map(Pair::getFirst).orElse(null);
    }

    public List<Tile> getBorder() {
        return tiles.stream()
                .filter(tile -> !tile.getNeighbours(t -> !this.contains(t)).isEmpty())
                .collect(Collectors.toList());
    }

    public boolean isEmpty() {
        return size() == 0;
    }
}
