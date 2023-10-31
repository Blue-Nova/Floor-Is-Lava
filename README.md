# **Floor-Is-Lava**
This is the official "Floor is Lava" github repo. Made by BlueNova, CO Developed by Bedless.

## Command Structure

![command_source](https://github.com/Bedlesssgod/Floor-Is-Lava/assets/77199633/968f0c63-08e8-4abd-be1d-0b87defd976d)
<details>
<summary>Raw JSON</summary>

```json
{
  "fil": {
    "lobby": {
      "create": {},
      "list": {},
      "leave": {},
      "remove": {},
      "start": {}
    },
    "invite": {
      "accept": {}
    },
    "game": {
      "leave": {}
    }
  }
}
```

</details>

## Permission Structure
COMING SOON

## Config Structure

### Margin: 1000
How far apart each game plot should be from its neighbor (in blocks).

### Size: 50
How big each plot will be. 50 blocks will have a 50x50 plot of land per game.
lowering this number will lower the amount of time needed before each start of a game.

### Amount: 5
How many plots (or lobbies) are there at a time.
THIS IS THE AMOUNT OF PLOTS **ACROSS** (meaning if this is 5, that means 5x5 = 25 total plots)
Lowering this helps very weak servers stay alive lmao (but only when lobbies are at MAX CAPACITY).


### MAX_MILLIS_PER_TICK: 10
This tells how long this plugin is allowed to process each tick. This is a workload distro.
Lowering this number will increase the amount of time needed to generate a game.

Lower this number if your server begins to lag **when making a new game** .
Increase this number if server does not lag but waiting time is too long for a game to start.
MAX VALUE IS 1000

