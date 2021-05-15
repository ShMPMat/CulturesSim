package shmp.simulation.culture.thinking.meaning;

import org.jetbrains.annotations.NotNull;
import shmp.simulation.CulturesController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class MemeTemplate extends Meme {
    MemeTemplate(String observerWord, List<Meme> predicates) {
        super(observerWord, predicates, 1);
        if (!CulturesController.session.templateBase.templateChars.contains(observerWord.charAt(0))) {
            throw new RuntimeException("Wrong template observerWord");
        }
    }

    @Override
    public int getImportance() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public String toString() {
        return getObserverWord();
    }

    @NotNull
    @Override
    public Meme copy() {
        return new MemeTemplate(getObserverWord(), getPredicates().stream()
                .map(Meme::copy)
                .collect(Collectors.toList()));
    }

    @NotNull
    @Override
    public MemeTemplate topMemeCopy() {
        return new MemeTemplate(getObserverWord(), new ArrayList<>());
    }
}
