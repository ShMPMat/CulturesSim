package simulation.culture.group.cultureaspect;

import simulation.culture.group.CultureCenter;
import simulation.culture.group.Group;
import simulation.culture.group.request.Request;

public interface CultureAspect {
    Request getRequest();
    void use(CultureCenter center);
    CultureAspect copy(Group group);
}
