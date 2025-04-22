package com.bluenova.floorislava.util;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.game.object.gamelobby.GameLobby;
import org.bukkit.Location;
import org.bukkit.block.Biome;

import java.util.concurrent.ThreadLocalRandom;

public class FindAllowedLocation implements Workload {

    GameLobby gp;
    int x;
    int z;
    Biome gameBiome;

    public FindAllowedLocation(GameLobby gp) {
        this.gp = gp;
    }

    @Override
    public void compute() {
        x = ThreadLocalRandom.current().nextInt(-2999999, 2999999);
        z = ThreadLocalRandom.current().nextInt(-2999999, 2999999);
        gameBiome = new Location(FloorIsLava.getNormalWorld(), x, 0, z).getChunk().getChunkSnapshot(true,true,true).getBiome(8,8,8);

        if (gameBiome.equals(Biome.OCEAN) || gameBiome.equals(Biome.DEEP_OCEAN) || gameBiome.equals(Biome.COLD_OCEAN) ||
                gameBiome.equals(Biome.FROZEN_OCEAN) || gameBiome.equals(Biome.DEEP_FROZEN_OCEAN) || gameBiome.equals(Biome.WARM_OCEAN) ||
                gameBiome.equals(Biome.DEEP_LUKEWARM_OCEAN) || gameBiome.equals(Biome.LUKEWARM_OCEAN) || gameBiome.equals(Biome.DEEP_COLD_OCEAN)) {

            FloorIsLava.getWorkloadRunnable().addWorkload(new FindAllowedLocation(gp));
            return;
        }
        gp.generatePlot(x, z);
    }
}
