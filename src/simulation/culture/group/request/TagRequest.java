package simulation.culture.group.request;

import kotlin.Pair;
import simulation.culture.group.Group;
import simulation.culture.group.Stratum;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourcePack;
import simulation.space.resource.tag.ResourceTag;

import java.util.Collection;
import java.util.function.BiFunction;

public class TagRequest extends Request {
    private ResourceTag tag;

    public TagRequest(Group group, ResourceTag tag, int floor, int ceiling, BiFunction<Pair<Group, ResourcePack>, Double, Void> penalty,
                      BiFunction<Pair<Group, ResourcePack>, Double, Void> reward) {
        super(group, floor, ceiling, penalty, reward);
        this.tag = tag;
    }

    private boolean hasTagFrom(Collection<ResourceTag> tags) {
        return tags.contains(tag);
    }

    @Override
    public ResourceEvaluator isAcceptable(Stratum stratum) {
        if (stratum.getAspects().stream().anyMatch(aspect -> hasTagFrom(aspect.getTags()))) {
            return new ResourceEvaluator(resourcePack -> resourcePack.getAllResourcesWithTag(tag),
                    resourcePack -> resourcePack.getAllResourcesWithTag(tag).getResources().stream()
                            .reduce(0, (x, y) -> x += y.getTagLevel(tag)*y.getAmount(), Integer::sum));
        }
        return null;
    }

    @Override
    public int satisfactionLevel(Resource sample) {
        int index = sample.getTags().indexOf(tag);
        return index == -1 ? 0 : sample.getTags().get(index).level;
    }

    @Override
    public void end(ResourcePack resourcePack) {
        int amount = 0;
        ResourcePack _rp = new ResourcePack();

        ResourcePack _r = resourcePack.getResourcesWithTagPartIsBigger(tag, ceiling);
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
