package simulation.space.resource;

import simulation.space.Tile;

import java.util.ArrayList;
import java.util.List;

public class ResourceDependency {
    private List<String> resourceNames = new ArrayList<>();
    private List<String> materialNames = new ArrayList<>();
    private double amount;
    private int currentAmount = 0;
    private Type type;

    public ResourceDependency(Type type, double amount, List<String> names) {
        for (String name: names) {
            if (name.charAt(0) == '@') {
                this.materialNames.add(name.substring(1));
            } else {
                this.resourceNames.add(name);
            }
        }
        this.amount = amount;
        this.type = type;
    }

    public List<String> getResourceNames() {
        return resourceNames;
    }

    public List<String> getMaterialNames() {
        return materialNames;
    }

    public double satisfactionPercent(Tile tile, Resource resource) {
        double _amount = amount * resource.amount;
        for (Resource res: tile.getResources()) {
            if (res == resource) {
                continue;
            }
            if (isResourceGood(res)) {
                switch (type) {
                    case CONSUME:
                        if (_amount + currentAmount < 0) {
                            currentAmount += _amount;
                            break;
                        }
                        Resource part = res.getPart((int) Math.ceil(_amount));
                        currentAmount += part.amount;
                        part.amount = 0;
                        break;
                    case AVOID:
                    case EXIST:
                        currentAmount += res.amount;
                }
                if (currentAmount >= _amount) {
                    break;
                }
            }
        }
        double res = Math.min(((double) currentAmount) / _amount, 1);
        if (currentAmount >= _amount) {
            currentAmount -= _amount;
        }
        res = (type == Type.AVOID ? 1 - res : res);
        return (res + 1) / 2;
    }

    public boolean hasNeeded(Tile tile) {
        return tile.getResources().stream().anyMatch(this::isResourceGood);
    }

    public boolean isResourceGood(Resource resource) {
        return (resourceNames.contains(resource.getSimpleName()) || resource.getGenome().getPrimaryMaterial() != null &&
                materialNames.contains(resource.getGenome().getPrimaryMaterial().getName())) && resource.amount > 0;
    }

    public boolean isPositive() {
        return type != Type.AVOID;
    }

    enum Type{
        CONSUME,
        EXIST,
        AVOID
    }
}
