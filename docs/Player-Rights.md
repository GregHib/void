There are 3 types of rights a player can have:
* Player
* Moderator
* Administrator


## Modifying rights

### File
Logout of the account, and in the player account TOML file found in `/data/saves/<player-name>.toml` you can modify or add the "rights" variable to the "variables" section. Valid values are "none", "mod" or "admin". Save the file then log-back into the game.

![image](https://github.com/user-attachments/assets/d72b1f2c-e596-4c86-9be4-49c42d70e2d1)

### Properties

In `game.properties` you can set the `rights` property to your player name and then reload the server, whenever your player will log in they will be granted admin rights, even if they have previously lost them.

### Command

If you already have access to an admin account you can use the `rights` command to grant rights to other logged-in players. E.g. `rights Zezima mod`
