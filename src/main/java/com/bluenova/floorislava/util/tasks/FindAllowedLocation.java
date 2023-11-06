package com.bluenova.floorislava.util.tasks;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.game.object.GameLobby;
import com.bluenova.floorislava.util.Workload;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Biome;

import java.util.concurrent.ThreadLocalRandom;

public class FindAllowedLocation implements Workload {

    GameLobby gp;
    int x;
    int z;
    Chunk gameChunk;

    public FindAllowedLocation(GameLobby gp) {
        this.gp = gp;
    }

    @Override
    public void compute() {
        x = ThreadLocalRandom.current().nextInt(-2999999, 2999999);
        z = ThreadLocalRandom.current().nextInt(-2999999, 2999999);
        gameChunk = new Location(FloorIsLava.getNormalWorld(), x, 0, z).getChunk();

        if (gameChunk.contains(Biome.OCEAN) || gameChunk.contains(Biome.DEEP_OCEAN) || gameChunk.contains(Biome.COLD_OCEAN) ||
                gameChunk.contains(Biome.FROZEN_OCEAN) || gameChunk.contains(Biome.DEEP_FROZEN_OCEAN) || gameChunk.contains(Biome.WARM_OCEAN) ||
                gameChunk.contains(Biome.DEEP_LUKEWARM_OCEAN) || gameChunk.contains(Biome.LUKEWARM_OCEAN) || gameChunk.contains(Biome.DEEP_COLD_OCEAN)) {

            FloorIsLava.getWorkLoadRunnable().addWorkload(new FindAllowedLocation(gp));
            return;
        }
        gp.generatePlot(x, z);
    }
}
