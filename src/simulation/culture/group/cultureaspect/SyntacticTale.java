package simulation.culture.group.cultureaspect;

import simulation.culture.group.Group;
import simulation.culture.group.request.Request;
import simulation.culture.thinking.language.templates.TextInfo;
import simulation.culture.thinking.meaning.Meme;

import java.util.Objects;

public class SyntacticTale extends AbstractCultureAspect {
    private Meme template;
    private TextInfo info;

    private Meme meme;

    public SyntacticTale(Group group, Meme template, TextInfo info) {
        super(group);
        this.template = template;
        this.info = info;
        this.meme = info.substitute(template);
    }

    @Override
    public Request getRequest() {
        return null;
    }

    @Override
    public void use() {
        group.getCultureCenter().getMemePool().strengthenMeme(meme);
        info.getMap().values().forEach(m -> {
            group.getCultureCenter().getMemePool().strengthenMeme(info.substitute(m));
        });
    }

    @Override
    public SyntacticTale copy(Group group) {
        return new SyntacticTale(group, template, info);
    }

    @Override
    public String toString() {
        return "Tale about " + meme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SyntacticTale that = (SyntacticTale) o;
        return Objects.equals(meme, that.meme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(meme);
    }
}
