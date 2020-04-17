package simulation.culture.thinking.meaning;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MemeSubject extends Meme {
    private MemeSubject(String observerWord, List<Meme> predicates, int importance) {
        super(observerWord, predicates, importance);
    }

    public MemeSubject(String observerWord, int importance) {
        this(observerWord, new ArrayList<>(), importance);
    }

    public MemeSubject(String observerWord) {
        this(observerWord, 1);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(observerWord);
        for (Meme meme : predicates) {
            stringBuilder.append(" ").append(meme);
        }
        return stringBuilder.toString();
    }

    @Override
    public Meme copy() {
        return new MemeSubject(
                observerWord,
                predicates.stream()
                        .map(Meme::copy)
                        .collect(Collectors.toList()),
                importance
        );
    }

    @Override
    public MemeSubject topMemeCopy() {
        return new MemeSubject(observerWord);
    }
}
