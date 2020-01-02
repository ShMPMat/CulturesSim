package simulation.culture.group;

import simulation.culture.Event;

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
