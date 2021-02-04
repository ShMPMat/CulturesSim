package shmp.visualizer.text;

import org.jetbrains.annotations.NotNull;
import shmp.simulation.Controller;
import shmp.simulation.World;
import shmp.simulation.culture.group.centers.Group;
import shmp.simulation.event.Event;
import shmp.simulation.event.Type;
import shmp.simulation.interactionmodel.InteractionModel;
import shmp.simulation.interactionmodel.MapModel;
import shmp.simulation.space.SpaceData;
import shmp.simulation.space.WorldMap;
import shmp.simulation.space.resource.Resource;
import shmp.simulation.space.tile.Tile;
import shmp.visualizer.EventConverterFunctionsKt;
import shmp.visualizer.StringFunctionsKt;
import shmp.visualizer.Turner;
import shmp.visualizer.Visualizer;
import shmp.visualizer.command.CommandManager;
import shmp.visualizer.printinfo.ConglomeratePrintInfo;
import shmp.visualizer.printinfo.MapPrintInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;

import static shmp.utils.OutputFunKt.addToRight;
import static shmp.utils.OutputFunKt.chompToLines;
import static shmp.visualizer.command.CultureCommandKt.registerCultureCommands;
import static shmp.visualizer.command.EnviromentalCommandKt.registerEnvironmentalCommands;
import static shmp.visualizer.text.TileMapperFunctionsKt.cultureTileMapper;
import static shmp.visualizer.text.TileMapperFunctionsKt.ecosystemTypeMapper;

/**
 * Main class, running and visualizing shmp.simulation.
 */
public class TextVisualizer implements Visualizer<TextVisualizer> {
    private ConglomeratePrintInfo groupInfo = new ConglomeratePrintInfo(new ArrayList<>());
    /**
     * Symbols for representation of resource on the Map.
     */
    public Map<Resource, String> resourceSymbols = new HashMap<>();
    private Map<Group, Set<Tile>> lastClaimedTiles;
    private Integer lastClaimedTilesPrintTurn = 0;
    public MapPrintInfo mapPrintInfo;
    /**
     * Main controller of the shmp.simulation
     */
    public Controller controller;

    private World world;
    private WorldMap map;
    private InteractionModel interactionModel;
    private Scanner s;
    private Turner currentTurner;
    private Thread turnerThread;
    private CommandManager<TextVisualizer> commandManager = new CommandManager<>(TextPassHandler.INSTANCE);

    private List<TileMapper> tileMappers = new ArrayList<>();

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
        registerEnvironmentalCommands(commandManager, TextEnvironmentalHandler.INSTANCE);
        registerCultureCommands(commandManager, TextCultureHandler.INSTANCE);

        addTileMapper(new TileMapper(t -> ecosystemTypeMapper(world, resourceSymbols, t), 10));
        addTileMapper(new TileMapper(t -> cultureTileMapper(lastClaimedTiles, groupInfo, t), 5));

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
        while (s.hasNextLine()) {
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
        System.out.println(StringFunctionsKt.printedConglomerates(world.getGroups(), groupInfo));
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
    public void printMap(Function<Tile, String> condition) {
        System.out.print(addToRight(
                printedMap(condition),
                chompToLines(printedResources(), map.getLinedTiles().size() + 2),
                true
        ));
    }

    /**
     * @param mapper function which adds an extra layer of information
     *                  above default map. If function returns non-empty string
     *                  for a tile, output of the function will be drawn above the tile.
     */
    private StringBuilder printedMap(Function<Tile, String> mapper) {
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
                token = mapper.apply(tile);
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
                    token += applyMappers(tile);
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

    private StringBuilder printedEvents(Collection<Event> events, boolean printAll) {
        StringBuilder main = new StringBuilder();
        for (Event event : events) {
            if (printAll || event.getType() == Type.Death
                    || event.getType() == Type.ResourceDeath
                    || event.getType() == Type.DisbandResources) {
                main.append(event).append("\n");
            }
        }
        return main;
    }

    public void launchTurner(int turnAmount) {
        currentTurner = new Turner(turnAmount, controller);
        turnerThread = new Thread(currentTurner);
        turnerThread.start();
    }

    private void stopTurner() throws InterruptedException {
        currentTurner.isAskedToStop.set(true);
        System.out.println("Turner is asked to stop");
        turnerThread.join();
        currentTurner = null;
        System.out.println("Turner has stopped");
    }

    /**
     * Runs interface for the shmp.simulation control.
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
                    if (currentTurner != null) {
                        stopTurner();
                        print();
                        continue;
                    }
                    commandManager.handleCommand(line, this);
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

    @NotNull
    @Override
    public CommandManager<TextVisualizer> getCommandManager() {
        return commandManager;
    }

    protected void addTileMapper(TileMapper mapper) {
        tileMappers.add(mapper);
        tileMappers.sort(Comparator.comparingInt(TileMapper::getOrder));
    }
    private String applyMappers(Tile tile) {
        for (TileMapper mapper : tileMappers) {
            String result = mapper.getMapper().invoke(tile);
            if (!result.equals("")) {
                return result;
            }
        }
        return " ";
    }
}
