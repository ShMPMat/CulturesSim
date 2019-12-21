package simulation.culture.group.cultureaspect;

import extra.ShnyPair;
import simulation.culture.aspect.ConverseWrapper;
import simulation.culture.group.Group;
import simulation.culture.group.ResourceBehaviour;
import simulation.culture.group.request.Request;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.space.resource.ResourcePack;

import java.util.Objects;

public class DepictObject extends AbstractCultureAspect {
    private String memeString;
    private ResourceBehaviour resourceBehaviour;
    private ConverseWrapper converseWrapper;

    public DepictObject(Group group, String memeString, ConverseWrapper converseWrapper, ResourceBehaviour resourceBehaviour) {
        super(group);
        this.memeString = memeString;
        this.converseWrapper = converseWrapper;
        this.resourceBehaviour = resourceBehaviour;
    }

    @Override
    public Request getRequest() {
        return null;
    }

    @Override
    public void use() {
        group.getCulturalCenter().putCurrentMeme(memeString);
        ShnyPair<Boolean, ResourcePack> pair = converseWrapper.use(1,
                new ResourceEvaluator(rp -> rp, ResourcePack::getAmount));
        if (pair.first) {
            group.cherishedResources.add(pair.second);
            resourceBehaviour.procedeResources(pair.second);
        }
        group.getCulturalCenter().getMemePool().strengthenMeme(memeString);
        group.getCulturalCenter().clearCurrentMeme();
    }

    @Override
    public String toString() {
        return "Depict " + memeString + " with " + converseWrapper.getName() + " " + resourceBehaviour;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DepictObject that = (DepictObject) o;
        return Objects.equals(memeString, that.memeString) && Objects.equals(converseWrapper, that.converseWrapper);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memeString, converseWrapper);
    }
}
