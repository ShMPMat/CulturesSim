package simulation.culture.group;

import extra.ShnyPair;
import simulation.culture.Event;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectTag;
import simulation.culture.aspect.ConverseWrapper;
import simulation.culture.aspect.MeaningInserter;
import simulation.culture.aspect.dependency.Dependency;
import simulation.culture.group.cultureaspect.AestheticallyPleasingObject;
import simulation.culture.group.cultureaspect.CultureAspect;
import simulation.culture.group.cultureaspect.DepictObject;
import simulation.culture.group.cultureaspect.Tale;
import simulation.culture.group.request.Request;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.culture.group.request.TagRequest;
import simulation.culture.thinking.meaning.GroupMemes;
import simulation.culture.thinking.meaning.Meme;
import simulation.culture.thinking.meaning.MemeSubject;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourcePack;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static extra.ProbFunc.*;
import static simulation.Controller.session;

/**
 * Takes responsibility of Group's cultural change.
 */
public class CulturalCenterOvergroup {
    private Group group;
    private List<Event> events = new ArrayList<>();


    CulturalCenterOvergroup(Group group) {
        this.group = group;
    }

    List<Event> getEvents() {
        return events;
    }
}
