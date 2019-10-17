package simulation.space.resource;

import extra.ProbFunc;
import simulation.World;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectTag;
import simulation.culture.thinking.meaning.Meme;
import simulation.space.Tile;

import java.util.*;

/**
 * Represents consumable objects found in the world.
 */
public class Resource { //TODO events of merging and stuff
    private String createdOn = "-1";
    private int _hash;
    private boolean isMarkedInTile = true;
    int amount;
    private Tile tile;
    ResourceCore resourceCore;

    private int deathTurn = 0;
    private double deathPart = 1;

    Resource(ResourceCore resourceCore, int amount) {
        this.createdOn = resourceCore.getGenome().world.getTurn();
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

    public int getEfficiencyCoof() {
        return resourceCore.getEfficiencyCoof();
    }

    public double getSpreadProbability() {
        return resourceCore.getSpreadProbability();
    }

    public Collection<AspectTag> getTags() {
        return resourceCore.getTags();
    }

    public Resource getPart(int part) {
        int result;
        double prob = /*Math.random() * 0.5*/ 1; //TODO change when groups start to die from starvation
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

    public Genome getGemome() {
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
            System.err.println("Unmovable resource being moved!");
            this.tile = tile;
        }
    }

    public void setHasMeaning(boolean b) {
        resourceCore.setHasMeaning(b);
    }

    public Resource merge(Resource resource) {
        if (!resource.getBaseName().equals(getBaseName())) {
            System.err.println("Different resource tried to merge.");
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

    public boolean update() {
        if (deathTurn >= resourceCore.getDeathTime()) {
            amount -= deathPart*amount;
            deathTurn = 0;
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
        return true;
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
        Tile newTile = ProbFunc.randomTileOnBrink(l, tile -> tile.canSettle(getGemome()));
        if (newTile == null) {
            return false;
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
                (getTile() != null ? " on tile " + getTile().x + " " + getTile().y : "") + ", efficiency coof - " + resourceCore.getEfficiencyCoof() +
                ", spread probability - " + getSpreadProbability() + ", amount - " + amount + ", tags: ");
        for (AspectTag aspectTag : resourceCore.getTags()) {
            stringBuilder.append(aspectTag.name).append(" ");
        }
        return stringBuilder.toString();
    }
}
