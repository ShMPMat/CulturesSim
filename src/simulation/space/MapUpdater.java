package simulation.space;

public class MapUpdater implements Runnable {
    private int i, s, e;
    private WorldMap map;

    public MapUpdater(int i, int s, int e, WorldMap map) {
        this.i = i;
        this.s = s;
        this.e = e;
        this.map = map;
    }

    @Override
    public void run() {
        for (int i = s; i < e; i++) {
            for (Tile tile : map.map.get(i)) {
                tile.startUpdate();
            }
        }
        map._execution.set(i, true);
        synchronized (map) {
            map.notify();
        }
    }
}
