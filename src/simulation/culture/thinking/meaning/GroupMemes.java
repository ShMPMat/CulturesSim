package simulation.culture.thinking.meaning;

import extra.ProbFunc;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.ConverseWrapper;

import java.util.*;
import java.util.stream.Collectors;

public class GroupMemes extends MemePool {
    private Set<Meme> memesCombinations;

    public GroupMemes() {
        super();
        memesCombinations = new HashSet<>();
        String[] subjects = {"group"};
        addAll(Arrays.stream(subjects).map(MemeSubject::new).collect(Collectors.toList()));
        String[] predicates = {"die", "acquireAspect"};
        addAll(Arrays.stream(predicates).map(MemeSubject::new).collect(Collectors.toList()));
    }

    public void addAll(GroupMemes groupMemes) {
        super.addAll(groupMemes);
        groupMemes.memesCombinations.forEach(this::addMemeCombination);
    }

    @Override
    public List<Meme> getMemes() {
        List<Meme> memeList = super.getMemes();
        memeList.addAll(memesCombinations);
        return memeList;
    }

    @Override
    public Meme getMemeByName(String name) {
        Meme memeS = super.getMemeByName(name);
        return memeS != null ? memeS : memesCombinations.stream()
                .filter(meme -> meme.toString().equals(name.toLowerCase())).findFirst().orElse(null);
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
        add(new MemePredicate(aspect.getName()));
        if (aspect instanceof ConverseWrapper) {
            ((ConverseWrapper) aspect).getResult().stream().map(a -> new MemeSubject(a.getBaseName())).forEach(this::add);
        }
    }

    public void addMemeCombination(Meme meme) {
        memesCombinations.add(meme);
    }

    public void strengthenAspectMeme(Aspect aspect) {
        strengthenMeme((new MemePredicate(aspect.getName())).toString());
    }

    public void strengthenMeme(String memeString) {
        strengthenMeme(memeString, 1);
    }

    public void strengthenMeme(String memeString, int delta) {
        Meme existing = getMemeByName(memeString);
        if (existing != null) {
            existing.increaseImportance(delta);
        } else {//TODO shit.
            int i = 0;
        }
    }
}
