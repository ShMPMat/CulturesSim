package simulation.culture.aspect;

import simulation.culture.group.request.ResourceEvaluator;

public class AspectController {
    public int ceiling;
    public int floor;
    public ResourceEvaluator evaluator;
    public boolean isMeaningNeeded;//TODO implement

    public AspectController(int ceiling, int floor, ResourceEvaluator evaluator, boolean isMeaningNeeded) {
        this.ceiling = ceiling;
        this.floor = floor;
        this.evaluator = evaluator;
        this.isMeaningNeeded = isMeaningNeeded;
    }

    public AspectController(int ceiling, int floor, ResourceEvaluator evaluator) {
        this(ceiling, floor, evaluator, false);
    }
}
