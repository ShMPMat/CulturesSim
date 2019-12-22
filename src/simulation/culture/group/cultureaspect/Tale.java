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

public class Tale extends AbstractCultureAspect {
    private Meme meme;

    public Tale(Group group, Meme meme) {
        super(group);
        this.meme = meme;
    }

    @Override
    public Request getRequest() {
        return null;
    }

    @Override
    public void use() {
        group.getCulturalCenter().getMemePool().strengthenMeme(meme.toString());
    }

    @Override
    public String toString() {
        return "Tale about " + meme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tale that = (Tale) o;
        return Objects.equals(meme, that.meme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(meme);
    }
}
