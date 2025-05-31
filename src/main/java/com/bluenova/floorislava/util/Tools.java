package com.bluenova.floorislava.util;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.game.object.GamePlot;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Random;

public class Tools {

    public static void announce(ArrayList<Player> players, String msg) {
        for (Player player : players) {
            player.sendMessage(msg);
        }
    }

    /*Gets a safe location*/
    public static Location getSafeLocation(World gameWorld, GamePlot plot) {
        int x_local = new Random().nextInt(FloorIsLava.getGamePlotDivider().plotSize);
        int z_local = new Random().nextInt(FloorIsLava.getGamePlotDivider().plotSize);
        Location safeLoc = new Location(gameWorld, x_local, gameWorld.getMaxHeight()-1, z_local);
        Location chunkGlobal = new Location(gameWorld, plot.plotStart.getX(), 1, plot.plotStart.getZ());
        safeLoc.setX(chunkGlobal.getX() + x_local);
        safeLoc.setZ(chunkGlobal.getZ() + z_local);
        Block safeBlock = getHighestUsableBlockAt(gameWorld, x_local, z_local);
        if (safeBlock == null) getSafeLocation(gameWorld, plot);
        safeLoc.setY(safeBlock.getLocation().getY() + 1.03);
        safeLoc.setX(safeLoc.getX() + 0.5);
        safeLoc.setZ(safeLoc.getZ() + 0.5);
        return safeLoc;
    }

    /*Gets the highest usable block in the world*/
    public static Block getHighestUsableBlockAt(World world, int x, int z) {
        Block block = world.getHighestBlockAt(x, z);

        while (block.getType().isAir()) {
            block = block.getRelative(0, -1, 0);
            if (block.getY() <= world.getMinHeight()) {
                return null; // Couldn't find any block
            }
        }
        FloorIsLava.getInstance().getPluginLogger()
                .debug("Found safe block at " + block.getX() + ", " + block.getY() + ", " + block.getZ() + " of type " + block.getType());
        if (block.getType() == Material.LAVA) {
            return null;
        }
        return block;
    }

    // WORLD EDIT TOOLS
    public static Clipboard createClipboard(World bukkitWorld, CuboidRegion region) {
        BukkitWorld worldEditWorld = new BukkitWorld(bukkitWorld); // go figure
        Clipboard clipboard = null;
        try (EditSession session = WorldEdit.getInstance().newEditSession(worldEditWorld)) {
            clipboard = new BlockArrayClipboard(region);
            ForwardExtentCopy copyOperation = new ForwardExtentCopy(worldEditWorld, region, clipboard, region.getMinimumPoint());

            Operations.complete(copyOperation);
        } catch (WorldEditException error) {
            error.printStackTrace();
        }
        return clipboard;
    }

    public static void pasteClipboard(Clipboard clipboard, Location targetLocation) {
        BukkitWorld worldEditWorld = new BukkitWorld(targetLocation.getWorld());
        Bukkit.getScheduler().runTask(FloorIsLava.getInstance(), () -> {
            try (EditSession session = WorldEdit.getInstance().newEditSession(worldEditWorld)) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(session)
                        .to(BlockVector3.at(targetLocation.getX(), targetLocation.getY(), targetLocation.getZ()))
                        .ignoreAirBlocks(false)
                        .build();
                Operations.complete(operation);
            } catch (WorldEditException error) {
                error.printStackTrace();
            }
        });

    }
}