package visualizer;

import simulation.Controller;
import simulation.event.Event;
import simulation.World;
import simulation.culture.aspect.Aspect;
import simulation.culture.group.GroupTileTagKt;
import simulation.culture.group.centers.Group;
import simulation.culture.group.GroupConglomerate;
import simulation.culture.group.GroupTileTag;
import simulation.interactionmodel.InteractionModel;
import simulation.interactionmodel.MapModel;
import simulation.space.SpaceData;
import simulation.space.WorldMap;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourceType;
import simulation.space.tile.Tile;
import visualizer.printinfo.ConglomeratePrintInfo;
import visualizer.printinfo.MapPrintInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;

import static extra.OutputFunKt.*;
import static visualizer.PrintFunctionsKt.*;
import static visualizer.TextCommandsKt.getCommand;

/**
 * Main class, running and visualizing simulation.
 */
public class TextVisualizer implements Visualizer {
    private ConglomeratePrintInfo groupInfo = new ConglomeratePrintInfo(new ArrayList<>());
    /**
     * Symbols for representation of resource on the Map.
     */
    private Map<Resource, String> resourceSymbols = new HashMap<>();
    private Map<Group, Set<Tile>> lastClaimedTiles;
    private Integer lastClaimedTilesPrintTurn = 0;
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
        List<String> l = new ArrayList<>();
        while (s.hasNextLine()){
            l.add(s.nextLine());
        }
        groupInfo = new ConglomeratePrintInfo(l);
    }

    /**
     * Prints default map and information output.
     */
    public void print() {
        StringBuilder main = new StringBuilder();
        main.append(world.getStringTurn()).append("\n");
        lastClaimedTiles = EventConverterFunctionsKt.lastClaimedTiles(
                interactionModel.getEventLog(),
                lastClaimedTilesPrintTurn
        );
        lastClaimedTilesPrintTurn = world.getTurn();
        System.out.println(PrintFunctionsKt.printedConglomerates(world.getGroups(), groupInfo));
        System.out.print(main.append(addToRight(
                printedMap(tile -> ""),
                addToRight(
                        chompToLines(printedResources().toString(), map.getLinedTiles().size() + 2),
                        printedEvents(interactionModel.getEventLog().getNewEvents(), false),
                        false
                ),
                true
                ))
        );
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
                                        (tile.getResourcePack().contains(world.getResourcePool().getBaseName("Snow")) ? "\033[30m" : "\033[93m") + "^";
                                break;
                            default:
                                token += " ";
                        }
                    } else {
                        Group group = ((GroupTileTag) tile.getTagPool().getByType("Group").get(0)).getGroup();
                        if (lastClaimedTiles.get(group) != null) {
                            token += lastClaimedTiles.get(group).contains(tile)
                                    ? "\033[31m"
                                    : "\033[96m\033[1m";
                        } else {
                            token += "\033[96m\033[1m";
                        }
                        token += groupInfo.getConglomerateSymbol(group.getParentGroup());
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
                        }
                        case GroupTileReach: {
                            GroupConglomerate group = getConglomerate(splitCommand[0]);
                            if (group == null) {
                                break;
                            }
                            printMap(t -> TileMapperFunctionsKt.groupReachMapper(group.subgroups.get(0), t));
                            break;
                        }
                        case GroupProduced: {
                            GroupConglomerate group = getConglomerate(splitCommand[0]);
                            if (group == null) {
                                break;
                            }
                            System.out.println(chompToSize(PrintFunctionsKt.printProduced(group), 150));
                            break;
                        }
                        case GroupRelations: {
                            GroupConglomerate c1 = getConglomerate(splitCommand[0]);
                            GroupConglomerate c2 = getConglomerate(splitCommand[1]);
                            if (c1 == null || c2 == null) {
                                System.out.println("No such Conglomerates exist");
                                break;
                            }
                            System.out.println(printConglomerateRelations(c1, c2));
                            break;
                        }
                        case Tile:
                            printTile(
                                    map.get(
                                            Integer.parseInt(splitCommand[0]),
                                            Integer.parseInt(splitCommand[1]) + mapPrintInfo.getCut()
                                    )
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
                                    Integer.parseInt(splitCommand[2]),
                                    t,
                                    group.subgroups.get(0).getTerritoryCenter()::tilePotentialMapper,
                                    Integer.parseInt(splitCommand[2])
                            ));
                            break;
                        }
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
                        case TileTag:
                            printMap(t -> TileMapperFunctionsKt.tileTagMapper(splitCommand[1], t));
                            break;
                        case Resource:
                            try {
                                Resource resource = world.getResourcePool().getBaseName(line.substring(2));
                                printResource(resource);
                            } catch (NoSuchElementException e) {
                                Optional<Resource> _oo = resourceSymbols.entrySet().stream()
                                        .filter(entry -> entry.getValue().equals(line.substring(2)))
                                        .map(Map.Entry::getKey).findFirst();
                                _oo.ifPresent(this::printResource);
                            }
                            break;
                        case ResourceSubstring:
                            printMap(t -> TileMapperFunctionsKt.resourceSubstringMapper(splitCommand[1], t));
                            System.out.println(briefPrintResourcesWithSubstring(map, splitCommand[1]));
                            break;
                        case ResourceSubstringOnTile:
                            System.out.println(printResourcesOnTile(
                                    map.get(
                                            Integer.parseInt(splitCommand[0]),
                                            Integer.parseInt(splitCommand[1]) + mapPrintInfo.getCut()
                                    ),
                                    splitCommand[3]
                            ));
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
                        }
                        case ResourceDensity: {
                            printMap(t ->
                                    TileMapperFunctionsKt.resourceDensityMapper(
                                            SpaceData.INSTANCE.getData().getTileResourceCapacity(),
                                            t
                                    )
                            );
                            break;
                        }
                        case Events: {
                            int amount = 100;
                            int drop = splitCommand[0].length() + 1;
                            if (splitCommand[0].charAt(0) != 'e') {
                                amount = Integer.parseInt(splitCommand[0]);
                                drop += splitCommand[1].length() + 1;
                            }
                            String regexp = line.substring(drop);
                            System.out.println(printRegexEvents(
                                    controller.interactionModel.getEventLog().getLastEvents(),
                                    amount,
                                    regexp
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
                        } case Strata: {
                            printMap(t -> TileMapperFunctionsKt.strataMapper(splitCommand[1], t));
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
