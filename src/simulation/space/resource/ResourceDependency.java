package simulation.space.resource;

import simulation.space.Tile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResourceDependency {//TODO please, write an interface and BUNCH OF IMPLEMENTATIONS
    private List<String> resourceNames = new ArrayList<>();
    private List<String> materialNames = new ArrayList<>();
    private double amount;
    private boolean isNecessary;
    private double deprivationCoefficient;
    private int currentAmount = 0;
    private Type type;
    public Set<String> lastConsumed = new HashSet<>();

    public ResourceDependency(Type type, double amount, double deprivationCoefficient, boolean isNecessary,
                              List<String> names) {
        for (String name: names) {
            if (name.charAt(0) == '@') {
                this.materialNames.add(name.substring(1));
            } else {
                this.resourceNames.add(name);
            }
        }
        this.amount = amount;
        this.deprivationCoefficient = deprivationCoefficient;
        this.type = type;
        this.isNecessary = isNecessary;
    }

    public List<String> getResourceNames() {
        return resourceNames;
    }

    public List<String> getMaterialNames() {
        return materialNames;
    }

    public double satisfactionPercent(Tile tile, Resource resource) {
        double result;
        if (type == Type.TEMPERATURE_MAX || type == Type.TEMPERATURE_MIN) {
            result = tile.getTemperature() - amount;
            result = type == Type.TEMPERATURE_MIN ? -result : result;
            result = 1 / Math.sqrt(Math.max(0, result) + 1);
        } else if (type == Type.AVOID_TILES) {
            return resourceNames.stream().noneMatch(name -> Tile.Type.valueOf(name) == tile.getType()) ? 1 : 0;
        } else {
            double _amount = amount * (resource == null ? 1 : resource.amount);
            for (Resource res : tile.getAccessibleResources()) {
                if (res.equals(resource)) {
                    continue;
                }
                if (isResourceDependency(res)) {
                    switch (type) {
                        case CONSUME:
                        case AVOID:
                            if (_amount + currentAmount < 0) {
                                currentAmount += _amount;
                                break;
                            }
                            Resource part = res.getPart((int) Math.ceil(_amount));
                            currentAmount += part.amount;
                            if (part.amount > 0) {
                                lastConsumed.add(part.getFullName());
                            }
                            part.amount = 0;
                            break;
                        case EXIST:
                            currentAmount += res.amount;
                    }
                    if (currentAmount >= _amount) {
                        break;
                    }
                }
            }
            result = Math.min(((double) currentAmount) / _amount, 1);
            if (currentAmount >= _amount) {
                currentAmount -= _amount;
            }
            result = (type == Type.AVOID ? 1 - result : result);
        }
        return result + (1 - result) / deprivationCoefficient;
    }

    public Type getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public boolean hasNeeded(Tile tile) {
        return tile.getResources().stream().anyMatch(this::isResourceGood);
    }

    public boolean isResourceGood(Resource resource) {
        return type == Type.AVOID ? !isResourceDependency(resource) : isResourceDependency(resource);
    }

    public boolean isResourceDependency(Resource resource) {
        return (resourceNames.contains(resource.getSimpleName()) || resource.getGenome().getPrimaryMaterial() != null &&
                materialNames.contains(resource.getGenome().getPrimaryMaterial().getName())) && resource.amount > 0;
    }

    public boolean isPositive() {
        return type != Type.AVOID;
    }

    public boolean isResourceNeeded() {
        return type.isResourceDependent;
    }

    public boolean isNecessary() {
        return isNecessary;
    }

    public enum Type{
        CONSUME(true),
        EXIST(true),
        AVOID(true),
        TEMPERATURE_MIN(false),
        TEMPERATURE_MAX(false),
        AVOID_TILES(false);

        boolean isResourceDependent;

        Type(boolean isResourceDependent) {this.isResourceDependent = isResourceDependent;}
    }
}
