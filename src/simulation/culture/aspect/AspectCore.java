package simulation.culture.aspect;

import simulation.culture.aspect.dependency.Dependency;
import simulation.culture.group.Group;
import simulation.space.resource.tag.ResourceTag;

import java.util.*;

/**
 * Class which contains all general information about all Aspects with the same name.
 */
class AspectCore {
    String name;
    /**
     * AspectTags which represent which resources are provided by this Aspect.
     */
    private List<ResourceTag> tags = new ArrayList<>();
    /**
     * Requirements for an Aspect to be added to the group.
     */
    private List<ResourceTag> requirements = new ArrayList<>();
    List<AspectMatcher> matchers = new ArrayList<>();
    boolean applyMeaning = false;

    public AspectCore(String[] tags) {
        for (int i = 0; i < tags.length; i++) {
            String tag = tags[i];
            if (i == 0) {
                this.name = tag;
            } else {
                switch ((tag.charAt(0))) {
                    case '-':
                        this.tags.add(new ResourceTag(tag.substring(1), false, false, false));
                        i++;
                        break;
                    case '+':
                        this.tags.add(new ResourceTag(tag.substring(1), true, false, false));
                        i++;
                        break;
                    case '/':
                        this.requirements.add(new ResourceTag(tag.substring(1), false, false, false));
                        break;
                    case '*':
                        this.requirements.add(new ResourceTag(tag.substring(1), false, false, true));
                        break;
                    case '#':
                        if (tag.substring(1).equals("MEANING")) {
                            applyMeaning = true;
                        } else {
                            this.requirements.add(new ResourceTag(tag.substring(1), false, true, false));
                        }
                        break;
                    case '&':
                        matchers.add(new AspectMatcher(tag.substring(1).split("-+"), this));
                        break;
                }
            }
        }
    }


    String getName() {
        return name;
    }

    List<ResourceTag> getTags() {
        return tags;
    }

    Collection<ResourceTag> getRequirements() {
        return requirements;
    }

    void addAllTags(Collection<ResourceTag> tags) {
        for (ResourceTag tag : tags) {
            if (!this.tags.contains(tag)) {
                this.tags.add(tag);
            } else if (this.tags.get(this.tags.indexOf(tag)).level < tag.level) {
                this.tags.remove(tag);
                this.tags.add(tag);
            }
        }
    }

    Aspect copy(Map<ResourceTag, Set<Dependency>> dependencies, Group group) {
        return new Aspect(this, dependencies, group);
    }
}
