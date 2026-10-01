Void has Player Bots to keep the game world constantly feeling active and alive.

To support hundreds or thousands of bots each bot runs a simple [Finite State Machine (FSM)](https://en.wikipedia.org/wiki/Finite-state_machine) that attempts a single action per tick. Instead of expensive planning algorithms bots chain resolving requirements over time in order to complete activities.

Core concepts:
- Activities (High level goals)
- Actions (Small FSM units of work)
- Conditions (Requirement checks)
- Resolvers (Requirement handlers)
- Navigation
- Behaviours - Joint term for Activities, Resolvers or Shortcuts


# Architecture

## Behaviour Stack
Each bot maintains a stack of Behaviours, processed from Resolvers at the top to the original Activity at the bottom.

```
-> Withdraw Coins (Resolver)
-> Buy Axe (Resolver)
-> Go to Axe Shop (Resolver)
-> Get Hatchet (Resolver)
Chop Trees (Activity)
```

If a behaviour requirement is unmet a resolver is added to the top.
When a behaviour is complete it is removed.

# Activities
High-level activities around the map assigned to bots based on their available items, skill levels and current world state.

- Capacity
  Max number of bots that can perform the activity at once.
- Requirements
  Conditions have to be met to be assigned the activity.
- Setup/Resolvables
  Conditions that can be satisfied with Resolvers before the activity starts.
- Actions
  Steps required to complete the activity.
- Products
  States or outcomes produced upon completion.

Activities are written in `*.bots.toml` config files.

# Resolvers

Resolvers are specialised activities which can be triggered when a setup requirement is unmet.

Resolvers can be configured in `*.setups.toml` config files or some common resolvers are automatically produced by DynamicResolvers.kt.

Examples:
- Buying an item from a shop
- Equipping an inventory item
- Moving to a location
- Withdrawing items from a bank

# Conditions

Conditions determine whether a bot satisfies a requirement and provide a identifiers for finding resolvers that could satisfy it.

Examples:
- Has at least level 15 Woodcutting
- Owns a specific item
- Has a specific variable set
- Is at a specific location

Conditions are used for requirements, setups and by navigation edges.

# Actions

Small self-contained FSMs that produce instructions, check world state and handle branching logic and failures.

An action runs until it succeeds, fails or times-out due to not producing enough producers recently.

# Instructions
The lowest-level event for player entities used by player clients and bots to control player characters

# Bot Manager
Coordinates all bot behaviour, handling allocation of activities and resolvers and checks and updates the state of the top behaviour for every bot each tick.

# Navigation Graph

Simplified representation of the game world used for efficient path planning between distant locations (e.g. cities).

<img width="915" height="595" alt="image" src="https://github.com/user-attachments/assets/18da6dab-25d3-4e2b-a463-22c8950403bb" />


Supports discovery of nearby locations of interest like banks, shops or other areas.

## Edges
Defines how bots move between nodes in the navigation graph.

Can include:
- Movement cost (weight)
- Requirements (e.g. key, coins)
- Actions (e.g. open door, use portal)

Edges with unmet requirements aren't evaluated.

Edges are written in `*.nav-edges.toml` config files.

# Shortcuts

An activity only evaluated as virtual starting points to navigate from.

E.g. Comparing using runes and jewellery to teleport to a nearby location vs running directly.

Shortcuts are written in `*.shortcuts.toml` config files.