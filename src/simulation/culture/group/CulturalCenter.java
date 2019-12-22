package simulation.culture.group;

import extra.ShnyPair;
import simulation.culture.Event;
import simulation.culture.aspect.*;
import simulation.culture.aspect.dependency.Dependency;
import simulation.culture.group.cultureaspect.AestheticallyPleasingObject;
import simulation.culture.group.cultureaspect.CultureAspect;
import simulation.culture.group.cultureaspect.DepictObject;
import simulation.culture.group.cultureaspect.Tale;
import simulation.culture.group.request.Request;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.culture.group.request.TagRequest;
import simulation.culture.thinking.meaning.GroupMemes;
import simulation.culture.thinking.meaning.Meme;
import simulation.culture.thinking.meaning.MemeSubject;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourcePack;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static extra.ProbFunc.*;
import static simulation.Controller.*;

/**
 * Takes responsibility of Group's cultural change.
 */
public class CulturalCenter {//TODO sometimes for whatever reason Overgroup has Aspects, which Subgroup lack
    private List<Aspiration> aspirations = new ArrayList<>();
    private Group group;
    private Set<Aspect> aspects = new HashSet<>();
    private Set<Aspect> changedAspects = new HashSet<>(); // always equals aspects except while making a turn
    private List<Event> events = new ArrayList<>();
    private List<Request> requests = new ArrayList<>();
    private List<CultureAspect> cultureAspects = new ArrayList<>();
    private GroupMemes memePool = new GroupMemes();

    private List<ConverseWrapper> _converseWrappers = new ArrayList<>();
    private List<Resource> _lastResourcesForCw = new ArrayList<>();

    private Meme currentMeme;

    CulturalCenter(Group group) {
        this.group = group;
        if (group.type == 'O') {
            aspirations = null;
            aspects = null;
            changedAspects = null;
            //
            // events = null;
            requests = null;
            cultureAspects = null;
            memePool = null;
            _converseWrappers = null;
            _lastResourcesForCw = null;
        }
    }

    List<Aspiration> getAspirations() {
        return aspirations;
    }

    Set<Aspect> getAspects() {
        return aspects;
    }

    Set<ConverseWrapper> getMeaningAspects() {
        return aspects.stream()
                .filter(aspect -> aspect instanceof ConverseWrapper && aspect.canReturnMeaning())
                .map(aspect -> (ConverseWrapper) aspect).collect(Collectors.toSet());
    }

    Aspect getAspect(Aspect aspect) {
        return getAspects().stream().filter(aspect::equals).findFirst().orElse(null);
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

    public List<CultureAspect> getCultureAspects() {
        return group.subgroups.isEmpty() ? cultureAspects : group.subgroups.get(0).getCulturalCenter().getCultureAspects();
    }

    private Set<ShnyPair<Aspect, Group>> getNeighboursAspects() {
        Set<ShnyPair<Aspect, Group>> allExistingAspects = new HashSet<>();
        for (Group neighbour : session.world.map.getAllNearGroups(group)) {
            for (Aspect aspect : neighbour.overgroupGetAspects().stream().filter(aspect ->
                    !(aspect instanceof ConverseWrapper) || getAspect(((ConverseWrapper) aspect).aspect) != null)
                    .collect(Collectors.toList())) {
                allExistingAspects.add(new ShnyPair<>(aspect, neighbour));
            }
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
        return group.subgroups.isEmpty() ? memePool : group.subgroups.get(0).getCulturalCenter().getMemePool();
    }

    public Meme getCurrentMeme() {
        return currentMeme;
    }

    void addMemeCombination(Meme meme) {
        memePool.addMemeCombination(meme);
    }

    void addAspiration(Aspiration aspiration) { //TODO seems that aspirations are stuck in groups;
        if (aspirations.stream().noneMatch(aspir -> aspir.equals(aspiration))) {
            aspirations.add(aspiration);
        }
    }

    public boolean addAspect(Aspect aspect) {
        if (aspects.contains(aspect)) {
            aspect = aspects.stream().filter(aspect::equals).findFirst().orElse(aspect);
        }
        Map<AspectTag, Set<Dependency>> _m = group.canAddAspect(aspect);
        if (!aspect.isDependenciesOk(_m)) {
            return false;
        }

        addAspectNow(aspect, _m);
        Aspect finalAspect = aspect;
        group.subgroups.forEach(group -> group.getCulturalCenter().addAspectNow(finalAspect, _m));
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
            if (!(_a instanceof ConverseWrapper)) {
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
        Aspect final_a = _a;
        if (group.getStrata().stream().noneMatch(stratum -> stratum.containsAspect(final_a))) {
            group.getStrata().add(new Stratum(0, _a, group));
        }
    }

    public void addCultureAspect(CultureAspect cultureAspect) {
        if (cultureAspect != null && !cultureAspects.contains(cultureAspect)) {
            cultureAspects.add(cultureAspect);
        }
    }

    public void addResourceWant(Resource resource) {
        addCultureAspect(new AestheticallyPleasingObject(group, resource, ResourceBehaviour.getRandom(group)));
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
//        for (ShnyPair<Resource, ResourceBehaviour> want : wants) {
//            requests.add(new ResourceRequest(group, want.first, 1, 10, (pair, percent) -> {
//                pair.first.cherishedResources.add(pair.second);
//                addAspiration(new Aspiration(5, want.first));
//                want.second.procedeResources(pair.second);
//                return null;
//            },
//                    (pair, percent) -> {
//                pair.first.cherishedResources.add(pair.second);
//                want.second.procedeResources(pair.second);
//                return null;
//                    }));
//        }
    }

    void overgroupUpdate() {//TODO move aspects to subgroups and share them between subgroups

    }

    void update() {
        tryToFulfillAspirations();
        mutateAspects();
        createArtifact();
        useCultureAspects();
        addCultureAspect();
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
        if (!getChances(session.cultureAspectBaseProbability)) {
            return;
        }
        CultureAspect cultureAspect = null;
        switch (randomInt(3)){
            case 0:
                List<ConverseWrapper> _l = new ArrayList<>(getMeaningAspects());
                if (!_l.isEmpty()) {
                    cultureAspect = new DepictObject(group, memePool.getValuableMeme().toString(),
                            randomElement(_l), ResourceBehaviour.getRandom(group));
                    break;
                }
            case 1:
                Meme meme = getMemePool().getValuableMeme();
                if (meme != null) {
                    cultureAspect = new Tale(group, meme);
                    break;
                }
            case 2:
                Resource resource = randomElement(getAllProducedResources().stream().map(pair -> pair.first)
                        .collect(Collectors.toList()));
                if (resource != null) {
                    cultureAspect = new AestheticallyPleasingObject(group, resource, ResourceBehaviour.getRandom(group));
                    break;
                }
        }
        addCultureAspect(cultureAspect);
    }

    private void removeAspiration(Aspiration aspiration) {
        aspirations.remove(aspiration);
    }

    private void mutateAspects() { //TODO separate adding of new aspects und updating old

//        Set<ShnyPair<Aspect, Group>> allExistingAspects = getNeighboursAspects();
//
//        ShnyPair<Aspect, Group> _p = ProbFunc.addRandomAspectWithPairExcept(getChangedAspects(),
//                allExistingAspects, pair -> !getChangedAspects().contains(pair.first), rAspectLending);
//        if (_p != null) {
//            if (addAspect(_p.first)) {
//                group.addEvent(new Event(Event.Type.AspectGaining, world.getTurn(), "Group " + group.name +
//                        " got aspect " + _p.first.getBaseName() + " from group " + _p.second.name, "group", group));
//            }
//        }
        if (getChances(session.rAspectAcquisition)) {
            List<Aspect> options = new ArrayList<>(session.world.aspectPool);
            options.addAll(getAllConverseWrappers());
            Aspect _a = randomElement(options, aspect -> true);
            if (_a != null) {
                if (addAspect(_a)) {
                    group.addEvent(new Event(Event.Type.AspectGaining, "Group " + group.name +
                            " got aspect " + _a.getName() + " by itself", "group", group));
                }
            }
        }
    }

    private void createArtifact() {
        if (getChances(0.1)) {
            if (memePool.isEmpty()){
                return;
            }
            List<ConverseWrapper> _l = new ArrayList<>(getMeaningAspects());
            ConverseWrapper _a = randomElement(_l);
            if (_a == null) {
                return;
            }
            ConverseWrapper _b = _a.stripToMeaning();
            generateCurrentMeme();
            ShnyPair<Boolean, ResourcePack> _p = _b.use(1, new ResourceEvaluator(rp -> rp, ResourcePack::getAmount));
            clearCurrentMeme();
            for (Resource resource : _p.second.resources) {
                if (resource.hasMeaning()) {
                    group.uniqueArtifacts.add(resource);
                } else {
                    group.resourcePack.add(resource);
                }
            }
        }
    }

    public void putCurrentMeme(String memeString) {
        currentMeme = memePool.getMemeByName(memeString);
    }

    public void generateCurrentMeme() {
        currentMeme = memePool.getValuableMeme();
    }

    public void clearCurrentMeme() {
        currentMeme = null;
    }

    public Meme getMeaning() {
        return getCurrentMeme();
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

    private List<ShnyPair<Aspect, Group>> findOptions(Aspiration aspiration) {//TODO add potentially good options (incinerate for warmth etc.) (should I though?)
        List<ShnyPair<Aspect, Group>> options = new ArrayList<>();

        for (Aspect aspect : session.world.getAllDefaultAspects().stream().filter(aspiration::isAcceptable).collect(Collectors.toList())) {
            Map<AspectTag, Set<Dependency>> _m = group.canAddAspect(aspect);
            if (aspect.isDependenciesOk(_m)) {
                options.add(new ShnyPair<>(aspect.copy(_m, group), null));
            }
        }

        getAllConverseWrappers().stream().filter(aspiration::isAcceptable)
                .forEach(wrapper -> options.add(new ShnyPair<>(wrapper, null)));

        Set<ShnyPair<Aspect, Group>> aspects = getNeighboursAspects();
        for (ShnyPair<Aspect, Group> pair : aspects) {
            Map<AspectTag, Set<Dependency>> _m = group.canAddAspect(pair.first);
            if (aspiration.isAcceptable(pair.first) && pair.first.isDependenciesOk(_m)) {
                pair.first = pair.first.copy(_m, group);
                options.add(pair);
            }
        }
        return options;
    }

    private List<ConverseWrapper> getAllConverseWrappers() {
        List<ConverseWrapper> options = new ArrayList<>(_converseWrappers);
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
        newResources.forEach(resource -> getMemePool().add(new MemeSubject(resource.getBaseName())));
        return options;
    }

    private void addConverseWrapper(Aspect aspect, Resource resource) {
        ConverseWrapper _w;
        if (aspect.canApplyMeaning()) {
            _w = new MeaningInserter(aspect, resource,
                    group);
        } else {
            _w = new ConverseWrapper(aspect, resource,
                    group);
        }
        Map<AspectTag, Set<Dependency>> _m = group.canAddAspect(_w);
        if (_w.isDependenciesOk(_m)) {
            _converseWrappers.add(_w);
        }
    }

    void finishUpdate() {
        pushAspects();
    }

    public void pushAspects() {
        if (group.type == 'O') {
            int i = 0;
        }
        aspects = new HashSet<>();
        aspects.addAll(getChangedAspects());
    }
}
