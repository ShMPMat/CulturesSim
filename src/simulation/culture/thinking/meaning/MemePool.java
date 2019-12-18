package simulation.culture.thinking.meaning;


import java.util.*;

public class MemePool {
    protected Set<Meme> memes;

    public MemePool(Collection<Meme> memes) {
        this.memes = new HashSet<>(memes);
    }

    public MemePool() {
        this(new ArrayList<>());
    }

    public void add(Meme meme) {
        memes.add(meme);
    }

    public void addAll(Collection<Meme> memes) {
        memes.forEach(this::add);
    }

    public void addAll(MemePool memePool) {
        memePool.memes.forEach(this::add);
    }

    public List<Meme> getMemes() {
        return new ArrayList<>(memes);
    }

    public Meme getMemeByName(String name) {
        return memes.stream().filter(meme -> meme.observerWord.equals(name.toLowerCase())).findFirst().orElse(null);
    }

    public boolean isEmpty() {
        return memes.isEmpty();
    }
}
