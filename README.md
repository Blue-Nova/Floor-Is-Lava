# **Floor Is Lava**
This is the official "Floor is Lava" github repo. Made by BlueNova.

## What is Floor Is Lava?
Floor is Lava is a minigame plugin for Minecraft that allows players to compete in a fun and exciting game of survival.
The objective of the game is to stay alive on a floating platform while avoiding the lava that rises from below.
Players must use their skills and strategy to quickly collect resources, stay alive and build upwards to avoid falling into the lava.

Of course, PVP is a part of the game, putting players behind will make them lose faster than you.

Last player to die EXCLUSIVELY from the rising lava wins the game!

# Permissions

### "floorislava.menu.use"
Allows you to use the GUI. You still need "floorislava.lobby.create" to create a lobby.

### "floorislava.lobby.create"
Allows you to create a lobby. This is the main permission you will need to make a game yourself.
Other players can join your lobby if you invite them without needing this permission.

# Command Structure
### /floorislava (alias /fil)
Allows you to open the GUI and use the plguin (This is the only command you really need).
### /fil lobby create
Creates a new lobby. This is the command you will use to create a new game.
### /fil lobby invite player1 player2 playerN...
Invites a player to your lobby.
### /fil lobby list
Lists all players in your lobby. Also shows pending invite sent out by you.
### /fil lobby leave
Leave the current lobby/game you are in.
### /fil lobby kick player1 player2 playerN...
Kicks players from your lobby.
### /fil game start
Starts the game in your lobby.

## Permission Structure
### floorislava.lobby.create
Allows you to create a lobby.

# Config Structure

## Plot:

### Margin: 1000
How far apart each game plot should be from its neighbor (in blocks).

### Size: 75
How big each plot will be. 50 blocks will have a 50x50 plot of land per game.
lowering this number will lower the amount of time needed before each start of a game.

### Amount: 5
How many plots (or lobbies) are there at a time.
THIS IS THE AMOUNT OF PLOTS **ACROSS** (meaning if this is 5, that means 5x5 = 25 total plots)
Lowering this helps very weak servers stay alive lmao (but only when lobbies are at MAX CAPACITY).
## Game:
### PreGameCountdown: 3
How long the game will wait before being teleported to the game.

### StartCountdown: 10
How long the game will wait before starting the game.

### LavaRiseCooldown: 3
How long the game will wait before each lava rise.

### LavaRiseAmount: 1
How many blocks the lava will rise each LavaRaiseCooldown.

### PlayMusic: true
If true, the game will play music when the game starts.

## Server:

### MAX_MILLIS_PER_TICK: 10
This tells how long this plugin is allowed to process each tick. This is a workload distro.
Lowering this number will increase the amount of time needed to generate a game.

Lower this number if your server begins to lag **when making a new game** .
Increase this number if server does not lag but waiting time is too long for a game to start.
MAX VALUE IS 1000 (meaning server freezes until a game terrain is generated)

### developer-mode: false
If true, the plugin will run in developer mode. This means that the plugin will log more information to the console.
Only use this if you are a developer or if you are having issues with the plugin and need support.