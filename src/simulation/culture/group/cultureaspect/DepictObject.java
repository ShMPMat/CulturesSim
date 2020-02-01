package simulation.culture.group.cultureaspect;

import simulation.culture.aspect.AspectController;
import simulation.culture.aspect.AspectResult;
import simulation.culture.aspect.ConverseWrapper;
import simulation.culture.group.Group;
import simulation.culture.group.ResourceBehaviour;
import simulation.culture.group.request.Request;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.culture.thinking.meaning.Meme;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourcePack;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    public DepictObject(Group group, Meme meme, ConverseWrapper converseWrapper) {
        this(group, meme, converseWrapper, ResourceBehaviour.getRandom(group));
    }

    @Override
    public Request getRequest() {
        return null;
    }

    @Override
    public void use() {
        group.getCulturalCenter().putCurrentMeme(meme.toString());
        AspectResult result = converseWrapper.use(new AspectController(1, 1,
                new ResourceEvaluator(rp -> rp, ResourcePack::getAmount), true));
        if (result.isFinished) {
            ResourcePack meaningful = new ResourcePack(result.resources.getResources().stream().filter(Resource::hasMeaning)
                    .collect(Collectors.toList()));
            result.resources.removeAll(meaningful.resources);
            group.cherishedResources.add(meaningful);
            resourceBehaviour.procedeResources(meaningful);
            result.resources.disbandOnTile(group.getDisbandTile());
            group.getCulturalCenter().getMemePool().strengthenMeme(meme);
        }
        group.getCulturalCenter().clearCurrentMeme();
    }

    @Override
    public DepictObject copy(Group group) {
        return new DepictObject(group, meme, converseWrapper, resourceBehaviour);
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
