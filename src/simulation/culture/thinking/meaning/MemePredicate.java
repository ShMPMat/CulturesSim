package simulation.culture.thinking.meaning;

import java.util.ArrayList;
import java.util.List;

public class MemePredicate extends Meme {
    public MemePredicate(String observerWord) {
        super(observerWord);
        predicates = new ArrayList<>();
    }

    @Override
    public String toString() {
        return observerWord;
    }

    @Override
    public Meme copy() {
        return new MemePredicate(observerWord);
    }
}
