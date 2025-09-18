package world.gregs.voidps.engine

import world.gregs.voidps.engine.entity.Spawn
import world.gregs.voidps.engine.entity.character.player.skill.level.LevelChanged

/**
 * A helper interface made up of all [world.gregs.voidps.engine.dispatch.Dispatcher] for easier [world.gregs.voidps.engine.event.Script] usage.
 */
interface Api : Spawn, LevelChanged