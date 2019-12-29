package simulation.culture.thinking.meaning;


import java.util.*;

public class MemePool {
    protected Map<String, Meme> memes;

    public MemePool(Collection<Meme> memes) {
        this.memes = new HashMap<>();
        memes.forEach(this::add);
    }

    public MemePool() {
        this(new ArrayList<>());
    }

    public void add(Meme meme) {
        memes.put(meme.toString(), meme);
    }

    public void addAll(Collection<Meme> memes) {
        memes.forEach(this::add);
    }

    public void addAll(MemePool memePool) {
        memePool.memes.values().forEach(this::add);
    }

    public List<Meme> getMemes() {
        return new ArrayList<>(memes.values());
    }

    public Meme getMemeByName(String name) {
        return memes.get(name.toLowerCase());
    }

    public boolean isEmpty() {
        return memes.isEmpty();
    }

    public boolean strengthenMeme(Meme meme) {
        return strengthenMeme(meme, 1);
    }

    public boolean strengthenMeme(Meme meme, int delta) {
        Meme existing = memes.get(meme.toString());
        if (existing != null) {
            existing.increaseImportance(delta);
            return true;
        }
        return false;
    }
}
