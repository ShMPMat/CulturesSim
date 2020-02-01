package simulation.culture.thinking.language.templates;

import extra.ProbFunc;
import simulation.culture.thinking.meaning.Meme;
import simulation.culture.thinking.meaning.MemePredicate;

import java.util.ArrayList;
import java.util.List;

public class TemplateBase {
    public static TemplateBase sessionBase;

    public List<Meme> base = new ArrayList<>();

    public TemplateBase() {
        if (sessionBase != null) {
            throw new RuntimeException();
        }
        String[] simpleSentences = {"!actor !verb", "!actor !verb !receiver", "!actor exist"};
        for (String sentence: simpleSentences) {
            String[] splitted = sentence.split(" ");
            Meme head = new MemePredicate(splitted[splitted.length - 1]);
            for (int i = splitted.length - 2; i >= 0; i--) {
                head = (new MemePredicate(splitted[i])).addPredicate(head);
            }
            base.add(head);
        }
        sessionBase = this;
    }

    public Meme getRandomTemplate() {
        return ProbFunc.randomElement(base);
    }
}
