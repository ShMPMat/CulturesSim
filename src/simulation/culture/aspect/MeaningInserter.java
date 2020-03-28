package simulation.culture.aspect;

import simulation.culture.aspect.dependency.AspectDependencies;
import simulation.culture.aspect.dependency.Dependency;
import simulation.space.resource.Resource;
import simulation.space.resource.tag.ResourceTag;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Subclass of ConverseWrapper which inserts meaning in certain objects.
 */
public class MeaningInserter extends ConverseWrapper {
    public MeaningInserter(Aspect aspect, Resource resource) {
        super(aspect, resource.fullCopy());
        canInsertMeaning = true;
        this.resource.setHasMeaning(true);
    }

    @Override
    public List<Resource> getResult() {
        return Collections.singletonList(resource);
    }

    @Override
    public AspectResult use(AspectController controller) {
        AspectResult result = super.use(controller);
        Collection<Resource> res = new ArrayList<>(result.resources.getResourceAndRemove(resource).getResources());
        res.removeIf(r -> r.getAmount() == 0);
        result.resources.addAll(res.stream().map(r -> r.insertMeaning(controller.getMeaning(), result))
                .collect(Collectors.toList()));
        return result;
    }

    @Override
    protected boolean shouldPassMeaningNeed(boolean isMeaningNeeded) {
        return false;
    }

    @Override
    public MeaningInserter copy(AspectDependencies dependencies) {
        MeaningInserter copy = new MeaningInserter(aspect, resource);
        copy.initDependencies(dependencies);
        Collection<ResourceTag> unwantedTags = new ArrayList<>();
        for (ResourceTag resourceTag : dependencies.getMap().keySet()) {
            if (!(resourceTag.isInstrumental || resourceTag.name.equals("phony"))) {
                unwantedTags.add(resourceTag);
            }
        }
        unwantedTags.forEach(dependencies.getMap()::remove);
        return copy;
    }
}
