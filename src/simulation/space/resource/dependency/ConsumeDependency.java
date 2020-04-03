package simulation.space.resource.dependency;

import simulation.space.resource.tag.labeler.ResourceTagLabeler;
import simulation.space.tile.Tile;
import simulation.space.resource.Resource;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ConsumeDependency extends ResourceMaterialDependency {
    public Set<String> lastConsumed = new HashSet<>();

    public ConsumeDependency(
            double deprivationCoefficient,
            boolean isNecessary,
            double amount,
            ResourceTagLabeler goodResource) {
        super(deprivationCoefficient, isNecessary, amount, goodResource);
    }

    @Override
    double satisfaction(Tile tile, Resource resource) {
        double result;
        double _amount = amount * (resource == null ? 1 : resource.getAmount());
        for (Resource res : tile.getAccessibleResources()) {
            if (res.equals(resource)) {
                continue;
            }
            if (isResourceDependency(res)) {
                if (!res.getSimpleName().equals("Vapour")) {
                    int i = 0; //TODO something wrong but i don't remember what
                }
                if (_amount + currentAmount < 0) {
                    currentAmount += _amount;
                    break;
                }
                Resource part = res.getPart((int) Math.ceil(_amount));
                currentAmount += part.getAmount();
                if (part.getAmount() > 0) {
                    lastConsumed.add(part.getFullName());
                }
                part.destroy();
                if (currentAmount >= _amount) {
                    break;
                }
            }
        }
        result = Math.min(((double) currentAmount) / _amount, 1);
        if (currentAmount >= _amount) {
            currentAmount -= _amount;
        }
        return result;
    }

    @Override
    public boolean isPositive() {
        return true;
    }
}
