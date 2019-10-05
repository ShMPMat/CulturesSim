package simulation.space.resource;

import extra.ProbFunc;
import simulation.World;
import simulation.culture.Event;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectTag;
import simulation.culture.thinking.meaning.Meme;
import simulation.space.Tile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents consumable objects found in the world.
 */
public class Resource { //TODO events of merging and stuff
    private int _hash;
    private boolean isMarkedInTile = true;
    int amount;
    private Tile tile;
    protected ResourceCore resourceCore;
    private double spreadProbability;

    Resource(ResourceCore resourceCore, int amount) {
        this.spreadProbability = resourceCore.defaultSpreadability;
        this.amount = amount;
        this.resourceCore = resourceCore;
        computeHash();
        setTile(null);
    }

    Resource(String[] tags, int efficiencyCoof, int amount, World world) {
        this(new ResourceCore(tags, efficiencyCoof, world), amount);
    }

    public Resource(String[] tags, int efficiencyCoof, World world) {
        this(tags, efficiencyCoof, 100 + ProbFunc.randomInt(10), world);
    }

    public Resource(ResourceCore resourceCore) {
        this(resourceCore, resourceCore.defaultAmount);
    }

    void computeHash() {
        _hash = Objects.hash(getFullName(), resourceCore.getTags());
    }

    public void actualizeLinks() {
        resourceCore.actualizeLinks();
    }

    public String getName() {
        return resourceCore.name;
    }

    public int getAmount() {
        return amount;
    }

    public String getFullName() {
        return resourceCore.name + resourceCore.meaningPrefix;
    }

    public int getEfficiencyCoof() {
        return resourceCore.efficiencyCoof;
    }

    public double getSpreadAbility() {
        return resourceCore.defaultSpreadability;
    }

    public Collection<AspectTag> getTags() {
        return resourceCore.getTags();
    }

    public Resource getPart(int part) {
        int result = (amount > part ? part : amount);
        amount -= result;
        return copy(result);
    }

    public Resource getCleanPart(int part) {
        int result = (amount > part ? part : amount);
        amount -= result;
        return cleanCopy(result);
    }

    public boolean hasMeaning() {
        return resourceCore.isHasMeaning();
    }

    public Resource merge(Resource resource) {
        if (!resource.getName().equals(getName())) {
            System.err.println("Different resource tried to merge.");
            return this;
        }
        addAmount(resource.amount);
        return this;
    }

    public void setTile(Tile tile) {
        if (resourceCore.isMovable()) {
            this.tile = tile;
            return;
        }
        if (this.tile == null) {
            this.tile = tile;
        } else {
            this.tile = tile;
        }
    }

    public boolean isMovable() {
        return resourceCore.isMovable();
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
        if (resource.isMovable()) {
            return resource;
        }
        if (tile != null) {
            tile.addDelayedResource(resource);
        }
        return resource;
    }

    public Resource insertMeaning(Meme meaning, Aspect aspect) {
        return new Resource(resourceCore.insertMeaning(meaning, aspect), amount);
    }

    public boolean update() {
        if (amount <= 0) {
            getTile().removeResource(this);
            return false;
        }
        if (ProbFunc.getChances(spreadProbability)) {
            expand();
        }
        return true;
    }

    public void addAmount(int amount) {
        this.amount += amount;
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
        Tile newTile = ProbFunc.randomTileOnBrink(l, tile -> true);
        if (newTile == null) {
            return false;
        }
        Resource resource = cleanCopy();
        resource.tile = null;
        newTile.addResource(resource);
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
                (isMovable() || tile == null || resource.tile == null || tile.equals(resource.tile));
    }

    @Override
    public int hashCode() {
        return resourceCore.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Resource " + getFullName() +
                (getTile() != null ? " on tile " + getTile().x + " " + getTile().y : "") + ", efficiency coof - " + resourceCore.efficiencyCoof +
                ", spread probability - " + spreadProbability + ", amount - " + amount + ", tags: ");
        for (AspectTag aspectTag : resourceCore.getTags()) {
            stringBuilder.append(aspectTag.name).append(" ");
        }
        return stringBuilder.toString();
    }

    public void setHasMeaning(boolean b) {
        resourceCore.setHasMeaning(b);
    }

    public Tile getTile() {
        return tile;
    }
}
