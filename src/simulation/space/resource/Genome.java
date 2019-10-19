package simulation.space.resource;

import extra.ProbFunc;
import simulation.World;
import simulation.space.Tile;

import java.util.ArrayList;
import java.util.List;

public class Genome {
    World world;
    private int efficiencyCoof;
    private String name;
    private boolean isMutable;
    private double size;
    private List<Resource> parts;
    private double spreadProbability;
    private boolean isMovable;
    private boolean isTemplate;
    private boolean hasLegacy;
    private int deathTime;
    private int defaultAmount;
    private int temperatureMin;
    private int temperatureMax;
    private ResourceCore legacy;

    private Material _primaryMaterial;
    private int _mutationCount = 0;

    Genome(String name, double size, double spreadProbability, int temperatureMin, int temperatureMax, boolean isMutable,
                  boolean isMovable, boolean isTemplate, boolean hasLegacy, int deathTime, int defaultAmount,
                  int efficiencyCoof, ResourceCore legacy, World world) {
        this.name = name;
        this.world = world;
        this.spreadProbability = spreadProbability;
        this.temperatureMin = temperatureMin;
        this.temperatureMax = temperatureMax;
        this.parts = new ArrayList<>();
        setLegacy(legacy);
        this.size = size;
        this.isMutable = isMutable;
        this.isMovable = isMovable;
        this.isTemplate = isTemplate;
        this.hasLegacy = hasLegacy;
        this.deathTime = deathTime;
        this.defaultAmount = defaultAmount;
        this.efficiencyCoof = efficiencyCoof;
        _primaryMaterial = null;
        computePrimaryMaterial();
    }

    Genome(Genome genome) {
        this(genome.name, genome.size, genome.spreadProbability, genome.temperatureMin, genome.temperatureMax,
                genome.isMutable, genome.isMovable, genome.isTemplate, genome.hasLegacy, genome.deathTime,
                genome.defaultAmount, genome.efficiencyCoof, genome.legacy, genome.world);
        genome.parts.forEach(this::addPart);
    }

    private void computePrimaryMaterial() {
        if (_primaryMaterial == null && !parts.isEmpty()) {
            _primaryMaterial = parts.get(0).resourceCore.getMaterials().get(0);
        }
    }

    Material getPrimaryMaterial() {
        return _primaryMaterial;
    }

    public String getName() {
        return name;
    }

    List<Resource> getParts() {
        return parts;
    }

    public double getSize() {
        return size;
    }

    double getSpreadProbability() {
        return spreadProbability;
    }

    int getDefaultAmount() {
        return defaultAmount;
    }

    ResourceCore getLegacy() {
        return legacy;
    }

    public int getTemperatureMin() {
        return temperatureMin;
    }

    public int getTemperatureMax() {
        return temperatureMax;
    }

    int getEfficiencyCoof() {
        return efficiencyCoof;
    }

    String getLegacyPostfix() {
        return legacy == null ? "" : "_of_" + legacy.getGenome().getName() + legacy.getLegacyPostfix();
    }

    public boolean isAcceptable(Tile tile) {
        return tile.getType() != Tile.Type.Water && (tile.getTemperature() >= temperatureMin && tile.getTemperature() <= temperatureMax);
    }

    boolean isMovable() {
        return isMovable;
    }

    boolean isTemplate() {
        return isTemplate;
    }

    boolean hasLegacy() {
        return hasLegacy;
    }

    boolean isMutable() {
        return isMutable;
    }

    int getDeathTime() {
        return deathTime;
    }

    void addPart(Resource part) { //TODO inserted without legacy insertion write it in the documentation
        int i = parts.indexOf(part);
        if (i == -1) {
            parts.add(part);
            computePrimaryMaterial();
        } else {
            parts.get(i).addAmount(part.getAmount());
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLegacy(ResourceCore legacy) {
        this.legacy = legacy;
    }

    public void setMutable(boolean mutable) {
        isMutable = mutable;
    }

    void setTemplate(boolean template) {
        isTemplate = template;
    }

    void setSpreadProbability(double spreadProbability) {
        this.spreadProbability = spreadProbability;
    }

    public Genome mutate() {
        if (!isMutable()) {
            System.err.println("Method mutate called from non-mutable genome!");
            return null;
        }
        Genome genome = new Genome(this);
        genome.setName(name + "_Mutation" + _mutationCount);
        _mutationCount++;
        for (Resource part : parts) {
            part.amount += ProbFunc.randomInt(2) - 1;
        }
        return genome;
    }

}
