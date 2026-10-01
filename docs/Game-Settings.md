Game settings are listed in the [`game.properties`](https://github.com/GregHib/void/blob/main/game/src/main/resources/game.properties) file allows you to customise the server to your liking.

The most common and useful ones to players will be:

## Server name
```properties
# The name of the game server
server.name=Void
```

## Networking

```properties
# The port the game server will listen on
network.port=43594
```


## Admin
```properties
# The admin username (always sets administrative privileges on login)
development.admin.name=Greg
```

## Home spawn

```properties
# The default home spawn coordinates (X, Y, Level)
world.home.x=3221
world.home.y=3219
```

## Experience rates

```properties
# Experience multiplier (1.0 = normal XP rate)
world.experienceRate=1.0
```

## Bots

```properties
# The number of AI-controlled bots spawned on startup
bots.count=10

# Frequently between spawning bots on startup
bots.spawnSeconds=60
```

## Running
```properties
# Whether players energy drains while running
players.energy.drain=true
```

## Combat

```properties
# Whether reducing hits by equipment absorption is enabled in combat mechanics
combat.damageSoak=true

# Whether to spawn gravestones on death
combat.gravestones=true
```

## Skills

There's lots of properties for individual skills, quests, events and more.

```properties
# Degrade runecrafting essence pouches when in use
runecrafting.pouch.degrade=true

# Day of the week penguins reset on from 1 (Monday) to 7 (Sunday)
events.penguinHideAndSeek.resetDay=3

# Whether Stronghold of Security doors should give questions or not
strongholdOfSecurity.quiz=true
```

