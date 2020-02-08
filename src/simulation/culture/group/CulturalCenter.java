package simulation.culture.group;

import extra.ShnyPair;
import simulation.culture.Event;
import simulation.culture.aspect.*;
import simulation.culture.aspect.dependency.*;
import simulation.culture.group.cultureaspect.*;
import simulation.culture.group.intergroup.Relation;
import simulation.culture.group.reason.BetterAspectUseReason;
import simulation.culture.group.reason.Reason;
import simulation.culture.group.reason.Reasons;
import simulation.culture.group.request.Request;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.culture.group.request.TagRequest;
import simulation.culture.thinking.language.templates.TextInfo;
import simulation.culture.thinking.meaning.GroupMemes;
import simulation.culture.thinking.meaning.Meme;
import simulation.culture.thinking.meaning.MemePredicate;
import simulation.culture.thinking.meaning.MemeSubject;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourcePack;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static extra.ProbabilityFuncs.*;
import static simulation.Controller.*;

/**
 * Takes responsibility of Group's cultural change.
 */
public class CulturalCenter {
    private AspectCenter aspectCenter;
    private List<Aspiration> aspirations = new ArrayList<>();
    private Group group;
    private Set<Aspect> aspects = new HashSet<>();
    private Set<Resource> aestheticallyPleasingResources = new HashSet<>();
    private Set<Aspect> changedAspects = new HashSet<>(); // always equals to aspects except while making a turn
    private List<Event> events = new ArrayList<>();
    private List<Request> requests = new ArrayList<>();
    private Set<CultureAspect> cultureAspects = new HashSet<>();
    private GroupMemes memePool = new GroupMemes();

    private List<ConverseWrapper> _converseWrappers = new ArrayList<>();
    private List<Resource> _lastResourcesForCw = new ArrayList<>();
    private Set<Reason> reasonsWithSystems = new HashSet<>();

    private static int constructProbes = 5;

    private Meme currentMeme;

    Map<Group, Relation> relations = new HashMap<>();

    CulturalCenter(Group group) {
        this.group = group;
        this.aspectCenter = new AspectCenter(group);
    }

    List<Aspiration> getAspirations() {
        return aspirations;
    }

    Set<Aspect> getAspects() {
        return aspects;
    }

    public List<ConverseWrapper> getConverseWrappers() {
        return aspects.stream()
                .filter(aspect -> aspect instanceof ConverseWrapper).map(aspect -> (ConverseWrapper) aspect)
                .collect(Collectors.toList());
    }

    Set<ConverseWrapper> getMeaningAspects() {
        return aspects.stream()
                .filter(aspect -> aspect instanceof ConverseWrapper && aspect.canReturnMeaning())
                .map(aspect -> (ConverseWrapper) aspect).collect(Collectors.toSet());
    }

    Aspect getAspect(Aspect aspect) {
        return getAspects().stream().filter(aspect::equals).findFirst().orElse(null);
    }

    Aspect getAspect(String name) {
        return getAspects().stream().filter(aspect -> aspect.getName().equals(name)).findFirst().orElse(null);
    }

    List<Request> getRequests() {
        return requests;
    }

    Set<Aspect> getChangedAspects() {
        return changedAspects;
    }

    List<Event> getEvents() {
        return events;
    }

    public Set<CultureAspect> getCultureAspects() {
        return cultureAspects;
    }

    public Set<Group> getNeighbourGroups() {
        Set<Group> groups = new HashSet<>(group.getParentGroup().subgroups);
        groups.remove(group);
        groups.addAll(relations.keySet());
        return groups;
    }

    private List<Aspect> getNeighboursAspects() {
        List<Aspect> allExistingAspects = new ArrayList<>();
        for (Group neighbour : relations.keySet()) {
            allExistingAspects.addAll(neighbour.getAspects().stream()
                    .filter(aspect -> !(aspect instanceof ConverseWrapper) || getAspect(((ConverseWrapper) aspect).aspect) != null)
                    .collect(Collectors.toList()));
        }
        return allExistingAspects;
    }

    private List<CultureAspect> getNeighboursCultureAspects() {
        List<CultureAspect> allExistingAspects = new ArrayList<>();
        for (Group neighbour : relations.keySet()) {
            allExistingAspects.addAll(neighbour.getCulturalCenter().getCultureAspects());
        }
        return allExistingAspects;
    }

    public List<ShnyPair<Resource, ConverseWrapper>> getAllProducedResources() {
        List<ShnyPair<Resource, ConverseWrapper>> _m = new ArrayList<>();
        for (ConverseWrapper converseWrapper : aspects.stream().filter(aspect -> aspect instanceof ConverseWrapper)
                .map(aspect -> (ConverseWrapper) aspect).collect(Collectors.toList())) {
            for (Resource resource : converseWrapper.getResult()) {
                _m.add(new ShnyPair<>(resource, converseWrapper));
            }
        }
        return _m;
    }

    public GroupMemes getMemePool() {
        return memePool;
    }

    public Meme getCurrentMeme() {
        return currentMeme;
    }

    void addMemeCombination(Meme meme) {
        memePool.addMemeCombination(meme);
    }

    void addAspiration(Aspiration aspiration) {
        if (aspirations.stream().noneMatch(aspir -> aspir.equals(aspiration))) {
            aspirations.add(aspiration);
        }
    }

    public boolean addAspect(Aspect aspect) {
        if (!aspect.isValid()) {
            return false;
        }
        if (aspects.contains(aspect)) {
            aspect = aspects.stream().filter(aspect::equals).findFirst().orElse(aspect);
        }
        Map<AspectTag, Set<Dependency>> _m = canAddAspect(aspect);
        if (!aspect.isDependenciesOk(_m)) {
            return false;
        }
        if (aspect.getName().contains("Incrust")) {
            int i = 0;
        }

        addAspectNow(aspect, _m);
        Aspect finalAspect = aspect;
        return true;
    }

    void addAspectNow(Aspect aspect, Map<AspectTag, Set<Dependency>> dependencies) {
        Aspect _a = null;
        if (getChangedAspects().contains(aspect)) {
            for (Aspect as : getChangedAspects()) {
                if (as.equals(aspect)) {
                    as.addOneDependency(dependencies);
                    _a = as;
                }
            }
        } else {
            _a = aspect.copy(dependencies, group);
            getChangedAspects().add(_a);
            if (!(_a instanceof ConverseWrapper)) {//TODO maybe should do the same in straight
                Set<Resource> allResources = new HashSet<>(group.getOverallTerritory().getDifferentResources());
                allResources.addAll(getAllProducedResources().stream().map(pair -> pair.first).collect(Collectors.toSet()));
                for (Resource resource : allResources) {
                    addConverseWrapper(_a, resource);
                }
            }
        }
        memePool.addAspectMemes(aspect);
        addMemeCombination((new MemeSubject(group.name).addPredicate(
                session.world.getPoolMeme("acquireAspect").addPredicate(new MemeSubject(aspect.getName())))));
        neededAdding(_a);
    }

    void hardAspectAdd(Aspect aspect) {
        changedAspects.add(aspect);
        aspects.add(aspect);
        neededAdding(aspect);
    }

    void neededAdding(Aspect aspect) {
        if (group.getStrata().stream().noneMatch(stratum -> stratum.containsAspect(aspect))) {
            group.getStrata().add(new Stratum(0, aspect, group));
        }
    }


    Map<AspectTag, Set<Dependency>> canAddAspect(Aspect aspect) {
        Map<AspectTag, Set<Dependency>> dep = new HashMap<>();
        if (aspect instanceof ConverseWrapper) {
            addForConverseWrapper((ConverseWrapper) aspect, dep);
        }
        for (AspectTag requirement : aspect.getRequirements()) {
            if (requirement.name.equals(AspectTag.phony().name) || requirement.isWrapperCondition()) {
                continue;
            }
            addAspectDependencies(requirement, dep, aspect);
        }
        return dep;
    }

    private void addForConverseWrapper(ConverseWrapper converseWrapper, Map<AspectTag, Set<Dependency>> dep) {
        if (converseWrapper.resource.hasApplicationForAspect(converseWrapper.aspect)) {
            if (converseWrapper.canTakeResources() && group.getOverallTerritory().getDifferentResources().contains(converseWrapper.resource)) {
                addDependenciesInMap(dep, Collections.singleton(
                        new ConversionDependency(converseWrapper.getRequirement(), group,
                                new ShnyPair<>(converseWrapper.resource, converseWrapper.aspect))), converseWrapper.getRequirement());
            }
            addDependenciesInMap(dep, getAllProducedResources().stream()
                            .filter(pair -> pair.first.equals(converseWrapper.resource))
                            .map(pair -> new LineDependency(converseWrapper.getRequirement(), group,
                                    new ShnyPair<>(converseWrapper, pair.second)))
                            .filter(dependency -> !dependency.isCycleDependency(converseWrapper))
                            .collect(Collectors.toList()),
                    converseWrapper.getRequirement());
        }
    }

    private void addResourceDependencies(AspectTag requirement, Map<AspectTag, Set<Dependency>> dep) {
        List<Resource> _r = group.getTerritory().getResourcesWithAspectTag(requirement);
        if (_r != null) {
            addDependenciesInMap(dep, _r.stream()
                    .map(resource -> new ResourceDependency(requirement, group, resource))
                    .collect(Collectors.toList()), requirement);
        }
    }

    private void addAspectDependencies(AspectTag requirement, Map<AspectTag, Set<Dependency>> dep, Aspect aspect) {
        for (Aspect selfAspect : getAspects()) {
            if (selfAspect.getTags().contains(requirement)) {
                Dependency dependency = new AspectDependency(requirement, selfAspect);
                if (dependency.isCycleDependency(selfAspect) || dependency.isCycleDependencyInner(aspect)) {
                    continue;
                }
                addDependenciesInMap(dep, Collections.singleton(dependency), requirement);
            }
            addDependenciesInMap(dep, group.getTerritory().getResourcesWhichConverseToTag(selfAspect, requirement).stream() //Make converse Dependency_
                            .map(resource ->
                                    new ConversionDependency(requirement, group, new ShnyPair<>(resource, selfAspect)))
                            .filter(dependency -> !dependency.isCycleDependency(aspect))
                            .collect(Collectors.toList()),
                    requirement);
        }
    }

    private void addDependenciesInMap(Map<AspectTag, Set<Dependency>> dep, Collection<Dependency> dependencies,
                                      AspectTag requirement) {
        if (dependencies.isEmpty()) {
            return;
        }
        if (!dep.containsKey(requirement)) {
            dep.put(requirement, new HashSet<>());
        }
        dep.get(requirement).addAll(dependencies);
    }

    public void addCultureAspect(CultureAspect cultureAspect) {
        if (cultureAspect != null) {
            cultureAspects.add(cultureAspect);
        }
        if (cultureAspect instanceof AestheticallyPleasingObject) {
            aestheticallyPleasingResources.add(((AestheticallyPleasingObject) cultureAspect).getResource());
        }
    }

    public void addResourceWant(Resource resource) {
        addCultureAspect(new AestheticallyPleasingObject(group, resource));
    }

    void updateRequests() {
        requests = new ArrayList<>();
        int foodFloor = group.population / group.getFertility() + 1;
        BiFunction<ShnyPair<Group, ResourcePack>, Double, Void> foodPenalty = (pair, percent) -> {
            pair.first.starve(percent);
            pair.second.destroyAllResourcesWithTag(new AspectTag("food"));
            return null;
        };
        BiFunction<ShnyPair<Group, ResourcePack>, Double, Void> foodReward = (pair, percent) -> {
            pair.first.population += ((int) (percent * pair.first.population)) / 10 + 1;
            pair.first.population = Math.min(pair.first.population, group.getMaxPopulation());
            pair.second.destroyAllResourcesWithTag(new AspectTag("food"));
            return null;
        };
        requests.add(new TagRequest(group, new AspectTag("food"), foodFloor,
                foodFloor + group.population / 100 + 1, foodPenalty, foodReward));

        if (group.getTerritory().getMinTemperature() < 0) {
            BiFunction<ShnyPair<Group, ResourcePack>, Double, Void> warmthPenalty = (pair, percent) -> {
                pair.first.freeze(percent);
                return null;
            };
            BiFunction<ShnyPair<Group, ResourcePack>, Double, Void> warmthReward = (pair, percent) -> {
                return null;
            };
            requests.add(new TagRequest(group, new AspectTag("warmth"), group.population,
                    group.population, warmthPenalty, warmthReward));
        }

        cultureAspects.forEach(cultureAspect -> addRequest(cultureAspect.getRequest()));
    }

    void update() {
        tryToFulfillAspirations();
        mutateAspects();
        createArtifact();
        useCultureAspects();
        addCultureAspect();
        mutateCultureAspects();
    }

    void intergroupUpdate() {
        adoptAspects();
        adoptCultureAspects();
    }

    private void useCultureAspects() {
        cultureAspects.forEach(CultureAspect::use);
    }

    private void addRequest(Request request) {
        if (request != null) {
            requests.add(request);
        }
    }

    private void addCultureAspect() {
        if (!testProbability(session.cultureAspectBaseProbability)) {
            return;
        }
        CultureAspect cultureAspect = null;
        switch (randomInt(5)){
            case 0: {
                List<ConverseWrapper> _l = new ArrayList<>(getMeaningAspects());
                if (!_l.isEmpty()) {
                    Meme meme = constructMeme();
                    if (meme != null) {
                        cultureAspect = new DepictObject(group, meme, randomElement(_l));
                    }
                }
                break;
            } case 1: {
                Meme meme = constructTale();
                if (meme != null) {
                    cultureAspect = new Tale(group, meme);
                }
                break;
            } case 2: {
                Resource resource = getAllProducedResources().stream().map(pair -> pair.first)
                        .filter(r -> !aestheticallyPleasingResources.contains(r))
                        .max(Comparator.comparingInt(Resource::getBaseDesireability)).orElse(null);
                if (resource != null) {
                    cultureAspect = new AestheticallyPleasingObject(group, resource);
                }
                break;
            } case 3: {//TODO recursively go in dependencies;
                addCultureAspect(constructRitualForReason(constructReason()));
                break;
            } case 4: {
                Meme template = session.templateBase.getRandomSentenceTemplate();
                TextInfo info = generateTextInfo();
                if (template != null && info != null) {
                    cultureAspect = new SyntacticTale(group, template, info);
                }
                break;
            } case 5: {//TODO should I add such a thing here even? (I did break in previous section for now)
                ConverseWrapper converseWrapper = randomElement(getConverseWrappers());
                Reason reason = Reasons.randomReason(group);
                if (converseWrapper != null && reason != null) {
                    cultureAspect = new AspectRitual(group, converseWrapper, reason);
                }
                break;
            }
        }
        addCultureAspect(cultureAspect);
    }

    private TextInfo generateTextInfo() {//TODO too slow
        return complicateInfo(randomElement(getConverseWrappers().stream()
                .flatMap(cw -> memePool.getAspectTextInfo(cw).stream()).collect(Collectors.toList())));
    }

    private TextInfo complicateInfo(TextInfo info) {
        if (info == null) {
            return null;
        }
        Map<String, Meme> substitutions = new HashMap<>();
        for (Map.Entry<String, Meme> entry: info.getMap().entrySet()) {
            if (entry.getKey().charAt(0) == '!') {
                substitutions.put(entry.getKey(), randomElement(session.templateBase.nounClauseBase).refactor(m -> {
                    if (m.getObserverWord().equals("!n!")) {
                        return entry.getValue().topCopy();
                    } else if (session.templateBase.templateChars.contains(m.getObserverWord().charAt(0))) {
                        substitutions.put(entry.getKey() + m.getObserverWord(),
                                randomElementWithProbability(session.templateBase.wordBase.get(m.getObserverWord()),
                                        n -> (double) memePool.getMeme(n.getObserverWord()).getImportance()));
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

    private Reason constructReason() {
        ConverseWrapper converseWrapper;
        Reason reason;
        int i = 0;
        do {
            converseWrapper = randomElementWithProbability(new ArrayList<>(getConverseWrappers()),
                    cw -> Math.max(cw.getUsefulness(), (double) 1));
            if (converseWrapper == null) {
                return null;
            }
            reason = new BetterAspectUseReason(group, converseWrapper);
            i++;
        } while (i <= constructProbes && !reasonsWithSystems.contains(reason));
        return reasonsWithSystems.contains(reason) ? null : reason;
    }

    public Ritual constructRitualForReason(Reason reason) {
        if (reason == null) {
            return null;
        }
        if (reason instanceof BetterAspectUseReason) {
            ConverseWrapper converseWrapper = ((BetterAspectUseReason) reason).getConverseWrapper();

            ShnyPair<List<Meme>, List<Meme>> temp = memePool.getAspectMemes(converseWrapper);
            List<Meme> aspectMemes = temp.first;
            aspectMemes.addAll(temp.second);
            Collections.shuffle(aspectMemes);
            for (Meme meme : aspectMemes) {//TODO maybe depth check
                if (meme.getObserverWord().equals(converseWrapper.getName())) {
                    continue;
                }
                Aspect myAspect = getAspect(meme.getObserverWord());
                if (myAspect instanceof ConverseWrapper) {
                    return new AspectRitual(group, (ConverseWrapper) myAspect, reason);
                }
                List<ConverseWrapper> options = getAllProducedResources().stream()
                        .filter(pair -> pair.first.getBaseName().equals(meme.getObserverWord()))
                        .map(pair -> pair.second).collect(Collectors.toList());
                if (!options.isEmpty()) {
                    return new AspectRitual(group, randomElement(options), reason);
                }
                switch (randomInt(2)) {
                    case 0:
                        List<ConverseWrapper> meaningAspects = new ArrayList<>(getMeaningAspects());
                        if (!meaningAspects.isEmpty()) {
                            return new CultureAspectRitual(group,
                                    new DepictObject(group, randomElement(aspectMemes), randomElement(meaningAspects)),
                                    reason);
                        }
                        break;
                    case 1:
                        return new CultureAspectRitual(group, new Tale(group, randomElement(aspectMemes)),
                                reason);//TODO make more complex tale;
                }
            }
        }
        return null;
    }

    private Meme constructTale() {
        return constructMeme();
    }

    private Meme constructMeme() {
        Meme meme = getMemePool().getMemeWithComplexityBias();
        if (testProbability(0.5)) {
            Meme second;
            do {
                second = getMemePool().getMemeWithComplexityBias().copy();
            } while (second.hasPart(meme, Collections.singleton("and")));
            meme = meme.copy().addPredicate(getMemePool().getMemeCopy("and").addPredicate(second));
            getMemePool().addMemeCombination(meme);
        }
        return meme;
    }

    private void removeAspiration(Aspiration aspiration) {
        aspirations.remove(aspiration);
    }

    private void mutateAspects() { //TODO separate adding of new aspects and updating old
        if (testProbability(session.rAspectAcquisition)) {
            List<Aspect> options = new ArrayList<>();
            if (session.independentCvSimpleAspectAdding) {
                if (testProbability(0.1)) {
                    options.addAll(session.world.aspectPool);
                } else {
                    options.addAll(getAllPossibleConverseWrappers());
                }
            } else {
                options.addAll(session.world.aspectPool);
                options.addAll(getAllPossibleConverseWrappers());
            }

            Aspect _a = randomElement(options, aspect -> true);
            if (_a != null) {
                if (_a instanceof ConverseWrapper && ((ConverseWrapper) _a).aspect.getName().equals("Paint")) {
                    int i = 0;
                }
                if (addAspect(_a)) {
                    group.addEvent(new Event(Event.Type.AspectGaining, "Group " + group.name +
                            " got aspect " + _a.getName() + " by itself", "group", group));
                }
            }
        }
    }

    private void mutateCultureAspects() {
        if (!testProbability(session.groupCultureAspectCollapse)) {
            return;
        }
        List<Ritual> rituals = cultureAspects.stream()
                .filter(ca -> ca instanceof Ritual)
                .map(ca -> (Ritual) ca).collect(Collectors.toList());
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
            cultureAspects.removeAll(popularReasonRituals);
            reasonsWithSystems.add(popularReasonRituals.get(0).getReason());
        }
    }

    private void adoptAspects() {
        if (!session.isTime(session.groupTurnsBetweenAdopts)) {
            return;
        }
        List<Aspect> allExistingAspects = getNeighboursAspects().stream()
                .filter(aspect -> !getChangedAspects().contains(aspect)).collect(Collectors.toList());

        Aspect aspect = randomElementWithProbability(allExistingAspects,
                a -> a.getUsefulness() * getNormalizedRelation(a.getGroup()));
        if (aspect != null) {
            if (addAspect(aspect)) {
                group.addEvent(new Event(Event.Type.AspectGaining,
                        String.format("Group %s got aspect %s from group %s",
                                group.name, aspect.getName(), aspect.getGroup().name),
                        "group", group));
            }
        }
    }

    private void adoptCultureAspects() {
        try {
            if (!session.isTime(session.groupTurnsBetweenAdopts)) {
                return;
            }
            List<CultureAspect> cultureAspects = getNeighboursCultureAspects().stream()
                    .filter(aspect -> !getCultureAspects().contains(aspect)).collect(Collectors.toList());
            CultureAspect aspect = randomElementWithProbability(cultureAspects,
                    a -> getNormalizedRelation(a.getGroup()));
            if (aspect != null) {
                addCultureAspect(aspect);
            }
        } catch (NullPointerException e) {
            throw new RuntimeException();
        }
    }

    private double getNormalizedRelation(Group group) {
        return relations.containsKey(group) ? relations.get(group).getPositiveNormalized() : 2;
    }

    private void createArtifact() {
        if (testProbability(0.1)) {
            if (memePool.isEmpty()){
                return;
            }
            List<ConverseWrapper> _l = new ArrayList<>(getMeaningAspects());
            ConverseWrapper _a = randomElement(_l);
            if (_a == null) {
                return;
            }
            //ConverseWrapper _b = _a.stripToMeaning();
            generateCurrentMeme();
            AspectResult result = _a.use(new AspectController(1, 1,
                    new ResourceEvaluator(rp -> rp, ResourcePack::getAmount), true));
            clearCurrentMeme();
            for (Resource resource : result.resources.resources) {
                if (resource.hasMeaning()) {
                    group.uniqueArtifacts.add(resource);
                } else {
                    group.resourcePack.add(resource);
                }
            }
        }
    }

    public void putCurrentMeme(String memeString) {
        currentMeme = memePool.getMeme(memeString);
    }

    public void generateCurrentMeme() {
        currentMeme = memePool.getValuableMeme();
    }

    public void clearCurrentMeme() {
        currentMeme = null;
    }

    public Meme getMeaning() {
        Meme meme = getCurrentMeme();
        return meme == null ? memePool.getValuableMeme() : meme;
    }

    private void tryToFulfillAspirations() {
        Optional _o = getAspirations().stream().max((Comparator.comparingInt(o -> o.level)));
        if (_o.isPresent()) {
            Aspiration aspiration = (Aspiration) _o.get();
            List<ShnyPair<Aspect, Group>> options = findOptions(aspiration);
            ShnyPair<Aspect, Group> pair = randomElement(options);
            if (pair == null) {
                return;
            }
            addAspect(pair.first);
            removeAspiration(aspiration);
            if (pair.second == null) {
                group.addEvent(new Event(Event.Type.AspectGaining, "Group " + group.name +
                        " developed aspect " + pair.first.getName(), "group", this));
            } else {
                group.addEvent(new Event(Event.Type.AspectGaining, "Group " + group.name +
                        " took aspect " + pair.first.getName() + " from group " + pair.second.name, "group", this));
            }
        }
    }

    private List<ShnyPair<Aspect, Group>> findOptions(Aspiration aspiration) {
        List<ShnyPair<Aspect, Group>> options = new ArrayList<>();

        for (Aspect aspect : session.world.getAllDefaultAspects().stream().filter(aspiration::isAcceptable).collect(Collectors.toList())) {
            Map<AspectTag, Set<Dependency>> _m = canAddAspect(aspect);
            if (aspect.isDependenciesOk(_m)) {
                options.add(new ShnyPair<>(aspect.copy(_m, group), null));
            }
        }

        getAllPossibleConverseWrappers().stream().filter(aspiration::isAcceptable)
                .forEach(wrapper -> options.add(new ShnyPair<>(wrapper, null)));

        List<Aspect> aspects = getNeighboursAspects();
        for (Aspect aspect : aspects) {
            Map<AspectTag, Set<Dependency>> _m = canAddAspect(aspect);
            if (aspiration.isAcceptable(aspect) && aspect.isDependenciesOk(_m)) {
                aspect = aspect.copy(_m, group);
                options.add(new ShnyPair<>(aspect, aspect.getGroup()));
            }
        }
        return options;
    }

    private List<ConverseWrapper> getAllPossibleConverseWrappers() {
        List<ConverseWrapper> options = new ArrayList<>(_converseWrappers); //TODO maybe do it after the middle part?
        Set<Resource> newResources = new HashSet<>(group.getOverallTerritory().getDifferentResources());
        newResources.addAll(getAllProducedResources().stream().map(pair -> pair.first).collect(Collectors.toSet()));
        newResources.removeAll(_lastResourcesForCw);
        for (Aspect aspect : aspects.stream().filter(aspect -> !(aspect instanceof ConverseWrapper))
                .collect(Collectors.toList())) {
            for (Resource resource : newResources) {
                addConverseWrapper(aspect, resource);
            }
        }
        _lastResourcesForCw.addAll(newResources);
        newResources.forEach(resource -> getMemePool().addResourceMemes(resource));
        return options;
    }

    private void addConverseWrapper(Aspect aspect, Resource resource) { //TODO I'm adding a lot of garbage
        ConverseWrapper _w;
        if (aspect.canApplyMeaning()) {
            _w = new MeaningInserter(aspect, resource,
                    group);
        } else {
            _w = new ConverseWrapper(aspect, resource,
                    group);
        }
        if (!_w.isValid()) {
            return;
        }
        _converseWrappers.add(_w);
    }

    void finishUpdate() {
        aspects.forEach(Aspect::finishUpdate);
        pushAspects();
    }

    public void pushAspects() {
        aspects = new HashSet<>();
        aspects.addAll(getChangedAspects());
    }

    public void initializeFromCenter(CulturalCenter culturalCenter) {
        culturalCenter.aspects.forEach(this::addAspect);
    }
}
