package simulation.culture.thinking.meaning;

import kotlin.Pair;
import shmp.random.RandomProbabilitiesKt;
import simulation.Controller;
import simulation.culture.aspect.Aspect;
import simulation.space.resource.Resource;

import java.util.*;
import java.util.stream.Collectors;

public class GroupMemes extends MemePool {
    private Map<String, Meme> memesCombinations;

    public GroupMemes() {
        super();
        memesCombinations = new HashMap<>();
        String[] subjects = {"group", "time", "space", "life", "death", "sun", "luck", "misfortune"};
        addAll(Arrays.stream(subjects).map(MemeSubject::new).collect(Collectors.toList()));
        String[] predicates = {"die", "acquireAspect", "consume", "exist", "and"};
        addAll(Arrays.stream(predicates).map(MemeSubject::new).collect(Collectors.toList()));
        addAll(Controller.session.templateBase.wordBase.values().stream()
                .flatMap(Collection::stream)
                .map(Meme::copy).collect(Collectors.toList()));
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
    public Meme getMeme(String name) {
        Meme memeS = super.getMeme(name);
        return memeS != null ? memeS : getMemeCombinationByName(name);
    }

    @Override
    public Meme getMemeCopy(String name) {
        Meme meme = super.getMemeCopy(name);
        if (meme != null) {
            return meme;
        }
        meme = getMemeCombinationByName(name);
        return meme == null ? null : meme.copy();
    }

    private Meme getMemeCombinationByName(String name) {
        return memesCombinations.get(name.toLowerCase());
    }

    public Meme getValuableMeme() {
        return chooseMeme(getMemes());
    }

    public Meme getMemeWithComplexityBias() {
        if (RandomProbabilitiesKt.testProbability(0.5, Controller.session.random)) {
            return getValuableMeme();
        }
        return chooseMeme(new ArrayList<>(memesCombinations.values()));
    }

    private Meme chooseMeme(List<Meme> memeList) {
        Long reduced = memeList.stream()
                .map(m -> (long) m.importance)
                .reduce(0L, Long::sum);
        if (reduced < 0) {
            List<Meme> bad = memeList.stream().filter(m -> m.importance < 0).collect(Collectors.toList());
            int i = 0;
        }
        long prob = Controller.session.random.nextLong(reduced);
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
        addPairMemes(ConstructMemeKt.constructAspectMemes(aspect));
    }

    public void addResourceMemes(Resource resource) {
        addPairMemes(ConstructMemeKt.constructResourceMemes(resource));
    }

    public void addPairMemes(Pair<List<Meme>, List<Meme>> memes) {
        memes.getFirst().forEach(this::add);
        memes.getSecond().forEach(this::addMemeCombination);
    }

    public void addMemeCombination(Meme meme) {
        if (!memesCombinations.containsKey(meme.toString())) {
            memesCombinations.put(meme.toString(), meme.copy());
        }
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
        }
        if (meme.isSimple()) {
            add(meme.copy());
            return true;
        }
        addMemeCombination(meme.copy());
        return true;
    }
}
