package simulation.culture.aspect;

import simulation.culture.group.request.ResourceEvaluator;
import simulation.space.resource.ResourcePack;

public class AspectController {
    public int ceiling;
    public int floor;
    public ResourceEvaluator evaluator;
    public boolean isMeaningNeeded;

    public AspectController(int ceiling, int floor, ResourceEvaluator evaluator, boolean isMeaningNeeded) {
        this.ceiling = ceiling;
        this.floor = floor;
        this.evaluator = evaluator;
        this.isMeaningNeeded = isMeaningNeeded;
    }

    public AspectController(int ceiling, int floor, ResourceEvaluator evaluator) {
        this(ceiling, floor, evaluator, false);
    }

    boolean isFloorExceeded(ResourcePack resourcePack) {
        return evaluator.evaluate(resourcePack) >= floor;
    }

    boolean isCeilingExceeded(ResourcePack resourcePack) {
        return evaluator.evaluate(resourcePack) >= ceiling;
    }
}
