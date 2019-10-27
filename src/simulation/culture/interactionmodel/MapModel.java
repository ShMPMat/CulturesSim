package simulation.culture.interactionmodel;

import simulation.World;
import simulation.culture.Event;
import simulation.culture.group.Group;

import java.util.*;

/**
 * Model with 2d map on which all interactions take place.
 */
public class MapModel implements InteractionModel {
    private double rAspectAquisition, rAspectLending;

    public List<Event> newEvents = new ArrayList<>();

    public MapModel(double rAspectAquisition, double rAspectLending) {
        this.rAspectAquisition = rAspectAquisition;
        this.rAspectLending = rAspectLending;
    }

    @Override
    public void turn(World world) {
        for (Group group : world.groups) {
            int e = group.getEvents().size();
            group.update(rAspectAquisition, rAspectLending);
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
