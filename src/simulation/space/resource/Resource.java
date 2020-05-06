package simulation.space.resource;

import extra.SpaceProbabilityFuncs;
import kotlin.Pair;
import simulation.Event;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectResult;
import simulation.culture.thinking.meaning.Meme;
import simulation.space.resource.tag.AspectImprovementTag;
import simulation.space.tile.Tile;
import simulation.space.resource.dependency.ResourceDependency;
import simulation.space.resource.tag.ResourceTag;
import simulation.space.tile.TileTag;

import java.util.*;

import static shmp.random.RandomProbabilitiesKt.*;
import static simulation.Controller.session;

public class Resource {
    protected int amount;
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
    private OwnershipMarker ownershipMarker = OwnershipMarkerKt.getFreeMarker();

    Resource(ResourceCore resourceCore, int amount) {
        this.amount = amount;
        this.resourceCore = resourceCore;
        computeHash();
        events.add(new Event(Event.Type.Creation, "Resource was created", "name", getFullName()));
    }

    public Resource(ResourceCore resourceCore) {
        this(resourceCore, resourceCore.getGenome().getDefaultAmount());
    }

    public Map<Aspect, List<Pair<Resource, Integer>>> getAspectConversion() {
        return resourceCore.getAspectConversion();
    }

    void computeHash() {
        _hash = Objects.hash(getFullName());
    }

    public String getSimpleName() {
        return getGenome().getName();
    }

    public String getBaseName() {
        return getGenome().getBaseName();
    }

    public String getFullName() {
        return getGenome().getBaseName() + resourceCore.getMeaningPostfix();
    }

    public int getAmount() {
        return amount;
    }

    public List<ResourceTag> getTags() {
        return getGenome().getTags();
    }

    public double getAspectImprovement(Aspect aspect) {
        return getTags().stream()
                .filter(t -> t instanceof AspectImprovementTag && ((AspectImprovementTag) t).getLabeler().isSuitable(aspect))
                .map(t -> ((AspectImprovementTag) t).getImprovement())
                .reduce(0.0, Double::sum) * amount  ;
    }

    public double getTagPresence(ResourceTag tag) {
        return amount * getTagLevel(tag) * getGenome().getSize();
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
        return copy(result);
    }

    public Genome getGenome() {
        return resourceCore.getGenome();
    }

    public OwnershipMarker getOwnershipMarker() {
        return ownershipMarker;
    }

    public void setOwnershipMarker(OwnershipMarker ownershipMarker) {
        this.ownershipMarker = ownershipMarker;
    }

    public boolean hasMeaning() {
        return resourceCore.getHasMeaning();
    }

    public void setHasMeaning(boolean b) {
        resourceCore.setHasMeaning(b);
    }

    public Resource merge(Resource resource) {
        if (!resource.getBaseName().equals(getBaseName())) {
            throw new RuntimeException(String.format(
                    "Different resource tried to merge - %s and %s",
                    getFullName(),
                    resource.getFullName()
            ));
        }
        if (this == resource) {
            return this;
        }
        addAmount(resource.amount);
        resource.destroy();
        return this;
    }

    public Resource copy() {
        Resource resource = resourceCore.copy();
        resource.setOwnershipMarker(ownershipMarker);
        return resource;
    }

    public Resource copy(int amount) {
        Resource resource = resourceCore.copy(amount);
        resource.setOwnershipMarker(ownershipMarker);
        return resource;
    }

    public Resource fullCopy() {
        Resource resource = resourceCore.fullCopy();
        resource.setOwnershipMarker(ownershipMarker);
        return resource;
    }

    public Resource insertMeaning(Meme meaning, AspectResult result) {
        Resource resource = new Resource(resourceCore.insertMeaning(meaning, result), amount);
        destroy();
        return resource;
    }

    public ResourceUpdateResult update(Tile tile) {
        List<Resource> result = new ArrayList<>();
        if (amount <= 0) {
            return new ResourceUpdateResult(false, result);
        }
        for (ResourceDependency dependency: resourceCore.getGenome().getDependencies()) {
            double part = dependency.satisfactionPercent(tile, this);
            deathOverhead += (1 - part) * resourceCore.getGenome().getDeathTime();
        }
        if (deathTurn + deathOverhead >= resourceCore.getGenome().getDeathTime()) {
            int deadAmount = (int) (deathPart*amount);
            amount -= deadAmount;
            deathTurn = 0;
            deathOverhead = 0;
            deathPart = 1;
            result = applyAspect(ResourcesKt.getDEATH_ASPECT(), deadAmount);
        }
        if (amount <= 0) {
            new ResourceUpdateResult(false, result);
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
        }
        distribute(tile);
        return new ResourceUpdateResult(true, result);
    }

    private void distribute(Tile tile) {
        if (getGenome().getCanMove() && amount > getGenome().getNaturalDensity()) {
            List<Tile> tiles = tile.getNeighbours(t -> getGenome().isAcceptable(t));
            tiles.sort(Comparator.comparingInt(t -> t.getResourcePack().getAmount(this)));
            for (Tile neighbour: tiles) {
                if (amount <= getGenome().getNaturalDensity() / 2) {
                    break;
                }
                int part = Math.min(amount - getGenome().getNaturalDensity() / 2,
                        getGenome().getNaturalDensity() - neighbour.getResourcePack().getAmount(this));
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

    public List<Resource> applyAspect(Aspect aspect, int part) {
        List<Resource> result = resourceCore.applyAspect(aspect);
        result.forEach(r -> r.amount *= part);
        return result;
    }

    public List<Resource> applyAspect(Aspect aspect) {
        return applyAspect(aspect, 1);
    }

    public boolean hasApplicationForAspect(Aspect aspect) {
        return resourceCore.hasApplicationForAspect(aspect);
    }

    public void destroy() {
        this.amount = 0;
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
    public boolean equals(Object o) {//TODO maybe re
        return fullEquals(o);
    }

    public boolean ownershiplessEquals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Resource resource = (Resource) o;
        if (_hash != resource._hash) {
            return false;
        }
        return getFullName().equals(resource.getFullName());
    }

    public boolean fullEquals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Resource resource = (Resource) o;
        if (_hash != resource._hash) {
            return false;
        }
        if (getFullName().equals(resource.getFullName()) && !ownershipMarker.equals(resource.ownershipMarker)) {
            int j = 0;
        }
        return getFullName().equals(resource.getFullName()) && ownershipMarker.equals(resource.ownershipMarker);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFullName(), resourceCore.hashCode(), ownershipMarker);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Resource " + getFullName() + ", natural density - " +
                getGenome().getNaturalDensity() + ", spread probability - " + getGenome().getSpreadProbability() +
                ", mass - " + getGenome().getMass() + ", amount - " + amount + ", tags: ");
        for (ResourceTag resourceTag : getTags()) {
            stringBuilder.append(resourceTag.name).append(" ");
        }
        return stringBuilder.toString();
    }
}
