package simulation.culture.group.cultureaspect;

import simulation.culture.group.centers.Group;

public abstract class AbstractCultureAspect implements CultureAspect {
    Group group;

    public AbstractCultureAspect(Group group) {
        this.group = group;
    }
}