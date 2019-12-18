package simulation.culture.thinking.meaning;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

abstract public class Meme {
    String observerWord;
    List<Meme> predicates;
    int importance;

    Meme(String observerWord, List<Meme> predicates, int importance) {
        this.observerWord = observerWord.toLowerCase();
        this.predicates = predicates;
        this.importance = importance;
    }

    Meme(String observerWord, List<Meme> predicates) {
        this(observerWord, predicates, 1);
    }

    Meme(String observerWord) {
        this(observerWord, new ArrayList<>());
    }

    public int getImportance() {
        return importance;
    }

    public void increaseImportance(int delta) {
        importance += delta;
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
        return Objects.hash(toString());
    }

    public abstract Meme copy();
}
