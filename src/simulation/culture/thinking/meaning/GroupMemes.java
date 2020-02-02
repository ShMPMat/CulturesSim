package simulation.culture.thinking.meaning;

import extra.ProbFunc;
import extra.ShnyPair;
import simulation.Controller;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.ConverseWrapper;
import simulation.culture.thinking.language.templates.TextInfo;
import simulation.space.resource.Resource;
import simulation.space.resource.dependency.ConsumeDependency;
import simulation.space.resource.dependency.ResourceDependency;

import java.util.*;
import java.util.stream.Collectors;

public class GroupMemes extends MemePool {
    private Map<String, Meme> memesCombinations;

    public GroupMemes() {
        super();
        memesCombinations = new HashMap<>();
        String[] subjects = {"group"};
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
        if (ProbFunc.testProbability(0.5)) {
            return getValuableMeme();
        }
        return chooseMeme(new ArrayList<>(memesCombinations.values()));
    }

    private Meme chooseMeme(List<Meme> memeList) {
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
        addPairMemes(getAspectMemes(aspect));
    }

    public void addResourceMemes(Resource resource) {
        addPairMemes(getResourceMemes(resource));
    }

    public void addPairMemes(ShnyPair<List<Meme>, List<Meme>> memes) {
        memes.first.forEach(this::add);
        memes.second.forEach(this::addMemeCombination);
    }

    public ShnyPair<List<Meme>, List<Meme>> getAspectMemes(Aspect aspect) {
        ShnyPair<List<Meme>, List<Meme>> aspectMemes = new ShnyPair<>(new ArrayList<>(), new ArrayList<>());
        aspectMemes.first.add(Meme.getMeme(aspect));
        if (aspect instanceof ConverseWrapper) {
            ShnyPair<List<Meme>, List<Meme>> resourceMemes = getResourceMemes(((ConverseWrapper) aspect).resource);
            aspectMemes.first.addAll(resourceMemes.first);
            aspectMemes.second.addAll(resourceMemes.second);
            ((ConverseWrapper) aspect).getResult().forEach(resource -> {
                ShnyPair<List<Meme>, List<Meme>> resMemes = getResourceMemes(resource);
                aspectMemes.first.addAll(resMemes.first);
                aspectMemes.second.addAll(resMemes.second);
            });
        }
        return aspectMemes;
    }

    public ShnyPair<List<Meme>, List<Meme>> getResourceMemes(Resource resource) {
        ShnyPair<List<Meme>, List<Meme>> resourceMemes = getResourceInformationMemes(resource);
        resourceMemes.first.add(Meme.getMeme(resource));
        return resourceMemes;
    }

    public ShnyPair<List<Meme>, List<Meme>> getResourceInformationMemes(Resource resource) {
        ShnyPair<List<Meme>, List<Meme>> infoMemes = new ShnyPair<>(new ArrayList<>(), new ArrayList<>());
        for (ResourceDependency resourceDependency : resource.getGenome().getDependencies()) {
            if (resourceDependency instanceof ConsumeDependency) {
                for (String res : ((ConsumeDependency) resourceDependency).lastConsumed) {
                    Meme subject = new MemeSubject(res.toLowerCase());
                    infoMemes.first.add(subject);
                    Meme object = Meme.getMeme(resource).addPredicate(getMemeCopy("consume"));
                    object.predicates.get(0).addPredicate(subject);
                    infoMemes.second.add(object);
                }
            }
        }
        return infoMemes;
    }

    public List<TextInfo> getAspectTextInfo(Aspect aspect) {
        List<TextInfo> infos = new ArrayList<>();
        if (aspect instanceof ConverseWrapper) {
            infos.addAll(getResourceTextInfo(((ConverseWrapper) aspect).resource));
            ((ConverseWrapper) aspect).getResult().forEach(resource -> {
                infos.addAll(getResourceTextInfo(resource));
            });
        }
        return infos;
    }

    public List<TextInfo> getResourceTextInfo(Resource resource) {
        return getResourceInformationTextInfo(resource);
    }

    public List<TextInfo> getResourceInformationTextInfo(Resource resource) {
        List<TextInfo> infos = new ArrayList<>();
        for (ResourceDependency resourceDependency : resource.getGenome().getDependencies()) {
            if (resourceDependency instanceof ConsumeDependency) {
                for (String res : ((ConsumeDependency) resourceDependency).lastConsumed) {
                    infos.add(generateInfo(Meme.getMeme(resource),
                            getMemeCopy("consume"),
                            new MemeSubject(res.toLowerCase())));
                }
            }
        }
        return infos;
    }

    private TextInfo generateInfo(Meme actor, Meme verb, Meme receiver) {
        return new TextInfo(actor, verb, receiver);
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
        }
        if (meme.isSimple()) {
            add(meme.copy());
            return true;
        }
        addMemeCombination(meme.copy());
        return true;
    }
}
