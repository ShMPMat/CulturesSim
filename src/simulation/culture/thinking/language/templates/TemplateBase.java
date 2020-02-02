package simulation.culture.thinking.language.templates;

import extra.ProbFunc;
import simulation.culture.thinking.meaning.Meme;
import simulation.culture.thinking.meaning.MemePredicate;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

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
            Meme head = new MemePredicate(splitted[splitted.length - 1]);
            for (int i = splitted.length - 2; i >= 0; i--) {
                head = (new MemePredicate(splitted[i])).addPredicate(head);
            }
            sentenceBase.add(head);
        }

        String[] simpleNounClauses = {"!n! @adjective", "!n!"};
        for (String sentence: simpleNounClauses) {
            String[] splitted = sentence.split(" ");
            Meme head = new MemePredicate(splitted[splitted.length - 1]);
            for (int i = splitted.length - 2; i >= 0; i--) {
                head = (new MemePredicate(splitted[i])).addPredicate(head);
            }
            nounClauseBase.add(head);
        }

        String[] adjectives = {"big", "small", "old", "young"};
        wordBase.put("@adjective", Arrays.stream(adjectives).map(MemePredicate::new).collect(Collectors.toList()));
        sessionBase = this;
    }

    public Meme getRandomSentenceTemplate() {
        return ProbFunc.randomElement(sentenceBase);
    }
}
