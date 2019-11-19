package simulation.culture.thinking.meaning;

import extra.ProbFunc;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.ConverseWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GroupMemes extends MemePool {
    private List<Meme> memesCombinations;

    public GroupMemes() {
        super();
        memesCombinations = new ArrayList<>();
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

    public Meme getValuableMeme() {
        List<Meme> memeList = getMemes();
        int prob = ProbFunc.randomInt(memeList.stream().reduce(0, (x, y) -> x + y.importance, Integer::sum));
        memeList.sort(Comparator.comparingInt(meme -> meme.importance));
        for (Meme meme: memeList) {
            prob -= meme.importance;
            if (prob <= 0) {
                meme.importance++;
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
        if (memesCombinations.contains(meme)) {
            return;
        }
        memesCombinations.add(meme);
    }
}
