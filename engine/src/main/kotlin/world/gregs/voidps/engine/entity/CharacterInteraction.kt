package world.gregs.voidps.engine.entity

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.InteractionType
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.Events

data class InteractPlayerPlayer(
    val player: Player,
    val target: Player,
    val option: String,
) : InteractionType {
    override fun hasOperate() = Operation.playerPlayerBlocks.containsKey(option)

    override fun hasApproach() = Approachable.playerPlayerBlocks.containsKey(option)

    override fun operate() = Events.events.launch { Operation.operate(player, target, option) }

    override fun approach() = Events.events.launch { Approachable.approach(player, target, option) }
}

data class InteractNPCPlayer(
    val npc: NPC,
    val target: Player,
    val option: String,
) : InteractionType {
    override fun hasOperate() = Operation.playerPlayerBlocks.containsKey(option)

    override fun hasApproach() = Approachable.playerPlayerBlocks.containsKey(option)

    override fun operate() = Events.events.launch { Operation.operate(npc, target, option) }

    override fun approach() = Events.events.launch { Approachable.approach(npc, target, option) }
}

/**
 * Interaction between two [Character]'s
 * Provides a way to check if an interaction is available and a way of starting it
 *  TODO rename to Interaction after current Interaction has been full migrated
 */
class CharacterInteraction(
    private val character: Character,
    private val target: Entity,
    private val option: String,
    private val key: String,
    private val operateKeys: Set<String>,
    private val approachKeys: Set<String>,
) : InteractionType {

    constructor(character: Character, target: NPC, def: NPCDefinition, option: String) : this(character, target, option, "$option:${def.stringId}", if (character is Player) Operation.playerNpcBlocks.keys else Operation.npcNpcBlocks.keys, if (character is Player) Approachable.playerNpcBlocks.keys else Approachable.npcNpcBlocks.keys)
    constructor(character: Character, target: GameObject, def: ObjectDefinition, option: String) : this(character, target, option, "$option:${def.stringId}", if (character is Player) Operation.playerObjectBlocks.keys else Operation.npcObjectBlocks.keys, if (character is Player) Approachable.playerObjectBlocks.keys else Approachable.npcObjectBlocks.keys)
    constructor(character: Character, target: FloorItem, def: ItemDefinition, option: String) : this(character, target, option, "$option:${def.stringId}", if (character is Player) Operation.playerFloorItemBlocks.keys else Operation.npcFloorItemBlocks.keys, if (character is Player) Approachable.playerFloorItemBlocks.keys else Approachable.npcFloorItemBlocks.keys)

    override fun hasOperate() = operateKeys.contains(key) || operateKeys.contains("$option:*")

    override fun hasApproach() = approachKeys.contains(key) || approachKeys.contains("$option:*")

    override fun operate() {
        Events.events.launch {
            if (character is NPC) {
                when (target) {
                    is Player -> Operation.operate(character, target, option)
                    is NPC -> Operation.operate(character, target, option)
                    is GameObject -> Operation.operate(character, target, option)
                    is FloorItem -> Operation.operate(character, target, option)
                }
            } else if (character is Player) {
                when (target) {
                    is Player -> Operation.operate(character, target, option)
                    is NPC -> Operation.operate(character, target, option)
                    is GameObject -> Operation.operate(character, target, option)
                    is FloorItem -> Operation.operate(character, target, option)
                }
            }
        }
    }

    override fun approach() {
        Events.events.launch {
            if (character is NPC) {
                when (target) {
                    is Player -> Approachable.approach(character, target, option)
                    is NPC -> Approachable.approach(character, target, option)
                    is GameObject -> Approachable.approach(character, target, option)
                    is FloorItem -> Approachable.approach(character, target, option)
                }
            } else if (character is Player) {
                when (target) {
                    is Player -> Approachable.approach(character, target, option)
                    is NPC -> Approachable.approach(character, target, option)
                    is GameObject -> Approachable.approach(character, target, option)
                    is FloorItem -> Approachable.approach(character, target, option)
                }
            }
        }
    }

}