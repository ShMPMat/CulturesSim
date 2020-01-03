package visualizer;
//TODO check smart territory acquisition it seems not to work

import simulation.Controller;
import simulation.World;
import simulation.culture.Event;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.ConverseWrapper;
import simulation.culture.aspect.MeaningInserter;
import simulation.culture.group.Group;
import simulation.culture.group.GroupConglomerate;
import simulation.culture.group.cultureaspect.CultureAspect;
import simulation.culture.interactionmodel.InteractionModel;
import simulation.culture.interactionmodel.MapModel;
import simulation.culture.thinking.meaning.Meme;
import simulation.space.TectonicPlate;
import simulation.space.Tile;
import simulation.space.WorldMap;
import simulation.space.resource.Genome;
import simulation.space.resource.Resource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static extra.OutputFunc.*;

/**
 * Main class, running and visualizing simulation.
 */
public class TextVisualizer implements Visualizer{
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

    /**
     * Base constructor.
     */
    public TextVisualizer() {
        controller = new Controller(new MapModel());
        Controller.visualizer = this;
        world = controller.world;
        map = world.map;
        interactionModel = controller.interactionModel;
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
        for (int y = 0; y < controller.mapSizeY; y++) {
            boolean isLineClear = true;
            for (int x = 0; x < controller.mapSizeX; x++) {
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

    public static void main(String[] args) {
        TextVisualizer textVisualizer = new TextVisualizer();
        textVisualizer.initialize();
        textVisualizer.run();
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
        for (Resource resource : world.resourcePool) {
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
                map.map.size() + 2), printedEvents(interactionModel.getEvents(), false),
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
        System.out.print(addToRight(printedMap(condition), chompToLines(printedResources(), map.map.size() + 2),
                true));
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
                if (aspect.getUsefulness() <= 0 ) {
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
        for (int i = 0; i < worldMap.map.get(0).size(); i++) {
            main.append((i < 100 ? " " : i / 100 % 100));
        }
        main.append("\n").append("  ");
        for (int i = 0; i < worldMap.map.get(0).size(); i++) {
            main.append((i < 10 ? " " : i / 10 % 10));
        }
        main.append("\n").append("  ");
        for (int i = 0; i < worldMap.map.get(0).size(); i++) {
            main.append(i % 10);
        }
        main.append("\n");
        StringBuilder map = new StringBuilder();
        for (int i = 0; i < controller.mapSizeX; i++) {
            List<Tile> line = worldMap.map.get(i);
            String token;
            map.append((i < 10 ? " " + i : i));
            for (int j = 0; j < controller.mapSizeY; j++) {
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
                    if (tile.group == null) {
                        switch (tile.getType()) {
                            case Water:
                            case Ice:
                            case Woods:
                            case Growth:
                            case Normal:
                                List<Resource> actual = tile.getResources().stream().filter(resource ->
                                        resource.getGenome().getType() != Genome.Type.Plant && resource.getAmount() > 0 &&
                                                !resource.getSimpleName().equals("Vapour")).collect(Collectors.toList());
                                if (/*actual.size() > 0*/ false) {
                                    token += "\033[30m" + (resourceSymbols.get(actual.get(0)) == null ? "Ё" :
                                            resourceSymbols.get(actual.get(0)));
                                } else {
                                    token += " ";
                                }
                                break;
                            case Mountain:
                                token = (tile.getLevel() > 130 ? "\033[43m" : "") +
                                        (tile.getResources().contains(world.getPoolResource("Snow")) ? "\033[30m" : "\033[93m") + "^";
                                break;
                            default:
                                token += " ";
                        }
                    } else {
                        if (tile.group.state == Group.State.Dead) {
                            token += "\033[33m☠";
                        } else {
                            token += (lastClaimedTiles.get(tile.group.getParentGroup().name).contains(tile) ? "\033[31m" :
                                    "\033[96m\033[1m") + groupSymbols.get(tile.group.getParentGroup().name);
                        }
                    }
                }
                map.append(token).append("\033[0m");
            }
            map.append("\033[0m").append("\n");
        }
        return main.append(map);
    }

    /**
     * @return StringBuilder with all basic Resources names and its Symbols on the map.
     */
    private StringBuilder printedResources() {
        StringBuilder resources = new StringBuilder();
        for (Resource resource : map.resourcePool) {
            resources.append("\033[31m").append(resourceSymbols.get(resource)).append(" - ")
                    .append(resource.getBaseName()).append("\n");
        }
        return resources;

    }

    /**
     * Prints group information.
     *
     * @param group Group which will be printed.
     */
    private void printGroup(GroupConglomerate group) {
        printMap(tile -> (group.getTiles().contains(tile) ?
                (group.subgroups.stream().anyMatch(sg -> sg.getTiles().contains(tile)) ? "\033[31mO" :
                        (tile.getResources().stream().anyMatch(resource -> resource.getBaseName().contains("House")) ?
                                "\033[31m+" : "\033[31mX")) : ""));
        System.out.println(group);
    }

    /**
     * Prints resource information.
     *
     * @param resource Resource which will be printed.
     */
    private void printResource(Resource resource) {
        printMap(tile -> (tile.getResources().stream().anyMatch(r -> r.getSimpleName().equals(resource.getSimpleName())
                && r.getAmount() > 0) ? "\033[30m\033[41m" + tile.getResource(resource).getAmount() % 10 : ""));
        System.out.println(resource);
    }

    /**
     * Prints tile information.
     *
     * @param tile Tile which will be printed.
     */
    private void printTile(Tile tile) {
        printMap(t -> (t.equals(tile) ? "\033[31m\033[41mX" : ""));
        System.out.println(tile);
    }

    /**
     * Prints events.
     *
     * @param events   list of events.
     * @param printAll whether prints all events or only the most important.
     */
    private void printEvents(Collection<Event> events, boolean printAll) {
        System.out.print(printedEvents(events, printAll));
    }

    /**
     * @param events   list of events.
     * @param printAll whether StringBuilder will contain all events or only the most important.
     * @return StringBuilder with events.
     */
    private StringBuilder printedEvents(Collection<Event> events, boolean printAll) {
        StringBuilder main = new StringBuilder();
        for (Event event : events) {
            if (printAll || event.type == Event.Type.Death || event.type == Event.Type.ResourceDeath || event.type == Event.Type.DisbandResources) {
                main.append(event).append("\n");
            }
        }
        return main;
    }

    private void addAspectToGroup(GroupConglomerate group, String aspectName) {
        group.subgroups.forEach(g -> addAspectToGroup(g, aspectName));
    }

    /**
     * Adds aspect to the group. Null-safe.
     *
     * @param group      group to which Aspect will be added. Can be null.
     * @param aspectName name of the aspect which will be added to the Group. Can
     *                   represent Aspect which doesn't exists.
     */
    private void addAspectToGroup(Group group, String aspectName) {
        if (group == null) {
            System.err.println("Cannot add aspect to the group");
            return;
        }
        Aspect aspect;
        if (aspectName.contains("On")) {
            String resourceName = aspectName.split("On")[1];
            Resource resource = group.getOverallTerritory().getDifferentResources().stream()
                    .filter(res -> res.getSimpleName().equals(resourceName)).findFirst()
                    .orElse(group.getCulturalCenter().getAllProducedResources().stream().map(pair -> pair.first)
                            .filter(res -> res.getSimpleName().equals(resourceName)).findFirst().orElse(null));
//            Resource resource = controller.world.getPoolResource(aspectName.split("On")[1]);
            if (resource == null) {
                System.err.println("Cannot add aspect to the group");
                return;
            }
            Aspect a = world.getPoolAspect(aspectName.split("On")[0]);
            if (a == null) {
                System.err.println("Cannot add aspect to the group");
                return;
            }
            if (a.canApplyMeaning()) {
                aspect = new MeaningInserter(a, resource, group);
            } else {
                aspect = new ConverseWrapper(a, resource, group);
            }
        } else {
            aspect = world.getPoolAspect(aspectName);
            if (aspect == null) {
                System.err.println("Cannot add aspect to the group");
                return;
            }
        }
        if (aspect instanceof ConverseWrapper) {
            group.getCulturalCenter().addAspect(((ConverseWrapper) aspect).aspect);
            group.getCulturalCenter().pushAspects();
            if (group.getAspect(((ConverseWrapper) aspect).aspect) == null) {
                System.err.println("Cannot add aspect to the group: cant add base aspect for the Converse Wrapper.");
                return;
            }
        }
        group.subgroups.forEach(g -> g.getCulturalCenter().addAspect(aspect));
        group.subgroups.forEach(g -> g.getCulturalCenter().pushAspects());
    }

    private void addWantToGroup(GroupConglomerate group, String wantName) {
        group.subgroups.forEach(g -> addWantToGroup(g, wantName));
    }

    /**
     * Adds want to the group. Null-safe.
     *
     * @param group    group to which want will be added. Can be null.
     * @param wantName name of the Resource which will be added to the wants of the Group. Can
     *                 *                   represent Resource which doesn't exists.
     */
    private void addWantToGroup(Group group, String wantName) {
        if (group == null) {
            System.err.println("Cannot add aspect to the group");
            return;
        }
        if (!group.subgroups.isEmpty()) {
            group.subgroups.forEach(subgroup -> addWantToGroup(subgroup, wantName));
            return;
        }
        Resource resource = world.getPoolResource(wantName);
        if (resource == null) {
            System.err.println("Cannot add want to the group");
            return;
        }
        group.getCulturalCenter().addResourceWant(resource);
    }

    private void addResourceOnTile(Tile tile, String resourceName) {
        if (tile == null) {
            System.err.println("No such Tile");
            return;
        }
        Resource resource = world.getPoolResource(resourceName);
        if (resource == null) {
            System.err.println("No such Resource");
            return;
        }
        tile.addDelayedResource(resource.copy());
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
                    switch (getCommand(line)) {
                        case Group:
                            printGroup(world.groups.get(Integer.parseInt(line.substring(1))));
                            break;
                        case Turns:
                            for (int i = 0; i < Integer.parseInt(line); i++) {
                                controller.turn();
                            }
                            print();
                            break;
                        case Tile:
                            printTile(map.get(Integer.parseInt(line.substring(0, line.indexOf(' '))),
                                    Integer.parseInt(line.substring(line.indexOf(' ') + 1)) + mapCut));
                            break;
                        case Plates:
                            printMap(tile -> {
                                List<Tile> affectedTiles = new ArrayList<>();
                                for (TectonicPlate tectonicPlate : map.getTectonicPlates()) {
                                    affectedTiles.addAll(tectonicPlate.getAffectedTiles().stream()
                                            .map(pair -> pair.first).collect(Collectors.toList()));
                                }
                                for (int i = 0; i < map.getTectonicPlates().size(); i++) {
                                    if (map.getTectonicPlates().get(i).contains(tile)) {
                                        if (affectedTiles.contains(tile)) {
                                            return "\033[" + (30 + i) + "mX";
                                        }
                                        String direction = "0";
                                        switch (map.getTectonicPlates().get(i).getDirection()) {
                                            case D:
                                                direction = "v";
                                                break;
                                            case L:
                                                direction = "<";
                                                break;
                                            case R:
                                                direction = ">";
                                                break;
                                            case U:
                                                direction = "^";
                                                break;
                                        }
                                        return "\033[" + (30 + i) + "m" + direction;
                                    }
                                }
                                return " ";
                            });
                            break;
                        case Temperature:
                            printMap(tile -> {
                                String colour;
                                if (tile.getTemperature() < -20) {
                                    colour = "\033[44m";
                                } else if (tile.getTemperature() < -10) {
                                    colour = "\033[104m";
                                } else if (tile.getTemperature() < 0) {
                                    colour = "\033[46m";
                                } else if (tile.getTemperature() < 10) {
                                    colour = "\033[47m";
                                } else if (tile.getTemperature() < 20){
                                    colour = "\033[43m";
                                } else {
                                    colour = "\033[41m";
                                }
                                return "\033[90m" + colour + Math.abs(tile.getTemperature() % 10);
                            });
                            break;
                        case Wind:
                            printMap(tile -> {
                                String direction;
                                int level = tile.getWind().getAffectedTiles().stream()
                                        .reduce(Integer.MIN_VALUE, (x, y) -> Math.max(x, (int) Math.ceil(y.second)), Integer::compareTo);
                                if (level > 4) {
                                    direction = "\033[41m";
                                } else if (level > 3) {
                                    direction = "\033[43m";
                                } else if (level > 2) {
                                    direction = "\033[47m";
                                } else if (level > 1){
                                    direction = "\033[46m";
                                } else {
                                    direction = "\033[44m";
                                }
                                if (tile.getWind().getAffectedTiles().size() >= 1) {
                                    Tile affected = tile.getWind().getAffectedTiles().stream()
                                            .sorted(Comparator.comparingDouble(pair -> -pair.second))
                                            .collect(Collectors.toList()).get(0).first;
                                    if (affected.getX() - tile.getX() == 1 && affected.getY() - tile.getY() == 1) {
                                        direction += "J";
                                    } else if (affected.getX() - tile.getX() == 1 && affected.getY() - tile.getY() == -1) {
                                        direction += "L";
                                    } else if (affected.getX() - tile.getX() == -1 && affected.getY() - tile.getY() == 1) {
                                        direction += "⏋";
                                    } else if (affected.getX() - tile.getX() == -1 && affected.getY() - tile.getY() == -1) {
                                        direction += "Г";
                                    } else if (affected.getX() - tile.getX() == 1) {
                                        direction += "V";
                                    } else if (affected.getX() - tile.getX() == -1) {
                                        direction += "^";
                                    } else if (affected.getY() - tile.getY() == 1) {
                                        direction += ">";
                                    } else if (affected.getY() - tile.getY() == -1) {
                                        direction += "<";
                                    }
                                } else {
                                    direction = " ";
                                }
                                return direction;
                            });
                            break;
                        case TerrainLevel:
                            printMap(tile -> {
                                String colour = "";
                                if (tile.getSecondLevel() < 90) {
                                    colour = "\033[44m";
                                } else if (tile.getSecondLevel() < controller.defaultWaterLevel) {
                                    colour = "\033[104m";
                                } else if (tile.getSecondLevel() < 105) {
                                    colour = "\033[46m";
                                } else if (tile.getSecondLevel() < 110) {
                                    colour = "\033[47m";
                                } else if (tile.getSecondLevel() < 130){
                                    colour = "\033[43m";
                                } else {
                                    colour = "\033[41m";
                                }
                                return "\033[90m" + colour + Math.abs(tile.getSecondLevel() % 10);
                            });
                            break;
                        case Vapour:
                            printMap(tile -> {
                                String colour = "";
                                int level = tile.getResourcesWithMoved().stream().filter(resource ->
                                        resource.getSimpleName().equals("Vapour")).reduce(0,
                                        (x, y) -> x + y.getAmount(), Integer::sum);
                                if (level == 0) {
                                    colour = "\033[44m";
                                } else if (level < 50) {
                                    colour = "\033[104m";
                                } else if (level < 100) {
                                    colour = "\033[46m";
                                } else if (level < 150) {
                                    colour = "\033[47m";
                                } else if (level < 200) {
                                    colour = "\033[43m";
                                } else {
                                    colour = "\033[41m";
                                }
                                return "\033[90m" + colour + (level / 10) % 10;
                            });
                            break;
                        case Fixed:
                            printMap(tile -> tile.fixedWater ? "\033[41mX" : "");
                            break;
                        case Resource:
                            Resource resource = world.getPoolResource(line.substring(2));
                            if (resource != null) {
                                printResource(resource);
                            } else {
                                Optional<Resource> _oo = resourceSymbols.entrySet().stream()
                                        .filter(entry -> entry.getValue().equals(line.substring(2)))
                                        .map(Map.Entry::getKey).findFirst();
                                _oo.ifPresent(this::printResource);
                            }
                            break;
                        case MeaningfulResources:
                            printMap(tile -> (tile.getResources().stream().anyMatch(Resource::hasMeaning) ? "\033[31mX"
                                    : ""));
                            break;
                        case ArtificialResources:
                            printMap(tile -> (tile.getResources().stream().anyMatch(res -> res.hasMeaning() ||
                                    res.getBaseName().equals("House") || res.getBaseName().equals("Clothes")) ? "\033[31mX" : ""));
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
                        case AddAspect:
                            _s = line.split(" ");
                            addAspectToGroup(world.groups.get(Integer.parseInt(_s[0].substring(1))), _s[1]);
                            break;
                        case AddWant:
                            _s = line.split(" ");
                            addWantToGroup(world.groups.get(Integer.parseInt(_s[1].substring(1))), _s[2]);
                            break;
                        case AddResource:
                            _s = line.split(" ");
                            addResourceOnTile(map.get(Integer.parseInt(_s[0]), Integer.parseInt(_s[1])), _s[2]);
                            break;
                        case GeologicalTurn:
                            controller.geologicTurn();
                            print();
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
         * Command for making sequence of turns.
         */
        Turns("\\d+"),
        /**
         * Command for making turns until something important happens.
         */
        IdleGo("go"),
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
        Vapour("vapour"),
        Fixed("fixed"),
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
        Turn("");

        Pattern pattern;
        Command(String command) {pattern = Pattern.compile(command);}
    }
}
