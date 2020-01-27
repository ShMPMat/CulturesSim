package simulation.culture.aspect;

import simulation.culture.aspect.dependency.Dependency;
import simulation.culture.group.Group;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.space.resource.Resource;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Subclass of ConverseWrapper which inserts meaning in certain objects.
 */
public class MeaningInserter extends ConverseWrapper {

    public MeaningInserter(Aspect aspect, Resource resource, Group group) {
        super(aspect, resource.fullClearCopy(), group);
        canInsertMeaning = true;
        this.resource.setHasMeaning(true);
    }

    @Override
    public List<Resource> getResult() {
        return Collections.singletonList(resource);
    }

    @Override
    public AspectResult use(int ceiling, ResourceEvaluator evaluator) {
        AspectResult result = super.use(ceiling, evaluator);
        Collection<Resource> res = new ArrayList<>(result.resources.getResourceAndRemove(resource).getResources());
        res.removeIf(r -> r.getAmount() == 0);
        result.resources.add(res.stream().map(r -> r.insertMeaning(group.getCulturalCenter().getMeaning(), result))
                .collect(Collectors.toList()));
        return result;
    }

    @Override
    public MeaningInserter stripToMeaning() {
        return copy(dependencies, group);
    }

    @Override
    public MeaningInserter copy(Map<AspectTag, Set<Dependency>> dependencies, Group group) {
        MeaningInserter copy = new MeaningInserter(aspect, resource, group);
        copy.initDependencies(dependencies);
        Collection<AspectTag> unwantedTags = new ArrayList<>();
        for (AspectTag aspectTag : dependencies.keySet()) {
            if (!(aspectTag.isInstrumental || aspectTag.name.equals("phony"))) {
                unwantedTags.add(aspectTag);
            }
        }
        unwantedTags.forEach(dependencies::remove);
        return copy;
    }
}
