package simulation.culture.thinking.meaning;

import extra.ProbFunc;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.ConverseWrapper;

import java.util.*;
import java.util.stream.Collectors;

public class GroupMemes extends MemePool {
    private Map<String, Meme> memesCombinations;

    public GroupMemes() {
        super();
        memesCombinations = new HashMap<>();
        String[] subjects = {"group"};
        addAll(Arrays.stream(subjects).map(MemeSubject::new).collect(Collectors.toList()));
        String[] predicates = {"die", "acquireAspect"};
        addAll(Arrays.stream(predicates).map(MemeSubject::new).collect(Collectors.toList()));
    }

    public void addAll(GroupMemes groupMemes) {
        super.addAll(groupMemes);
        groupMemes.memesCombinations.values().forEach(this::addMemeCombination);
    }

    @Override
    public List<Meme> getMemes() {
        List<Meme> memeList = super.getMemes();
        memeList.addAll(memesCombinations.values());
        return memeList;
    }

    @Override
    public Meme getMemeByName(String name) {
        Meme memeS = super.getMemeByName(name);
        return memeS != null ? memeS : getMemeCombinationByName(name);
    }

    private Meme getMemeCombinationByName(String name) {
        return memesCombinations.get(name.toLowerCase());
    }

    public Meme getValuableMeme() {
        List<Meme> memeList = getMemes();
        int prob = ProbFunc.randomInt(memeList.stream().reduce(0, (x, y) -> x + y.importance, Integer::sum));
        memeList.sort(Comparator.comparingInt(meme -> meme.importance));
        for (Meme meme: memeList) {
            prob -= meme.importance;
            if (prob <= 0) {
                meme.increaseImportance(1);
                return meme;
            }
        }
        return memeList.get(0);
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && memesCombinations.isEmpty();
    }

    public void addAspectMemes(Aspect aspect) {
        add(Meme.getMeme(aspect));
        if (aspect instanceof ConverseWrapper) {
            ((ConverseWrapper) aspect).getResult().stream().map(Meme::getMeme).forEach(this::add);
        }
    }

    public void addMemeCombination(Meme meme) {
        memesCombinations.put(meme.toString(), meme);
    }

    @Override
    public boolean strengthenMeme(Meme meme) {
        return strengthenMeme(meme, 1);
    }

    public boolean strengthenMeme(Meme meme, int delta) {
        if (super.strengthenMeme(meme, delta)) {
            return true;
        }
        Meme existing = getMemeCombinationByName(meme.toString());
        if (existing != null) {
            existing.increaseImportance(delta);
            return true;
        }//TODO add simple memes.
        return false;
    }

//    public void addOrStrengthenSimpleMeme(String memeString) { //TODO
//        strengthenMeme(memeString, 1);
//    }
}
