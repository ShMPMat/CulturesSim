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
    private Set<Tile> tiles;
    private Set<Tile> outerBrink = new HashSet<>();
    private Tile center;

    public Territory(Collection<Tile> tiles) {
        this.tiles = new HashSet<>();
        addAll(tiles);
    }

    public Territory() {
        this(new ArrayList<>());
    }

    public Set<Tile> getTiles() {
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

    public List<Tile> getOuterBrink() {
        return new ArrayList<>(outerBrink);
    }

    public List<Tile> getOuterBrink(Predicate<Tile> predicate) {
        return getOuterBrink().stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public List<Tile> getInnerBrink() {
        return outerBrink.stream()
                .flatMap(t -> t.getNeighbours(this::contains).stream())
                .distinct()
                .collect(Collectors.toList());
    }

    public List<Tile> getInnerBrink(Predicate<Tile> predicate) {
        return getInnerBrink().stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public List<Resource> getResourcesWithAspectTag(ResourceTag resourceTag) {
        List<Resource> resources = new ArrayList<>();
        for (Tile tile : tiles) {
            for (Resource resource : tile.getResourcePack().getResources()) {
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
            for (Resource resource : tile.getResourcePack().getResources()) {
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
            list.addAll(
                    tile.getResourcePack().getResources(res -> res.equals(resource) && !res.hasMeaning()).getResources()
            );
        }
        return list;
    }

    public int getMinTemperature() {
        return tiles.stream()
                .map(Tile::getTemperature)
                .reduce(Integer.MAX_VALUE, Math::min);
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
            outerBrink.remove(tile);
            tile.getNeighbours().forEach(this::addToOuterBrink);
        }
        if (tiles.size() == 1) {
            center = tile;
        }
    }

    private void addToOuterBrink(Tile tile) {
        if (!outerBrink.contains(tile) && !tiles.contains(tile)) {
            outerBrink.add(tile);
        }
    }

    public void removeTile(Tile tile) {
        if (tile == null) {
            return;
        }
        if (!tiles.remove(tile)) {
            return;
        }
        if (!tile.getNeighbours(this::contains).isEmpty()) {
            addToOuterBrink(tile);
        }
        tile.getNeighbours().forEach(t -> {
            if (t.getNeighbours().stream().noneMatch(this::contains)) {
                outerBrink.remove(t);
            }
        });
    }

    public Tile getMostUselessTile(Function<Tile, Integer> mapper) {
        Optional<Tile> exclude = tiles.stream().min(Comparator.comparingInt(mapper::apply));
        return exclude.orElse(null);
    }

    public Tile getMostUsefulTileOnOuterBrink(Predicate<Tile> predicate, Function<Tile, Integer> mapper) {
        Optional<Pair<Tile, Integer>> _o = getOuterBrink(predicate).stream()
                .map(t -> new Pair<>(t, mapper.apply(t)))
                .max(Comparator.comparingInt(Pair::getSecond));
        return _o.map(Pair::getFirst).orElse(null);
    }

    public boolean isEmpty() {
        return size() == 0;
    }
}
