package simulation.culture.thinking.language.templates;

import simulation.Controller;
import simulation.culture.thinking.meaning.Meme;

import java.util.HashMap;
import java.util.Map;

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
        return meme.refactor(m -> {
            if (Controller.session.templateBase.templateChars.contains(m.getObserverWord().charAt(0))) {
                Meme substitution = map.get(m.getObserverWord());
                if (substitution == null) {
                    throw new RuntimeException();
                }
                return substitution.copy();
            } else {
                return m.topCopy();
            }
        });
    }
}
