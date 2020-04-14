package simulation.culture.group.cultureaspect;

import simulation.culture.group.centers.Group;
import simulation.culture.group.request.Request;
import simulation.culture.thinking.language.templates.TextInfo;
import simulation.culture.thinking.meaning.Meme;

import java.util.Objects;

public class Tale implements CultureAspect {
    private Meme template;
    private TextInfo info;

    private Meme meme;

    public Tale(Meme template, TextInfo info) {
        this.template = template;
        this.info = info;
        this.meme = info.substitute(template);
    }

    public TextInfo getInfo() {
        return info;
    }

    @Override
    public Request getRequest(Group group) {
        return null;
    }

    @Override
    public void use(Group group) {
        group.getCultureCenter().getMemePool().strengthenMeme(meme);
        info.getMap().values().forEach(m -> {
            group.getCultureCenter().getMemePool().strengthenMeme(info.substitute(m));
        });
    }

    @Override
    public Tale copy(Group group) {
        return new Tale(template, info);
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
