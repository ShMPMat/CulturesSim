package simulation.space.resource;

import extra.SpaceProbabilityFuncs;
import simulation.culture.Event;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectResult;
import simulation.culture.thinking.meaning.Meme;
import simulation.space.Tile;
import simulation.space.resource.dependency.ResourceDependency;
import simulation.space.resource.tag.ResourceTag;

import java.util.*;
import java.util.stream.Collectors;

import static shmp.random.RandomCollectionsKt.*;
import static shmp.random.RandomProbabilitiesKt.*;
import static simulation.Controller.session;

public class Resource {
    private int amount;
    ResourceCore resourceCore;

    /**
     * Precomputed hash.
     */
    private int _hash;
    /**
     * How many turns has this Resource been existing.
     */
    private int deathTurn = 0;
    /**
     * How many additional years added to this Resource due to bad environment.
     * Large numbers results in sooner death.
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
        events.add(new Event(Event.Type.Creation, "Resource was created", "name", getFullName()));
    }

    public Resource(ResourceCore resourceCore) {
        this(resourceCore, resourceCore.getGenome().getDefaultAmount());
    }

    void computeHash() {
        _hash = Objects.hash(getFullName());
    }

    public String getSimpleName() {
        return resourceCore.getGenome().getName();
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

    public List<ResourceTag> getTags() {
        return resourceCore.getTags();
    }

    public int getTagLevel(ResourceTag tag) {
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
        double prob = session.random.nextDouble() * 0.5;
        if (part <= amount * prob) {
            result = Math.min(amount, part);
        } else {
            result = amount * prob + 1 < amount ? (int) (amount * prob) + 1 : amount;
        }
        amount -= result;
        return copy(result);
    }

    public Resource getCleanPart(int part) {
        int result = Math.min(amount, part);
        amount -= result;
        return cleanCopy(result);
    }

    public Genome getGenome() {
        return resourceCore.getGenome();
    }

    public boolean hasMeaning() {
        return resourceCore.isHasMeaning();
    }

    public void setHasMeaning(boolean b) {
        resourceCore.setHasMeaning(b);
    }

    public Resource merge(Resource resource) {
        if (!resource.getBaseName().equals(getBaseName())) {
            throw new RuntimeException(String.format("Different resource tried to merge - %s and %s",
                    getFullName(), resource.getFullName()));
        }
        if (this == resource) {
            return this;
//            int i = 0;//TODO this happens, yes
        }
        addAmount(resource.amount);
        resource.amount = 0;
        return this;
    }

    public Resource copy() {
        return resourceCore.copy();
    }

    public Resource cleanCopy() {
        return resourceCore.copy();
    }

    public Resource copy(int amount) {
        return resourceCore.copy(amount);
    }

    public Resource cleanCopy(int amount) {
        return resourceCore.copy(amount);
    }

    public Resource fullClearCopy() {
        return resourceCore.fullCopy();
    }

    public Resource insertMeaning(Meme meaning, AspectResult result) {
        Resource resource = new Resource(resourceCore.insertMeaning(meaning, result), amount);
        this.amount = 0;
        return resource;
    }

    public boolean update(Tile tile) {
        if (amount <= 0) {
            return false;
        }
        for (ResourceDependency dependency: resourceCore.getGenome().getDependencies()) {
            double part = dependency.satisfactionPercent(tile, this);
            deathOverhead += (1 - part) * resourceCore.getGenome().getDeathTime();
        }
        if (deathTurn + deathOverhead >= resourceCore.getGenome().getDeathTime()) {
            amount -= deathPart*amount;
            deathTurn = 0;
            deathOverhead = 0;
            deathPart = 1;
        }
        if (amount <= 0) {
            return false;
        }
        deathTurn++;
        if (testProbability(resourceCore.getGenome().getSpreadProbability(), session.random)) {
            expand(tile);
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
                tile.addDelayedResource(session.world.getResourcePool().get("Snow").copy(amount / 2));
                amount -= amount / 2;
            }
        } else if (getSimpleName().equals("Water")) {
            if (!tile.getNeighbours(t -> t.getType() == Tile.Type.Water || t.fixedWater).isEmpty()) {
                tile.fixedWater = true;
            } else {
                tile.fixedWater = false;
                if (tile.getType() != Tile.Type.Water && tile.getNeighbours(t -> t.getType() == Tile.Type.Water).isEmpty()) {
                    List<Tile> tiles = tile.getNeighbours(t -> t.getLevelWithWater() <= tile.getLevelWithWater());
                    List<Tile> tilesWithWater = tiles.stream().filter(t -> t.getResourcesWithMoved().contains(this))
                            .collect(Collectors.toList());
                    if (tilesWithWater.isEmpty()) {
                        if (!tiles.isEmpty()) {
                            int min = tiles.stream().min(Comparator.comparingInt(Tile::getLevelWithWater)).get()
                                    .getLevelWithWater();
                            randomElement(
                                    tiles.stream()
                                            .filter(t -> t.getLevelWithWater() == min)
                                            .collect(Collectors.toList()),
                                    session.random
                            )
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
            if (tile.getType() == Tile.Type.Mountain && testProbability(1, session.random)) {
                Resource water = session.world.getResourcePool().get("Water").copy(2);
                if (tile.getResource(water).getAmount() < 2 && (tile.getNeighboursInRadius(t ->
                        t.getType() == Tile.Type.Mountain &&
                        t.getResource(this).getAmount() != 0 && t.getResource(water).getAmount() != 0, 5).isEmpty() ||
                        tile.getResource(water).getAmount() != 0)) {
                    tile.addDelayedResource(water);
                }
            }
        }
        distribute(tile);
        return true;
    }

    private void distribute(Tile tile) {
        if (getGenome().canMove() && amount > getGenome().getNaturalDensity()) {
            List<Tile> tiles = tile.getNeighbours(t -> getGenome().isAcceptable(t));
            tiles.sort(Comparator.comparingInt(t -> t.getResource(this).amount));
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
        int p = Math.min(part, _r.amount);
        List<Resource> result = _r.resourceCore.applyAspect(aspect);
        result.forEach(resource -> resource.amount *= p);
        _r.amount -= p;
        return result;
    }

    private boolean expand(Tile tile) {
        List<Tile> l = new ArrayList<>();
        l.add(tile);
        Tile newTile = SpaceProbabilityFuncs.randomTileOnBrink(l, t -> getGenome().isAcceptable(t)
                && getGenome().getDependencies().stream().allMatch(dependency -> dependency.hasNeeded(t)));
        if (newTile == null) {
            if (getGenome().getDependencies().stream().allMatch(dependency -> dependency.hasNeeded(tile))) {
                newTile = tile;
            } else {
                newTile = SpaceProbabilityFuncs.randomTileOnBrink(l, t -> getGenome().isAcceptable(t));
                if (newTile == null) {
                    newTile = tile;
                }
            }
        }
        Resource resource = copy();
        resource.amount = Math.min(resourceCore.getGenome().getDefaultAmount(), amount);
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
        return getFullName().equals(resource.getFullName()) && resourceCore.equals(resource.resourceCore);
    }

    @Override
    public int hashCode() {
        return resourceCore.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Resource " + getFullName()
                + ", natural density - " +
                getGenome().getNaturalDensity() + ", spread probability - " + getGenome().getSpreadProbability() + ", mass - " +
                getGenome().getMass() + ", amount - " + amount + ", tags: ");
        for (ResourceTag resourceTag : resourceCore.getTags()) {
            stringBuilder.append(resourceTag.name).append(" ");
        }
        return stringBuilder.toString();
    }
}
