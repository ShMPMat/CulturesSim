package simulation.space.resource;

import simulation.space.SpaceData;
import simulation.space.Tile;
import simulation.space.resource.dependency.ResourceDependency;
import simulation.space.resource.dependency.TemperatureMax;
import simulation.space.resource.dependency.TemperatureMin;

import java.util.ArrayList;
import java.util.List;


public class Genome {
    private String name;
    private Type type;
    private boolean isMutable;
    private boolean canMove;
    private boolean willResist;
    private double size;
    private int naturalDensity;
    private List<Resource> parts = new ArrayList<>();
    private List<ResourceDependency> dependencies;
    private double spreadProbability;

    private boolean isMovable;
    private boolean hasLegacy;
    protected int deathTime;
    private int defaultAmount;
    private int temperatureMin;
    private int temperatureMax;
    private ResourceCore legacy;
    protected ResourceCore templateLegacy;
    private List<ResourceTag> tags;

    private Material primaryMaterial;
    private List<Material> secondaryMaterials;
    private int baseDesirability;

    Genome(String name, Type type, double size, double spreadProbability, int temperatureMin, int temperatureMax,
           int baseDesirability, boolean canMove, boolean isMutable, boolean isMovable, boolean willResist,
           boolean hasLegacy, int deathTime, int defaultAmount, ResourceCore legacy, ResourceCore templateLegacy,
           List<ResourceDependency> dependencies,
           List<ResourceTag> tags,
           Material primaryMaterial,
           List<Material> secondaryMaterials
           ) {
        this.name = name;
        this.type = type;
        this.baseDesirability = baseDesirability;
        this.spreadProbability = spreadProbability;
        this.temperatureMin = temperatureMin;
        this.temperatureMax = temperatureMax;
        this.tags = new ArrayList<>(tags);
        this.dependencies = new ArrayList<>(dependencies);
        this.dependencies.add(new TemperatureMin(temperatureMin, 2));
        this.dependencies.add(new TemperatureMax(temperatureMax, 2));
        setLegacy(legacy);
        this.templateLegacy = templateLegacy;
        this.size = size;
        this.canMove = canMove;
        this.isMutable = isMutable;
        this.isMovable = isMovable;
        this.willResist = willResist;
        this.hasLegacy = hasLegacy;
        this.deathTime = deathTime;
        this.defaultAmount = defaultAmount;
        naturalDensity = (int) Math.ceil(SpaceData.INSTANCE.getData().getResourceDenseCoefficient() * defaultAmount);
        if (naturalDensity > 1000000000) {
            System.err.println("Very high density in Genome " + name + " - " + naturalDensity);
        }
        setPrimaryMaterial(primaryMaterial);
        computePrimaryMaterial();
        this.secondaryMaterials = new ArrayList<>(secondaryMaterials);
    }

    Genome(Genome genome) {
        this(genome.name, genome.type, genome.size, genome.spreadProbability, genome.temperatureMin, genome.temperatureMax,
                genome.baseDesirability, genome.canMove, genome.isMutable, genome.isMovable, genome.willResist,
                genome.hasLegacy, genome.deathTime, genome.defaultAmount, genome.legacy, genome.templateLegacy,
                genome.dependencies,
                genome.tags,
                genome.primaryMaterial,
                genome.secondaryMaterials
        );
        genome.parts.forEach(this::addPart);
        genome.tags.forEach(this::addResourceTag);
    }

    private void computePrimaryMaterial() {
        if (parts.size() == 1) {
            primaryMaterial = parts.get(0).resourceCore.getMaterials().get(0);
        }
    }

    public List<Material> getSecondaryMaterials() {
        return secondaryMaterials;
    }

    public Material getPrimaryMaterial() {
        return primaryMaterial;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
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

    void setSpreadProbability(double spreadProbability) {
        this.spreadProbability = spreadProbability;
    }

    int getDefaultAmount() {
        return defaultAmount;
    }

    public boolean willResist() {
        return willResist;
    }

    ResourceCore getLegacy() {
        return legacy;
    }

    public ResourceCore getTemplateLegacy() {
        return templateLegacy;
    }

    public void setWillResist(boolean willResist) {
        this.willResist = willResist;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLegacy(ResourceCore legacy) {
        this.legacy = legacy;
    }

    public int getTemperatureMin() {
        return temperatureMin;
    }

    public int getTemperatureMax() {
        return temperatureMax;
    }

    String getLegacyPostfix() {
        return (templateLegacy == null ? "" : "_of_" + templateLegacy.getGenome().getName() + templateLegacy.getLegacyPostfix()) +
                (legacy == null ? "" : "_of_" + legacy.getGenome().getName() + legacy.getLegacyPostfix());
    }

    int getDeathTime() {
        return deathTime;
    }

    public double getMass() {
        return primaryMaterial == null ? 0 : primaryMaterial.getDensity() * size * size * size;
    }

    public int getBaseDesirability() {
        return baseDesirability;
    }

    public int getNaturalDensity() {
        return naturalDensity;
    }

    public List<ResourceDependency> getDependencies() {
        return dependencies;
    }

    public List<ResourceTag> getTags() {
        return tags;
    }

    public boolean isAcceptable(Tile tile) {
        return dependencies.stream().filter(ResourceDependency::isNecessary)
                .allMatch(d -> d.satisfactionPercent(tile, null) == 1);
    }

    public boolean isOptimal(Tile tile) {
        return isAcceptable(tile) && dependencies.stream().filter(d -> !d.isPositive())
                .allMatch(d -> d.satisfactionPercent(tile, null) >= 0.9);
    }

    boolean isMovable() {
        return isMovable;
    }

    boolean isMutable() {
        return isMutable;
    }

    public boolean canMove() {
        return canMove;
    }

    boolean hasLegacy() {
        return hasLegacy;
    }

    public void setPrimaryMaterial(Material primaryMaterial) {
        this.primaryMaterial = primaryMaterial;
    }

    public void setMutable(boolean mutable) {
        isMutable = mutable;
    }

    /**
     * Adds part to Genome.
     *
     * @param part Part which will be added. Legacy must by ALREADY inserted inside it.
     */
    void addPart(Resource part) {
        int i = parts.indexOf(part);
        if (i == -1) {
            parts.add(part);
            computePrimaryMaterial();
        } else {
            parts.get(i).addAmount(part.getAmount());
        }
    }

    void addDependency(ResourceDependency dependency) {
        dependencies.add(dependency);
    }

    void addResourceTag(ResourceTag tag) {
        tags.add(tag);
    }

    public enum Type {
        Plant,
        Animal,
        Mineral,
        None
    }
}
