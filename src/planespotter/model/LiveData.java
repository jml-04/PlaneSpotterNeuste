package planespotter.model;

import planespotter.constants.Areas;
import planespotter.controller.Scheduler;
import planespotter.dataclasses.Flight;
import planespotter.dataclasses.Frame;
import planespotter.model.nio.proto.ProtoDeserializer;

import java.util.Collection;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class LiveData {
    // frames, which will be inserted later (first loaded into the view)
    private static final ConcurrentLinkedQueue<Frame> insertLater;
    // static initializer
    static {
        insertLater = new ConcurrentLinkedQueue<>();
    }

    // boolean isLive shows if the live map is shown at the moment
    private static boolean live;

    public static void insertLater(Collection<Frame> data) {
        insertLater.addAll(data);
    }

    public static Vector<Flight> directLiveData(final Scheduler scheduler) {
        var deserializer = new ProtoDeserializer();
        var world = Areas.getWorldAreas();
        var frames = deserializer.getFr24Frames(world, scheduler);
        // termorary if // daten gehen verloren
        if (mayLoad()) {
            insertLater(frames);
        }
        var id = new AtomicInteger(0);
        return frames.stream()
                .map(frame -> Flight.parseFlight(frame, id.getAndIncrement()))
                .collect(Collectors.toCollection(Vector::new));
    }

    /**
     *
     *
     * @return
     */
    protected static boolean mayLoad() {
        return insertLater.size() < 10000;
    }

    /**
     *
     * @return true, if insertLater.size() is greater or equals 1000, else false
     *         if true, another Method gets ac
     */
    protected static boolean canInsert(final int count) {
        return insertLater.size() > count;
    }

    protected static boolean isEmpty() {
        return insertLater.size() == 0;
    }

    public static Queue<Frame> pollFrames(final int count) {
        var counter = new AtomicInteger();
        var frames = new ConcurrentLinkedQueue<Frame>();

        insertLater.parallelStream()
                .forEach(frame -> {
                    if (counter.get() < count) {
                        frames.add(insertLater.poll());
                        counter.getAndIncrement();
                    }
                });
        return frames;
    }

    public static boolean isLive() {
        return live;
    }

    public static void setLive(boolean b) {
        live = b;
    }

}
