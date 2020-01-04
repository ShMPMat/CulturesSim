package simulation.culture.thinking.meaning;

import java.util.ArrayList;
import java.util.List;

public class MemePredicate extends Meme {
    public MemePredicate(String observerWord, int importance) {
        super(observerWord, new ArrayList<>(), importance);
    }

    public MemePredicate(String observerWord) {
        this(observerWord, 1);
    }

    @Override
    public String toString() {
        return observerWord;
    }

    @Override
    public Meme copy() {
        return new MemePredicate(observerWord, importance);
    }
}
