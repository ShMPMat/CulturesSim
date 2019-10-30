package simulation.culture.aspect;

import simulation.culture.group.Group;

import java.util.*;

/**
 * Class which contains all general information about all Aspects with the same name.
 */
class AspectCore {
    /**
     * Name of the Aspect.
     */
    String name;
    /**
     * AspectTags which represent which resources are provided by this Aspect.
     */
    private List<AspectTag> tags;
    /**
     * Requirements for an Aspect to be added to the group.
     */
    private List<AspectTag> requirements;
    /**
     * Whether it can apply meaning.
     */
    boolean applyMeaning = false;

    public AspectCore(String[] tags) {
        this.tags = new ArrayList<>();
        this.requirements = new ArrayList<>();
        for (int i = 0; i < tags.length; i++) {
            String tag = tags[i];
            if (i == 0) {
                this.name = tag;
            } else {
                switch ((tag.charAt(0))) {
                    case '-':
                        this.tags.add(new AspectTag(tag.substring(1), false, false));
                        i++;
                        break;
                    case '+':
                        this.tags.add(new AspectTag(tag.substring(1), true, false));
                        i++;
                        break;
                    case '/':
                        this.requirements.add(new AspectTag(tag.substring(1), false, false));
                        break;
                    case '*':
                        this.requirements.add(new AspectTag(tag.substring(1), true, false));
                        break;
                    case '#':
                        if (tag.substring(1).equals("MEANING")) {
                            applyMeaning = true;
                        } else {
                            this.requirements.add(new AspectTag(tag.substring(1), false, true));
                        }
                        break;
                }
            }
        }
    }


    String getName() {
        return name;
    }

    List<AspectTag> getTags() {
        return tags;
    }

    Collection<AspectTag> getRequirements() {
        return requirements;
    }

    void addAllTags(Collection<AspectTag> tags) {
        for (AspectTag tag : tags) {
            if (!this.tags.contains(tag)) {
                this.tags.add(tag);
            } else if (this.tags.get(this.tags.indexOf(tag)).level < tag.level) {
                this.tags.remove(tag);
                this.tags.add(tag);
            }
        }
    }

    Aspect copy(Map<AspectTag, Set<Dependency>> dependencies, Group group) {
        return new Aspect(this, dependencies, group);
    }
}
