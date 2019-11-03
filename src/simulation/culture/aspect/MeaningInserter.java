package simulation.culture.aspect;

import extra.ShnyPair;
import simulation.culture.group.Group;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourcePack;

import java.util.*;
import java.util.function.Function;
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
    public ShnyPair<Boolean, ResourcePack> use(int ceiling, Function<ResourcePack, ResourcePack> amount) {
        ShnyPair<Boolean, ResourcePack> pair = super.use(ceiling, amount);
        Collection<Resource> res = new ArrayList<>(pair.second.getResourceAndRemove(resource).getResources());
        res.removeIf(r -> r.getAmount() == 0);
        pair.second.add(res.stream().map(r -> r.insertMeaning(group.getCulturalCenter().getMeaning(), aspect))
                .collect(Collectors.toList()));
        return pair;
    }

    @Override
    public MeaningInserter stripToMeaning() {
        return copy(dependencies, group);
    }

    @Override
    public MeaningInserter copy(Map<AspectTag, Set<Dependency>> dependencies, Group group) {
        MeaningInserter _w = new MeaningInserter(aspect, resource, group);
        _w.dependencies.putAll(dependencies);
        Collection<AspectTag> unwantedTags = new ArrayList<>();
        for (AspectTag aspectTag : dependencies.keySet()) {
            if (!(aspectTag.isInstrumental || aspectTag.name.equals("phony"))) {
                unwantedTags.add(aspectTag);
            }
        }
        unwantedTags.forEach(dependencies::remove);
        return _w;
    }
}
