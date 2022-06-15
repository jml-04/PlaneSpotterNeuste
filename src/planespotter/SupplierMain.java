package planespotter;

import planespotter.constants.Areas;
import planespotter.controller.Scheduler;
import planespotter.display.PaneModels;
import planespotter.model.nio.Fr24Supplier;
import planespotter.model.nio.FastKeeper;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class SupplierMain {
    // monitor object
    public static final Lock lock = new ReentrantLock(); // ich weiß nicht genau was das kann aber ich glaube es kann viel

    public static final int INSERT_PERIOD_SEC = 60; // seconds

    /**
     * Second Supplier Test-Main, single, scheduled World-Supplier
     *
     * @param args can be ignored
     */
    public static void main(String[] args) {

        final var scheduler = new Scheduler();
        final var supplier0 = new Fr24Supplier(0, Areas.AMERICA);
        final var supplier1 = new Fr24Supplier(1, Areas.EURASIA);
        final var keeper = new FastKeeper(1200L);
        final var display = new PaneModels.SupplierDisplay();
        final AtomicInteger insertedNow = new AtomicInteger(0), insertedFrames = new AtomicInteger(0),
                newPlanesNow = new AtomicInteger(0), newPlanesAll = new AtomicInteger(0),
                newFlightsNow = new AtomicInteger(0), newFlightsAll = new AtomicInteger(0);

        display.start();

        scheduler.schedule(() -> {
            // executing two suppliers to collect Fr24-Data
            scheduler.exec(supplier0, "Supplier-0", true, 2, false)
                     .exec(supplier1, "Supplier-1", true, 2, false);
        }, "Supplier-Main", 0, INSERT_PERIOD_SEC)
                // executing the keeper every 400 seconds
                .schedule(keeper, "Keeper", 100, 400)
                // executing the GC every 20 seconds
                .schedule(System::gc, "GC Caller", 30, 20)
                // updating display
                .schedule(() -> {
                    insertedNow.set(Fr24Supplier.getFrameCount() - insertedFrames.get());
                    newPlanesNow.set(Fr24Supplier.getPlaneCount() - newPlanesAll.get());
                    newFlightsNow.set(Fr24Supplier.getFlightCount() - newFlightsAll.get());

                    display.update(insertedNow.get(), newPlanesNow.get(), newFlightsNow.get());

                    insertedFrames.set(Fr24Supplier.getFrameCount());
                    newPlanesAll.set(Fr24Supplier.getPlaneCount());
                    newFlightsAll.set(Fr24Supplier.getFlightCount());
                }, 0, 1);
    }

}
