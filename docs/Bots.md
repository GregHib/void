As Void can have a number of bot controlled [players](players) to make the world feel more alive and active even when playing offline and locally.

<img width="767" height="535" alt="Screenshot 2026-02-17 230947" src="https://github.com/user-attachments/assets/0ccbd161-a456-4fa7-99cc-d4b2d5ce0257" />

You can customise the number of bots spawned in your world in [Game Settings](game-settings#bots).

Bot configuration is split into 5 parts total; the most important two being `*.bots.toml` and `*.setups.toml`, with `*.templates.toml` shared across them. The remaining two `*.nav-edges.toml` and `*.shortcuts.toml` are used for bot navigation only.

See the [bot-architecture](bot-architecture) page for more details on how the system works.

# Activities

Activities are things a bot can do, what requirements it needs, and where it needs to be to do it.

```toml
[cut_trees]
# Hard Requirements - What a bot must have to be assigned this activity
requires = [
    { skill = { id = "woodcutting", min = 1, max = 15 } },
]
# Things it might not have right now but will need to be setup before starting
setup = [
    { inventory = [{ id = "steel_hatchet,iron_hatchet" }, { id = "empty", min = 27 }] },
    { area = { id = "lumbridge_trees" } },
]
# The action(s) to carry out to do the activity
actions = [
    { object = { option = "Chop down", id = "tree*", delay = 5, success = { inventory = { id = "empty", max = 0 } } } },
]
# Things the activity produces (used for resolver matching and checking if a bot has timed-out)
produces = [
    { item = "logs" },
    { skill = "woodcutting" }
]
```

# Resolvers
Resolvers are activities bots can complete to get stuff which is needed for another activity.
E.g. if a bot needs air and mind runes to cast spells on a combat dummy it will need to talk with the magic tutor to get them.

```toml
[magic_tutor_runes]
requires = [
    { clock = { id = "claimed_tutor_consumables", max = 0, seconds = true } },
    { bank = [
        { id = "air_rune", max = 0 },
        { id = "mind_rune", max = 0 },
    ] },
    { area = { id = "lumbridge_combat_tutors" } }
]
actions = [
    # There are lots of different types of actions most types of interactions
    { npc = { option = "Talk-to", id = "mikasi", delay = 5, success = { interface_open = { id = "dialogue_npc_chat3" } } } },
    { continue = { id = "dialogue_npc_chat3:continue", success = { interface_open = { id = "dialogue_multi4" } } } },
    { continue = { id = "dialogue_multi4:line3", success = { interface_open = { id = "dialogue_obj_box" } } } },
    { continue = { id = "dialogue_obj_box:continue", success = { inventory = { id = "air_rune", min = 30 } } } },
    { continue = { id = "dialogue_obj_box:continue", success = { inventory = { id = "mind_rune", min = 30 } } } },
]
produces = [
    { item = "air_rune" }, # Produces is how setups figure out what resolvers can solve their requirement.
    { item = "mind_rune" }
]
```


## Templates
Templates are re-usable activities where you can put placeholder `$fields` which can be replaced later.

For example a template:

```toml
[cooking_template]
requires = [
    { skill = { id = "cooking", min = "$level" } },
]
```

Can be used like this:
```toml
[cooking_trout]
template = "cooking_template"
fields = { level = 15 }
```

And is equivalent to:

```toml
[cooking_trout]
requires = [
    { skill = { id = "cooking", min = 15  } },
]
```

You can have multiple fields for any kind of value, anywhere in an activity including in the middle of a string.
Templates can be shared between activities, resolvers and shortcuts, but templates can't reference each other.

```toml
[cooking_template]
requires = [
    { bank = { id = "$raw", amount = 28 } },
    { skill = { id = "cooking", min = "$level" } },
]
setup = [
    { inventory = { id = "$raw", min = 28 } },
    { area = { id = "$location" } },
]
actions = [
    { item_on_object = { id = "$raw", object = "$obj", success = { interface_open = { id = "dialogue_skill_creation" } } } },
    { interface = { option = "All", id = "skill_creation_amount:all" } },
    { continue = { id = "dialogue_skill_creation:choice1" } },
    { restart = { wait_if = { queue = { id = "cooking" } }, success = { inventory = { id = "$raw", max = 0 } } } }
]
produces = [
    { skill = "cooking" },
    { item = "$cooked" },
    { item = "$burnt" }
]
```