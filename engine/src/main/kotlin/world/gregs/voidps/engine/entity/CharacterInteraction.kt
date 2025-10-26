package world.gregs.voidps.engine.entity

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.dispatch.MapDispatcher
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
    val character: Character,
    val target: Entity,
    private val option: String,
    private val key: String,
    private val operateDispatcher: MapDispatcher<Operation>,
    private val approachDispatcher: MapDispatcher<Approachable>,
) : InteractionType {

    constructor(character: Character, target: Player, option: String) : this(character, target, option, option, Operation.playerPlayerDispatcher, Approachable.playerPlayerDispatcher)
    constructor(character: Character, target: NPC, def: NPCDefinition, option: String) : this(character, target, option, "$option:${def.stringId}", Operation.playerNpcDispatcher, Approachable.playerNpcDispatcher)
    constructor(character: Character, target: GameObject, def: ObjectDefinition, option: String) : this(character, target, option, "$option:${def.stringId}", Operation.playerObjectDispatcher, Approachable.playerObjectDispatcher)
    constructor(character: Character, target: FloorItem, def: ItemDefinition, option: String) : this(character, target, option, "$option:${def.stringId}", Operation.playerFloorItemDispatcher, Approachable.playerFloorItemDispatcher)

    override fun hasOperate() = operateDispatcher.instances.containsKey(key) || operateDispatcher.instances.containsKey(option)

    override fun hasApproach() = approachDispatcher.instances.containsKey(key) || approachDispatcher.instances.containsKey(option)

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