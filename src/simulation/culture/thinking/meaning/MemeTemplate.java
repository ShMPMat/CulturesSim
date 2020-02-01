package simulation.culture.thinking.meaning;

import java.util.List;
import java.util.stream.Collectors;

public class MemeTemplate extends Meme {
    MemeTemplate(String observerWord, List<Meme> predicates) {
        super(observerWord, predicates);
        if (observerWord.charAt(0) != '!') {
            throw new RuntimeException("Wrong template observerWord");
        }
    }

    @Override
    public int getImportance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return observerWord;
    }

    @Override
    public Meme copy() {
        return new MemeTemplate(observerWord, predicates.stream().map(Meme::copy).collect(Collectors.toList()));
    }
}
