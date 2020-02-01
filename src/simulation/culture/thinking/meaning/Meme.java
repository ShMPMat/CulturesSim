package simulation.culture.thinking.meaning;

import simulation.culture.aspect.Aspect;
import simulation.space.resource.Resource;

import java.util.*;
import java.util.stream.Collectors;

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

    public static Meme getMeme(Aspect aspect) {
        return new MemePredicate(aspect.getName());
    }

    public static Meme getMeme(Resource resource) {
        return new MemeSubject(resource.getFullName());
    }

    public int getImportance() {
        return importance;
    }

    public String getObserverWord() {
        return observerWord;
    }

    public List<Meme> getPredicates() {
        return predicates;
    }

    public boolean isSimple() {
        return predicates.isEmpty();
    }

    public void increaseImportance(int delta) {
        importance += delta;
    }

    public Meme addPredicate(Meme predicate) {
        predicates.add(predicate);
        return this;
    }

    public List<Meme> splitOn(Collection<String> splitters) {
        List<Meme> memes = new ArrayList<>();
        Queue<Meme> newMemes = new ArrayDeque<>();
        Meme copy = copy();
        memes.add(copy);
        newMemes.add(copy);
        while (!newMemes.isEmpty()) {
            Meme current = newMemes.poll();
            if (splitters.contains(current.observerWord)) {
                memes.addAll(current.predicates);
                continue;
            }
            List<Meme> children = current.predicates;
            for (int i = 0; i < children.size(); i++) {
                Meme child = children.get(i);
                if (splitters.contains(child.observerWord)) {
                    children.remove(i);
                    i--;
                    memes.addAll(child.predicates);
                }
                newMemes.addAll(child.predicates);
            }
        }
        return memes.stream().distinct().collect(Collectors.toList());
    }

    public boolean hasPart(Meme that, Collection<String> splitters) {
        Collection<Meme> thatMemes = that.splitOn(splitters);
        return splitOn(splitters).stream().anyMatch(thatMemes::contains);
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
