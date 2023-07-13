# Floor-Is-Lava
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
![permission_source](https://github.com/Bedlesssgod/Floor-Is-Lava/assets/77199633/eb859727-a0d6-4a20-8ea2-cfd082037d83)
<details>
  <summary>Raw JSON</summary>
  
  ```json
  {
  "fil": {
    "Permission": "fil.command.fil",
    "lobby": {
      "permission": "fil.command.fil.lobby",
      "create": {
        "Permission:": "fil.command.fil.lobby.create"
      },
      "list": {
        "Permission:": "fil.command.fil.lobby.list"
      },
      "leave": {
        "Permission:": "fil.command.fil.lobby.leave"
      },
      "remove": {
        "Permission:": "fil.command.fil.lobby.remove"
      },
      "start": {
        "Permission:": "fil.command.fil.lobby.start"
      }
    },
    "invite": {
      "Permission": "fil.command.fil.invite",
      "accept": {
        "Permission:": "fil.command.fil.invite.accept"
      }
    },
    "game": {
      "Permission": "fil.command.game",
      "leave": {
        "Permission:": "fil.command.fil.game.leave"
      }
    }
  }
}
```
</details>

## Config Structure
Coming soon!
