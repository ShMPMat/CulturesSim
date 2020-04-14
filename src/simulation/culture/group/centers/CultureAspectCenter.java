package simulation.culture.group.centers;


import kotlin.Pair;
import simulation.culture.group.cultureaspect.*;
import simulation.culture.group.reason.ConstructReasonsKt;
import simulation.culture.group.reason.Reason;
import simulation.culture.thinking.meaning.ConstructMemeKt;
import simulation.space.resource.Resource;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static shmp.random.RandomCollectionsKt.randomElement;
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
        aspectPool.getAll().forEach(a -> a.use(group));
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
                        ConstructMemeKt.constructAndAddSimpleMeme(
                                group.getCultureCenter().getMemePool(),
                                session.random,
                                0.5
                        ),
                        group,
                        session.random
                );
                break;
            }
            case 2: {
                cultureAspect = ConstructCultureAspectKt.createAestheticallyPleasingObject(
                        group.getCultureCenter().getAspectCenter().getAspectPool().getProducedResources()
                                .stream()
                                .map(Pair::getFirst)
                                .filter(r -> !aestheticallyPleasingResources.contains(r))
                                .max(Comparator.comparingInt(r -> r.getGenome().getBaseDesirability()))
                                .orElse(null),
                        group,
                        session.random
                );
                break;
            }
            case 3: {//TODO recursively go in dependencies;
                cultureAspect = ConstructCultureAspectKt.constructRitual(
                        ConstructReasonsKt.constructBetterAspectUseReason(
                                group,
                                group.getCultureCenter().getAspectCenter().getAspectPool().getConverseWrappers(),
                                reasonsWithSystems,
                                session.random,
                                5//TODO meh, java
                        ),
                        group,
                        session.random
                );
                break;
            }
            case 4: {
                cultureAspect = ConstructCultureAspectKt.createTale(
                        group,
                        session.templateBase,
                        session.random
                );
                break;
            }
        }
        addCultureAspect(cultureAspect);
    }

    void mutateCultureAspects() {
        if (!testProbability(session.groupCultureAspectCollapse, session.random)) {
            return;
        }
        switch (session.random.nextInt(3)) {
            case 0: {
                joinSimilarRituals();
                break;
            }
            case 1: {
                joinSimilarTalesBy("!actor");
//                joinSimilarTalesBy("@verb");//TODO debug off
                break;
            }
            case 2: {
                addCultureAspect(
                        ChangeCultureAspectsKt.takeOutDeity(aspectPool, session.random)
                );
                break;
            }
        }
    }

    void joinSimilarRituals() {
        RitualSystem system = ChangeCultureAspectsKt.takeOutSimilarRituals(aspectPool, group, 3);
        if (system != null) {
            addCultureAspect(system);
            reasonsWithSystems.add(system.getReason());
        }
    }

    void joinSimilarTalesBy(String infoTag) {
        TaleSystem system = ChangeCultureAspectsKt.takeOutSimilarTalesByTag(infoTag, aspectPool, 3);
        if (system != null) {
            addCultureAspect(system);
        }
    }

    public List<Pair<CultureAspect, Group>> getNeighbourCultureAspects() {
        return group.getRelationCenter().getRelatedGroups().stream()
                .flatMap(g ->
                        g.getCultureCenter().getCultureAspectCenter().getAspectPool().getAll().stream()
                                .map(a -> new Pair<>(a, g))
                ).collect(Collectors.toList());
    }

    public List<Pair<CultureAspect, Group>> getNeighbourCultureAspects(Predicate<CultureAspect> predicate) {
        return getNeighbourCultureAspects().stream()
                .filter(p -> predicate.test(p.getFirst()))
                .collect(Collectors.toList());
    }

    void adoptCultureAspects(Group group) {
        if (!session.isTime(session.groupTurnsBetweenAdopts)) {
            return;
        }
        List<Pair<CultureAspect, Group>> cultureAspects = getNeighbourCultureAspects(a -> !aspectPool.contains(a));
        if (!cultureAspects.isEmpty()) {
            try {
                CultureAspect aspect = randomElement(
                        cultureAspects,
                        a -> group.getRelationCenter().getNormalizedRelation(a.getSecond()),
                        session.random
                ).getFirst().copy(group);
                addCultureAspect(aspect);
            } catch (NoSuchElementException e) {}//TODO mb some more smart check?
        }
    }
}
