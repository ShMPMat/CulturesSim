package simulation.culture.aspect;

import extra.ShnyPair;
import simulation.culture.group.Group;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourcePack;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MeaningInserter extends ConverseWrapper {//TODO has unnesessary dependencies which go to phony

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
    public ShnyPair<Boolean, ResourcePack> use(int ceiling, Function<ResourcePack, Integer> amount) {//TODO different resources get into dependencies
            ShnyPair<Boolean, ResourcePack> pair = super.use(ceiling, amount);
            Collection<Resource> res = pair.second.getResourceAndRemove(resource);
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
        Collection<AspectTag> aspectTags = new ArrayList<>();
        for (AspectTag aspectTag : dependencies.keySet()) {
            if (!(aspectTag.isInstrumental || aspectTag.name.equals("phony"))) {
                aspectTags.add(aspectTag);
            }
        }
        aspectTags.forEach(dependencies::remove);
        return _w;
    }
}
