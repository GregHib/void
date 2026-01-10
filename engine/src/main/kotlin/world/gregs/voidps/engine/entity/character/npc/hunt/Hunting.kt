package world.gregs.voidps.engine.entity.character.npc.hunt

import org.rsmod.game.pathfinder.LineValidator
import world.gregs.voidps.engine.client.ui.hasMenuOpen
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.data.config.HuntModeDefinition
import world.gregs.voidps.engine.data.definition.HuntModeDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterSearch
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.mode.move.hasLineOfSight
import world.gregs.voidps.engine.entity.character.mode.move.hasLineOfWalk
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.combatLevel
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
    private val seed: Random = random,
) : Runnable {

    private var count = 0
    private val playerTargets = arrayOfNulls<Player>(TARGET_CAP)
    private val npcTargets = arrayOfNulls<NPC>(TARGET_CAP)
    private val objectTargets = arrayOfNulls<GameObject>(TARGET_CAP)
    private val itemTargets = arrayOfNulls<FloorItem>(TARGET_CAP)

    override fun run() {
        for (npc in npcs) {
            if (npc.huntCounter == -1) {
                continue
            }
            val mode: String? = npc.huntMode ?: npc.def.getOrNull("hunt_mode")
            if (mode == null || mode == "") {
                npc.huntCounter = -1
                continue
            }
            if (npc.contains("delay") || --npc.huntCounter >= 0) {
                continue
            }
            val definition = huntModes.get(mode)
            npc.huntCounter = definition.rate
            if (definition.checkNotCombatSelf && npc.mode is CombatMovement) {
                continue
            }
            if (!definition.findKeepHunting && npc.mode !is EmptyMode) {
                continue
            }
            val range = npc.def["hunt_range", 5]
            when (definition.type) {
                "player" -> {
                    val target = findCharacter(npc, players, range, definition, playerTargets) ?: continue
                    Hunt.hunt(npc, target, mode)
                }
                "npc" -> {
                    val target = findCharacter(npc, npcs, range, definition, npcTargets) ?: continue
                    Hunt.hunt(npc, target, mode)
                }
                "object" -> {
                    listObjects(npc, definition)
                    if (count == 0) {
                        continue
                    }
                    val index = seed.nextInt(0, count)
                    val target = objectTargets[index] ?: continue
                    Hunt.hunt(npc, target, mode)
                }
                "floor_item" -> {
                    listItems(npc, range, definition)
                    if (count == 0) {
                        continue
                    }
                    val index = seed.nextInt(0, count)
                    val target = itemTargets[index] ?: continue
                    Hunt.hunt(npc, target, mode)
                }
            }
        }
        // Just to avoid dangling references
        playerTargets.fill(null)
        npcTargets.fill(null)
        objectTargets.fill(null)
        itemTargets.fill(null)
    }

    /**
     * Returns all the [FloorItem]s in the first [Zone] with possible targets
     */
    private fun listItems(npc: NPC, range: Int, definition: HuntModeDefinition) {
        count = 0
        for (zone in npc.tile.zone.toRectangle(ceil(range / 8.0).toInt()).toZonesReversed(npc.tile.level)) {
            for (items in floorItems[zone]) {
                for (floorItem in items) {
                    if (definition.id != null && floorItem.id != definition.id) {
                        continue
                    }
                    if (canSee(npc, floorItem.tile, 1, 1, definition)) {
                        if (count < TARGET_CAP) {
                            itemTargets[count++] = floorItem
                        }
                    }
                }
            }
            if (count > 0) {
                break
            }
        }
    }

    /**
     * Breadth first searches for the first [TARGET_CAP] possible [GameObject] targets
     */
    private fun listObjects(npc: NPC, definition: HuntModeDefinition) {
        count = 0
        val queue: Queue<Tile> = LinkedList()
        queue.add(npc.tile)
        while (queue.isNotEmpty()) {
            val parent = queue.poll()
            var obj = findTargets(parent, queue, definition, npc, Direction.cardinal)
            if (obj != null) {
                if (count < TARGET_CAP) {
                    objectTargets[count++] = obj
                }
                continue
            }
            obj = findTargets(parent, queue, definition, npc, Direction.ordinal) ?: continue
            if (count < TARGET_CAP) {
                objectTargets[count++] = obj
            }
        }
    }

    private fun findTargets(
        parent: Tile,
        queue: Queue<Tile>,
        definition: HuntModeDefinition,
        npc: NPC,
        directions: List<Direction>,
    ): GameObject? {
        for (direction in directions) {
            val tile = parent.add(direction)
            queue.add(tile)
            val obj = objects[tile, definition.layer] ?: continue
            if (definition.id != null && obj.id != definition.id) {
                continue
            }
            if (canSee(npc, obj.tile, obj.width, obj.height, definition)) {
                return obj
            }
        }
        return null
    }

    fun <T : Character> findCharacter(
        npc: NPC,
        characters: CharacterSearch<T>,
        range: Int,
        definition: HuntModeDefinition,
        targets: Array<T?>
    ) : T? {
        listCharacters(npc, characters, range, definition, targets)
        if (count == 0) {
            return null
        }
        val index = seed.nextInt(0, count)
        return targets[index]
    }

    /**
     * Finds the first [TARGET_CAP] possible [Character] targets
     */
    fun <T : Character> listCharacters(
        npc: NPC,
        characterList: CharacterSearch<T>,
        range: Int,
        definition: HuntModeDefinition,
        targets: Array<T?>
    ) {
        count = 0
        for (zone in npc.tile.zone.toRectangle(ceil(range / 8.0).toInt()).toZonesReversed(npc.tile.level)) {
            for (character in characterList[zone]) {
                if (canHunt(npc, character, definition, range)) {
                    if (count >= TARGET_CAP) {
                        return
                    }
                    targets[count++] = character
                }
            }
        }
    }

    /**
     * Checks if [target] meets all the [definition] requirements
     */
    private fun canHunt(
        npc: NPC,
        target: Character,
        definition: HuntModeDefinition,
        range: Int,
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
        if (definition.checkNotCombat && target.hasClock("under_attack") && !target.contains("in_multi_combat")) {
            return false
        }
        if (definition.checkAfk && !target.hasClock("tolerance")) {
            return false
        }
        if (definition.checkNotBusy && (target.contains("delay") || target.hasMenuOpen())) {
            return false
        }
        if (definition.checkSameGod && target is Player && wearsGodArmour(npc, target)) {
            return false
        }
        if (definition.checkZamorak && target is NPC && target.def["god", ""] != "zamorak") {
            return false
        }
        if (definition.checkNotZamorak && target is NPC && target.def["god", ""] == "zamorak") {
            return false
        }
        return true
    }

    private fun wearsGodArmour(npc: NPC, target: Player): Boolean {
        val gods = target.get<Set<String>>("gods") ?: return false
        return gods.contains("zaros") || gods.contains(npc.def["god", ""])
    }

    private fun targetTooStrong(npc: NPC, character: Character): Boolean = character is Player && character.combatLevel > npc.def.combat * 2

    private fun canSee(
        npc: NPC,
        tile: Tile,
        width: Int,
        height: Int,
        definition: HuntModeDefinition,
    ) = when (definition.checkVisual) {
        "line_of_sight" -> lineValidator.hasLineOfSight(npc, tile, width, height)
        "line_of_walk" -> lineValidator.hasLineOfWalk(npc, tile, width, height)
        else -> true
    }

    companion object {
        private const val TARGET_CAP = 20
    }
}
