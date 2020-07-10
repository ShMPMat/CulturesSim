package visualizer;

import simulation.Controller;
import simulation.Event;
import simulation.World;
import simulation.culture.aspect.Aspect;
import simulation.culture.group.GroupTileTagKt;
import simulation.culture.group.centers.Group;
import simulation.culture.group.GroupConglomerate;
import simulation.culture.group.GroupTileTag;
import simulation.culture.group.cultureaspect.CultureAspect;
import simulation.culture.thinking.meaning.Meme;
import simulation.interactionmodel.InteractionModel;
import simulation.interactionmodel.MapModel;
import simulation.space.SpaceData;
import simulation.space.WorldMap;
import simulation.space.resource.Genome;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourceType;
import simulation.space.tile.Tile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static extra.OutputFunKt.*;
import static visualizer.PrintFunctionsKt.*;
import static visualizer.TextCommandsKt.getCommand;

/**
 * Main class, running and visualizing simulation.
 */
public class TextVisualizer implements Visualizer {
    /**
     * Symbols for representation of groups on the Map.
     */
    private Map<String, String> groupSymbols = new HashMap<>();
    /**
     * Symbols for representation of resource on the Map.
     */
    private Map<Resource, String> resourceSymbols = new HashMap<>();
    /**
     * List of group population before beginning of the last sequence turn;
     * Used for estimating population change.
     */
    private List<Integer> groupPopulations = new ArrayList<>();
    /**
     * Map of all the tiles claimed by groups during the last sequence of turns;
     * Used for displaying new tiles for groups.
     */
    private Map<String, Set<Tile>> lastClaimedTiles;
    private MapPrintInfo mapPrintInfo;
    /**
     * Main controller of the simulation
     */
    private Controller controller;

    private World world;
    private WorldMap map;
    private InteractionModel interactionModel;
    private Scanner s;
    private Turner currentTurner;
    private Thread turnerThread;

    /**
     * Base constructor.
     */
    public TextVisualizer(Controller controller) {
        this.controller = controller;
        Controller.visualizer = this;
        world = controller.world;
        map = world.getMap();
        interactionModel = controller.interactionModel;
        mapPrintInfo = new MapPrintInfo(map);
    }

    public static void main(String[] args) {
        TextVisualizer textVisualizer = new TextVisualizer(new Controller(new MapModel()));
        textVisualizer.initialize();
        textVisualizer.run();
    }

    private void initialize() {
        print();
        controller.initializeFirst();
        print();
        controller.initializeSecond();
        print();
        controller.initializeThird();
        for (int i = 0; i < controller.startGroupAmount; i++) {
            groupPopulations.add(0);
        }
        mapPrintInfo.computeCut();
    }

    /**
     * Fills Symbol fields for map drawing.
     *
     * @throws IOException when files with symbols isn't found.
     */
    private void readSymbols() throws IOException {
        s = new Scanner(new FileReader("SupplementFiles/Symbols/SymbolsResourceLibrary"));
        List<String> symbols = new ArrayList<>();
        while (s.hasNextLine()) {
            symbols.add(s.nextLine());
        }
        int i = 0;
        for (Resource resource : world.getResourcePool().getAll()) {
            resourceSymbols.put(resource, symbols.get(i % symbols.size()));
            i++;
        }
        s = new Scanner(new FileReader("SupplementFiles/Symbols/SymbolsLibrary"));
        for (GroupConglomerate group : world.getGroups()) {
            groupSymbols.put(group.getName(), s.nextLine());
        }
    }

    /**
     * Prints default map and information output.
     */
    public void print() {
        StringBuilder main = new StringBuilder();
        main.append(world.getStringTurn()).append("\n");
        lastClaimedTiles = new HashMap<>();
        main.append(printedGroups());
        for (GroupConglomerate group : world.getGroups()) {
            lastClaimedTiles.put(group.getName(), new HashSet<>());
        }
        for (Event event : interactionModel.getEvents().stream().
                filter(event -> event.getType() == Event.Type.TileAcquisition).collect(Collectors.toList())) {
            lastClaimedTiles.get(((Group) event.getAttribute("group")).getParentGroup().getName())
                    .add((Tile) event.getAttribute("tile"));
        }
        System.out.print(main.append(addToRight(
                printedMap(tile -> ""),
                addToRight(
                        chompToLines(printedResources().toString(), map.getLinedTiles().size() + 2),
                        printedEvents(interactionModel.getEvents(), false),
                        false
                ),
                true
                ))
        );
        interactionModel.clearEvents();
    }

    /**
     * Prints world map.
     *
     * @param condition function which adds an extra layer of information
     *                  above default map. If function returns non-empty string
     *                  for a tile, output of the function will be drawn above the tile.
     */
    private void printMap(Function<Tile, String> condition) {
        System.out.print(addToRight(
                printedMap(condition),
                chompToLines(printedResources(), map.getLinedTiles().size() + 2),
                true
        ));
    }

    private StringBuilder printedGroups() {
        StringBuilder main = new StringBuilder();
        int i = -1;
        while (groupPopulations.size() < world.getGroups().size()) {
            groupSymbols.put(world.getGroups().get(groupPopulations.size()).getName(), s.nextLine());
            groupPopulations.add(0);
        }
        for (GroupConglomerate group : world.getGroups()) {
            StringBuilder stringBuilder = new StringBuilder();
            i++;
            if (group.getState() == GroupConglomerate.State.Dead) {
                continue;
            }
            stringBuilder.append(groupSymbols.get(group.getName())).append(" ").append(group.getName()).append(" \033[31m");
            List<Aspect> aspects = new ArrayList<>(group.getAspects());
            aspects.sort(Comparator.comparingInt(Aspect::getUsefulness).reversed());
            for (Aspect aspect : aspects) {
                if (aspect.getUsefulness() <= 0) {
                    break;
                }
                stringBuilder.append("(").append(aspect.getName()).append(" ").append(aspect.getUsefulness())
                        .append(") ");
            }
            stringBuilder.append(" \033[32m\n");
            for (CultureAspect aspect : group.getCultureAspects()) {
                stringBuilder.append("(").append(aspect).append(") ");
            }
            stringBuilder.append(" \033[33m\n");
            List<Meme> memes = group.getMemes();
            memes.sort(Comparator.comparingInt(Meme::getImportance).reversed());
            for (int j = 0; j < 10 && memes.size() > j; j++) {
                Meme meme = memes.get(j);
                stringBuilder.append("(").append(meme).append(" ").append(meme.getImportance()).append(") ");
            }
            stringBuilder.append(" \033[39m\n");
            boolean hasGrown = group.getPopulation() > groupPopulations.get(i);
            stringBuilder.append(hasGrown ? "\033[32m" : "\033[31m").append("population=").append(group.getPopulation())
                    .append(hasGrown ? "↑" : "↓").append("\033[39m\n\n");
            groupPopulations.set(i, group.getPopulation());
            main.append(chompToSize(stringBuilder.toString(), 220));
        }
        return main;
    }

    /**
     * @param condition function which adds an extra layer of information
     *                  above default map. If function returns non-empty string
     *                  for a tile, output of the function will be drawn above the tile.
     */
    private StringBuilder printedMap(Function<Tile, String> condition) {
        StringBuilder main = new StringBuilder();
        WorldMap worldMap = controller.world.getMap();
        main.append("  ");
        for (int i = 0; i < worldMap.getLinedTiles().get(0).size(); i++) {
            main.append((i < 100 ? " " : i / 100 % 100));
        }
        main.append("\n").append("  ");
        for (int i = 0; i < worldMap.getLinedTiles().get(0).size(); i++) {
            main.append((i < 10 ? " " : i / 10 % 10));
        }
        main.append("\n").append("  ");
        for (int i = 0; i < worldMap.getLinedTiles().get(0).size(); i++) {
            main.append(i % 10);
        }
        main.append("\n");
        StringBuilder map = new StringBuilder();
        for (int i = 0; i < SpaceData.INSTANCE.getData().getMapSizeX(); i++) {
            String token;
            map.append((i < 10 ? " " + i : i));
            for (int j = 0; j < SpaceData.INSTANCE.getData().getMapSizeY(); j++) {
                Tile tile = worldMap.get(i, j + mapPrintInfo.getCut());
                token = condition.apply(tile);
                if (token.equals("")) {
                    switch (tile.getType()) {
                        case Ice:
                            token = "\033[47m";
                            break;
                        case Water:
                            token = "\033[44m";
                            break;
                        case Woods:
                            token = "\033[42m";
                            break;
                        case Growth:
                            token = "\033[103m";
                            break;
                    }
                    if (tile.getTagPool().getByType(GroupTileTagKt.GROUP_TAG_TYPE).isEmpty()) {
                        switch (tile.getType()) {
                            case Water:
                            case Ice:
                            case Woods:
                            case Growth:
                            case Normal:
                                List<Resource> actual = tile.getResourcePack().getResources(r ->
                                        r.getGenome().getType() != ResourceType.Plant && r.isNotEmpty() &&
                                                !r.getSimpleName().equals("Vapour")).getResources();
                                if (/*actual.size() > 0*/ false) {
                                    token += "\033[30m" + (resourceSymbols.get(actual.get(0)) == null ? "Ё" :
                                            resourceSymbols.get(actual.get(0)));
                                } else {
                                    token += " ";
                                }
                                break;
                            case Mountain:
                                token = (tile.getLevel() > 130 ? "\033[43m" : "") +
                                        (tile.getResourcePack().contains(world.getResourcePool().get("Snow")) ? "\033[30m" : "\033[93m") + "^";
                                break;
                            default:
                                token += " ";
                        }
                    } else {
                        Group group = ((GroupTileTag) tile.getTagPool().getByType("Group").get(0)).getGroup();
                        token += (lastClaimedTiles.get(group.getParentGroup().getName()).contains(tile)
                                ? "\033[31m" :
                                "\033[96m\033[1m") + groupSymbols.get(group.getParentGroup().getName());
                    }
                }
                map.append(token).append("\033[0m");
            }
            map.append("\033[0m").append("\n");
        }
        return main.append(map);
    }

    private StringBuilder printedResources() {
        StringBuilder resources = new StringBuilder();
        for (Resource resource : world.getResourcePool().getAll()) {
            resources.append("\033[31m").append(resourceSymbols.get(resource)).append(" - ")
                    .append(resource.getBaseName()).append("\n");
        }
        return resources;

    }

    private void printGroupConglomerate(GroupConglomerate groupConglomerate) {
        printMap(t -> TileMapperFunctionsKt.groupConglomerateMapper(groupConglomerate, t));
        System.out.println(groupConglomerate);
    }

    private void printGroup(Group group) {
        printMap(t -> TileMapperFunctionsKt.groupMapper(group, t));
        PrintFunctionsKt.printGroup(group);
    }

    private void printResource(Resource resource) {
        printMap(tile -> (tile.getResourcePack().any(r -> r.getSimpleName().equals(resource.getSimpleName())
                && r.isNotEmpty()) ? "\033[30m\033[41m" + tile.getResourcePack().getAmount(resource) % 10 : ""));
        System.out.println(PrintFunctionsKt.printResource(resource));
    }

    private void printTile(Tile tile) {
        printMap(t -> (t.equals(tile) ? "\033[31m\033[41mX" : ""));
        System.out.println(tile);
    }

    private StringBuilder printedEvents(Collection<Event> events, boolean printAll) {
        StringBuilder main = new StringBuilder();
        for (Event event : events) {
            if (printAll || event.getType() == Event.Type.Death
                    || event.getType() == Event.Type.ResourceDeath
                    || event.getType() == Event.Type.DisbandResources) {
                main.append(event).append("\n");
            }
        }
        return main;
    }

    private GroupConglomerate getConglomerate(String string) {
        int index = Integer.parseInt(string.substring(1));
        return index < world.getGroups().size()
                ? world.getGroups().get(index)
                : null;
    }

    /**
     * Runs interface for the simulation control.
     */
    private void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            readSymbols();
            print();
            boolean isFirstTurn = true;
            while (true) {
                String line = isFirstTurn ? "10000" : br.readLine();
                isFirstTurn = false;
                if (line != null) {
                    final String[] splitCommand = line.split(" ");
                    if (currentTurner != null) {
                        currentTurner.isAskedToStop.set(true);
                        System.out.println("Turner is asked to stop");
                        turnerThread.join();
                        currentTurner = null;
                        System.out.println("Turner has stopped");
                        print();
                        continue;
                    }
                    switch (getCommand(line)) {
                        case Conglomerate: {
                            Optional<GroupConglomerate> conglomerate = world.getGroups().stream()
                                    .filter(g -> g.getName().equals(line)).findFirst();
                            Optional<Group> group = world.getGroups().stream()
                                    .flatMap(c -> c.subgroups.stream())
                                    .filter(g -> g.getName().equals(line)).findFirst();
                            if (conglomerate.isPresent()) {
                                printGroupConglomerate(conglomerate.get());
                            } else if (group.isPresent()) {
                                printGroup(group.get());
                            } else {
                                System.out.println("No such Group exist");
                            }
                            break;
                        } case GroupTileReach: {
                            GroupConglomerate group = getConglomerate(splitCommand[0]);
                            if (group == null) {
                                break;
                            }
                            printMap(t -> TileMapperFunctionsKt.groupReachMapper(group.subgroups.get(0), t));
                            break;
                        } case GroupProduced: {
                            GroupConglomerate group = getConglomerate(splitCommand[0]);
                            if (group == null) {
                                break;
                            }
                            System.out.println(chompToSize(PrintFunctionsKt.printProduced(group), 150));
                            break;
                        } case GroupRelations: {
                            GroupConglomerate c1 = getConglomerate(splitCommand[0]);
                            GroupConglomerate c2 = getConglomerate(splitCommand[1]);
                            if (c1 == null || c2 == null) {
                                System.out.println("No such Conglomerates exist");
                                break;
                            }
                            System.out.println(printConglomerateRelations(c1, c2));
                            break;
                        } case Tile:
                            printTile(
                                    map.get(Integer.parseInt(line.substring(0, line.indexOf(' '))),
                                            Integer.parseInt(line.substring(line.indexOf(' ') + 1)) + mapPrintInfo.getCut())
                            );
                            break;
                        case Plates:
                            printMap(t -> TileMapperFunctionsKt.platesMapper(map.getTectonicPlates(), t));
                            break;
                        case Temperature:
                            printMap(TileMapperFunctionsKt::temperatureMapper);
                            break;
                        case GroupPotentials: {
                            GroupConglomerate group = getConglomerate(splitCommand[0]);
                            if (group == null) {
                                break;
                            }
                            printMap(t -> TileMapperFunctionsKt.hotnessMapper(
                                    group.subgroups.get(0).getTerritoryCenter()::tilePotentialMapper,
                                    Integer.parseInt(splitCommand[2]),
                                    t,
                                    Integer.parseInt(splitCommand[2])
                            ));
                            break;
                        } case Wind:
                            printMap(TileMapperFunctionsKt::windMapper);
                            break;
                        case TerrainLevel:
                            printMap(TileMapperFunctionsKt::levelMapper);
                            break;
                        case Vapour:
                            printMap(TileMapperFunctionsKt::vapourMapper);
                            break;
                        case MeaningfulResources:
                            printMap(TileMapperFunctionsKt::meaningfulResourcesMapper);
                            break;
                        case ArtificialResources:
                            printMap(TileMapperFunctionsKt::artificialResourcesMapper);
                            break;
                        case TileTag:
                            printMap(t -> TileMapperFunctionsKt.tileTagMapper(splitCommand[1], t));
                            break;
                        case Resource:
                            try {
                                Resource resource = world.getResourcePool().get(line.substring(2));
                                printResource(resource);
                            } catch (NoSuchElementException e) {
                                Optional<Resource> _oo = resourceSymbols.entrySet().stream()
                                        .filter(entry -> entry.getValue().equals(line.substring(2)))
                                        .map(Map.Entry::getKey).findFirst();
                                _oo.ifPresent(this::printResource);
                            }
                            break;
                        case ResourceSubstring:
                            printMap(t ->
                                    t.getResourcesWithMoved().stream()
                                            .anyMatch(r -> r.getFullName().contains(splitCommand[1]))
                                        ? "X" : ""
                            );
                            System.out.println(printResourcesWithSubstring(map, splitCommand[1]));
                            break;
                        case ResourceType:
                            if (Arrays.stream(ResourceType.values()).anyMatch(t -> t.toString().equals(splitCommand[1]))) {
                                ResourceType type = ResourceType.valueOf(splitCommand[1]);
                                printMap(t -> TileMapperFunctionsKt.resourceTypeMapper(type, t));
                            } else {
                                System.out.println("Unknown type - " + splitCommand[1]);
                            }
                            break;
                        case ResourceOwner:
                            printMap(t -> TileMapperFunctionsKt.resourceOwnerMapper(splitCommand[1], t));
                            break;
                        case AllResources: {
                            System.out.println(PrintFunctionsKt.resourcesCounter(world));
                            break;
                        } case Events: {
                            System.out.println(printRegexEvents(
                                    controller.interactionModel.getAllEvents(),
                                    100,
                                    splitCommand[1]
                                    ));
                            break;
                        } case Aspects: {
                            printMap(t -> TileMapperFunctionsKt.aspectMapper(splitCommand[1], t));
                            Aspect aspect = world.getAspectPool().get(splitCommand[1]);
                            if (aspect != null) {
                                System.out.println(printApplicableResources(
                                        aspect,
                                        world.getResourcePool().getAll()
                                ));
                            }
                            break;
                        } case Map:
                            printMap(tile -> "");
                            break;
                        case Exit:
                            return;
                        case AddAspect: {
                            AddFunctionsKt.addGroupConglomerateAspect(
                                    getConglomerate(splitCommand[0]),
                                    splitCommand[1],
                                    world.getAspectPool()
                            );
                            break;
                        }
                        case AddWant: {
                            AddFunctionsKt.addGroupConglomerateWant(
                                    getConglomerate(splitCommand[1]),
                                    splitCommand[2],
                                    world.getResourcePool()
                            );
                            break;
                        }
                        case AddResource:
                            AddFunctionsKt.addResourceOnTile(
                                    map.get(Integer.parseInt(splitCommand[0]), Integer.parseInt(splitCommand[1])),
                                    splitCommand[2],
                                    world.getResourcePool()
                            );
                            break;
                        case GeologicalTurn:
                            controller.geologicTurn();
                            print();
                            break;
                        case Turner:
                            try {
                                currentTurner = new Turner(Integer.parseInt(line), controller);
                                turnerThread = new Thread(currentTurner);
                                turnerThread.start();
                            } catch (NumberFormatException e) {
                                System.out.println("Wrong number format for amount of turns");
                        }
                            break;
                        default:
                            controller.turn();
                            print();
                    }
                } else {
                    break;
                }
            }
        } catch (Throwable t) {
            System.err.println(t.toString());
            for (StackTraceElement stackTraceElement : t.getStackTrace()) {
                System.err.println(stackTraceElement);
            }
        }
    }
}
