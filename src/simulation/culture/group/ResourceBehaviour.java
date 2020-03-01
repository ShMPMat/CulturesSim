package simulation.culture.group;

import kotlin.collections.ArraysKt;
import simulation.Controller;
import simulation.space.resource.ResourcePack;

public class ResourceBehaviour {
    private PlacementStrategy placementStrategy;

    ResourceBehaviour(PlacementStrategy placementStrategy) {
        this.placementStrategy = placementStrategy;
    }

    public void procedeResources(ResourcePack resourcePack) {
        placementStrategy.place(resourcePack);
    }

    @Override
    public String toString() {
        return placementStrategy.toString();
    }

    public static ResourceBehaviour getRandom(Group group) {
        return new ResourceBehaviour(new PlacementStrategy(group.getOverallTerritory(),
                shmp.random.RandomCollectionsKt.randomElement(
                        ArraysKt.toList(PlacementStrategy.Strategy.values()),
                        Controller.session.random)
        ));
    }
}
