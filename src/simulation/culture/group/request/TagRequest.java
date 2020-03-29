package simulation.culture.group.request;

import kotlin.Pair;
import simulation.culture.group.Group;
import simulation.culture.group.Stratum;
import simulation.space.resource.Resource;
import simulation.space.resource.MutableResourcePack;
import simulation.space.resource.tag.ResourceTag;

import java.util.Collection;
import java.util.function.BiFunction;

public class TagRequest extends Request {
    private ResourceTag tag;

    public TagRequest(Group group, ResourceTag tag, int floor, int ceiling, BiFunction<Pair<Group, MutableResourcePack>, Double, Void> penalty,
                      BiFunction<Pair<Group, MutableResourcePack>, Double, Void> reward) {
        super(group, floor, ceiling, penalty, reward);
        this.tag = tag;
    }

    private boolean hasTagFrom(Collection<ResourceTag> tags) {
        return tags.contains(tag);
    }

    @Override
    public ResourceEvaluator isAcceptable(Stratum stratum) {
        if (stratum.getAspects().stream().anyMatch(aspect -> hasTagFrom(aspect.getTags()))) {
            return getEvaluator();
        }
        return null;
    }

    @Override
    public ResourceEvaluator getEvaluator() {
        return new ResourceEvaluator(
                rp -> rp.getResources(tag),
                rp -> rp.getResources(tag).getResources().stream()
                        .map(r -> r.getTagLevel(tag) * r.getAmount())
                        .reduce(0, Integer::sum)
        );
    }

    @Override
    public int satisfactionLevel(Resource sample) {
        int index = sample.getTags().indexOf(tag);
        return index == -1 ? 0 : sample.getTags().get(index).level;
    }

    @Override
    public void end(MutableResourcePack resourcePack) {
        int amount = 0;
        MutableResourcePack _rp = new MutableResourcePack();

        MutableResourcePack _r = resourcePack.getResourcesWithTagPartIsBigger(tag, ceiling);
        amount = _r.getAmount();
        _rp.addAll(_r);

        if (amount < floor) {
            penalty.apply(new Pair<>(group, _rp), amount / ((double) floor));
            return;
        }
        reward.apply(new Pair<>(group, _rp), amount / ((double) floor) - 1);
    }

    @Override
    public String toString() {
        return "want for " + tag;
    }
}
