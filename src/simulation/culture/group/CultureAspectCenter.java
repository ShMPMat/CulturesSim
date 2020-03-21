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
import simulation.culture.thinking.language.templates.ConstructTextInfoKt;
import simulation.culture.thinking.language.templates.TextInfo;
import simulation.culture.thinking.meaning.ConstructMemeKt;
import simulation.culture.thinking.meaning.Meme;
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
                        ConstructMemeKt.constructAndAddMeme(
                                group.getCultureCenter().getMemePool(),
                                session.random,
                                0.5 //TODO meh, java
                        ),
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
                cultureAspect = constructRitual(ConstructReasonsKt.constructBetterAspectUseReason(
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
                TextInfo info = ConstructTextInfoKt.constructTextInfo(
                        group.getCultureCenter(),
                        session.templateBase,
                        session.random
                );
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

    public Ritual constructRitual(Reason reason) {
        if (reason == null) {
            return null;
        } else if (reason instanceof BetterAspectUseReason) {
            return constructBetterAspectUseReasonRitual((BetterAspectUseReason) reason);
        }
        return null;
    }

    private Ritual constructBetterAspectUseReasonRitual(BetterAspectUseReason reason) {
        ConverseWrapper converseWrapper = reason.getConverseWrapper();
        Pair<List<Meme>, List<Meme>> temp = group.getCultureCenter().getMemePool().getAspectMemes(converseWrapper);
        List<Meme> aspectMemes = temp.getFirst();
        aspectMemes.addAll(temp.getSecond());
        Collections.shuffle(aspectMemes);
        for (Meme meme : aspectMemes) {//TODO maybe depth check
            if (meme.getObserverWord().equals(converseWrapper.getName())) {
                continue;
            }
            if (group.getCultureCenter().getAspectCenter().getAspectPool().contains(meme.getObserverWord())) {
                Aspect myAspect = group.getCultureCenter().getAspectCenter()
                        .getAspectPool().get(meme.getObserverWord());
                if (myAspect instanceof ConverseWrapper) {
                    return new AspectRitual(group, (ConverseWrapper) myAspect, reason);
                }
            } else {
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
        return null;
    }

    public List<CultureAspect> getNeighbourCultureAspects() {
        return group.getRelationCenter().getRelatedGroups().stream()
                .flatMap(g -> g.getCultureCenter().getCultureAspectCenter().getAspectPool().getAll().stream())
                .collect(Collectors.toList());
    }

    public List<CultureAspect> getNeighbourCultureAspects(Predicate<CultureAspect> predicate) {
        return getNeighbourCultureAspects().stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    void adoptCultureAspects() {
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
    }
}
