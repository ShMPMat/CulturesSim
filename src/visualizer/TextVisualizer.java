package visualizer;
//TODO check smart territory acquisition it seems not to work

import simulation.Controller;
import simulation.World;
import simulation.culture.Event;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.ConverseWrapper;
import simulation.culture.aspect.MeaningInserter;
import simulation.culture.group.Group;
import simulation.culture.interactionmodel.InteractionModel;
import simulation.culture.interactionmodel.MapModel;
import simulation.space.TectonicPlate;
import simulation.space.Tile;
import simulation.space.WorldMap;
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
public class TextVisualizer {
    /**
     * Symbols for representation of groups on the Map.
     */
    private Map<Group, String> groupSymbols;
    /**
     * Symbols for representation of resource on the Map.
     */
    private Map<Resource, String> resourceSymbols;
    /**
     * List of group population before beginning of the last sequence turn;
     * Used for estimating population change.
     */
    private List<Integer> groupPopulations;
    /**
     * Map of all the tiles claimed by groups during the last sequence of turns;
     * Used for displaying new tiles for groups.
     */
    private Map<Group, Set<Tile>> lastClaimedTiles;
    /**
     * Main controller of the simulation
     */
    private Controller controller;
    private World world;
    private WorldMap map;
    private InteractionModel interactionModel;

    /**
     * Base constructor.
     */
    public TextVisualizer() {
        int numberOfGroups = 10, mapSize = 30, numberOrResources = 5;
        controller = new Controller(numberOfGroups, mapSize, numberOrResources,
                new MapModel(0.01, 0.25));
        groupPopulations = new ArrayList<>();
        for (int i = 0; i < numberOfGroups; i++) {
            groupPopulations.add(0);
        }
        controller.world.groups.forEach(group -> group.getCulturalCenter().addAspect(controller.world.getAspectFromPoolByName("TakeApart")));
        controller.world.groups.forEach(group -> group.getCulturalCenter().addAspect(controller.world.getAspectFromPoolByName("Take")));
        controller.world.groups.forEach(Group::finishUpdate);
        world = controller.world;
        map = world.map;
        interactionModel = controller.interactionModel;
    }

    public static void main(String[] args) {
        TextVisualizer textVisualizer = new TextVisualizer();
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
        groupSymbols = new HashMap<>();
        resourceSymbols = new HashMap<>();
        Scanner s = new Scanner(new FileReader("SupplementFiles/Symbols/SymbolsLibrary"));
        for (Group group : world.groups) {
            groupSymbols.put(group, s.nextLine());
        }
        s = new Scanner(new FileReader("SupplementFiles/Symbols/SymbolsResourceLibrary"));
        for (Resource resource : world.resourcePool) {
            resourceSymbols.put(resource, s.nextLine());
        }
    }

    /**
     * Prints default map and information output.
     */
    private void print() {
        StringBuilder main = new StringBuilder();
        main.append(world.getTurn()).append("\n");
        int i = -1;
        for (Group group : world.groups) {
            StringBuilder stringBuilder = new StringBuilder();
            i++;
            if (group.state == Group.State.Dead) {
                continue;
            }
            stringBuilder.append(groupSymbols.get(group)).append(" ").append(group.name).append(" ");
            for (Aspect aspect : group.getAspects()) {
                stringBuilder.append(aspect.getName()).append(" ");
            }
            stringBuilder.append("   population=").append(group.population)
                    .append(group.population <= groupPopulations.get(i) ? "↓" : "↑").append('\n');
            groupPopulations.set(i, group.population);
            main.append(chompToSize(stringBuilder.toString(), 160));
        }
        lastClaimedTiles = new HashMap<>();
        for (Group group : controller.world.groups) {
            lastClaimedTiles.put(group, new HashSet<>());
        }
        for (Event event : interactionModel.getEvents().stream().
                filter(event -> event.type == Event.Type.TileAquisition).collect(Collectors.toList())) {
            lastClaimedTiles.get((Group) event.getAttribute("group")).add((Tile) event.getAttribute("tile"));
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
            main.append((i < 10 ? " " : i / 10));
        }
        main.append("\n").append("  ");
        for (int i = 0; i < worldMap.map.get(0).size(); i++) {
            main.append(i % 10);
        }
        main.append("\n");
        StringBuilder map = new StringBuilder();
        for (int i = 0; i < worldMap.map.size(); i++) {
            List<Tile> line = worldMap.map.get(i);
            String token;
            map.append((i < 10 ? " " + i : i));
            for (Tile tile : line) {
                token = condition.apply(tile);
                if (token.equals("")) {
                    switch (tile.getType()) {
                        case Ice:
                            token = "\033[47m";
                            break;
                        case Water:
                            token = "\033[44m";
                            break;
                    }
                    if (tile.group == null) {
                        switch (tile.getType()) {
                            case Water:
                            case Ice:
                            case Normal:
                                if (tile.getResources().size() > 0) {
                                    token += "\033[30m" + (resourceSymbols.get(tile.getResources().get(0)) == null ? "Ё" :
                                            resourceSymbols.get(tile.getResources().get(0)));
                                } else {
                                    token += " ";
                                }
                                break;
                            case Mountain:
                                token = (tile.getLevel() > 130 ? "\033[43m\033[93m" : "\033[33m") + "^";
                                break;
                            default:
                                token += " ";
                        }
                    } else {
                        if (tile.group.state == Group.State.Dead) {
                            token += "\033[33m☠";
                        } else {
                            token += (lastClaimedTiles.get(tile.group).contains(tile) ? "\033[31m" : "\033[95m") + groupSymbols.get(tile.group);
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
    private void printGroup(Group group) {
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
        printMap(tile -> (tile.getResources().stream().anyMatch(r -> r.getSimpleName().equals(resource.getSimpleName())) ? "\033[31mX" : ""));
        System.out.println(resource);
    }

    /**
     * Prints tile information.
     *
     * @param tile Tile which will be printed.
     */
    private void printTile(Tile tile) {
        printMap(t -> (t.equals(tile) ? "\033[31mX" : ""));
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
//            Resource resource = controller.world.getResourceFromPoolByName(aspectName.split("On")[1]);
            if (resource == null) {
                System.err.println("Cannot add aspect to the group");
                return;
            }
            Aspect a = world.getAspectFromPoolByName(aspectName.split("On")[0]);
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
            aspect = world.getAspectFromPoolByName(aspectName);
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
        group.getCulturalCenter().addAspect(aspect);
        group.getCulturalCenter().pushAspects();
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
        Resource resource = world.getResourceFromPoolByName(wantName);
        if (resource == null) {
            System.err.println("Cannot add want to the group");
            return;
        }
        group.getCulturalCenter().addWant(resource);
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
                                    Integer.parseInt(line.substring(line.indexOf(' ') + 1))));
                            break;
                        case Plates:
                            printMap(tile -> {
                                List<Tile> affectedTiles = new ArrayList<>();
                                for (TectonicPlate tectonicPlate : map.getTectonicPlates()) {
                                    affectedTiles.addAll(tectonicPlate.getAffectedTiles());
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
                                String colour = "";
                                if (tile.getTemperature() < -10) {
                                    colour = "\033[44m";
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
                        case Resource:
                            Resource resource = world.getResourceFromPoolByName(line.substring(2));
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
        Turn("");

        Pattern pattern;
        Command(String command) {pattern = Pattern.compile(command);}
    }
}
