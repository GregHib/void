This is a brief introduction to how to use the basic commands.

Commands are entered in the command-console which can be opened and closed by pressing the ` key (typically located top left on an English keyboard) or alt gr+2 on European keyboards.

<img width="767" height="535" alt="image" src="https://github.com/user-attachments/assets/f6e5f1e7-9e78-44f0-9b1a-ab9b1b4cb9cd" />

When the commands console is open you can type a command name and press enter to run it.

# Commands

The most useful command is `commands` which will show you a list of commands you have permissions to use.

<img width="767" height="535" alt="image" src="https://github.com/user-attachments/assets/3c8feacc-350a-48c5-8c95-8dded0630bd4" />

You can optionally add a search to to the end to filter the list e.g. `commands player`

# Help (command-name)

The help command explains a command and it's format in more detail for example `help commands` will explain the [commands](#commands) command in more detail.

<img width="418" height="178" alt="image" src="https://github.com/user-attachments/assets/807b4465-27b5-4b93-a6b3-a5e52d9ab13a" />


# Item

The item command allows [Admins](player-rights#modifying-rights) to spawn any item into their inventory.

It follows the format `item (item-id) [item-amount]`

- item-id - the items id using underscores e.g. `abyssal_whip`, `bandos_tasset` or `armadyl_godsword` (short-hand often works too e.g. `whip`, `tassy`, `ags` as does numbers e.g. `4151`, `11726`, `11694`)
- amount - the number of that item to spawn (abbreviations work for k for thousand, m for million, b for billion, t for trillion)

For example:
- `item bronze_sword`
- `item coins 100`
- `item feathers 2k`

<img width="767" height="535" alt="image" src="https://github.com/user-attachments/assets/a3292605-9e6c-45f3-8a1d-4eeb0849557d" />

# Unlock

Allows you to unlock different types of content

It follows the format `unlock (content-type)`

- activity-type - `all`, `music`, `songs`, `music_tracks`, `tasks`, `achievements`, `emotes` or `quests`
- player-name - optional to target another player; defaults to self.

Examples:
- `unlock all`
- `unlock quests`
- `unlock music`

# Find

Allows you to quickly find an item, object or npc id

# Position

`displayfps` shows tile location on the screen at all times
`pos` sends you your current tile coordinates in the game chat
