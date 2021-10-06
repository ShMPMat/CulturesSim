package shmp.simulation.culture.thinking.language.templates;

import shmp.generator.culture.worldview.Meme;
import shmp.simulation.CulturesController;

import java.util.*;
import java.util.stream.Collectors;

import static shmp.random.RandomElementKt.randomElement;


public class TemplateBase {
    public static TemplateBase sessionBase;

    public List<Meme> sentenceBase = new ArrayList<>();
    public List<Meme> nounClauseBase = new ArrayList<>();
    public Map<String, List<Meme>> wordBase = new HashMap<>();
    public Set<Character> templateChars = new HashSet<>();

    public TemplateBase() {
        if (sessionBase != null) {
            throw new RuntimeException();
        }

        templateChars.addAll(Arrays.asList('!', '@'));

        String[] simpleSentences = {"!actor @verb", "!actor @verb !receiver", "!actor exist"};
        for (String sentence: simpleSentences) {
            String[] splitted = sentence.split(" ");
            Meme head = new Meme(splitted[splitted.length - 1], new ArrayList<>(), 1);
            for (int i = splitted.length - 2; i >= 0; i--) {
                head = (new Meme(splitted[i], new ArrayList<>(), 1)).addPredicate(head);
            }
            sentenceBase.add(head);
        }

        String[] simpleNounClauses = {"!n! @adjective", "!n!"};
        for (String sentence: simpleNounClauses) {
            String[] splitted = sentence.split(" ");
            Meme head = new Meme(splitted[splitted.length - 1], new ArrayList<>(), 1);
            for (int i = splitted.length - 2; i >= 0; i--) {
                head = (new Meme(splitted[i], new ArrayList<>(), 1)).addPredicate(head);
            }
            nounClauseBase.add(head);
        }

        String[] adjectives = {"big", "small", "old", "young"};
        wordBase.put(
                "@adjective",
                Arrays.stream(adjectives).map((a) -> new Meme(a, new ArrayList<>(), 1)).collect(Collectors.toList())
        );
        sessionBase = this;
    }

    public Meme getRandomSentenceTemplate() {
        return randomElement(sentenceBase, CulturesController.session.random);
    }
}
