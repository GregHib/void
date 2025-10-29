package world.gregs.voidps.engine.entity

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.InteractionType
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.Events

/**
 * Interaction between two [Character]'s
 * Provides a way to check if an interaction is available and a way of starting it
 *  TODO rename to Interaction after current Interaction has been full migrated
 */
class CharacterInteraction(
    private val option: String,
    private val key: String,
    private val operateKeys: Set<String>,
    private val approachKeys: Set<String>,
) : InteractionType {

    constructor(option: String) : this(option, option, Operation.playerPlayerBlocks.keys, Approachable.playerPlayerBlocks.keys)
    constructor(def: NPCDefinition, option: String) : this(option, "$option:${def.stringId}", Operation.playerNpcBlocks.keys, Approachable.playerNpcBlocks.keys)
    constructor(def: ObjectDefinition, option: String) : this(option, "$option:${def.stringId}", Operation.playerObjectBlocks.keys, Approachable.playerObjectBlocks.keys)
    constructor(def: ItemDefinition, option: String) : this(option, "$option:${def.stringId}", Operation.playerFloorItemBlocks.keys, Approachable.playerFloorItemBlocks.keys)

    override fun hasOperate(character: Character) = operateKeys.contains(key) || operateKeys.contains(option)

    override fun hasApproach(character: Character) = approachKeys.contains(key) || approachKeys.contains(option)

    override fun operate(character: Character, target: Entity) {
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

    override fun approach(character: Character, target: Entity) {
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