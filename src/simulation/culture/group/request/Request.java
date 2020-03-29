package simulation.culture.group.request;

import kotlin.Pair;
import simulation.culture.aspect.ConverseWrapper;
import simulation.culture.group.Group;
import simulation.culture.group.Stratum;
import simulation.space.resource.Resource;
import simulation.space.resource.MutableResourcePack;

import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Class which represents a request which may by executed by a Group.
 */
public abstract class Request {
    protected Group group;
    public int floor;
    public int ceiling;
    BiFunction<Pair<Group, MutableResourcePack>, Double, Void> penalty, reward;

    Request(Group group, int floor, int ceiling, BiFunction<Pair<Group, MutableResourcePack>, Double, Void> penalty,
            BiFunction<Pair<Group, MutableResourcePack>, Double, Void> reward) {
        this.group = group;
        this.floor = floor;
        this.ceiling = ceiling;
        this.penalty = penalty;
        this.reward = reward;
    }

    public abstract ResourceEvaluator isAcceptable(Stratum stratum);

    public abstract ResourceEvaluator getEvaluator();

    public abstract int satisfactionLevel(Resource sample);

    public abstract void end(MutableResourcePack resourcePack);

    public int satisfactionLevel(Stratum stratum) {
        int result = 0;
        for (ConverseWrapper converseWrapper : stratum.getAspects().stream()
                .filter(aspect -> aspect instanceof ConverseWrapper)
                .map(aspect -> (ConverseWrapper) aspect)
                .collect(Collectors.toList())) {
            result += converseWrapper.getResult().stream()
                    .map(this::satisfactionLevel)
                    .reduce(0, Integer::sum);
        }
        return result;
    }
}
