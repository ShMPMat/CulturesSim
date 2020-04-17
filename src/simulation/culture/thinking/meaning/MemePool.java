package simulation.culture.thinking.meaning;


import java.util.*;

public class MemePool {
    private Map<String, Meme> memes;

    private MemePool(Collection<Meme> memes) {
        this.memes = new HashMap<>();
        memes.forEach(this::add);
    }

    MemePool() {
        this(new ArrayList<>());
    }

    public boolean add(Meme meme) {
        if (memes.containsKey(meme.toString())) {
            return false;
        }
        memes.put(meme.toString(), meme.copy());
        return true;
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

    public Meme getMeme(String name) {
        return memes.get(name.toLowerCase());
    }

    public Meme getMemeCopy(String name) {
        Meme meme = getMeme(name);
        return meme == null ? null : meme.copy();
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
