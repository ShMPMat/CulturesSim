package simulation.culture.thinking.language.templates;

import simulation.Controller;
import simulation.culture.thinking.meaning.Meme;
import simulation.culture.thinking.meaning.MemePredicate;

import java.util.*;

public class TextInfo {
    Map<String, Meme> map = new HashMap<>();

    public TextInfo(Meme actor, Meme verb, Meme receiver) {
        map.put("!actor", actor);
        map.put("@verb", verb);
        map.put("!receiver", receiver);
    }

    public Map<String, Meme> getMap() {
        return map;
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
                if (Controller.session.templateBase.templateChars.contains(child.getObserverWord().charAt(0))) {
                    Meme substitution = map.get(child.getObserverWord());
                    if (substitution == null) {
                        throw new RuntimeException();
                    }
                    substitution = substitution.copy();
                    child.getPredicates().forEach(substitution::addPredicate);
                    predicates.set(i, substitution);
                }
            }
            queue.addAll(current.getPredicates());
        }
        return dummy.getPredicates().get(0);
    }
}
