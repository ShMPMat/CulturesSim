package simulation.culture.thinking.meaning;

import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.ConverseWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GroupMemes extends MemePool {
    private List<Meme> memesCombinations;

    public GroupMemes() {
        super();
        memesCombinations = new ArrayList<>();
        String[] subjects = {"group"};
        addAll(Arrays.stream(subjects).map(MemeSubject::new).collect(Collectors.toList()));
        String[] predicates = {"die"};
        addAll(Arrays.stream(predicates).map(MemeSubject::new).collect(Collectors.toList()));
    }

    @Override
    public List<Meme> getMemes() {
        List<Meme> memeList = super.getMemes();
        memeList.addAll(memesCombinations);
        return memeList;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && memesCombinations.isEmpty();
    }

    public void addAspectMemes(Aspect aspect) {
        add(new MemePredicate(aspect.getName()));
        if (aspect instanceof ConverseWrapper) {
            ((ConverseWrapper) aspect).getResult().stream().map(a -> new MemeSubject(a.getName())).forEach(this::add);
        }
    }

    public void addMemeCombination(Meme meme) {
        if (memesCombinations.contains(meme)) {
            return;
        }
        memesCombinations.add(meme);
    }
}
