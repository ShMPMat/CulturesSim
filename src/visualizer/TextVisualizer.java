package visualizer;
//TODO check smart territory acquisition it seems not to work
import extra.OutputFunc;
import simulation.Controller;
import simulation.World;
import simulation.culture.Event;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.ConverseWrapper;
import simulation.culture.aspect.MeaningInserter;
import simulation.culture.group.Group;
import simulation.culture.interactionmodel.MapModel;
import simulation.space.Tile;
import simulation.space.WorldMap;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourceIdeal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    /**
     * Pattern used for recognizing command for printing group information.
     */
    private Pattern groupPattern = Pattern.compile("^G\\d+");
    /**
     * Pattern used for recognizing command for making sequence of turns.
     */
    private Pattern turnsPattern = Pattern.compile("\\d+");
    /**
     * Pattern used for recognizing command for printing tile information.
     */
    private Pattern tilePattern = Pattern.compile("\\d+ \\d+");
    /**
     * Pattern used for recognizing command for making turns until
     * something important happens.
     */
    private Pattern idleGoPattern = Pattern.compile("go");
    /**
     * Pattern used for recognizing command for printing resource information.
     */
    private Pattern resourcePattern = Pattern.compile("r \\w+");
    private Pattern meaningfulResourcePattern = Pattern.compile("meaning");
    private Pattern artificialResourcePattern = Pattern.compile("artificial");
    /**
     * Pattern used for recognizing command for printing map.
     */
    private Pattern mapPattern = Pattern.compile("[mM]");
    /**
     * Pattern used for recognizing command for exiting simulation.
     */
    private Pattern exitPattern = Pattern.compile("EXIT");
    /**
     * Pattern used for recognizing command for adding Aspect for a group.
     */
    private Pattern addAspectPattern = Pattern.compile("^G\\d+ \\w+");
    private Pattern addWantPattern = Pattern.compile("^want G\\d+ \\w+");

    /**
     * Base constructor.
     */
    public TextVisualizer() {
        int numberOfGroups = 10, mapSize = 20, numberOrResources = 5;
        controller = new Controller(numberOfGroups, mapSize, numberOrResources,
                new MapModel(0.01, 0.25));
        groupPopulations = new ArrayList<>();
        for (int i = 0; i < numberOfGroups; i++) {
            groupPopulations.add(0);
        }
        controller.world.groups.forEach(group -> group.getCulturalCenter().addAspect(controller.world.getAspectFromPoolByName("TakeApart")));
        controller.world.groups.forEach(group -> group.getCulturalCenter().addAspect(controller.world.getAspectFromPoolByName("Take")));
        controller.world.groups.forEach(Group::finishUpdate);
    }

    /**
     * Function returning a command represented in the line.
      * @param line String line with a command.
     * @return Command token represented by the line.
     */
    private Command getCommand(String line) {
        if (groupPattern.matcher(line).matches()) {
            return Command.Group;
        } else if (turnsPattern.matcher(line).matches()) {
            return Command.Turns;
        } else if (tilePattern.matcher(line).matches()) {
            return Command.Tile;
        } else if (resourcePattern.matcher(line).matches()) {
            return Command.Resource;
        } else if (meaningfulResourcePattern.matcher(line).matches()) {
            return Command.MeaningfulResources;
        } else if (artificialResourcePattern.matcher(line).matches()) {
            return Command.ArtificialResources;
        } else if (idleGoPattern.matcher(line).matches()) {
            return Command.IdleGo;
        } else if (exitPattern.matcher(line).matches()) {
            return Command.Exit;
        } else if (mapPattern.matcher(line).matches()) {
            return Command.Map;
        } else if (addAspectPattern.matcher(line).matches()) {
            return Command.AddAspect;
        } else if (addWantPattern.matcher(line).matches()) {
            return Command.AddWant;
        } else {
            return Command.Turn;
        }
    }

    /**
     * Fills Symbol fields for map drawing.
     * @throws IOException when files with symbols isn't found.
     */
    private void readSymbols() throws IOException {
        groupSymbols = new HashMap<>();
        resourceSymbols = new HashMap<>();
        Scanner s = new Scanner(new FileReader("SupplementFiles/Symbols/SymbolsLibrary"));
        for (Group group : controller.world.groups) {
            groupSymbols.put(group, s.nextLine());
        }
        s = new Scanner(new FileReader("SupplementFiles/Symbols/SymbolsResourceLibrary"));
        for (Resource resource : controller.world.resourcePool) {
            resourceSymbols.put(resource, s.nextLine());
        }
    }

    /**
     * Prints default map and information output.
     */
    private void print() {
        System.out.println(controller.world.getTurn());
        int i = -1;
        for (Group group : controller.world.groups) {
            i++;
            if (group.state == Group.State.Dead) {
                continue;
            }
            System.out.print(groupSymbols.get(group) + " " + group.name + " ");
            for (Aspect aspect : group.getAspects()) {
                System.out.print(aspect.getName() + " ");
            }
            System.out.print("   population=" + group.population +
                    (group.population <= groupPopulations.get(i) ? "↓" : "↑") + '\n');
            groupPopulations.set(i, group.population);
        }
        lastClaimedTiles = new HashMap<>();
        for (Group group : controller.world.groups) {
            lastClaimedTiles.put(group, new HashSet<>());
        }
        for (Event event : controller.interactionModel.getEvents().stream().
                filter(event -> event.type == Event.Type.TileAquisition).collect(Collectors.toList())) {
            lastClaimedTiles.get((Group) event.getAttribute("group")).add((Tile) event.getAttribute("tile"));
        }
        printMap(tile -> "");
        printEvents(controller.interactionModel.getEvents());
        controller.interactionModel.clearEvents();
    }

    /**
     * Prints world map.
     * @param condition function which adds an extra layer of information
     *                  above default map. If function returns non-empty string
     *                  for a tile, output of the function will be drawn above the tile.
     */
    private void printMap(Function<Tile, String> condition) {
        WorldMap worldMap = controller.world.map;
        System.out.print("  ");
        for (int i = 0; i < worldMap.map.get(0).size(); i++) {
            System.out.print((i < 10 ? " " : i / 10));
        }
        System.out.println();
        System.out.print("  ");
        for (int i = 0; i < worldMap.map.get(0).size(); i++) {
            System.out.print(i % 10);
        }
        System.out.println();
        StringBuilder map = new StringBuilder();
        for (int i = 0; i < worldMap.map.size(); i++) {
            List<Tile> line = worldMap.map.get(i);
            String token;
            map.append((i < 10 ? " " + i : i));
            for (Tile tile : line) {
                token = condition.apply(tile);
                if (token.equals("")) {
                    if (tile.group == null) {
                        switch (tile.type) {
                            case Water:
                                token = "\033[34m~";
                                break;
                            case Normal:
                                if (tile.getResources().size() > 0) {
                                    token = "\033[36m" + (resourceSymbols.get(tile.getResources().get(0)) == null ? "Ё" :
                                            resourceSymbols.get(tile.getResources().get(0)));
                                } else {
                                    token = " ";
                                }
                                break;
                            case Mountain:
                                token = "\033[33m^";
                                break;
                        }
                    } else {
                        if (tile.group.state == Group.State.Dead) {
                            token = "\033[33m☠";
                        } else {
                            token = (lastClaimedTiles.get(tile.group).contains(tile) ? "\033[31m" : (tile.getResources().size() > 0 ? "\033[36m" : "\033[0m")) + groupSymbols.get(tile.group);
                        }
                    }
                }
                map.append(token).append("\033[0m");
            }
            map.append("\033[0m").append("\n");
        }
        StringBuilder resources = new StringBuilder();
        for (Resource resource : worldMap.resourcePool) {
            resources.append("\033[31m").append(resourceSymbols.get(resource)).append(" - ")
                    .append(resource.getBaseName()).append("\n");
        }
        System.out.print(OutputFunc.addToRight(map.toString(),
                OutputFunc.chompToLines(resources.toString(), worldMap.map.size()).toString(), true));
    }

    /**
     * Prints group information.
     * @param group Group which will be printed.
     */
    private void printGroup(Group group) {
        printMap(tile -> (group.getTiles().contains(tile) ?
                (group.subgroups.stream().anyMatch(sg -> sg.getTiles().contains(tile)) ? "\033[31mO" :
                        (tile.getResources().stream().anyMatch(resource -> resource.getBaseName().contains("House")) ?
                                "\033[31m+" : "\033[30mX")) : ""));
        System.out.println(group);
    }

    /**
     * Prints resource information.
     * @param resource Resource which will be printed.
     */
    private void printResource(Resource resource) {
        printMap(tile -> (tile.getResources().stream().anyMatch(r -> r.equals(resource)) ? "\033[30mX" : ""));
        System.out.println(resource);
    }

    /**
     * Prints tile information.
     * @param tile Tile which will be printed.
     */
    private void printTile(Tile tile) {
        printMap(t -> (t.equals(tile) ? "\033[30mX" : ""));
        System.out.println(tile);
    }

    /**
     * Prints important events.
     * @param events list of events.
     */
    private void printEvents(Collection<Event> events) {
        for (Event event : events) {
            if (event.type == Event.Type.Death || event.type == Event.Type.ResourceDeath || event.type == Event.Type.DisbandResources) {
                System.out.println(event);
            }
        }
    }

    /**
     * Adds aspect to the group. Null-safe.
     * @param group group to which Aspect will be added. Can be null.
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
            Resource resource = controller.world.getResourceFromPoolByName(aspectName.split("On")[1]);
            if (resource == null) {
                System.err.println("Cannot add aspect to the group");
                return;
            }
            Aspect a = controller.world.getAspectFromPoolByName(aspectName.split("On")[0]);
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
            aspect = controller.world.getAspectFromPoolByName(aspectName);
            if (aspect == null) {
                System.err.println("Cannot add aspect to the group");
                return;
            }
        }
        group.getCulturalCenter().addAspect(aspect);
        group.getCulturalCenter().pushAspects();
    }

    private void addWantToGroup(Group group, String wantName) {
        if (group == null) {
            System.err.println("Cannot add aspect to the group");
            return;
        }
        if (!group.subgroups.isEmpty()) {
            group.subgroups.forEach(subgroup -> addWantToGroup(subgroup, wantName));
            return;
        }
        Resource resource = controller.world.getResourceFromPoolByName(wantName);
        if (resource == null) {
            System.err.println("Cannot add want to the group");
            return;
        }
        group.getCulturalCenter().addWant(resource);
    }

    /**
     * Runs interface for the simulation control.
     */
    private void run(){
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            readSymbols();
            print();
            while (true) {
                String[] _s;
                String line = br.readLine();
                if (line != null) {
                    switch (getCommand(line)) {
                        case Group:
                            printGroup(controller.world.groups.get(Integer.parseInt(line.substring(1))));
                            break;
                        case Turns:
                            for (int i = 0; i < Integer.parseInt(line); i++) {
                                controller.turn();
                            }
                            print();
                            break;
                        case Tile:
                            printTile(controller.world.map.get(Integer.parseInt(line.substring(0, line.indexOf(' '))),
                                    Integer.parseInt(line.substring(line.indexOf(' ') + 1))));
                            break;
                        case Resource:
                            Resource resource = controller.world.getResourceFromPoolByName(line.substring(2));
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
                            printMap(tile -> (tile.getResources().stream().anyMatch(Resource::hasMeaning) ? "\033[30mX"
                                    : ""));
                            break;
                        case ArtificialResources:
                            printMap(tile -> (tile.getResources().stream().anyMatch(res -> res.hasMeaning() ||
                                    res.getBaseName().equals("House")) ? "\033[30mX" : ""));
                            break;
                        case IdleGo:
                            for (int i = 0; i < 500; i++) {
                                controller.turn();
                                if (controller.interactionModel.getEvents().stream()
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
                            addAspectToGroup(controller.world.groups.get(Integer.parseInt(_s[0].substring(1))), _s[1]);
                            break;
                        case AddWant:
                            _s = line.split(" ");
                            addWantToGroup(controller.world.groups.get(Integer.parseInt(_s[1].substring(1))), _s[2]);
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

    public static void main(String[] args) {
        TextVisualizer textVisualizer = new TextVisualizer();
        textVisualizer.run();
    }

    /**
     * Represents commands which can be given to visualizer.
     */
    private enum Command {
        Group,
        Turns,
        Turn,
        Tile,
        IdleGo,
        Resource,
        MeaningfulResources,
        ArtificialResources,
        Map,
        Exit,

        AddAspect,
        AddWant
    }
}
