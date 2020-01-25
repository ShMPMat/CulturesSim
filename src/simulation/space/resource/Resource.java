package simulation.space.resource;

import extra.ProbFunc;
import simulation.culture.Event;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectTag;
import simulation.culture.thinking.meaning.Meme;
import simulation.space.Tile;
import simulation.space.resource.dependency.ResourceDependency;
import simulation.space.resource.dependency.ResourceDependencyDep;

import java.util.*;
import java.util.stream.Collectors;

import static simulation.Controller.*;

/**
 * Represents consumable objects found in the world.
 */
public class Resource {//TODO check parts it seems that simple Plant has Fruits of ColdPlant
    /**
     * How many instances are in this Resources.
     */
    int amount;
    ResourceCore resourceCore;

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
    /**
     * How many additional years added to this Resource due to bad environment.
     * Large numbers result in sooner death.
     */
    private int deathOverhead = 0;
    /**
     * What part of this Resource will be destroyed on the next death.
     */
    private double deathPart = 1;
    private List<Event> events = new ArrayList<>();

    Resource(ResourceCore resourceCore, int amount) {
        this.amount = amount;
        this.resourceCore = resourceCore;
        computeHash();
        setTile(null);
        events.add(new Event(Event.Type.Creation,
                "Resource was created", "name", getFullName()));
    }

    Resource(String[] tags, int amount) {
        this(new ResourceCore(tags), amount);
    }

    public Resource(String[] tags) {
        this(tags, 100 + ProbFunc.randomInt(10));
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

    public int getBaseDesireability() {
        return getGenome().getBaseDesirability();
    }

    public double getSpreadProbability() {
        return resourceCore.getSpreadProbability();
    }

    public List<AspectTag> getTags() {
        return resourceCore.getTags();
    }

    public int getTagLevel(AspectTag tag) {
        int index = getTags().indexOf(tag);
        return index == -1 ? 0 : getTags().get(index).level;
    }

    /**
     * Returns part of this resource and subtracts its amount from this resource amount;
     * @param part what amount of this Resource is requested.
     * @return Copy of this Resource with amount equal or less than requested.
     * Exact amount depends on current amount of this Resource.
     */
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
//            events.add(new Event(Event.Type.Move, "Resource was moved", "name", getFullName(),
//                    "tile", tile));
            return;
        }
        if (this.tile == null || deathTurn == 0) {
            this.tile = tile;
//            events.add(new Event(Event.Type.Move, "Resource was moved", "name", getFullName(),
//                    "tile", tile));
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
        if (this == resource) {
            int i = 0;//TODO this happens, yes
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
        tile.addDelayedResource(resource);
        this.amount = 0;
        return resource;
    }

    public boolean update() {
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
            if (amount > 100000) {//TODO debug off
//                if (tile.getType() != Tile.Type.Water &&
//                        tile.getResource("Water").getAmount() < 5) {
//                    tile.addDelayedResource(session.world.getPoolResource("Water").copy(amount / 100));
//                    amount /= 100;
//                }
            }
            if (tile.getTemperature() < 0) {
                tile.addDelayedResource(session.world.getPoolResource("Snow").copy(amount / 2));
                amount -= amount / 2;
            }
        } else if (getSimpleName().equals("Water")) {
            if (!tile.getNeighbours(t -> t.getType() == Tile.Type.Water || t.fixedWater).isEmpty()) {
                tile.fixedWater = true;
            } else {
                tile.fixedWater = false;
//            if ((tile.x + " " + tile.y).equals("21 118")) {
//                int i = 0;
//            }
                if (tile.getType() != Tile.Type.Water && tile.getNeighbours(t -> t.getType() == Tile.Type.Water).isEmpty()) {
                    List<Tile> tiles = tile.getNeighbours(t -> t.getLevelWithWater() <= tile.getLevelWithWater());
                    List<Tile> tilesWithWater = tiles.stream().filter(t -> t.getResourcesWithMoved().contains(this))
                            .collect(Collectors.toList());
                    if (tilesWithWater.isEmpty()) {
                        if (!tiles.isEmpty()) {
                            int min = tiles.stream().min(Comparator.comparingInt(Tile::getLevelWithWater)).get()
                                    .getLevelWithWater();
                            ProbFunc.randomElement(tiles.stream().filter(t -> t.getLevelWithWater() == min).collect(Collectors.toList()))
                                    .addDelayedResource(getCleanPart(amount <= 1 ? 1 : 1));
                        }
                    } else {
                        int size = tilesWithWater.size();
                        tilesWithWater.sort(Comparator.comparingInt(Tile::getLevelWithWater));
                        for (int i = 0; i < size; i++) {
                            if (amount == 0) {
                                break;
                            }
                            if (tilesWithWater.get(i).getResource("Water").getAmount() > 1) {//TODO more water in deeps but not much water in rivers
                                continue;
                            }
                            tilesWithWater.get(i).addDelayedResource(getCleanPart(1));
                        }
                    }
                }
            }
        } else if (getSimpleName().equals("Snow")) {
            if (tile.getType() == Tile.Type.Mountain && ProbFunc.getChances(1)) {
//                if ((tile.x + " " + tile.y).equals("21 118")) {
//                    int i = 0;
//                }
                Resource water = session.world.getPoolResource("Water").copy(2);
                if (tile.getResource(water).getAmount() < 2 && (tile.getNeighboursInRadius(t ->
                        t.getType() == Tile.Type.Mountain &&
                        t.getResource(this).getAmount() != 0 && t.getResource(water).getAmount() != 0, 5).isEmpty() ||
                        tile.getResource(water).getAmount() != 0)) {
                    tile.addDelayedResource(water);
                }
            }
        }
        distribute();
        return true;
    }

    private void distribute() {
        if (getGenome().canMove() && amount > getGenome().getNaturalDensity()) {
            List<Tile> tiles = tile.getNeighbours(t -> getGenome().isAcceptable(t));
            tiles.sort(Comparator.comparingInt(tile -> tile.getResource(this).amount));
            for (Tile neighbour: tiles) {
                if (amount <= getGenome().getNaturalDensity() / 2) {
                    break;
                }
                int part = Math.min(amount - getGenome().getNaturalDensity() / 2,
                        getGenome().getNaturalDensity() - neighbour.getResource(this).amount);
                part = part <= 0 ? (amount - getGenome().getNaturalDensity() / 2) / tiles.size() : part;
                neighbour.addDelayedResource(getCleanPart(part));
            }
        }
    }

    public void addAmount(int amount) {
        if (amount > 0) {
            deathPart = (this.amount * deathPart) / (this.amount + amount);
        }
//        events.add(new Event(Event.Type.Change, "Resource amount increased", "name", getFullName(),
//                "oldAmount", this.amount, "newAmount", this.amount + amount));
        this.amount += amount;
    }

    public List<Resource> applyAspect(Aspect aspect) {
        return resourceCore.applyAspect(aspect);
    }

    public boolean hasApplicationForAspect(Aspect aspect) {
        return resourceCore.hasApplicationForAspect(aspect);
    }

    public void setAmount(int amount) {
        this.amount = amount;
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
