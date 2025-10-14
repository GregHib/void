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

class CharacterInteraction(
    private val option: String,
    private val key: String,
    private val operateDispatcher: MapDispatcher<Operation>,
    private val approachDispatcher: MapDispatcher<Approachable>,
) : InteractionType {

    constructor(option: String) : this(option, option, Operation.playerPlayerDispatcher, Approachable.playerPlayerDispatcher)
    constructor(def: NPCDefinition, option: String) : this(option, "$option:${def.stringId}", Operation.playerNpcDispatcher, Approachable.playerNpcDispatcher)
    constructor(def: ObjectDefinition, option: String) : this(option, "$option:${def.stringId}", Operation.playerObjectDispatcher, Approachable.playerObjectDispatcher)
    constructor(def: ItemDefinition, option: String) : this(option, "$option:${def.stringId}", Operation.playerFloorItemDispatcher, Approachable.playerFloorItemDispatcher)

    override fun hasOperate(character: Character) = operateDispatcher.instances.containsKey(key) || operateDispatcher.instances.containsKey(option)

    override fun hasApproach(character: Character) = approachDispatcher.instances.containsKey(key) || approachDispatcher.instances.containsKey(option)

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