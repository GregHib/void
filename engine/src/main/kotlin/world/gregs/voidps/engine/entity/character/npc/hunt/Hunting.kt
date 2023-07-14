package world.gregs.voidps.engine.entity.character.npc.hunt

import org.rsmod.game.pathfinder.LineValidator
import world.gregs.voidps.engine.client.ui.hasMenuOpen
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.config.HuntModeDefinition
import world.gregs.voidps.engine.data.definition.HuntModeDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.size
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.Spiral
import world.gregs.voidps.type.Tile

/**
 * Picks new target for an [NPC] based on it's [HuntModeDefinition]
 * Examples of targets are:
 *   players - regular aggression
 *   npcs - Godwars or Chaos dwarves
 *   objects - Pest control ravagers
 *   floor items - Varrock ash cleaner
 */
class Hunting(
    private val npcs: NPCs,
    private val players: Players,
    private val objects: GameObjects,
    private val floorItems: FloorItems,
    private val huntModes: HuntModeDefinitions,
    private val lineValidator: LineValidator
) : Runnable {

    override fun run() {
        for (npc in npcs) {
            val mode: String? = npc.getOrNull("hunt_mode")
            if (mode == null || npc.hasClock("hunting")) {
                continue
            }
            val range = npc["hunt_range", 5]
            val definition = huntModes.get(mode)
            npc.start("hunting", definition.rate)
            when (definition.type) {
                "player" -> {
                    val targets = players.filter { canHunt(npc, it, definition, range) }
                    if (targets.isEmpty()) {
                        return
                    }
                    npc.events.emit(HuntPlayer(mode, targets.random()))
                }
                "npc" -> {
                    val targets = npcs.filter { canHunt(npc, it, definition, range) }
                    if (targets.isEmpty()) {
                        return
                    }
                    npc.events.emit(HuntNPC(mode, targets.random()))
                }
                "object" -> {
                    // Should prefer cardinal directions over ordinal but this is close enough
                    for (tile in Spiral.spiral(npc.tile, range)) {
                        val obj = objects[tile, definition.layer] ?: continue
                        if (canSee(npc, obj.tile, obj.width, obj.height, definition)) {
                            npc.events.emit(HuntObject(mode, obj))
                            break
                        }
                    }
                }
                "floor_item" -> {
                    spiral@ for (tile in Spiral.spiral(npc.tile, range)) {
                        val items = floorItems[tile]
                        for (item in items) {
                            if (canSee(npc, item.tile, 1, 1, definition)) {
                                npc.events.emit(HuntFloorItem(mode, item))
                                break@spiral
                            }
                        }
                    }
                }
            }
        }
    }

    private fun canSee(
        npc: NPC,
        tile: Tile,
        width: Int,
        height: Int,
        definition: HuntModeDefinition
    ): Boolean {
        if (definition.checkVisual == "line_of_sight" && !lineOfSight(npc, tile, width, height)) {
            return false
        } else if (definition.checkVisual == "line_of_walk" && !lineOfWalk(npc, tile, width, height)) {
            return false
        }
        return true
    }

    private fun canHunt(
        npc: NPC,
        character: Character,
        definition: HuntModeDefinition,
        range: Int
    ): Boolean {
        // Npc checks from south-west tile
        if (character.tile.distanceTo(npc.tile) > range) {
            return false
        }
        if (!canSee(npc, character.tile, character.size, character.size, definition)) {
            return false
        }
        if (definition.checkNotTooStrong && withinCombatLevel(npc, character)) {
            return false
        }
        if (definition.checkNotCombat && character.hasClock("in_combat")) {
            return false
        }
        if (definition.checkNotCombatSelf && npc.hasClock("in_combat")) {
            return false
        }
        if (definition.checkNotBusy && (character.hasClock("delay") || character.hasMenuOpen())) {
            return false
        }
        return true
    }

    private fun withinCombatLevel(npc: NPC, character: Character): Boolean {
        if (character is Player) {
            return character.combatLevel <= npc.def.combat * 2
        } else if (character is NPC) {
            return character.def.combat <= npc.def.combat * 2
        }
        return false
    }

    private fun lineOfSight(npc: NPC, tile: Tile, width: Int, height: Int) = lineValidator.hasLineOfSight(
        srcX = npc.tile.x,
        srcZ = npc.tile.y,
        level = npc.tile.level,
        srcSize = npc.def.size,
        destX = tile.x,
        destZ = tile.y,
        destWidth = width,
        destHeight = height
    )

    private fun lineOfWalk(npc: NPC, tile: Tile, width: Int, height: Int) = lineValidator.hasLineOfWalk(
        srcX = npc.tile.x,
        srcZ = npc.tile.y,
        level = npc.tile.level,
        srcSize = npc.def.size,
        destX = tile.x,
        destZ = tile.y,
        destWidth = width,
        destHeight = height
    )
}