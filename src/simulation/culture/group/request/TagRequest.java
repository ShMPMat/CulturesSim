package simulation.culture.group.request;

import kotlin.Pair;
import simulation.culture.aspect.Aspect;
import simulation.culture.group.Stratum;
import simulation.culture.group.centers.Group;
import simulation.culture.group.AspectStratum;
import simulation.space.resource.Resource;
import simulation.space.resource.MutableResourcePack;
import simulation.space.resource.tag.ResourceTag;

import java.util.Collection;
import java.util.function.BiFunction;

public class TagRequest extends Request {
    private ResourceTag tag;

    public TagRequest(
            Group group,
            ResourceTag tag,
            int floor,
            int ceiling,
            BiFunction<Pair<Group, MutableResourcePack>, Double, Void> penalty,
            BiFunction<Pair<Group, MutableResourcePack>, Double, Void> reward
    ) {
        super(group, floor, ceiling, penalty, reward);
        this.tag = tag;
    }

    private boolean hasTagFrom(Collection<ResourceTag> tags) {
        return tags.contains(tag);
    }

    @Override
    public ResourceEvaluator isAcceptable(Stratum stratum) {
        if (!(stratum instanceof AspectStratum)) {
            return null;
        }
        if (((AspectStratum) stratum).getAspects().stream().anyMatch(aspect -> hasTagFrom(aspect.getTags()))) {
            return getEvaluator();
        }
        return null;
    }

    @Override
    public ResourceEvaluator getEvaluator() {
        return EvaluatorsKt.tagEvaluator(tag);
    }

    @Override
    public int satisfactionLevel(Resource sample) {
        int index = sample.getTags().indexOf(tag);
        return index == -1 ? 0 : sample.getTags().get(index).level;
    }

    @Override
    public String toString() {
        return "want for " + tag;
    }
}
