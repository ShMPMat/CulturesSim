package simulation.culture.thinking.meaning;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MemePool {
    protected List<Meme> memes;

    public MemePool(List<Meme> memes) {
        this.memes = memes;
    }

    public MemePool() {
        this(new ArrayList<>());
    }

    public void add(Meme meme) {
        if (!memes.contains(meme)) {
            memes.add(meme);
        }
    }

    public void addAll(Collection<Meme> memes) {
        memes.forEach(this::add);
    }

    public List<Meme> getMemes() {
        return memes;
    }

    public Meme getMemeByName(String name) {
        return memes.stream().filter(meme -> meme.observerWord.equals(name.toLowerCase())).findFirst().orElse(null);
    }

    public boolean isEmpty() {
        return memes.isEmpty();
    }
}
