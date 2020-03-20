package simulation.culture.group;


import extra.ShnyPair;
import kotlin.Pair;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.ConverseWrapper;
import simulation.culture.group.cultureaspect.MutableCultureAspectPool;
import simulation.culture.group.cultureaspect.*;
import simulation.culture.group.reason.BetterAspectUseReason;
import simulation.culture.group.reason.Reason;
import simulation.culture.group.reason.ConstructReasonsKt;
import simulation.culture.thinking.language.templates.TextInfo;
import simulation.culture.thinking.meaning.Meme;
import simulation.culture.thinking.meaning.MemePredicate;
import simulation.space.resource.Resource;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static shmp.random.RandomCollectionsKt.randomElement;
import static shmp.random.RandomCollectionsKt.randomElementWithProbability;
import static shmp.random.RandomProbabilitiesKt.testProbability;
import static simulation.Controller.session;

public class CultureAspectCenter {
    private Group group;
    private MutableCultureAspectPool aspectPool;
    private Set<Resource> aestheticallyPleasingResources = new HashSet<>();

    private Set<Reason> reasonsWithSystems = new HashSet<>();

    CultureAspectCenter(Group group, Set<CultureAspect> cultureAspects) {
        this.group = group;
        this.aspectPool = new MutableCultureAspectPool(cultureAspects);
    }

    public MutableCultureAspectPool getAspectPool() {
        return aspectPool;
    }

    public void addCultureAspect(CultureAspect cultureAspect) {
        if (cultureAspect != null) {
            aspectPool.add(cultureAspect);
        }
        if (cultureAspect instanceof AestheticallyPleasingObject) {
            aestheticallyPleasingResources.add(
                    ((AestheticallyPleasingObject) cultureAspect).getResource()
            );
        }
    }

    void useCultureAspects() {
        aspectPool.getAll().forEach(a -> a.use(group.getCultureCenter()));
    }

    void addRandomCultureAspect(Group group) {
        if (!testProbability(session.cultureAspectBaseProbability, session.random)) {
            return;
        }
        CultureAspect cultureAspect = null;
        switch (session.random.nextInt(5)) {
            case 0: {
                cultureAspect = ConstructCultureAspectKt.createDepictObject(
                        group.getCultureCenter().getAspectCenter().getAspectPool().getMeaningAspects(),
                        constructMeme(),
                        group
                );
                break;
            }
            case 2: {
                cultureAspect = ConstructCultureAspectKt.createAestheticallyPleasingObject(
                        group.getCultureCenter().getAspectCenter().getAspectPool()
                                .getProducedResources().stream()
                                .map(Pair::getFirst)
                                .filter(r -> !aestheticallyPleasingResources.contains(r))
                                .max(Comparator.comparingInt(r -> r.getGenome().getBaseDesirability()))
                                .orElse(null),
                        group
                );
                break;
            }
            case 3: {//TODO recursively go in dependencies;
                cultureAspect = constructRitualForReason(ConstructReasonsKt.constructBetterAspectUseReason(
                        group,
                        group.getCultureCenter().getAspectCenter().getAspectPool().getConverseWrappers(),
                        reasonsWithSystems,
                        session.random,
                        5//TODO meh, java
                ));
                break;
            }
            case 4: {
                Meme template = session.templateBase.getRandomSentenceTemplate();
                TextInfo info = generateTextInfo();
                if (template != null && info != null) {
                    cultureAspect = new Tale(group, template, info);
                }
                break;
            }
            case 5: {//TODO should I addAll such a thing here even? (I did break in previous section for now)
                List<ConverseWrapper> wrappers =
                        group.getCultureCenter().getAspectCenter().getAspectPool().getConverseWrappers();
                if (!wrappers.isEmpty()) {
                    ConverseWrapper converseWrapper = randomElement(wrappers, session.random);
                    Reason reason = ConstructReasonsKt.randomReason(group, session.random);
                    if (reason != null) {
                        cultureAspect = new AspectRitual(group, converseWrapper, reason);
                    }
                }
                break;
            }
        }
        addCultureAspect(cultureAspect);
    }

    private TextInfo generateTextInfo() {//TODO too slow
        List<TextInfo> textInfos = group.getCultureCenter().getAspectCenter().getAspectPool().getConverseWrappers()
                .stream()
                .flatMap(cw -> group.getCultureCenter().getMemePool().getAspectTextInfo(cw).stream())
                .collect(Collectors.toList());
        return textInfos.isEmpty() ? null : complicateInfo(randomElement(textInfos, session.random));
    }

    private TextInfo complicateInfo(TextInfo info) {
        if (info == null) {
            return null;
        }
        Map<String, Meme> substitutions = new HashMap<>();
        for (Map.Entry<String, Meme> entry : info.getMap().entrySet()) {
            if (entry.getKey().charAt(0) == '!') {
                substitutions.put(entry.getKey(), randomElement(session.templateBase.nounClauseBase, session.random)
                        .refactor(m -> {
                            if (m.getObserverWord().equals("!n!")) {
                                return entry.getValue().topCopy();
                            } else if (session.templateBase.templateChars.contains(m.getObserverWord().charAt(0))) {
                                substitutions.put(entry.getKey() + m.getObserverWord(),
                                        randomElementWithProbability(
                                                session.templateBase.wordBase.get(m.getObserverWord()),
                                                n -> (double) group.getCultureCenter()
                                                        .getMemePool()
                                                        .getMeme(n.getObserverWord())
                                                        .getImportance(),
                                                session.random)
                                );
                                return new MemePredicate(entry.getKey() + m.getObserverWord());
                            } else {
                                return m.topCopy();
                            }
                        }));
            }
        }
        substitutions.forEach((key, value) -> info.getMap().put(key, value));
        return info;
    }

    void mutateCultureAspects() {
        if (!testProbability(session.groupCultureAspectCollapse, session.random)) {
            return;
        }
        List<Ritual> rituals = aspectPool
                .filter(ca -> ca instanceof Ritual)
                .stream()
                .map(ca -> (Ritual) ca)
                .collect(Collectors.toList());
        Map<Reason, List<Ritual>> dividedRituals = new HashMap<>();
        rituals.forEach(r -> {
            if (dividedRituals.containsKey(r.getReason())) {
                dividedRituals.get(r.getReason()).add(r);
            } else {
                List<Ritual> temp = new ArrayList<>();
                temp.add(r);
                dividedRituals.put(r.getReason(), temp);
            }
        });
        List<Ritual> popularReasonRituals = dividedRituals.values().stream()
                .max(Comparator.comparingInt(List::size))
                .orElse(new ArrayList<>());
        if (popularReasonRituals.size() >= 3) {
            addCultureAspect(new RitualSystem(group, popularReasonRituals, popularReasonRituals.get(0).getReason()));
            aspectPool.removeAll(popularReasonRituals);
            reasonsWithSystems.add(popularReasonRituals.get(0).getReason());
        }
    }

    private Meme constructMeme() {
        Meme meme = group.getCultureCenter().getMemePool().getMemeWithComplexityBias();
        if (testProbability(0.5, session.random)) {
            Meme second;
            do {
                second = group.getCultureCenter().getMemePool().getMemeWithComplexityBias().copy();
            } while (second.hasPart(meme, Collections.singleton("and")));
            meme = meme.copy().addPredicate(
                    group.getCultureCenter().getMemePool().getMemeCopy("and").addPredicate(second)
            );
            group.getCultureCenter().getMemePool().addMemeCombination(meme);
        }
        return meme;
    }

    public Ritual constructRitualForReason(Reason reason) {
        if (reason == null) {
            return null;
        }
        if (reason instanceof BetterAspectUseReason) {
            ConverseWrapper converseWrapper = ((BetterAspectUseReason) reason).getConverseWrapper();
            ShnyPair<List<Meme>, List<Meme>> temp =
                    group.getCultureCenter().getMemePool().getAspectMemes(converseWrapper);
            List<Meme> aspectMemes = temp.first;
            aspectMemes.addAll(temp.second);
            Collections.shuffle(aspectMemes);
            for (Meme meme : aspectMemes) {//TODO maybe depth check
                if (meme.getObserverWord().equals(converseWrapper.getName())) {
                    continue;
                }
                try {
                    Aspect myAspect = group.getCultureCenter().getAspectCenter()
                            .getAspectPool().get(meme.getObserverWord());
                    if (myAspect instanceof ConverseWrapper) {
                        return new AspectRitual(group, (ConverseWrapper) myAspect, reason);
                    }
                } catch (NoSuchElementException e) {
                    List<ConverseWrapper> options =
                            group.getCultureCenter().getAspectCenter().getAspectPool().getProducedResources().stream()
                                    .filter(pair -> pair.getFirst().getBaseName().equals(meme.getObserverWord()))
                                    .map(Pair::getSecond)
                                    .collect(Collectors.toList());
                    if (!options.isEmpty()) {
                        return new AspectRitual(group, randomElement(options, session.random), reason);
                    }
                    switch (session.random.nextInt(1)) {
                        case 0:
                            Set<ConverseWrapper> meaningAspects = group.getCultureCenter()
                                    .getAspectCenter()
                                    .getAspectPool()
                                    .getMeaningAspects();
                            CultureAspect aspect = ConstructCultureAspectKt.createDepictObject(
                                    meaningAspects,
                                    randomElement(aspectMemes, session.random),
                                    group
                            );
                            if (aspect != null) {
                                return new CultureAspectRitual(
                                        group,
                                        aspect,
                                        reason
                                );
                            }//TODO make complex tales;
                    }
                }
            }
        }
        return null;
    }

    public List<CultureAspect> getNeighbourCultureAspects() {
        List<CultureAspect> allExistingAspects = new ArrayList<>();
        for (Group neighbour : group.getRelationCenter().getRelatedGroups()) {
            allExistingAspects.addAll(neighbour.getCultureCenter().getCultureAspectCenter().getAspectPool().getAll());
        }
        return allExistingAspects;
    }

    public List<CultureAspect> getNeighbourCultureAspects(Predicate<CultureAspect> predicate) {
        return getNeighbourCultureAspects().stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    void adoptCultureAspects() {
        try {
            if (!session.isTime(session.groupTurnsBetweenAdopts)) {
                return;
            }
            List<CultureAspect> cultureAspects = getNeighbourCultureAspects(a -> !aspectPool.contains(a));
            if (!cultureAspects.isEmpty()) {
                CultureAspect aspect = randomElementWithProbability(
                        cultureAspects,
                        a -> group.getRelationCenter().getNormalizedRelation(a.getGroup()),
                        session.random
                );
                addCultureAspect(aspect);
            }
        } catch (NullPointerException e) {
            throw new RuntimeException();
        }
    }
}
