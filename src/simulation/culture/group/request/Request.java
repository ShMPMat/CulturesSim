package simulation.culture.group.request;

import extra.ShnyPair;
import simulation.culture.aspect.ConverseWrapper;
import simulation.culture.group.Group;
import simulation.culture.group.Stratum;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourcePack;

import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Class which represents a request which may by executed by a Group.
 */
public abstract class Request {
    protected Group group;
    int floor;
    public int ceiling;
    BiFunction<ShnyPair<Group, ResourcePack>, Double,Void> penalty, reward;

    Request(Group group, int floor, int ceiling, BiFunction<ShnyPair<Group, ResourcePack>, Double, Void> penalty,
            BiFunction<ShnyPair<Group, ResourcePack>, Double, Void> reward) {
        this.group = group;
        this.floor = floor;
        this.ceiling = ceiling;
        this.penalty = penalty;
        this.reward = reward;
    }

     public abstract ResourceEvaluator isAcceptable(Stratum stratum);

    public abstract int satisfactionLevel(Resource sample);

    public abstract void end(ResourcePack resourcePack);

    public int satisfactionLevel(Stratum stratum) {
        int result = 0;
        for (ConverseWrapper converseWrapper: stratum.getAspects().stream().filter(aspect ->
                aspect instanceof ConverseWrapper).map(aspect -> (ConverseWrapper) aspect).collect(Collectors.toList())) {
            result += converseWrapper.getResult().stream().reduce(0, (x, y) -> x += satisfactionLevel(y), Integer::sum);
        }
        return result;
    }
}
