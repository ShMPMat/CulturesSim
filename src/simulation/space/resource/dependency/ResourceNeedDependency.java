package simulation.space.resource.dependency;

import simulation.space.resource.tag.labeler.ResourceLabeler;
import simulation.space.tile.Tile;
import simulation.space.resource.Resource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResourceNeedDependency implements ResourceDependency {//TODO please, write an interface and BUNCH OF IMPLEMENTATIONS
    private ResourceLabeler goodResources;
    private double amount;
    private boolean isNecessary;
    private double deprivationCoefficient;
    private Type type;
    public Set<String> lastConsumed = new HashSet<>();

    public ResourceNeedDependency(Type type, double amount, double deprivationCoefficient, boolean isNecessary, ResourceLabeler goodResources) {
        this.goodResources = goodResources;
        this.amount = amount;
        this.deprivationCoefficient = deprivationCoefficient;
        this.type = type;
        this.isNecessary = isNecessary;
    }

    public double satisfactionPercent(Tile tile, Resource resource) {
        double result;
        double _amount = amount * (resource == null ? 1 : resource.getAmount());
        int currentAmount = 0;
        for (Resource res : tile.getAccessibleResources()) {
            if (res.equals(resource)) {
                continue;
            }
            if (isInDependency(res)) {
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
        return tile.getAccessibleResources().stream().anyMatch(this::isResourceDependency);
    }

    public boolean isResourceDependency(Resource resource) {
        return type != Type.AVOID && isInDependency(resource);
    }

    private boolean isInDependency(Resource resource) {
        return goodResources.isSuitable(resource.getGenome()) && resource.getAmount() > 0;
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
