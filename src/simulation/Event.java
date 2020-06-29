package simulation;

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
        TileAcquisition,
        DisbandResources,
        GroupInteraction,

        Creation,
        Move,
        Change,

        Other
    }
    public Type type;
    public Map<String, Object> attributes = new HashMap<>();

    private String turn;
    public String description;

    public Event(Type type, String description, Object... attributes) {
        this(type, description);
        for (int i = 0; i < attributes.length; i += 2) {
            String name = (String) attributes[i];
            if (name.equals(""))
                break;
            this.attributes.put(name, attributes[i + 1]);
        }
    }

    public Event(Type type, String description, Map<String, Object> attributes) {
        this(type, description);
        this.attributes.putAll(attributes);
    }

    public Event(Type type, String description) {
        this.type = type;
        this.turn = Controller.session.world == null ? "None" : Controller.session.world.getTurn();
        this.description = description;
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public String toString() {
        return turn + ". " + description;
    }
}
