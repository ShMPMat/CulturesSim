package simulation.culture;

import java.util.HashMap;
import java.util.Map;

/**
 * Object storing memory about something that happened in the world.
 */
public class Event {
    public enum Type{
        Death,
        ResourceDeath,
        AspectGaining,
        TileAquisition,
        DisbandResources
    }
    public Type type;
    public Map<String, Object> attributes;

    private String turn;
    private String description;

    /**
     *
     * @param type - type of event.
     * @param turn - turn on which event occurred.
     * @param description - string describing event.
     * @param attributes - list of attributes in pair of String name for attribute and any Object linked to it.
     */
    public Event(Type type, String turn, String description, Object ... attributes) {
        this.type = type;
        this.turn = turn;
        this.description = description;
        this.attributes = new HashMap<>();
        for (int i = 0; i < attributes.length; i += 2) {
            String name = (String) attributes[i];
            if (name.equals("")) {
                break;
            }
            this.attributes.put(name, attributes[i + 1]);
        }
    }

    public Event(Type type, String turn, String description) {
        this(type, turn, description, "");
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public String toString() {
        return turn + ". " + description;
    }
}
