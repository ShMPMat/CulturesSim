package simulation.culture.group;

import simulation.space.resource.ResourcePack;

import java.util.function.Function;

public class ResourceEvaluator {
    private Function<ResourcePack, ResourcePack> picker;
    private Function<ResourcePack, Integer> evaluator;

    public ResourceEvaluator(Function<ResourcePack, ResourcePack> picker, Function<ResourcePack, Integer> evaluator) {
        this.picker = picker;
        this.evaluator = evaluator;
    }

    public ResourceEvaluator () {
        this(resourcePack -> new ResourcePack(), resourcePack -> 0);
    }

    public ResourcePack pick(ResourcePack resourcePack) {
        return picker.apply(resourcePack);
    }

    public int evaluate(ResourcePack resourcePack) {
        return evaluator.apply(resourcePack);
    }
}
