package simulation.culture.interactionmodel;

import simulation.Controller;
import simulation.World;
import simulation.culture.Event;
import simulation.culture.group.Group;

import java.util.*;

import static simulation.Controller.*;

/**
 * Model with 2d map on which all interactions take place.
 */
public class MapModel implements InteractionModel {

    public List<Event> newEvents = new ArrayList<>();

    @Override
    public void turn(World world) {
        int size = world.groups.size();
        for (int j = 0; j < size; j++) {
            Group group = world.groups.get(j);
            int e = group.getEvents().size();
            group.update();
            for (int i = e; i < group.getEvents().size(); i++) {
                newEvents.add(group.getEvents().get(i));
            }
        }

        int a = world.events.size();
        world.map.update();

        for (Group group : world.groups) {
            group.finishUpdate();
        }

        world.map.finishUpdate();

        for (int i = a; i < world.events.size(); i++) {
            newEvents.add(world.events.get(i));
        }
    }

    @Override
    public void geologicTurn(World world) {
        int a = world.events.size();
        world.map.geologicUpdate();

        for (int i = a; i < world.events.size(); i++) {
            newEvents.add(world.events.get(i));
        }
    }

    @Override
    public Collection<Event> getEvents() {
        return newEvents;
    }

    @Override
    public void clearEvents() {
        newEvents = new ArrayList<>();
    }
}
