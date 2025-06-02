# Floor is Lava *(Changelog v1.0.6)*
- Added Manual Respawn Anchor item:
  - Players receive a special item (default: Red Bed) at the start of the game.
  - Right-clicking this item sets a one-time manual respawn point for the current match.
  - The item is consumed on use.
  - If the manual spawn point becomes unsafe (e.g., covered by lava), respawn logic falls back to automatic safe spawning.
  - This feature is toggleable in `config.yml` (`Game.ManualSpawnPoint.Enabled`, default: true).
  - The item's appearance (material, name, lore) is customizable in `config.yml`.
  - If the item is dropped before use, it vanishes.
  - The item is not lost on non-lava deaths if it hasn't been used yet.

# Floor is Lava *(Changelog v1.0.4)*
- Better player data saving (data is saved on disk to avoid any data loss)
- Players now only lose half of their items when dying from non-lava reasons
- Game Logic improvements and optimizations
- Added a pre-game Teleport Countdown
- Reduced plugin messages syntax bugs
- Fixed daylight cycle to always be a clear day inside game world
- Added main config file version checker
- Added dev mode in config file for debugging and support
## Better player data saving (data is saved on disk to avoid any data loss)
When starting a game of **Floor is Lava**, Your items will need to be saved somewhere until you are done playing. These items are very valuable and losing would not make anyone happy! Especially not the BlueBed Workshop...

Prior to this update, I only saved player data on Memory during runtime which is really bad practice...
Items and player data could get lost if there was a server crash or a plugin crash while you were in a game, plus on Memory solutions are prone to many bugs and lots of frowns.

WORRY NO MORE! Now, with **Floor is Lava v.1.0.4**, your items are stored safely away from the plugin onto the server files; This leads to more stability and is not prone to crashes wiping your items away. Player items will now be loaded back from the latest game of **Floor is Lava**, added back in to your inventory in your cozy little house where you got teleported from.

Of course this also goes for your HP, hunger and XP levels ;)
## Players now only lose half of their items when dying from non-lava reasons
I finally got to play test my plugin on a server with my friends :) I came to some conclusions:

- **Losing your items late in the game is kind of a GG.**
    -	**Reason:** You have no time to gather resources again and you easily die the first in the lobby.
    -	**Solution:** I made it so that on non-lava death, each item stack has a 50% chance to drop from your inventory. This should incentivize people to battle each other not for resources but to control the playfield instead.

## Game Logic improvements and optimizations
Just to name a few on the top of my head:

- Labeled different stages of the game for more unified and reliable game state checking.
    - For example a game can be *GENERATING, STARTING, STARTED, INACTIVE*
- Players are not allowed to move before the start timer finishes.
- Big fixes and Edge Cases.

## Added a pre-game Teleport Countdown
Take a breath, relax, don't move or use any chests. You're about to be teleported in 3..., 2..., 1..., GO!

## Reduced plugin messages syntax bugs

Sometimes this happens when a message is rendered with the tags, for example:

> You invited: Player1 <.red><./red><.br>Player2
> <.red><./red><.br>Player3 <.red><./red><.br>

Yeah it doesnt look tidy at all. I went around and fixed however many I could find. Please report any more you see to my [Discord Server](https://discord.gg/NFXdemyT2Z)

## Fixed daylight cycle to always be a clear day inside game world

One of the major issues with balancing right now is that I cannot control how many monsters are allowed to spawn.. So I went around and fixed the void world time to be day so at least you get a safe zone all the time instead of half of it.

## Added main config file version checker
The next time you open **Floor is Lava**, It will probably yell at you for an "old main config" version.
Just simply backup your existing config file somewhere else and delete the present one. The new config version will be generated for you with new toggleable features! *(yay :D)*


##  Added dev mode in config file for debugging and support
If anything eve does not make sense or if you have found a bug, you can enable debug-mode in the main config and restart the server to get more information about what's going on...

Well.. ***I'll*** have a better idea because I'll be in touch with you on the [Discord Server](https://discord.gg/NFXdemyT2Z) If you end me the dev-logs while describing the issue.


### Floor is Lava is made with love, lava-warm love.