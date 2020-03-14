package simulation.interactionmodel;

import simulation.World;
import simulation.Event;

import java.util.Collection;

/**
 * Represents general model by which World changes.
 */
public interface InteractionModel {
    void turn(World world);

    void geologicTurn(World world);

    Collection<Event> getEvents();

    void clearEvents();
}
