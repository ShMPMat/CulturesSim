package simulation.culture.thinking.meaning;

import java.util.ArrayList;
import java.util.List;

public class MemePredicate extends Meme {
    private MemePredicate(String observerWord, List<Meme> predicates, int importance) {
        super(observerWord, predicates, importance);
    }

    public MemePredicate(String observerWord, int importance) {
        this(observerWord, new ArrayList<>(), importance);
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
        return new MemePredicate(observerWord, new ArrayList<>(predicates), importance);
    }

    @Override
    public MemePredicate topCopy() {
        return new MemePredicate(observerWord);
    }
}
