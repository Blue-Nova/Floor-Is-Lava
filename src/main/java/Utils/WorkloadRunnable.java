package Utils;

import com.sk89q.worldedit.WorldEditException;
import floorIsLava.FloorIsLava;
import org.bukkit.Bukkit;

import java.util.ArrayDeque;
import java.util.Deque;

public class WorkloadRunnable implements Runnable{

    private static final double MAX_MILLIS_PER_TICK = 10;
    private static final int MAX_NANOS_PER_TICK = (int) (MAX_MILLIS_PER_TICK * 1E6);

    public final Deque<Workload> workloadDeque = new ArrayDeque<>();

    public void addWorkload(Workload workload){
        this.workloadDeque.add(workload);
    }

    public void startWLR(){
        Bukkit.getScheduler().runTaskTimer(FloorIsLava.plugin, BukkitTask ->{
            FloorIsLava.WLR.run();
        },0L,1L);
    }

    @Override
    public void run() {
        long stopTime = System.nanoTime() + MAX_NANOS_PER_TICK;

        Workload nextload;

        while (System.nanoTime() <= stopTime && (nextload = this.workloadDeque.poll()) !=null) {
            try {
                nextload.compute();
            } catch (WorldEditException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
