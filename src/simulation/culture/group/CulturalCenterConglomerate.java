package simulation.culture.group;

import simulation.Event;

import java.util.*;

/**
 * Takes responsibility of Group's cultural change.
 */
public class CulturalCenterConglomerate {
    private GroupConglomerate groupConglomerate;
    private List<Event> events = new ArrayList<>();


    CulturalCenterConglomerate(GroupConglomerate group) {
        this.groupConglomerate = group;
    }

    List<Event> getEvents() {
        return events;
    }
}
