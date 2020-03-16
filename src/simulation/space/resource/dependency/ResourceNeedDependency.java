package simulation.space.resource.dependency;

import simulation.space.Tile;
import simulation.space.resource.Resource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResourceNeedDependency implements ResourceDependency {//TODO please, write an interface and BUNCH OF IMPLEMENTATIONS
    private List<String> resourceNames = new ArrayList<>();
    private List<String> materialNames = new ArrayList<>();
    private double amount;
    private boolean isNecessary;
    private double deprivationCoefficient;
    private int currentAmount = 0;
    private Type type;
    public Set<String> lastConsumed = new HashSet<>();

    public ResourceNeedDependency(Type type, double amount, double deprivationCoefficient, boolean isNecessary,
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
        double _amount = amount * (resource == null ? 1 : resource.getAmount());
        for (Resource res : tile.getAccessibleResources()) {
            if (res.equals(resource)) {
                continue;
            }
            if (isResourceDependency(res)) {
                switch (type) {
                    case EXIST:
                    case AVOID:
                        currentAmount += res.getAmount();
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
        return result + (1 - result) / deprivationCoefficient;
    }

    public Type getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public boolean hasNeeded(Tile tile) {
        return tile.getResourcePack().getResources().stream().anyMatch(this::isResourceGood);
    }

    private boolean isResourceGood(Resource resource) {
        return type == Type.AVOID ? !isResourceDependency(resource) : isResourceDependency(resource);
    }

    private boolean isResourceDependency(Resource resource) {
        return (resourceNames.contains(resource.getSimpleName()) || resource.getGenome().getPrimaryMaterial() != null &&
                materialNames.contains(resource.getGenome().getPrimaryMaterial().getName())) && resource.getAmount() > 0;
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
        EXIST(true),
        AVOID(true);

        boolean isResourceDependent;

        Type(boolean isResourceDependent) {this.isResourceDependent = isResourceDependent;}
    }
}
