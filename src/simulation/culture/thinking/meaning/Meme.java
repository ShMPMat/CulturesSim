package simulation.culture.thinking.meaning;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

abstract public class Meme {
    String observerWord;
    protected List<Meme> predicates;

    public Meme(String observerWord, List<Meme> predicates) {
        this.observerWord = observerWord.toLowerCase();
        this.predicates = predicates;
    }

    public Meme(String observerWord) {
        this(observerWord, new ArrayList<>());
    }

    public Meme addPredicate(Meme predicate) {
        predicates.add(predicate);
        return this;
    }

    @Override
    abstract public String toString();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Meme meme = (Meme) o;
        return Objects.equals(toString(), meme.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(observerWord);
    }

    public abstract Meme copy();
}
