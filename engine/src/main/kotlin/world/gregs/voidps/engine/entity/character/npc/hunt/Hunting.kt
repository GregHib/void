package world.gregs.voidps.engine.entity.character.npc.hunt

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import org.rsmod.game.pathfinder.LineValidator
import world.gregs.voidps.engine.client.ui.hasMenuOpen
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.data.config.HuntModeDefinition
import world.gregs.voidps.engine.data.definition.HuntModeDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.mode.move.hasLineOfSight
import world.gregs.voidps.engine.entity.character.mode.move.hasLineOfWalk
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
import world.gregs.voidps.type.Zone
import world.gregs.voidps.type.random
import java.util.*
import kotlin.math.ceil
import kotlin.random.Random

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
    private val lineValidator: LineValidator,
    private val seed: Random = random
) : Runnable {

    override fun run() {
        for (npc in npcs) {
            val mode: String = npc["hunt_mode"] ?: npc.def.getOrNull("hunt_mode") ?: continue
            if (mode == "null" || npc.contains("delay") || npc.dec("hunt_count_down") >= 0) {
                continue
            }
            val range = npc.def["hunt_range", 5]
            val definition = huntModes.get(mode)
            npc["hunt_count_down"] = definition.rate
            when (definition.type) {
                "player" -> {
                    val targets = getCharacters(npc, players, range, definition)
                    val target = targets.randomOrNull(seed) ?: continue
                    npc.emit(HuntPlayer(mode, target))
                }
                "npc" -> {
                    val targets = getCharacters(npc, npcs, range, definition)
                    val target = targets.randomOrNull(seed) ?: continue
                    npc.emit(HuntNPC(mode, target))
                }
                "object" -> {
                    val targets = getObjects(npc, definition)
                    val target = targets.randomOrNull(seed) ?: continue
                    npc.emit(HuntObject(mode, target))
                }
                "floor_item" -> {
                    val targets = getItems(npc, range, definition)
                    val target = targets.randomOrNull(seed) ?: continue
                    npc.emit(HuntFloorItem(mode, target))
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
        for (zone in npc.tile.zone.toRectangle(ceil(range / 8.0).toInt()).toZonesReversed(npc.tile.level)) {
            for (items in floorItems[zone]) {
                for (floorItem in items) {
                    if (definition.id != null && floorItem.id != definition.id) {
                        continue
                    }
                    if (definition.filter != null && !definition.filter!!(floorItem)) {
                        continue
                    }
                    if (canSee(npc, floorItem.tile, 1, 1, definition)) {
                        targets.add(floorItem)
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
        for (zone in npc.tile.zone.toRectangle(ceil(range / 8.0).toInt()).toZonesReversed(npc.tile.level)) {
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
     * Checks if [target] meets all the [definition] requirements
     */
    private fun canHunt(
        npc: NPC,
        target: Character,
        definition: HuntModeDefinition,
        range: Int
    ): Boolean {
        // Npc checks from south-west tile
        if (target.tile.distanceTo(npc.tile) > range) {
            return false
        }
        if (!canSee(npc, target.tile, target.size, target.size, definition)) {
            return false
        }
        if (definition.checkNotTooStrong && targetTooStrong(npc, target)) {
            return false
        }
        if (definition.checkNotCombat && target.hasClock("in_combat") && !target.contains("in_multi_combat")) {
            return false
        }
        if (definition.checkNotCombatSelf && npc.hasClock("in_combat")) {
            return false
        }
        if (definition.checkAfk && !target.hasClock("tolerance")) {
            return false
        }
        if (definition.checkNotBusy && (target.contains("delay") || target.hasMenuOpen())) {
            return false
        }
        return true
    }

    private fun targetTooStrong(npc: NPC, character: Character): Boolean {
        return character is Player && character.combatLevel > npc.def.combat * 2
    }

    private fun canSee(
        npc: NPC,
        tile: Tile,
        width: Int,
        height: Int,
        definition: HuntModeDefinition
    ) = when (definition.checkVisual) {
        "line_of_sight" -> lineValidator.hasLineOfSight(npc, tile, width, height)
        "line_of_walk" -> lineValidator.hasLineOfWalk(npc, tile, width, height)
        else -> true
    }

    companion object {
        private const val TARGET_CAP = 20
    }
}