package simulation.space.resource;

import extra.ProbFunc;
import simulation.World;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectTag;
import simulation.culture.thinking.meaning.Meme;
import simulation.space.Territory;
import simulation.space.Tile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents consumable objects found in the world.
 */
public class Resource { //TODO events of merging and stuff
    /**
     * How many instances are in this Resources.
     */
    int amount;
    ResourceCore resourceCore;

    /**
     * The turn Resource was created.
     */
    private String createdOn;
    /**
     * Precomputed hash.
     */
    private int _hash;
    /**
     * Tile on which this Resource is placed.
     */
    private Tile tile;
    /**
     * How many turns has this Resource been existing.
     */
    private int deathTurn = 0;
    private int deathOverhead = 0;
    /**
     * What part of this Resource will be destroyed on the next death.
     */
    private double deathPart = 1;

    Resource(ResourceCore resourceCore, int amount) {
        this.createdOn = resourceCore.getGenome().world.getTurn();
        this.amount = amount;
        this.resourceCore = resourceCore;
        computeHash();
        setTile(null);
    }

    Resource(String[] tags, int amount, World world) {
        this(new ResourceCore(tags, world), amount);
    }

    public Resource(String[] tags, World world) {
        this(tags, 100 + ProbFunc.randomInt(10), world);
    }

    public Resource(ResourceCore resourceCore) {
        this(resourceCore, resourceCore.getDefaultAmount());
    }

    void computeHash() {
        _hash = Objects.hash(getFullName());
    }

    public void actualizeLinks() {
        resourceCore.actualizeLinks();
    }

    public void actualizeParts() {
        resourceCore.actualizeParts();
    }

    public String getSimpleName() {
        return resourceCore.getSimpleName();
    }

    public String getBaseName() {
        return resourceCore.getBaseName();
    }

    public String getFullName() {
        return resourceCore.getBaseName() + resourceCore.getMeaningPostfix();
    }

    public int getAmount() {
        return amount;
    }

    public double getSpreadProbability() {
        return resourceCore.getSpreadProbability();
    }

    public List<AspectTag> getTags() {
        return resourceCore.getTags();
    }

    public Resource getPart(int part) {
        int result;
        double prob = Math.random() * 0.5;
        if (part <= amount * prob) {
            result = (amount > part ? part : amount);
        } else {
            result = amount * prob + 1 < amount ? (int) (amount * prob) + 1 : amount;
        }
        amount -= result;
        return copy(result);
    }

    public Resource getCleanPart(int part) {
        int result = (amount > part ? part : amount);
        amount -= result;
        return cleanCopy(result);
    }

    public Tile getTile() {
        return tile;
    }

    public Genome getGenome() {
        return resourceCore.getGenome();
    }

    public boolean isMovable() {
        return resourceCore.isMovable();
    }

    public boolean hasMeaning() {
        return resourceCore.isHasMeaning();
    }

    public void setTile(Tile tile) {
        if (resourceCore.isMovable()) {
            this.tile = tile;
            return;
        }
        if (this.tile == null || deathTurn == 0) {
            this.tile = tile;
        } else {
            System.err.println("Unmovable resource being moved! - " + getFullName());
            this.tile = tile;
        }
    }

    public void setHasMeaning(boolean b) {
        resourceCore.setHasMeaning(b);
    }

    public Resource merge(Resource resource) {
        if (!resource.getBaseName().equals(getBaseName())) {
            System.err.println("Different resource tried to merge - " + getFullName() + " and " + resource.getFullName());
            return this;
        }
        addAmount(resource.amount);
        return this;
    }

    public Resource copy() {
        return movableModificator(resourceCore.copy());
    }

    public Resource cleanCopy() {
        return resourceCore.copy();
    }

    public Resource copy(int amount) {
        return movableModificator(resourceCore.copy(amount));
    }

    public Resource cleanCopy(int amount) {
        return resourceCore.copy(amount);
    }

    public Resource fullClearCopy() {
        return resourceCore.fullCopy();
    }

    private Resource movableModificator(Resource resource) {
        if (tile != null) {
            tile.addDelayedResource(resource);
        }
        return resource;
    }

    public Resource insertMeaning(Meme meaning, Aspect aspect) {
        Resource resource = new Resource(resourceCore.insertMeaning(meaning, aspect), amount);
        if (tile == null) {
            int i = 0;
        }
        tile.addDelayedResource(resource);
        this.amount = 0;
        return resource;
    }

    public boolean update() {//TODO migration on gig numbers
        if (amount <= 0) {
            getTile().removeResource(this);
            return false;
        }
        for (ResourceDependency dependency: resourceCore.getGenome().getDependencies()) {
            double part = dependency.satisfactionPercent(tile, this);
            deathOverhead += (1 - part) * resourceCore.getDeathTime();
        }
        if (deathTurn + deathOverhead >= resourceCore.getDeathTime()) {
            amount -= deathPart*amount;
            deathTurn = 0;
            deathOverhead = 0;
            deathPart = 1;
        }
        if (amount <= 0) {
            getTile().removeResource(this);
            return false;
        }
        deathTurn++;
        if (ProbFunc.getChances(resourceCore.getSpreadProbability())) {
            expand();
        }
        if (getSimpleName().equals("Vapour")) {
            if (amount > 500) {
                if (tile.getType() != Tile.Type.Water) {
                    tile.addDelayedResource(getGenome().world.getResourceFromPoolByName("Water").copy(amount / 100));
                    amount /= 100;
                }
            }
            if (amount > 250) {
                int part = amount - 50;
                List<Tile> tiles = new ArrayList<>();
                tiles.add(tile);
                tiles.add(getGenome().world.map.get(tile.x - 1, tile.y));
                tiles.add(getGenome().world.map.get(tile.x + 1, tile.y));
                tiles.add(getGenome().world.map.get(tile.x, tile.y + 1));
                tiles.add(getGenome().world.map.get(tile.x, tile.y - 1));
                tiles.removeIf(Objects::isNull);
                tiles.sort((Comparator.comparingInt(tile -> {
                    Resource res = tile.getResources().stream().filter(r -> r.getSimpleName().equals(getSimpleName()))
                            .findFirst().orElse(null);
                    return res == null ? 0 : res.amount;
                })));
                tiles.get(0).addDelayedResource(getCleanPart(part));
            }
            if (tile.getTemperature() < 0) {
                tile.addDelayedResource(tile.getWorld().getResourceFromPoolByName("Snow").copy(amount / 2));
                amount -= amount / 2;
            }
        } else if (getSimpleName().equals("Water")) {
            if (tile.getType() != Tile.Type.Water && tile.getNeighbours(t -> t.getType() == Tile.Type.Water).isEmpty()) {
                List<Tile> tiles = tile.getNeighbours(t -> t.getLevelWithWater() <= tile.getLevelWithWater());
                List<Tile> tilesWithWater = tiles.stream().filter(t -> t.getResources().contains(this) &&
                        t.getLevelWithWater() < tile.getLevelWithWater()).collect(Collectors.toList());
                if (tilesWithWater.isEmpty()) {
                    if (!tiles.isEmpty()) {
                        ProbFunc.randomElement(tiles).addDelayedResource(getCleanPart(amount - 1));
                    }
                } else {
                    int a = amount - 1, size = tilesWithWater.size();
                    tilesWithWater.get(0).addDelayedResource(getCleanPart(a - (a / size) * (size - 1)));
                    for (int i = 1; i < size; i++) {
                        tilesWithWater.get(i).addDelayedResource(getCleanPart(a / size));
                    }
                }
            }
        }
        return true;
    }

    public void addAmount(int amount) {
        if (amount > 0) {
            deathPart = (this.amount * deathPart) / (this.amount + amount);
        }
        this.amount += amount;
    }

    public void addAmountSoftly(int amount) {
        int overhead = Math.max(0, this.amount + amount - getGenome().getNaturalDensity());
        if (overhead > 0) {
            amount -= overhead;
        }
        addAmount(amount);
        //TODO overhead must matter
    }

    public List<Resource> applyAspect(Aspect aspect) {
        return resourceCore.applyAspect(aspect);
    }

    public boolean hasApplicationForAspect(Aspect aspect) {
        return resourceCore.hasApplicationForAspect(aspect);
    }

    public List<Resource> applyAndConsumeAspect(Aspect aspect, int part) {
        Resource _r = getPart(part);
        int p = part > _r.amount ? _r.amount : part;
        List<Resource> result = _r.resourceCore.applyAspect(aspect);
        result.forEach(resource -> resource.amount *= p);
        result.forEach(this::movableModificator);
        _r.amount -= p;
        return result;
    }

    private boolean expand() {
        List<Tile> l = new ArrayList<>();
        l.add(getTile());
        Tile newTile = ProbFunc.randomTileOnBrink(l, tile -> getGenome().isAcceptable(tile) &&
                getGenome().getDependencies().stream().allMatch(dependency -> dependency.hasNeeded(tile)));
        if (newTile == null) {
            if (getGenome().getDependencies().stream().allMatch(dependency -> dependency.hasNeeded(tile))) {
                newTile = tile;
            } else {
                newTile = ProbFunc.randomTileOnBrink(l, tile -> getGenome().isAcceptable(tile));
                if (newTile == null) {
                    newTile = tile;
                }
            }
        }
        Resource resource = copy();
        resource.amount = Math.min(resourceCore.getDefaultAmount(), amount);
        newTile.addDelayedResource(resource);
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Resource resource = (Resource) o;
        return resourceCore.equals(resource.resourceCore);
    }

    public boolean fullEquals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Resource resource = (Resource) o;
        if (_hash != resource._hash) {
            return false;
        }
        return getFullName().equals(resource.getFullName()) && resourceCore.equals(resource.resourceCore) &&
                (tile == null || resource.tile == null || tile.equals(resource.tile));
    }

    @Override
    public int hashCode() {
        return resourceCore.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Resource " + getFullName() +
                (getTile() != null ? " on tile " + getTile().x + " " + getTile().y : "") + ", natural density - " +
                getGenome().getNaturalDensity() + ", spread probability - " + getSpreadProbability() + ", mass - " +
                getGenome().getMass() + ", amount - " + amount + ", tags: ");
        for (AspectTag aspectTag : resourceCore.getTags()) {
            stringBuilder.append(aspectTag.name).append(" ");
        }
        return stringBuilder.toString();
    }
}
