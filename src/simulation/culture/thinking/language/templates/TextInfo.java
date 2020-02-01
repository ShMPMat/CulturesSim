package simulation.culture.thinking.language.templates;

import simulation.culture.thinking.meaning.Meme;
import simulation.culture.thinking.meaning.MemePredicate;

import java.util.*;

public class TextInfo {
    Map<String, Meme> map = new HashMap<>();

    public TextInfo(Meme actor) {
        map.put("!actor", actor);
    }

    public Meme substitute(Meme meme) {
        Meme dummy = new MemePredicate("dummy");
        dummy.addPredicate(meme.copy());
        Queue<Meme> queue = new ArrayDeque<>();
        queue.add(dummy);
        while (!queue.isEmpty()) {
            Meme current = queue.poll();
            List<Meme> predicates = current.getPredicates();
            for (int i = 0; i < predicates.size(); i++) {
                Meme child = predicates.get(i);
                if (child.getObserverWord().charAt(0) == '!') {
                    Meme substitution = map.get(child.getObserverWord().substring(1));
                    if (substitution == null) {
                        throw new RuntimeException();
                    }
                    substitution = substitution.copy();
                    predicates.set(i, substitution);
                }
                queue.addAll(meme.getPredicates());
            }
        }
        return dummy.getPredicates().get(0);
    }
}
