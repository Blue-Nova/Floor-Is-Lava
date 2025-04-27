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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
// Bukkit Imports
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents an active "Floor is Lava" game session.
 * Manages players, spectators, the game plot, lava level, and game lifecycle events.
 */
public class GameLobby extends Lobby {

    // --- Injected Dependencies ---
    private final Plugin plugin; // Main plugin instance (for scheduler, config access)
    private final PluginLogger pluginLogger; // Logger for this class
    private final InviteLobbyManager inviteLobbyManager;
    private final WorkloadRunnable workloadRunnable;
    private final GamePlotDivider plotDivider;
    private final World voidWorld;
    private final BukkitScheduler scheduler;
    private final FILRegionManager FILRegionManager; // WorldGuard region manager
    // --- End Dependencies ---

    // Game State & Configuration
    private final List<Integer> LAVA_ANNOUNCE_HEIGHTS = new ArrayList<>();
    private static final int LAVA_INCREMENT = MainConfig.getInstance().getLavaRiseAmount(); // TODO: Make configurable
    private static final int GAME_START_COUNTDOWN = MainConfig.getInstance().getGameStartCountdown(); // TODO: Make configurable
    private static final int LAVA_RISE_COOLDOWN = MainConfig.getInstance().getLavaRiseCooldown(); // TODO: Make configurable
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

    /**
     * Creates a new Game Lobby instance.
     * Should be called by a GameManager or similar service.
     */
    public GameLobby(Plugin plugin, PluginLogger pluginLogger, ArrayList<Player> playerList, Player owner,
                     InviteLobbyManager inviteLobbyManager,
                     WorkloadRunnable workloadRunnable, GamePlotDivider plotDivider,
                     World voidWorld, FILRegionManager filRegionManager,
                     GamePlot plot) {

        super(playerList, owner); // Initialize inherited 'players' and 'owner'

        // Store injected dependencies
        this.plugin = plugin;

        this.pluginLogger = pluginLogger;

        this.inviteLobbyManager = inviteLobbyManager;
        // this.gameManager = gameManager; // Removed
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
        // Pass 'this' (GameLobby) to FindAllowedLocation if it needs to call back generatePlot
        workloadRunnable.addWorkload(new FindAllowedLocation(this));
    }

    /**
     * Called by FindAllowedLocation Workload task once source coords (x, z) are found.
     * Queues tasks to build barrier walls and copy terrain columns.
     */
    public void generatePlot(int sourceX, int sourceZ) {
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
            for (int y = minY; y < maxY; y++) {
                // Use plot End X/Z which are likely +size from start
                workloadRunnable.addWorkload(new MakeBarrierWall(borderStartX, borderStartZ, plotEndX, borderStartZ, y, voidWorld));     // Side 1 (Z constant)
                workloadRunnable.addWorkload(new MakeBarrierWall(borderStartX, borderStartZ + 1, borderStartX, plotEndZ - 1, y, voidWorld)); // Side 2 (X constant)
                workloadRunnable.addWorkload(new MakeBarrierWall(plotEndX, borderStartZ + 1, plotEndX, plotEndZ - 1, y, voidWorld));       // Side 3 (X constant)
                workloadRunnable.addWorkload(new MakeBarrierWall(borderStartX + 1, plotEndZ, plotEndX - 1, plotEndZ, y, voidWorld));         // Side 4 (Z constant) - Adjust corners if needed
            }
            gamePlot.setHasBorders(true);
        }

        // Queue terrain copy tasks
        int pasteStartX = (int) gamePlot.plotStart.getX();
        int pasteStartZ = (int) gamePlot.plotStart.getZ();
        GameLobby finalTaskSignal = null; // Used to trigger startGameCountdown

        pluginLogger.debug("Queueing terrain copy for plot at " + pasteStartX + "," + pasteStartZ + " from " + sourceX + "," + sourceZ);
        for (int x_index = 0; x_index < plotSize; x_index++) {
            for (int z_index = 0; z_index < plotSize; z_index++) {
                // Assign 'this' only to the very last task in the grid
                if (x_index == plotSize - 1 && z_index == plotSize - 1) {
                    finalTaskSignal = this;
                }
                // Pass dependencies needed by GenerateGameTerrain constructor
                workloadRunnable.addWorkload(new GenerateGameTerrain(
                        finalTaskSignal,
                        sourceX + x_index, sourceZ + z_index, // Source coords
                        pasteStartX + x_index, pasteStartZ + z_index // Paste coords
                        // Pass worlds needed
                ));
            }
        }
        pluginLogger.debug("Finished queueing terrain copy tasks.");
    }

    /**
     * Called by the last GenerateGameTerrain task to start the pre-game countdown.
     */
    public void startGameCountdown() {
        if (countdownTask != null && !countdownTask.isCancelled()) return; // Prevent double calls
        pluginLogger.debug("Starting game countdown for plot at " + gamePlot.plotStart.getBlockX() + "," + gamePlot.plotStart.getBlockZ());

        scheduler.runTaskTimer(plugin, (task) -> {
            if (countdown <= 0) {
                task.cancel();
                actuallyStartGame(); // Teleport players and start timers
                return;
            }
            // Announce countdown
            announce("game.countdown", Placeholder.unparsed("seconds", String.valueOf(countdown)));
            playGameSound(Sound.BLOCK_NOTE_BLOCK_PLING); // Example sound
            countdown--;
        }, 0L, 20L); // Every second
    }

    /**
     * Called after the countdown finishes. Teleports players, sets up borders, starts game timers.
     */
    private void actuallyStartGame() {
        gameON = true; // Mark game as officially started
        announce("game.teleporting");

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

        // Teleport players and setup
        // Use iterator for safe removal if teleport fails
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
            this.FILRegionManager.setRegionProfile(gamePlot.worldGuardRegionId, RegionProfiles.BASE);

            player.teleport(gameLoc);
            playerSpawnLocation.put(player, gameLoc);
            player.setWorldBorder(gameBorder); // Apply border specific to this game
            savePlayerInfo(player); // Save inventory/stats and clear for game
        }


        // Check if any players remain after teleport attempts
        if (this.players.isEmpty()) {
            pluginLogger.warning("Game ending immediately as no players could be spawned in plot " + gamePlot.plotStart.toString());
            announce("general.error_generic", Placeholder.unparsed("details", "No players could be spawned safely!"));
            endGame(false); // End game immediately, no winner
            return;
        }

        announce("game.started");
        runBackMusic();

        // Remove the InviteLobby that started this game
        InviteLobby originatingLobby = inviteLobbyManager.getLobbyFromOwner(this.owner);
        if (originatingLobby != null) {
            inviteLobbyManager.closeLobby(originatingLobby);
            pluginLogger.debug("Closed invite lobby for owner: " + this.owner.getName());
        } else {
            pluginLogger.warning("Could not find original invite lobby for owner: " + this.owner.getName() + " to close.");
        }


        // Start game mechanics timers
        beginLavaTimer();
        beginEventTimer(); // If ChaosEventManager is ready
    }

    /** Saves player state and prepares them for the game. */
    public void savePlayerInfo(Player player) {
        // Store previous state
        // Note: previousLocationList is filled in constructor
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
    }

    /** Restores player state after leaving/game end. */
    public void returnPlayerInfo(Player player) {
        // Check if data exists before trying to restore
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

    /** Starts background music task. */
    public void runBackMusic() {
        cancelTask(musicTask); // Cancel previous if any
         scheduler.runTaskTimer(plugin, playBackSongTask -> {
            if (gameON)
                playGameSound(Sound.MUSIC_DISC_PIGSTEP);
            else playBackSongTask.cancel();
        }, 0L, 2900L); // Pigstep duration ~2:48 = 3360 ticks? Maybe use config value.
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
        if (gameBorder == null) return; // Safety check
        Location center = gameBorder.getCenter();
        for (Player player : this.players) {
            if (player != null && player.isOnline()) {
                player.playSound(center, sound, SoundCategory.MASTER, 40, 1); // Use category, adjust volume/pitch
            }
        }
        for (Player player : specList) {
            if (player != null && player.isOnline()) {
                player.playSound(center, sound, SoundCategory.MASTER, 40, 1);
            }
        }
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

            placeLava(); // Queue lava placement tasks

            // Increase height for next time (if game still on and not at max)
            if (gameON && lavaHeight < voidWorld.getMaxHeight() - 1) // Check against world max Y
                lavaHeight += LAVA_INCREMENT;

        }, delay, LAVA_RISE_COOLDOWN * 20L); // Convert seconds to ticks
    }

    /** Queues workload tasks to place lava at the current height. */
    public void placeLava() {
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
    }

    /**
     * Removes a player from the game, either due to leaving or dying by lava.
     * Handles state cleanup, messaging, spectating, and win condition checks.
     * NOTE: This method needs to coordinate with GameManager to update player state maps.
     * @param leavingPlayer The player to remove.
     * @param died          True if removed due to lava death, false otherwise (leave/disconnect/kick).
     */
    public void remove(Player leavingPlayer, boolean died) {
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
            if(wasPlayer || wasSpectator) {
                MiniMessages.send(leavingPlayer, "game.self_left_game"); // Add key: "<gray>You have left the game.</gray>"
            }

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
                if(wasPlayer){
                    returnPlayerInfo(leavingPlayer);
                } else {
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
            leavingPlayer.playSound(leavingPlayer.getLocation(), Sound.ENTITY_PLAYER_BURP, 1, 1);
            leavingPlayer.setHealth(leavingPlayer.getMaxHealth()); // Restore health for spectator mode
            leavingPlayer.setFoodLevel(20); // Restore food
            leavingPlayer.setGameMode(GameMode.SPECTATOR);
            // Keep border applied for spectator? Or remove? Let's keep it for now.
            // Optional: Teleport spectator to a viewing spot
            // Location specSpawn = gameBorder.getCenter().add(0, 20, 0);
            // leavingPlayer.teleport(specSpawn);
        }

        // Check win condition ONLY if the game is still running
        if (gameON) {
            if (this.players.size() == 1) {
                // We have a winner!
                winPlayer(this.players.get(0));
            } else if (this.players.isEmpty()) {
                // Last player(s) left/died simultaneously?
                announce("game.no_winner"); // Add key: "<yellow>Everyone was eliminated! No winner.</yellow>"
                endGame(false); // End without a winner
            }
        }
    }

    /** Handles player deaths not caused by the main lava mechanic. */
    public void playerDiedNoLava(Player player) {
        if (player == null || !this.players.contains(player)) return; // Ensure player is actually alive in this game

        announce("game.other_death", Placeholder.unparsed("player", player.getName()));

        // Drop items at death location
        ItemStack[] inv = player.getInventory().getContents().clone(); // Clone before clearing
        player.getInventory().clear();
        Location deathLoc = player.getLocation();
        for (ItemStack is : inv) {
            if(is != null && is.getType() != Material.AIR)
                voidWorld.dropItemNaturally(deathLoc, is); // Drop naturally
        }
        player.setExp(0); // Drop XP? Standard death does.
        player.setLevel(0);

        // Teleport back to spawn point within the game plot
        Location spawn = playerSpawnLocation.get(player);
        if (spawn == null) {
            plugin.getLogger().warning("Spawn location missing for " + player.getName() + ", finding new safe spot...");
            spawn = Tools.getSafeLocation(voidWorld, gamePlot);
        }

        if (spawn != null) {
            // Teleport async for potentially unloaded chunks? Safer maybe.
            player.teleport(spawn);
            // Restore health/food AFTER teleport completes
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            // Apply brief invulnerability?
            player.setNoDamageTicks(60); // 3 seconds
        } else {
            plugin.getLogger().severe("Could not find any safe spawn for " + player.getName() + " after non-lava death!");
            remove(player, false); // Kick if cannot respawn
        }
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

        ((FloorIsLava)plugin).adventure().player(player).showTitle(fullTitle);

        endGame(true); // Trigger final cleanup
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

    }

    private void returnPlayers(){
        pluginLogger.debug("Cleaning up players for plot at " + gamePlot.plotStart.toString());
        // Use copies to allow 'remove' to modify original lists safely if needed
        ArrayList<Player> playersToClean = new ArrayList<>(this.players);
        ArrayList<Player> specsToClean = new ArrayList<>(this.specList);

        // Remove remaining players/spectators (teleport, restore state)
        for (Player p : playersToClean) {
            remove(p, false);
        }
        for (Player p : specsToClean) {
            remove(p, false);
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
    }

    // Helper to safely cancel tasks
    private void cancelTask(BukkitTask task) {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    private void flush(){
        // get worldhieght
        int worldHeight = voidWorld.getMaxHeight();
        boolean lastTask = false; // Flag to indicate if this is the last task
        // for loop from worldHeight to 0 (0 should be in CONFIG)
        for (int yLevel = worldHeight; yLevel >= voidWorld.getMinHeight()+1; yLevel--) {

            // if it's last task, set lastTask to true
            if (yLevel == voidWorld.getMinHeight() + 1) {
                lastTask = true;
            }

            // clear the y level
            workloadRunnable.addWorkload(new FlushGamePlot(this,yLevel, lastTask));
        }
    }

    public void flushDone() {
        gamePlot.setInUse(false);
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
                remove(p, false); // Remove player from game
            }
        }
        for (Player p : this.specList) {
            if (p != null && p.isOnline()) {
                remove(p, false); // Remove spectator from game
            }
        }

        endGame(false); // End game without a winner

        // Release the plot
        this.gamePlot.setInUse(false);
        pluginLogger.debug("Plot released: " + gamePlot.plotStart.toString());
    }
} // End of GameLobby class