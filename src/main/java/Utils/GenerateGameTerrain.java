package Utils;


import GameObjects.GameLobby;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import floorIsLava.FloorIsLava;
import org.bukkit.Location;

public class GenerateGameTerrain implements Workload{

    GameLobby gp;
    int x_copy;
    int z_copy;
    int x_paste;
    int z_paste;

    public GenerateGameTerrain(GameLobby gp, int x_copy, int z_copy, int x_paste, int z_paste){

        this.gp = gp;
        this.x_copy = x_copy;
        this.z_copy = z_copy;
        this.x_paste = x_paste;
        this.z_paste = z_paste;
    }

    @Override
    public void compute(){
        Clipboard clipboard = Tools.createClipboard(FloorIsLava.NORMAL_WORLD, new CuboidRegion(BlockVector3.at(x_copy, -64, z_copy),BlockVector3.at(x_copy, 319, z_copy)));
        Tools.pasteClipboard(clipboard, new Location(FloorIsLava.VOID_WORLD, x_paste, -64, z_paste));
        if(gp != null) gp.startGame();
    }
}
