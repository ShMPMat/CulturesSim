package simulation.culture.thinking.meaning;

import java.util.ArrayList;
import java.util.List;

public class MemeSubject extends Meme {
    public MemeSubject(String observerWord) {
        super(observerWord, new ArrayList<>());
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
        return new MemeSubject(observerWord);
    }
}
