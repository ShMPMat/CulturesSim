package simulation.culture.group.cultureaspect;

import extra.ShnyPair;
import simulation.culture.aspect.ConverseWrapper;
import simulation.culture.group.Group;
import simulation.culture.group.ResourceBehaviour;
import simulation.culture.group.request.Request;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.culture.thinking.meaning.Meme;
import simulation.space.resource.ResourcePack;

import java.util.Objects;

public class DepictObject extends AbstractCultureAspect {
    private Meme meme;
    private ResourceBehaviour resourceBehaviour;
    private ConverseWrapper converseWrapper;

    public DepictObject(Group group, Meme meme, ConverseWrapper converseWrapper, ResourceBehaviour resourceBehaviour) {
        super(group);
        this.meme = meme;
        this.converseWrapper = converseWrapper;
        this.resourceBehaviour = resourceBehaviour;
    }

    @Override
    public Request getRequest() {
        return null;
    }

    @Override
    public void use() {
        group.getCulturalCenter().putCurrentMeme(meme.toString());
        ShnyPair<Boolean, ResourcePack> pair = converseWrapper.use(1,
                new ResourceEvaluator(rp -> rp, ResourcePack::getAmount));
        if (pair.first) {
            group.cherishedResources.add(pair.second);
            resourceBehaviour.procedeResources(pair.second);
        }
        group.getCulturalCenter().getMemePool().strengthenMeme(meme);
        group.getCulturalCenter().clearCurrentMeme();
    }

    @Override
    public String toString() {
        return "Depict " + meme.toString() + " with " + converseWrapper.getName() + " " + resourceBehaviour;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DepictObject that = (DepictObject) o;
        return Objects.equals(meme, that.meme) && Objects.equals(converseWrapper, that.converseWrapper);
    }

    @Override
    public int hashCode() {
        return Objects.hash(meme, converseWrapper);
    }
}
