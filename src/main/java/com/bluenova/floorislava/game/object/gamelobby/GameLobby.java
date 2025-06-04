package com.bluenova.floorislava.game.object.gamelobby;

import com.bluenova.floorislava.FloorIsLava; // Needed for casting plugin instance
import com.bluenova.floorislava.config.MainConfig;
import com.bluenova.floorislava.game.object.GamePlot;
import com.bluenova.floorislava.game.object.GamePlotDivider; // Import actual class
import com.bluenova.floorislava.game.object.Lobby;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobby;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobbyManager;
import com.bluenova.floorislava.util.messages.PluginLogger;
import com.bluenova.floorislava.util.worldguard.FILRegionManager;
import com.bluenova.floorislava.util.*;

import com.bluenova.floorislava.util.worldedit.FlushGamePlot;

// Adventure Imports
import com.bluenova.floorislava.util.messages.MiniMessages;
import com.bluenova.floorislava.util.worldguard.RegionProfiles;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
// Bukkit Imports
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.block.Block;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Represents an active "Floor is Lava" game session.
 * Manages players, spectators, the game plot, lava level, and game lifecycle events.
 */
public class GameLobby extends Lobby {

    // --- Injected Dependencies ---
    private final Plugin plugin; // Main plugin instance (for scheduler, config access)
    private final PluginLogger pluginLogger; // Logger for this class
    private final InviteLobbyManager inviteLobbyManager;
    private final GameLobbyManager gameLobbyManager;
    private final WorkloadRunnable workloadRunnable;
    private final GamePlotDivider plotDivider;
    private final World voidWorld;
    private final BukkitScheduler scheduler;
    private final FILRegionManager FILRegionManager; // WorldGuard region manager
    // --- End Dependencies ---

    // Game State & Configuration
    private GameLobbyStates gameState = GameLobbyStates.INACTIVE; // Initial state
    private final List<Integer> LAVA_ANNOUNCE_HEIGHTS = new ArrayList<>();
    private static final int LAVA_INCREMENT = MainConfig.getInstance().getLavaRiseAmount(); // TODO: Make configurable
    private static final int GAME_START_COUNTDOWN = MainConfig.getInstance().getGameStartCountdown(); // TODO: Make configurable
    private static final int LAVA_RISE_COOLDOWN = MainConfig.getInstance().getLavaRiseCooldown(); // TODO: Make configurable
    private static final int PRE_GAME_COUNTDOWN = MainConfig.getInstance().getPreGameCountdown(); // TODO: Make configurable
    private static final double DEATH_ITEM_DROP_CHANCE = 0.5; // TODO: Make configurable

    // Player State Storage
    private final HashMap<Player, Location> previousLocationList = new HashMap<>();
    private final HashMap<Player, ItemStack[]> previousInventoryList = new HashMap<>();
    private final HashMap<Player, Double> previousHealthList = new HashMap<>();
    private final HashMap<Player, Integer> previousHungerList = new HashMap<>();
    private final HashMap<Player, Float> previousXPList = new HashMap<>();
    private final HashMap<Player, Location> playerSpawnLocation = new HashMap<>();
    public final ArrayList<Player> specList = new ArrayList<>(); // Spectators (public for potential external access?)

    // Game Runtime State
    public final GamePlot gamePlot; // Made final as it shouldn't change
    private int countdown = GAME_START_COUNTDOWN;
    private WorldBorder gameBorder;
    public final Location gameStartLoc; // Plot corner min
    public final Location gameEndLoc;   // Plot corner max
    private boolean gameON = false; // Game active flag
    public int lavaHeight;      // Current lava Y level (public for GameEventManager?)

    // Task IDs (for later use to have deployable tasks)
    private BukkitTask countdownTask = null;
    private BukkitTask musicTask = null;
    private BukkitTask eventTask = null;
    private BukkitTask lavaTask = null;

    // fields for manual spawn tool
    public final Map<UUID, Location> manualSpawnPoints = new HashMap<>();
    public final Set<UUID> manualSpawnItemUsed = new HashSet<>();


    /**
     * Creates a new Game Lobby instance.
     * Should be called by a GameManager or similar service.
     */
    public GameLobby(Plugin plugin, PluginLogger pluginLogger, ArrayList<Player> playerList, Player owner,
                     InviteLobbyManager inviteLobbyManager, GameLobbyManager gameLobbyManager,
                     WorkloadRunnable workloadRunnable, GamePlotDivider plotDivider,
                     World voidWorld, FILRegionManager filRegionManager,
                     GamePlot plot) {

        super(playerList, owner); // Initialize inherited 'players' and 'owner'

        // Store injected dependencies
        this.plugin = plugin;

        this.pluginLogger = pluginLogger;

        this.inviteLobbyManager = inviteLobbyManager;
        this.gameLobbyManager = gameLobbyManager; // Removed
        this.workloadRunnable = workloadRunnable;
        this.plotDivider = plotDivider;
        this.voidWorld = voidWorld;
        this.FILRegionManager = filRegionManager;
        this.scheduler = Bukkit.getScheduler();

        // Initialize game state
        this.gamePlot = plot;
        this.gameStartLoc = plot.plotStart.clone(); // Clone to be safe
        this.gameEndLoc = plot.plotEnd.clone();     // Clone to be safe
        this.lavaHeight = plot.plotStart.getBlockY() - 1; // Start just below plot bottom
        Collections.addAll(LAVA_ANNOUNCE_HEIGHTS, -32, 0, 20, 40); // Example heights

        // Store previous locations immediately
        for (Player p : this.players) {
            if (p != null) {
                previousLocationList.put(p, p.getLocation().clone());
            }
        }

        // Announce terrain generation and queue first workload task
        announce("game.generating_terrain");
        playGameSound(Sound.BLOCK_ENCHANTMENT_TABLE_USE); // Play sound for terrain generation
        // Pass 'this' (GameLobby) to FindAllowedLocation if it needs to call back generatePlot
        InviteLobby originatingLobby = inviteLobbyManager.getLobbyFromOwner(this.owner);
        if (originatingLobby != null) {
            inviteLobbyManager.closeLobby(originatingLobby);
            pluginLogger.debug("Closed invite lobby for owner: " + this.owner.getName());
        } else {
            pluginLogger.warning("Could not find original invite lobby for owner: " + this.owner.getName() + " to close.");
        }
        workloadRunnable.addWorkload(new FindAllowedLocation(this));
        setGameState(GameLobbyStates.GENERATING); // Set state to generating
    }

    /**
     * Called by FindAllowedLocation Workload task once source coords (x, z) are found.
     * Queues tasks to build barrier walls and copy terrain columns.
     */
    public void generatePlot(int x_copy_start, int z_copy_start) {
        int borderStartX = (int) gamePlot.plotStart.getX() - 1;
        int borderStartZ = (int) gamePlot.plotStart.getZ() - 1;
        int plotEndX = (int) gamePlot.plotEnd.getX(); // End X/Z are exclusive for size calc
        int plotEndZ = (int) gamePlot.plotEnd.getZ();
        int plotSize = plotDivider.plotSize; // Assumes public field or getter exists

        // Ensure Y range is valid for the void world
        int minY = voidWorld.getMinHeight();
        int maxY = voidWorld.getMaxHeight();

        // Build barrier walls if not already done for this plot instance
        if (!gamePlot.hasBorders()) {
            pluginLogger.info("Generating borders for plot at " + gamePlot.plotStart.getBlockX() + "," + gamePlot.plotStart.getBlockZ());
            // Queue barrier wall tasks
            workloadRunnable.addWorkload(new MakeBarrierWall(voidWorld,
                    BlockVector3.at(borderStartX, minY, borderStartZ),
                    BlockVector3.at(plotEndX, maxY, borderStartZ)));

            workloadRunnable.addWorkload(new MakeBarrierWall(voidWorld,
                    BlockVector3.at(borderStartX, minY, borderStartZ),
                    BlockVector3.at(borderStartX, maxY, plotEndZ)));

            workloadRunnable.addWorkload(new MakeBarrierWall(voidWorld,
                    BlockVector3.at(plotEndX, minY, plotEndZ),
                    BlockVector3.at(borderStartX, maxY, plotEndZ)));

            workloadRunnable.addWorkload(new MakeBarrierWall(voidWorld,
                    BlockVector3.at(plotEndX, minY, plotEndZ),
                    BlockVector3.at(plotEndX, maxY, borderStartZ)));

            gamePlot.setHasBorders(true);
        }

        int x_copy_end = x_copy_start + (plotSize-1);
        int z_copy_end = z_copy_start + (plotSize-1);

        // Queue terrain copy tasks
        int pasteStartX = (int) gamePlot.plotStart.getX();
        int pasteStartZ = (int) gamePlot.plotStart.getZ();
        GameLobby finalTaskSignal = null; // Used to trigger startGameCountdown

        pluginLogger.debug("Queueing terrain copy for plot at " + pasteStartX + "," + pasteStartZ + " from " + x_copy_start + "," + z_copy_start);

        GenerateGameTerrain terrainTask = new GenerateGameTerrain(
                x_copy_start,x_copy_end,
                z_copy_start,z_copy_end,
                pasteStartX, pasteStartZ);

        workloadRunnable.addWorkload(terrainTask);
        pluginLogger.debug("Finished queueing terrain copy tasks.");
        Bukkit.getScheduler().runTaskTimer(plugin, (task)->{
            if (terrainTask.isComplete()) {
                pluginLogger.debug("Terrain copy complete for plot at " + pasteStartX + "," + pasteStartZ);
                // Start pre-game countdown
                startPreGameCountdown();
                // Cancel this task
                task.cancel();
            }
        },0L,1L); // Dummy task to allow async tasks to finish
    }

    /**
     * Called by the last GenerateGameTerrain task to start the pre-game countdown.
     */

    public void startPreGameCountdown() {
        AtomicInteger countdown = new AtomicInteger(); // Reset countdown
        countdown.set(PRE_GAME_COUNTDOWN); // Set to pre-game countdown value
        playGameSound(Sound.ENTITY_PLAYER_LEVELUP);
        scheduler.runTaskTimer(plugin, (task) -> {
            pluginLogger.debug("Pre-game countdown: " + countdown.get());
            if (countdown.get() <= 0) {
                task.cancel();
                startGameCountdown();
                 // Teleport players and start timers
                return;
            }
            // Announce countdown
            announce("game.pre_countdown", Placeholder.unparsed("seconds", String.valueOf(countdown.get())));
            countdown.getAndDecrement();
        }, 0L, 20L); // Every second
    }

    private void startGameCountdown() {

        if (countdownTask != null && !countdownTask.isCancelled()) return; // Prevent double calls
        pluginLogger.debug("Starting game countdown for plot at " + gamePlot.plotStart.getBlockX() + "," + gamePlot.plotStart.getBlockZ());

        // save all player data before teleporting
        for (Player player : this.players) {
            if (player != null && player.isOnline()) {
                gameLobbyManager.savePlayerData(player);
            }
        }
        savePlayersInfo(); // Save inventory/stats and clear for game
        teleportPlayersToGame(); // Teleport players to lobby before countdown
        applyWorldBorder(); // Apply world border to the game plot
        checkWinCondition();

        this.setGameState(GameLobbyStates.STARTING);
        // AT THIS POINT THE GAME STATE IS STARTING
        // the plot is generated
        // players should be teleported to the plot
        // players are waiting for game start countdown
        // lobby region has the profile set to IDLE still

        // countdown task
        scheduler.runTaskTimer(plugin, (task) -> {
            if (countdown <= 0) {
                task.cancel();
                playGameSound(Sound.ENTITY_ENDER_DRAGON_GROWL);
                startGame(); // Teleport players and start timers
                return;
            }
            // Announce countdown
            announce("game.countdown", Placeholder.unparsed("seconds", String.valueOf(countdown)));
            if (countdown <= 3)
                playGameSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
            else
                // count down is
                playGameSound(Sound.BLOCK_DISPENSER_DISPENSE);
            countdown--;
        }, 0L, 20L); // Every second
    }

    /**
     * Called after the countdown finishes. Teleports players, sets up borders, starts game timers.
     */
    private void startGame() {
        gameON = true; // Mark game as officially started
        // check if players are still in the game

        // Check if any players remain after teleport attempts
        if (this.players.isEmpty()) {
            pluginLogger.warning("Game ending immediately as no players could be spawned in plot " + gamePlot.plotStart.toString());
            announce("general.error_generic", Placeholder.unparsed("details", "No players could be spawned safely!"));
            endGame(false); // End game immediately, no winner
            return;
        }

        showStartTitle();
        scheduler.runTaskTimer(plugin, (task) -> {
            if (!gameON) {
                task.cancel();
                return;
            }
            sendOutLavaLevel(); // Send out the current lava level to all players
        }, 0L, 20L); // Every second

        announce("game.started");
        if (MainConfig.getInstance().isGameMusicEnabled()) {
            runBackMusic();
        }


        // Remove the InviteLobby that started this game
        // planned update: Do not remove lobby, to allow players to play again right away

        this.FILRegionManager.setRegionProfile(gamePlot.worldGuardRegionId, RegionProfiles.BASE);
        // Start game mechanics timers
        beginLavaTimer();
        beginEventTimer(); // If ChaosEventManager is ready
        this.setGameState(GameLobbyStates.STARTED);
        checkWinCondition(); // Check if any player left the game
    }

    /** Starts the timer that potentially triggers chaos events. */
    public void beginEventTimer(){
        cancelTask(eventTask); // Cancel previous if any
        AtomicInteger eventSpawnChance = new AtomicInteger(0); // Start chance at 0
        Random rand = new Random();
        int minChance = 30; // TODO: Configurable
        int maxChance = 70; // TODO: Configurable
        int increment = 10; // TODO: Configurable
        long delay = 200L;   // TODO: Configurable (10 seconds)
        long period = 300L;  // TODO: Configurable (15 seconds)

        /*scheduler.runTaskTimer(plugin, (task) -> {
            if (!gameON){ task.cancel(); return; }

            int currentChance = eventSpawnChance.addAndGet(increment); // Increment first
            if(currentChance >= rand.nextInt(minChance, maxChance + 1)){ // Check if threshold met
                plugin.getLogger().info("Triggering Chaos Event for game at " + gamePlot.plotStart.toString());
                ChaosEventManager.eventate(this); // Call static method (needs refactor?)
                eventSpawnChance.set(0); // Reset chance
            }
        }, delay, period);
        */
    }

    /** Starts the timer that raises the lava level. */
    public void beginLavaTimer() {
        cancelTask(lavaTask); // Cancel previous if any
        long delay = 0L;  // TODO: Configurable (0 seconds)

        scheduler.runTaskTimer(plugin, (task) -> {
            if (!gameON) { task.cancel(); return; }

            placeLava(true); // Queue lava placement tasks

            // Increase height for next time (if game still on and not at max)
            if (gameON && lavaHeight < voidWorld.getMaxHeight() - 1) // Check against world max Y
                lavaHeight += LAVA_INCREMENT;

        }, delay, LAVA_RISE_COOLDOWN * 20L); // Convert seconds to ticks
    }

    /** Declares the winner and ends the game. */
    public void winPlayer(Player player) {
        if (!gameON || player == null) return; // Ensure game is active and player is valid

        gameON = false; // Mark game as ending *before* final actions

        playGameSound(Sound.UI_TOAST_CHALLENGE_COMPLETE);
        announce("game.win_broadcast", Placeholder.unparsed("player", player.getName()));

        // Send Title using Adventure API
        Component title = MiniMessages.getParsedComponent("game.win_title_top");
        Component subtitle = MiniMessages.getParsedComponent("game.win_title_bottom");
        // FadeIn(10 ticks), Stay(100 ticks), FadeOut(40 ticks) = 0.5s, 5s, 2s
        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(5), Duration.ofSeconds(2));
        Title fullTitle = Title.title(title, subtitle, times);

        player.showTitle(fullTitle);
    }

    /** Stops game timers, cleans up players, releases plot. */
    public void endGame(boolean fastCleanup) {
        if (!gameON && previousLocationList.isEmpty()) {
            // Avoid running endGame twice if already cleaning up
            return;
        }
        gameON = false; // Ensure game is marked off

        pluginLogger.debug("Ending game for plot at " + gamePlot.plotStart.toString());

        // Cancel all running tasks associated with this game instance
        cancelTask(countdownTask);
        cancelTask(musicTask);
        cancelTask(eventTask);
        cancelTask(lavaTask);

        // Announce end
        announce("game.game_over");
        scheduler.runTaskLater(plugin, () -> announce("game.teleport_back_notice"), 100L); // 5 seconds delay

        // Schedule final cleanup task
        if (fastCleanup) {
            returnPlayers();
        } else {
            // Delay cleanup to allow players to see end messages
            scheduler.runTaskLater(plugin, this::returnPlayers,8*20L);// 8 seconds total delay
        }

        // GAME IS NOW MARKED AS ENDING
        setGameState(GameLobbyStates.ENDING);
    }

    private void returnPlayers(){
        pluginLogger.debug("Cleaning up players for plot at " + gamePlot.plotStart.toString());
        // Use copies to allow 'remove' to modify original lists safely if needed
        ArrayList<Player> playersToClean = new ArrayList<>(this.players);
        ArrayList<Player> specsToClean = new ArrayList<>(this.specList);

        // Remove remaining players/spectators (teleport, restore state)
        for (Player p : playersToClean) {
            remove(p, false, false);
        }
        for (Player p : specsToClean) {
            remove(p, false, false);
        }

        // Release the plot (Mark as not in use)
        // --- TODO: Notify PlotManager if using one ---
        // plotManager.releasePlot(gamePlot);

        // --- TODO: Notify GameManager ---
        // gameManager.unregisterGame(this); // Use 'this' game lobby instance
        // ---

        // Clear lists stored in this instance
        this.players.clear();
        this.specList.clear();
        this.previousLocationList.clear();
        this.previousInventoryList.clear();
        this.previousHealthList.clear();
        this.previousHungerList.clear();
        this.previousXPList.clear();
        this.playerSpawnLocation.clear();
        LAVA_ANNOUNCE_HEIGHTS.clear(); // Clear announce heights
        this.flush(); // Clear the plot of blocks
        FILRegionManager.setRegionProfile(gamePlot.worldGuardRegionId, RegionProfiles.IDLE); // Reset region profile

        // Clear manual spawn data AFTER all players are processed and potentially teleported
        this.manualSpawnPoints.clear();
        this.manualSpawnItemUsed.clear();
        pluginLogger.debug("Cleared manual spawn data for game at " + gamePlot.plotStart.toString());
        
    }

    private void flush(){
        // get worldhieght
        int worldHeight = voidWorld.getMaxHeight();
        int worldMinHeight = voidWorld.getMinHeight();
        boolean lastTask = false; // Flag to indicate if this is the last task
        // for loop from worldHeight to 0 (0 should be in CONFIG)
        for (int yLevel = worldHeight; yLevel >= worldMinHeight; yLevel--) {

            // if it's last task, set lastTask to true
            if (yLevel == worldMinHeight) {
                lastTask = true;
            }

            // clear the y level
            workloadRunnable.addWorkload(new FlushGamePlot(this,yLevel, lastTask));
        }
    }

    public void flushDone() {
        gamePlot.setInUse(false);
        setGameState(GameLobbyStates.INACTIVE);
        pluginLogger.debug("Plot released: " + gamePlot.plotStart.toString());
    }

    public void shutdown() {
        // Cancel all tasks
        cancelTask(countdownTask);
        cancelTask(musicTask);
        cancelTask(eventTask);
        cancelTask(lavaTask);

        // Clean up players
        for (Player p : this.players) {
            if (p != null && p.isOnline()) {
                remove(p, false, false); // Remove player from game
            }
        }
        for (Player p : this.specList) {
            if (p != null && p.isOnline()) {
                remove(p, false, false); // Remove spectator from game
            }
        }

        endGame(false); // End game without a winner

        // Release the plot
        this.gamePlot.setInUse(false);
        pluginLogger.debug("Plot released: " + gamePlot.plotStart.toString());
    }

    public GameLobbyStates getGameState() {
        return gameState;
    }

    private void setGameState(GameLobbyStates gameState) {
        this.gameState = gameState;
    }

    private void teleportPlayersToGame() {
        Iterator<Player> playerIterator = this.players.iterator();
        while(playerIterator.hasNext()){
            Player player = playerIterator.next();
            if(player == null || !player.isOnline()){ // Check if player left during setup
                playerIterator.remove(); // Remove from game list
                previousLocationList.remove(player); // Remove stored data
                continue;
            }

            // Use Tools for safe location finding
            Location gameLoc = Tools.getSafeLocation(voidWorld, gamePlot);
            if (gameLoc == null) {
                pluginLogger.severe("Could not find safe spawn for " + player.getName() + " in plot " + gamePlot.plotStart.toString() + ". Removing from game.");
                MiniMessages.send(player, "general.error_generic", Placeholder.unparsed("details", "Could not find a safe spawn point!")); // Notify player
                // Keep player's original state (don't call savePlayerInfo)
                playerIterator.remove(); // Remove from game
                previousLocationList.remove(player);
                // Don't teleport if no safe spot found
                continue;
            }
            // set region to the wished Profile
            player.teleport(gameLoc);
            playerSpawnLocation.put(player, gameLoc);
        }

        // Check if any players remain after teleport attempts
        if (this.players.isEmpty()) {
            pluginLogger.warning("Game ending immediately as no players could be spawned in plot " + gamePlot.plotStart.toString());
            announce("general.error_generic", Placeholder.unparsed("details", "No players could be spawned safely!"));
            endGame(false); // End game immediately, no winner
        }
    }

    /** Saves player state and prepares them for the game. */
    public void savePlayersInfo() {
        // Store previous state
        // Note: previousLocationList is filled in constructor
        for (Player player : this.players) {
            if (player != null && player.isOnline()) {
                previousInventoryList.put(player, player.getInventory().getContents().clone()); // Clone arrays
                previousHealthList.put(player, player.getHealth());
                previousHungerList.put(player, player.getFoodLevel());
                previousXPList.put(player, player.getExp());

                // Clear and reset for game
                player.getInventory().clear();
                player.setHealth(player.getMaxHealth()); // Set to max health
                player.setFoodLevel(20);
                player.setExp(0);
                player.setLevel(0);
                // Clear potion effects?
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    player.removePotionEffect(effect.getType());
                }
                // Set gamemode? (Should be survival)
                player.setGameMode(GameMode.SURVIVAL);

                // Give manual spawn item if enabled
                if (MainConfig.getInstance().isManualSpawnEnabled()) {
                    giveManualSpawnItem(player);
                }
            }
        }
    }

    /** Restores player state after leaving/game end. */
    public void returnPlayerInfo(Player player) {
        if (gameLobbyManager.restorePlayerData(player)) {
            pluginLogger.debug("Restored player data from save files for: " + player.getName());
        } else {

            if (player == null || !player.isOnline()) {
                pluginLogger.warning("Player is null or offline, cannot restore state for: " + player.getName());
                return;
            }

            if (previousInventoryList.containsKey(player)) {
                player.getInventory().setContents(previousInventoryList.get(player));
            } else {
                player.getInventory().clear(); // Clear if no saved inventory
            }
            player.setHealth(previousHealthList.getOrDefault(player, player.getMaxHealth())); // Default to max health
            player.setFoodLevel(previousHungerList.getOrDefault(player, 20));
            player.setExp(previousXPList.getOrDefault(player, 0.0f));
            player.setLevel(0); // Reset level

            // Clear potentially game-specific effects
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            // Reset gamemode if needed (should be handled by server default on world change usually)
            player.setGameMode(Bukkit.getDefaultGameMode());
            // Clear world border applied by game
            player.setWorldBorder(null);

            // Clean up stored data for the player
            previousInventoryList.remove(player);
            previousHealthList.remove(player);
            previousHungerList.remove(player);
            previousXPList.remove(player);
            playerSpawnLocation.remove(player);
            // previousLocationList is cleared in endGame
        }
    }

    private void applyWorldBorder() {
        // Setup world border
        gameBorder = Bukkit.createWorldBorder(); // Consider per-world border if API allows
        int plotSize = plotDivider.plotSize;
        double centerX = gamePlot.plotStart.getX() + (plotSize / 2.0);
        double centerZ = gamePlot.plotStart.getZ() + (plotSize / 2.0);
        gameBorder.setCenter(centerX, centerZ); // Set center using X, Z
        gameBorder.setSize(plotSize);
        gameBorder.setDamageBuffer(1.0); // Buffer distance
        gameBorder.setDamageAmount(0.5); // Damage per second outside
        gameBorder.setWarningDistance(5);
        gameBorder.setWarningTime(10); // Seconds for warning screen

        for (Player player : this.players) {
            if (player != null && player.isOnline()) {
                player.setWorldBorder(gameBorder); // Apply border to each player
            }
        }
    }
    /** Starts background music task. */
    public void runBackMusic() {
        cancelTask(musicTask); // Cancel previous if any
        scheduler.runTaskTimer(plugin, playBackSongTask -> {
            if (gameON)
                playGameSound(Sound.MUSIC_DISC_PIGSTEP);
            else playBackSongTask.cancel();
        }, 0L, 2900L); // Pigstep duration ~2900 ticks. Maybe use config value.
    }

    /** Announces a message key to all players and spectators using MiniMessages. */
    public void announce(String messageKey) {
        announce(messageKey, TagResolver.empty());
    }

    /** Announces a message key with placeholders to all players and spectators using MiniMessages. */
    public void announce(String messageKey, TagResolver placeholders) {
        // Send to alive players
        for (Player player : this.players) {
            if (player != null && player.isOnline()) { // Check online
                MiniMessages.send(player, messageKey, placeholders);
            }
        }
        // Send to spectators
        for (Player spectator : specList) {
            if (spectator != null && spectator.isOnline()) { // Check online
                MiniMessages.send(spectator, messageKey, placeholders);
            }
        }
    }

    /** Plays a sound for all players and spectators. */
    public void playGameSound(Sound sound) {
        for (Player player : this.players) {
            if (player != null && player.isOnline()) {
                player.playSound(player.getLocation(), sound, SoundCategory.MASTER, 15, 1); // Use category, adjust volume/pitch
            }
        }
        for (Player player : specList) {
            if (player != null && player.isOnline()) {
                player.playSound(player.getLocation(), sound, SoundCategory.MASTER, 15, 1);
            }
        }
    }

    private void playSoundSpecificPlayer(Sound sound, Player player) {
        if (player != null && player.isOnline()) {
            player.playSound(player.getLocation(), sound, SoundCategory.MASTER, 15, 1);
        }
    }

    /** Queues workload tasks to place lava at the current height. */
    public void placeLava(boolean checkForPlayersUnderLava) {
        // Determine Y range to fill (ensure previous level is filled, fill up to new level)
        int startY = Math.max(voidWorld.getMinHeight()+1, lavaHeight - LAVA_INCREMENT); // Don't go below world min
        int endY = Math.min(voidWorld.getMaxHeight(), lavaHeight + LAVA_INCREMENT); // Don't exceed world max

        // Queue tasks via injected WorkloadRunnable
        for (int y = startY; y < endY; y++) {
            workloadRunnable.addWorkload(new ElevateLava(gamePlot, y));
        }

        // Check for announcement heights (use iterator for safe removal)
        Iterator<Integer> iterator = LAVA_ANNOUNCE_HEIGHTS.iterator();
        while(iterator.hasNext()){
            Integer y_height = iterator.next();
            if(y_height <= lavaHeight){
                TagResolver heightPlaceholder = TagResolver.resolver(
                        Placeholder.unparsed("y_level", String.valueOf(lavaHeight))
                );
                announce("game.lava_warning", heightPlaceholder);
                playGameSound(Sound.BLOCK_LAVA_AMBIENT);
                iterator.remove(); // Remove safely
                // Only announce one level per lava rise cycle
                break;
            }
        }

        if (checkForPlayersUnderLava) {
            // Check for players under lava level
            for (Player player : this.players) {
                if (player != null && player.isOnline()) {
                    Location playerLoc = player.getLocation();
                    if (playerLoc.getBlockY() <= lavaHeight) {
                        // Player is under lava, handle removal
                        pluginLogger.debug("Player " + player.getName() + " is under lava at Y: " + playerLoc.getBlockY());
                        refillZone(playerLoc); // Refill the zone under the player
                    }
                }
            }
        }
    }

    private void showStartTitle() {
        // Show title to all players
        for (Player player : this.players) {
            if (player != null && player.isOnline()) {
                Component title = MiniMessages.getParsedComponent("game.start_title_top");
                Component subtitle = MiniMessages.getParsedComponent("game.start_title_bottom");
                Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofSeconds(1));
                Title fullTitle = Title.title(title, subtitle, times);
                player.showTitle(fullTitle);
            }
        }
    }

    private void sendOutLavaLevel() {
        for (Player player : this.players) {
            if (player != null && player.isOnline()) {
                player.sendActionBar(
                        MiniMessages.miniM.deserialize("<gold>Lava is <bold><red>" + (player.getLocation().getBlockY() - lavaHeight) + "</red></bold><gold> blocks below you!")
                );
            }
        }
    }

    private void refillZone(Location playerLoc) {
        int startY = playerLoc.getBlockY() - 1; // Start just below player
        int endY = playerLoc.getBlockY() + 2; // End just above player height

        // Queue tasks via injected WorkloadRunnable
        for (int y = startY; y < endY; y++) {
            workloadRunnable.addWorkload(new ElevateLava(gamePlot, y));
        }
    }

    /**
     * Removes a player from the game, either due to leaving or dying by lava.
     * Handles state cleanup, messaging, spectating, and win condition checks.
     * NOTE: This method needs to coordinate with GameManager to update player state maps.
     * @param leavingPlayer The player to remove.
     * @param died          True if removed due to lava death, false otherwise (leave/disconnect/kick).
     */
    public void remove(Player leavingPlayer, boolean died, boolean disconnected) {
        if (leavingPlayer == null) return; // Safety check

        TagResolver playerPlaceholder = TagResolver.resolver(Placeholder.unparsed("player", leavingPlayer.getName()));

        // --- TODO: Notify GameManager ---
        // gameManager.removePlayerFromGameMap(leavingPlayer.getUniqueId()); // Use UUID
        // ---

        boolean wasPlayer = this.players.remove(leavingPlayer); // Remove from alive list
        boolean wasSpectator = this.specList.remove(leavingPlayer); // Remove from spectator list

        if (!died) { // Player left voluntarily / disconnected / game ended / kicked
            // Announce to others only if they were an active player or spectator
            if(wasPlayer || wasSpectator) {
                announce("game.player_left_game", playerPlaceholder);
            }

            // Send specific message only to leaving player if they were playing/spectating
            MiniMessages.send(leavingPlayer, "game.self_left_game"); // Add key: "<gray>You have left the game.</gray>"

            // Restore info and teleport only if game is still technically running OR called by endGame
            // (Check previousLocationList as indicator for endGame cleanup phase)
            if (gameON || previousLocationList.containsKey(leavingPlayer)) {
                Location previousLoc = previousLocationList.get(leavingPlayer);
                if (previousLoc != null) {
                    // Ensure world is loaded before teleport? Usually fine if coming from game world.
                    leavingPlayer.teleport(previousLoc);
                } else {
                    pluginLogger.warning("Previous location missing for " + leavingPlayer.getName() + " on game leave.");
                    // Consider teleporting to main world spawn as fallback
                    // player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                }
                // Only restore info if they were an active player, not spectator already

                if (!disconnected)
                    returnPlayerInfo(leavingPlayer);
                if(wasSpectator) {
                    // Ensure spectator is set back to default gamemode, border removed
                    leavingPlayer.setGameMode(Bukkit.getDefaultGameMode());
                    leavingPlayer.setWorldBorder(null);
                }
            }
            // Clean up any remaining data for the player now
            previousLocationList.remove(leavingPlayer);
            previousInventoryList.remove(leavingPlayer);
            previousHealthList.remove(leavingPlayer);
            previousHungerList.remove(leavingPlayer);
            previousXPList.remove(leavingPlayer);
            playerSpawnLocation.remove(leavingPlayer);

        } else { // Player died by lava (or other fatal cause routed here)
            this.specList.add(leavingPlayer); // Add to spectators
            announce("game.lava_death", playerPlaceholder);
            MiniMessages.send(leavingPlayer, "game.became_spectator");
            leavingPlayer.playSound(leavingPlayer.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 1);
            leavingPlayer.setHealth(leavingPlayer.getMaxHealth()); // Restore health for spectator mode
            leavingPlayer.setFoodLevel(20); // Restore food
            leavingPlayer.setGameMode(GameMode.SPECTATOR);
            // Keep border applied for spectator? Or remove? Let's keep it for now.
            // Optional: Teleport spectator to a viewing spot
            // Location specSpawn = gameBorder.getCenter().add(0, 20, 0);
            // leavingPlayer.teleport(specSpawn);
        }

        // Check win condition ONLY if the game is still running
        checkWinCondition(); // Check if any players left in game
    }

    private void checkWinCondition() {
        if (gameON) {
            if (this.players.size() == 1) {
                // We have a winner!
                winPlayer(this.players.get(0));
                endGame(false); // Trigger final cleanup
            } else if (this.players.isEmpty()) {
                // Last player(s) left/died simultaneously?
                announce("game.no_winner"); // Add key: "<yellow>Everyone was eliminated! No winner.</yellow>"
                endGame(false); // End without a winner
            }
        }
    }

    // New method to give the manual spawn item
    private void giveManualSpawnItem(Player player) {
        if (manualSpawnItemUsed.contains(player.getUniqueId())) {
            return; // Don't give if already used (e.g., if this method was called again for some reason)
        }

        MainConfig config = MainConfig.getInstance();
        Material itemMaterial = Material.matchMaterial(config.getManualSpawnItemMaterial());
        if (itemMaterial == null) {
            pluginLogger.warning("Invalid material for manual spawn item: " + config.getManualSpawnItemMaterial() + ". Defaulting to RED_BED.");
            itemMaterial = Material.RED_BED;
        }

        ItemStack spawnAnchorItem = new ItemStack(itemMaterial, 1);
        ItemMeta meta = spawnAnchorItem.getItemMeta();
        if (meta != null) {
            meta.displayName(MiniMessages.miniM.deserialize(config.getManualSpawnItemName()));
            List<Component> loreComponents = config.getManualSpawnItemLore().stream()
                    .map(MiniMessages.miniM::deserialize)
                    .collect(Collectors.toList());
            meta.lore(loreComponents);

            // Add NBT tag to identify the item
            meta.getPersistentDataContainer().set(FloorIsLava.RESPAWN_ANCHOR_KEY, PersistentDataType.BYTE, (byte) 1);

            if (config.isManualSpawnItemIsGlowing()) {
                meta.addEnchant(Enchantment.LURE, 1, false); // Dummy enchant for glow
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES); // Hide attributes like "When placed..." for beds
            spawnAnchorItem.setItemMeta(meta);
        }
        player.getInventory().addItem(spawnAnchorItem);
    }
    
    // helper method
    public boolean isLocationSafeForRespawn(Location loc) {
        if (loc == null) return false;
        World world = loc.getWorld();
        if (world == null) return false;

        // Check if above lava
        if (loc.getBlockY() <= lavaHeight) return false;

        // Check block at feet and head height for solidity
        // Ensure there's a 2-block high air gap for the player
        Block blockAtFeet = loc.getBlock();
        Block blockAtHead = loc.clone().add(0, 1, 0).getBlock();

        // Check for sufficient space (2 blocks high, 1 block wide)
        if (blockAtFeet.getType().isSolid() || blockAtHead.getType().isSolid()) {
            pluginLogger.debug("Respawn location " + loc.toString() + " unsafe: feet or head in solid block.");
            return false; // Spawning inside a solid block
        }
        
        // Optional: check block below feet for solid ground
        // Block blockBelowFeet = loc.clone().subtract(0,1,0).getBlock();
        // if (!blockBelowFeet.getType().isSolid() && blockBelowFeet.getType() != Material.WATER && blockBelowFeet.getType() != Material.LAVA) {
        //     pluginLogger.debug("Respawn location " + loc.toString() + " unsafe: no solid ground directly below.");
        //     return false; // No solid ground to stand on
        // }

        return true; // Location seems safe enough
    }

    /** Handles player deaths not caused by the main lava mechanic. */
    public void playerDiedNoLava(Player player) {
        if (player == null || !this.players.contains(player)) return; // Ensure player is actually alive in this game
        announce("game.other_death", Placeholder.unparsed("player", player.getName()));

        ItemStack manualSpawnItemToRestore = null;
        // Special handling for keeping the manual spawn item if unused
        if (MainConfig.getInstance().isManualSpawnEnabled() && !manualSpawnItemUsed.contains(player.getUniqueId())) {
            for (int i = 0; i < player.getInventory().getSize(); i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (item != null && item.hasItemMeta()) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta.getPersistentDataContainer().has(FloorIsLava.RESPAWN_ANCHOR_KEY, PersistentDataType.BYTE)) {
                        manualSpawnItemToRestore = item.clone();
                        player.getInventory().clear(i); // Remove it from this specific slot
                        pluginLogger.debug("Temporarily removed Respawn Anchor for " + player.getName() + " before non-lava death drop processing.");
                        break;
                    }
                }
            }
        }

        // Drop items at death location
        // each item has a 50% chance to drop
        ItemStack[] inv = player.getInventory().getContents().clone(); // Clone before clearing
        for (ItemStack is : inv) {
            if (is != null && is.getType() != Material.AIR) {
                if (Math.random() < DEATH_ITEM_DROP_CHANCE) { // 50% chance to drop
                    voidWorld.dropItemNaturally(player.getLocation(), is); // Drop naturally
                    // remove the item from the inventory
                    player.getInventory().remove(is); // Remove from inventory
                }
            }
        }
        MiniMessages.send(player, "game.death_items_dropped"); // Add key: "<gray>Your items have been dropped.</gray>"

        /*ItemStack[] inv = player.getInventory().getContents().clone(); // Clone before clearing
        player.getInventory().clear();
        Location deathLoc = player.getLocation();
        for (ItemStack is : inv) {
            if(is != null && is.getType() != Material.AIR)
                voidWorld.dropItemNaturally(deathLoc, is); // Drop naturally
        }*/
        player.setExp(0); // Drop XP? Standard death does.
        player.setLevel(0);

        // Respawn Logic
        Location respawnLocation = null;

        if (MainConfig.getInstance().isManualSpawnEnabled() && manualSpawnPoints.containsKey(player.getUniqueId())) {
            Location manualSpawn = manualSpawnPoints.get(player.getUniqueId());
            if (isLocationSafeForRespawn(manualSpawn)) {
                respawnLocation = manualSpawn;
                pluginLogger.debug("Using manual respawn point for " + player.getName() + " at " + respawnLocation);
            } else {
                MiniMessages.send(player, "game.manual_spawn_unsafe");
                pluginLogger.debug("Manual respawn point for " + player.getName() + " at " + manualSpawn + " is unsafe (lava: " + lavaHeight + "). Falling back.");
            }
        }

        if (respawnLocation == null) { // fallback to 'automatic' plot spawn
            respawnLocation = playerSpawnLocation.get(player);
            if (respawnLocation != null && !isLocationSafeForRespawn(respawnLocation)) {
                    pluginLogger.debug("Original plot spawn for " + player.getName() + " at " + respawnLocation + " is unsafe. Finding new.");
                    respawnLocation = null; // Mark as unsafe to trigger Tools.getSafeLocation
            } else if (respawnLocation != null) {
                    pluginLogger.debug("Using original plot spawn for " + player.getName() + " at " + respawnLocation);
            }
        }
        
        if (respawnLocation == null) {
            pluginLogger.warning("Original/Manual spawn location missing or unsafe for " + player.getName() + ", finding new safe spot...");
            respawnLocation = Tools.getSafeLocation(voidWorld, gamePlot);
            pluginLogger.debug("Found new safe spot for " + player.getName() + " at " + respawnLocation);
        }

        // capture the state of manualSpawnItemToRestore
        final ItemStack finalManualSpawnItemToRestore = manualSpawnItemToRestore;

        if (respawnLocation == null) {
            pluginLogger.severe("Could not find ANY safe spawn for " + player.getName() + " after non-lava death! Removing from game.");
            remove(player, false, false); // Kick if cannot respawn
            // If manualSpawnItemToRestore was set, it's lost as player is removed.
            // Consider dropping it at their death location if this happens.
            if (finalManualSpawnItemToRestore != null) {
                    voidWorld.dropItemNaturally(player.getLocation(), finalManualSpawnItemToRestore);
                    pluginLogger.debug("Dropped Respawn Anchor for " + player.getName() + " as they were removed due to no safe spawn.");
            }
            return;
        }

        // Teleport and restore state
        final Location finalRespawnLocation = respawnLocation;
        scheduler.runTask(plugin, () -> {
            player.teleport(finalRespawnLocation);
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            player.setFireTicks(0); // Clear fire
            for (PotionEffect effect : player.getActivePotionEffects()) { // Clear potions
                player.removePotionEffect(effect.getType());
            }
            player.setNoDamageTicks(60); // 3 seconds invulnerability
            playSoundSpecificPlayer(Sound.BLOCK_ANVIL_LAND, player);

            // Restore the manual spawn item if it was temporarily held and player wasn't kicked
            if (finalManualSpawnItemToRestore != null) {
                player.getInventory().addItem(finalManualSpawnItemToRestore);
                pluginLogger.debug("Restored Respawn Anchor to " + player.getName() + " after non-lava death respawn.");
            }
        });
    }
    // Helper to safely cancel tasks
    private void cancelTask(BukkitTask task) {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }
} // End of GameLobby class