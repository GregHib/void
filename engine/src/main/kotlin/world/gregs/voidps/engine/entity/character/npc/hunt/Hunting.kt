package world.gregs.voidps.engine.entity.character.npc.hunt

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import org.rsmod.game.pathfinder.LineValidator
import world.gregs.voidps.engine.client.ui.hasMenuOpen
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.config.HuntModeDefinition
import world.gregs.voidps.engine.data.definition.HuntModeDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.size
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import java.util.*
import kotlin.math.ceil

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
            val mode: String? = npc.def.getOrNull("hunt_mode")
            if (mode == null || npc.hasClock("hunting")) {
                continue
            }
            val range = npc.def["hunt_range", 5]
            val definition = huntModes.get(mode)
            npc.start("hunting", definition.rate)
            when (definition.type) {
                "player" -> {
                    val targets = getCharacters(npc, players, range, definition)
                    npc.events.emit(HuntPlayer(mode, targets.randomOrNull() ?: return))
                }
                "npc" -> {
                    val targets = getCharacters(npc, npcs, range, definition)
                    npc.events.emit(HuntNPC(mode, targets.randomOrNull() ?: return))
                }
                "object" -> {
                    val targets = getObjects(npc, definition)
                    npc.events.emit(HuntObject(mode, targets.randomOrNull() ?: return))
                }
                "floor_item" -> {
                    val targets = getItems(npc, range, definition)
                    npc.events.emit(HuntFloorItem(mode, targets.randomOrNull() ?: return))
                }
            }
        }
    }

    /**
     * Returns all the [FloorItem]s in the first [Zone] with possible targets
     */
    private fun getItems(
        npc: NPC,
        range: Int,
        definition: HuntModeDefinition
    ): MutableList<FloorItem> {
        val targets = ObjectArrayList<FloorItem>()
        for (zone in npc.tile.zone.toRectangle(ceil(range / 8.0).toInt()).toZones(npc.tile.level)) {
            for (items in floorItems[zone]) {
                for (item in items) {
                    if (item.owner != null) {
                        continue
                    }
                    if (definition.id != null && item.id != definition.id) {
                        continue
                    }
                    if (definition.filter != null && !definition.filter!!(item)) {
                        continue
                    }
                    if (canSee(npc, item.tile, 1, 1, definition)) {
                        targets.add(item)
                    }
                }
            }
            if (targets.isNotEmpty()) {
                break
            }
        }
        return targets
    }

    /**
     * Breadth first searches for the first [TARGET_CAP] possible [GameObject] targets
     */
    private fun getObjects(
        npc: NPC,
        definition: HuntModeDefinition
    ): ObjectArrayList<GameObject> {
        val targets = ObjectArrayList<GameObject>()
        val queue: Queue<Tile> = LinkedList()
        queue.add(npc.tile)
        while (queue.isNotEmpty()) {
            val parent = queue.poll()
            if (addTargets(parent, queue, definition, npc, targets, Direction.cardinal)) {
                return targets
            }
            if (addTargets(parent, queue, definition, npc, targets, Direction.ordinal)) {
                return targets
            }
        }
        return targets
    }

    private fun addTargets(
        parent: Tile,
        queue: Queue<Tile>,
        definition: HuntModeDefinition,
        npc: NPC,
        targets: MutableList<GameObject>,
        directions: List<Direction>
    ): Boolean {
        for (direction in directions) {
            val tile = parent.add(direction)
            queue.add(tile)
            val obj = objects[tile, definition.layer] ?: continue
            if (definition.id != null && obj.id != definition.id) {
                continue
            }
            if (definition.filter != null && !definition.filter!!(obj)) {
                continue
            }
            if (canSee(npc, obj.tile, obj.width, obj.height, definition)) {
                targets.add(obj)
                if (targets.size > TARGET_CAP) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Finds the first [TARGET_CAP] possible [Character] targets
     */
    private fun <T : Character> getCharacters(
        npc: NPC,
        characterList: CharacterList<T>,
        range: Int,
        definition: HuntModeDefinition
    ): MutableList<T> {
        val targets = mutableListOf<T>()
        for (zone in npc.tile.zone.toRectangle(ceil(range / 8.0).toInt()).toZones(npc.tile.level)) {
            for (character in characterList[zone]) {
                if (definition.filter != null && !definition.filter!!(character)) {
                    continue
                }
                if (canHunt(npc, character, definition, range)) {
                    targets.add(character)
                    if (targets.size > TARGET_CAP) {
                        return targets
                    }
                }
            }
        }
        return targets
    }

    /**
     * Checks if [character] meets all the [definition] requirements
     */
    @Suppress("RedundantIf")
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
        if (definition.checkNotTooStrong && withinCombatRange(npc, character)) {
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

    private fun withinCombatRange(npc: NPC, character: Character): Boolean {
        return character is Player && character.combatLevel <= npc.def.combat * 2
    }

    private fun canSee(
        npc: NPC,
        tile: Tile,
        width: Int,
        height: Int,
        definition: HuntModeDefinition
    ) = when (definition.checkVisual) {
        "line_of_sight" -> lineOfSight(npc, tile, width, height)
        "line_of_walk" -> lineOfWalk(npc, tile, width, height)
        else -> true
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

    companion object {
        private const val TARGET_CAP = 20
    }
}