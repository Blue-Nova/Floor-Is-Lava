package com.bluenova.floorislava.util;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.util.messages.PluginLogger;
import com.sk89q.worldedit.WorldEditException;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkloadRunnable extends BukkitRunnable {

    private static final double MAX_MILLIS_PER_TICK = 10;
    private static final int MAX_NANOS_PER_TICK = (int) (MAX_MILLIS_PER_TICK * 1E6);

    private final PluginLogger pluginLogger;
    private final Deque<Workload> workloadDeque = new ArrayDeque<>();
    private final List<Workload> runningWorkloads = new ArrayList<>();
    private final AtomicInteger runningWorkloadCount = new AtomicInteger(0);
    private BukkitTask bukkitTask; // Store the BukkitTask

    public WorkloadRunnable(PluginLogger pluginLogger) {
        this.pluginLogger = pluginLogger;
        this.pluginLogger.debug("WorkloadRunnable initialized.");
    }

    public void addWorkload(Workload workload) {
        this.workloadDeque.add(workload);
    }

    public void startWLR() {
        this.runTaskTimer(FloorIsLava.getInstance(), 0L, 1L); // Use BukkitRunnable's method
    }

    public void stopWLR() {
        if (bukkitTask != null && !bukkitTask.isCancelled()) {
            bukkitTask.cancel(); // Cancel the BukkitTask
            bukkitTask = null; // Clear the reference
        }
    }


    @Override
    public void run() {
        long stopTime = System.nanoTime() + MAX_NANOS_PER_TICK;
        Workload nextLoad;

        while (System.nanoTime() <= stopTime && (nextLoad = workloadDeque.poll()) != null) {
            try {

                final Workload workload = nextLoad;
                runningWorkloadCount.incrementAndGet();
                runningWorkloads.add(workload);

                Bukkit.getScheduler().runTaskAsynchronously(FloorIsLava.getInstance(), () -> {
                    try {
                        workload.compute();
                    } catch (WorldEditException ex) {
                        pluginLogger.severe("WorldEditException in workload: " + ex.getMessage());
                    } finally {
                        runningWorkloadCount.decrementAndGet();
                        Bukkit.getScheduler().runTask(FloorIsLava.getInstance(), () -> {
                            runningWorkloads.remove(workload);
                        });
                    }
                });

            } catch (Exception e) {
                pluginLogger.debug("Exception in WorkloadRunnable: " + e.getMessage());
            }
        }
    }
}

