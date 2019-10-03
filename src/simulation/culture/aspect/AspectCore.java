package simulation.culture.aspect;

import simulation.culture.group.Group;

import java.util.*;

/**
 * Class which contains all general information about all Aspects with the same name.
 */
class AspectCore {
    String name;
    List<AspectTag> tags, requirements;
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

    Collection<AspectTag> getTags() {
        return tags;
    }

    Collection<AspectTag> getRequirements() {
        return requirements;
    }

    void addAllTags(Collection<AspectTag> tags) {
        for (AspectTag tag : tags) {
            if (!getTags().contains(tag)) {
                getTags().add(tag);
            }
        }
    }

    Aspect copy(Map<AspectTag, Set<Dependency>> dependencies, Group group) {
        return new Aspect(this, dependencies, group);
    }
}
