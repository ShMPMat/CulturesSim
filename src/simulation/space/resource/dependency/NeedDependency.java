package simulation.space.resource.dependency;

import simulation.space.resource.tag.labeler.ResourceLabeler;
import simulation.space.tile.Tile;
import simulation.space.resource.Resource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NeedDependency extends LabelerDependency {//TODO please, write an interface and BUNCH OF IMPLEMENTATIONS
    private Type type;
    public Set<String> lastConsumed = new HashSet<>();

    public NeedDependency(Type type, double amount, double deprivationCoefficient, boolean isNecessary, ResourceLabeler goodResources) {
        super(deprivationCoefficient, isNecessary, amount, goodResources);
        this.type = type;
    }

    @Override
    double satisfaction(Tile tile, Resource resource) {
        double result;
        double _amount = amount * (resource == null ? 1 : resource.getAmount());
        int currentAmount = 0;
        for (Resource res : tile.getAccessibleResources()) {
            if (res.equals(resource)) {
                continue;
            }
            if (super.isResourceDependency(res)) {
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
        return (type == Type.AVOID ? 1 - result : result);
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean hasNeeded(Tile tile) {
        return tile.getAccessibleResources().stream().anyMatch(this::isResourceDependency);
    }

    @Override
    public boolean isResourceDependency(Resource resource) {
        return type != Type.AVOID && super.isResourceDependency(resource);
    }

    @Override
    public boolean isPositive() {
        return type != Type.AVOID;
    }

    public enum Type{
        EXIST(true),
        AVOID(true);

        boolean isResourceDependent;

        Type(boolean isResourceDependent) {this.isResourceDependent = isResourceDependent;}
    }
}
