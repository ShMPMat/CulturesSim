package visualizer;
//TODO check smart territory acquisition it seems not to work

import simulation.Controller;
import simulation.World;
import simulation.Event;
import simulation.culture.aspect.Aspect;
import simulation.culture.group.Group;
import simulation.culture.group.GroupConglomerate;
import simulation.culture.group.GroupTileTag;
import simulation.culture.group.cultureaspect.CultureAspect;
import simulation.interactionmodel.InteractionModel;
import simulation.interactionmodel.MapModel;
import simulation.culture.thinking.meaning.Meme;
import simulation.space.SpaceData;
import simulation.space.tile.Tile;
import simulation.space.WorldMap;
import simulation.space.resource.Genome;
import simulation.space.resource.Resource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static extra.OutputFunc.*;

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
    private int mapCut;
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
        map = world.map;
        interactionModel = controller.interactionModel;
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
        computeCut();
    }

    private void computeCut() {
        int gapStart = 0, gapFinish = 0, start = -1, finish = 0;
        for (int y = 0; y < SpaceData.INSTANCE.getData().getMapSizeY(); y++) {
            boolean isLineClear = true;
            for (int x = 0; x < SpaceData.INSTANCE.getData().getMapSizeX(); x++) {
                Tile tile = map.get(x, y);
                if (tile.getType() != Tile.Type.Water && tile.getType() != Tile.Type.Ice) {
                    isLineClear = false;
                    break;
                }
            }
            if (isLineClear) {
                if (start == -1) {
                    start = y;
                }
                finish = y;
            } else if (start != -1) {
                if (finish - start > gapFinish - gapStart) {
                    gapStart = start;
                    gapFinish = finish;
                }
                start = -1;
            }
        }
        mapCut = (gapStart + gapFinish) / 2;
    }

    /**
     * Function returning a command represented in the line.
     *
     * @param line String line with a command.
     * @return Command token represented by the line.
     */
    private Command getCommand(String line) {
        for (Command command : Command.values()) {
            if (command.pattern.matcher(line).matches()) {
                return command;
            }
        }
        return Command.Turn;
    }

    /**
     * Fills Symbol fields for map drawing.
     *
     * @throws IOException when files with symbols isn't found.
     */
    private void readSymbols() throws IOException {
        s = new Scanner(new FileReader("SupplementFiles/Symbols/SymbolsResourceLibrary"));
        for (Resource resource : world.getResourcePool().getAll()) {
            resourceSymbols.put(resource, s.nextLine());
        }
        s = new Scanner(new FileReader("SupplementFiles/Symbols/SymbolsLibrary"));
        for (GroupConglomerate group : world.groups) {
            groupSymbols.put(group.name, s.nextLine());
        }
    }

    /**
     * Prints default map and information output.
     */
    public void print() {
        StringBuilder main = new StringBuilder();
        main.append(world.getTurn()).append("\n");
        lastClaimedTiles = new HashMap<>();
        main.append(printedGroups());
        for (GroupConglomerate group : controller.world.groups) {
            lastClaimedTiles.put(group.name, new HashSet<>());
        }
        for (Event event : interactionModel.getEvents().stream().
                filter(event -> event.type == Event.Type.TileAcquisition).collect(Collectors.toList())) {
            lastClaimedTiles.get(((Group) event.getAttribute("group")).name).add((Tile) event.getAttribute("tile"));
        }
        System.out.print(main.append(addToRight(printedMap(tile -> ""), addToRight(chompToLines(printedResources().toString(),
                map.getTiles().size() + 2), printedEvents(interactionModel.getEvents(), false),
                false), true)));
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
                chompToLines(printedResources(), map.getTiles().size() + 2),
                true
        ));
    }

    private StringBuilder printedGroups() {
        StringBuilder main = new StringBuilder();
        int i = -1;
        while (groupPopulations.size() < world.groups.size()) {
            groupSymbols.put(world.groups.get(groupPopulations.size()).name, s.nextLine());
            groupPopulations.add(0);
        }
        for (GroupConglomerate group : world.groups) {
            StringBuilder stringBuilder = new StringBuilder();
            i++;
            if (group.state == GroupConglomerate.State.Dead) {
                continue;
            }
            stringBuilder.append(groupSymbols.get(group.name)).append(" ").append(group.name).append(" \033[31m");
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
            stringBuilder.append("population=").append(group.population)
                    .append(group.population <= groupPopulations.get(i) ? "↓" : "↑").append("\n\n");
            groupPopulations.set(i, group.population);
            main.append(chompToSize(stringBuilder.toString(), 220));
        }
        return main;
    }

    /**
     * @param condition function which adds an extra layer of information
     *                  above default map. If function returns non-empty string
     *                  for a tile, output of the function will be drawn above the tile.
     * @return StringBuilder with a printed map.
     */
    private StringBuilder printedMap(Function<Tile, String> condition) {
        StringBuilder main = new StringBuilder();
        WorldMap worldMap = controller.world.map;
        main.append("  ");
        for (int i = 0; i < worldMap.getTiles().get(0).size(); i++) {
            main.append((i < 100 ? " " : i / 100 % 100));
        }
        main.append("\n").append("  ");
        for (int i = 0; i < worldMap.getTiles().get(0).size(); i++) {
            main.append((i < 10 ? " " : i / 10 % 10));
        }
        main.append("\n").append("  ");
        for (int i = 0; i < worldMap.getTiles().get(0).size(); i++) {
            main.append(i % 10);
        }
        main.append("\n");
        StringBuilder map = new StringBuilder();
        for (int i = 0; i < SpaceData.INSTANCE.getData().getMapSizeX(); i++) {
            String token;
            map.append((i < 10 ? " " + i : i));
            for (int j = 0; j < SpaceData.INSTANCE.getData().getMapSizeY(); j++) {
                Tile tile = worldMap.get(i, j + mapCut);
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
                    if (tile.getTagPool().getByType("Group").isEmpty()) {//TODO remove hardcoded "Group"
                        switch (tile.getType()) {
                            case Water:
                            case Ice:
                            case Woods:
                            case Growth:
                            case Normal:
                                List<Resource> actual = tile.getResourcePack().getResources(r ->
                                        r.getGenome().getType() != Genome.Type.Plant && r.getAmount() > 0 &&
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
                        if (group.state == Group.State.Dead) {
                            token += "\033[33m☠";
                        } else {
                            token += (lastClaimedTiles.get(group.getParentGroup().name).contains(tile) ? "\033[31m" :
                                    "\033[96m\033[1m") + groupSymbols.get(group.getParentGroup().name);
                        }
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

    private void printGroup(GroupConglomerate group) {
        printMap(tile -> (group.getTerritory().contains(tile) ?
                (group.subgroups.stream().anyMatch(sg -> sg.getTerritoryCenter().getTerritory().contains(tile)) ? "\033[31mO" :
                        (tile.getResourcePack().getResources().stream().anyMatch(resource -> resource.getBaseName().contains("House")) ?
                                "\033[31m+" : "\033[31mX")) : ""));
        System.out.println(group);
    }

    private void printResource(Resource resource) {
        printMap(tile -> (tile.getResourcePack().getResources().stream().anyMatch(r -> r.getSimpleName().equals(resource.getSimpleName())
                && r.getAmount() > 0) ? "\033[30m\033[41m" + tile.getResourcePack().getAmount(resource) % 10 : ""));
        System.out.println(resource);
    }

    private void printTile(Tile tile) {
        printMap(t -> (t.equals(tile) ? "\033[31m\033[41mX" : ""));
        System.out.println(tile);
    }

    private StringBuilder printedEvents(Collection<Event> events, boolean printAll) {
        StringBuilder main = new StringBuilder();
        for (Event event : events) {
            if (printAll || event.type == Event.Type.Death || event.type == Event.Type.ResourceDeath || event.type == Event.Type.DisbandResources) {
                main.append(event).append("\n");
            }
        }
        return main;
    }

    private GroupConglomerate getConglomerate(String string) {
        int index = Integer.parseInt(string.substring(1));
        return index < world.groups.size()
                ? world.groups.get(index)
                : null;
    }

    /**
     * Runs interface for the simulation control.
     */
    private void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            readSymbols();
            print();
            while (true) {
                String[] _s;
                String line = br.readLine();
                if (line != null) {
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
                        case Group:
                            printGroup(world.groups.get(Integer.parseInt(line.substring(1))));
                            break;
                        case Tile:
                            printTile(
                                    map.get(Integer.parseInt(line.substring(0, line.indexOf(' '))),
                                    Integer.parseInt(line.substring(line.indexOf(' ') + 1)) + mapCut)
                            );
                            break;
                        case Plates:
                            printMap(t -> TileMapperFunctionsKt.platesMapper(map.getTectonicPlates(), t));
                            break;
                        case Temperature:
                            printMap(TileMapperFunctionsKt::temperatureMapper);
                            break;
                        case GroupPotentials:
                            _s = line.split(" ");
                            GroupConglomerate group = getConglomerate(_s[0]);
                            if (group == null) {
                                break;
                            }
                            printMap(t -> TileMapperFunctionsKt.hotnessMapper(
                                    group.subgroups.get(0).getTerritoryCenter()::tilePotentialMapper,
                                    Integer.parseInt(_s[2]),
                                    t,
                                    Integer.parseInt(_s[2])
                            ));
                            break;
                        case Wind:
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
                        case IdleGo:
                            for (int i = 0; i < 500; i++) {
                                controller.turn();
                                if (interactionModel.getEvents().stream()
                                        .anyMatch(event -> event.type == Event.Type.Death)) {
                                    break;
                                }
                            }
                            print();
                            break;
                        case Map:
                            printMap(tile -> "");
                            break;
                        case Exit:
                            return;
                        case AddAspect: {
                            _s = line.split(" ");
                            AddFunctionsKt.addGroupConglomerateAspect(
                                    getConglomerate(_s[1]),
                                    _s[1],
                                    world.getAspectPool()
                            );
                            break;
                        } case AddWant: {
                            _s = line.split(" ");
                            AddFunctionsKt.addGroupConglomerateWant(
                                    getConglomerate(_s[1]),
                                    _s[2],
                                    world.getResourcePool()
                            );
                            break;
                        } case AddResource:
                            _s = line.split(" ");
                            AddFunctionsKt.addResourceOnTile(
                                    map.get(Integer.parseInt(_s[0]), Integer.parseInt(_s[1])),
                                    _s[2],
                                    world.getResourcePool()
                            );
                            break;
                        case GeologicalTurn:
                            controller.geologicTurn();
                            print();
                            break;
                        case Turner:
                            currentTurner = new Turner(Integer.parseInt(line), controller);
                            turnerThread = new Thread(currentTurner);
                            turnerThread.start();
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

    /**
     * Represents commands which can be given to visualizer.
     */
    private enum Command {
        /**
         * Command for making turns until something important happens.
         */
        IdleGo("go"),//TODO does it work still even?
        /**
         * Command for printing group information.
         */
        Group("^G\\d+"),
        /**
         * Command for printing tile information.
         */
        Tile("\\d+ \\d+"),
        Plates("plates"),
        Temperature("temperature"),
        Wind("wind"),
        TerrainLevel("level"),
        GroupPotentials("^G\\d+ p \\d+"),
        Vapour("vapour"),
        /**
         * Command for printing resource information.
         */
        Resource("r \\w+"),
        MeaningfulResources("meaning"),
        ArtificialResources("artificial"),
        /**
         * Command for printing map.
         */
        Map("[mM]"),
        /**
         * Command for exiting simulation.
         */
        Exit("EXIT"),
        /**
         * Command for adding Aspect for a group.
         */
        AddAspect("^G\\d+ \\w+"),
        AddWant("^want G\\d+ \\w+"),
        AddResource("\\d+ \\d+ \\w+"),
        GeologicalTurn("Geo"),
        Turn(""),
        Turner("\\d+");

        Pattern pattern;

        Command(String command) {
            pattern = Pattern.compile(command);
        }
    }
}
