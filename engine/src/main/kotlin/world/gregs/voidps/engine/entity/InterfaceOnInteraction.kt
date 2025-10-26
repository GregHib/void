package world.gregs.voidps.engine.entity

import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.character.mode.interact.InteractionType
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.Events

data class InteractInterfaceOnPlayer(
    val player: Player,
    val target: Player,
    val id: String,
    val slot: Int,
) : InteractionType {
    override fun hasOperate() = Operation.onPlayerDispatcher.instances.containsKey(id)

    override fun hasApproach() = Approachable.onPlayerDispatcher.instances.containsKey(id)

    override fun operate() = Events.events.launch { Operation.operate(player, id, Item.EMPTY, slot, target) }

    override fun approach() = Events.events.launch { Approachable.approach(player, id, Item.EMPTY, slot, target) }
}

data class InteractInterfaceOnNPC(
    val player: Player,
    val target: NPC,
    val id: String,
    val slot: Int,
    val def: NPCDefinition,
) : InteractionType {
    override fun hasOperate() = Operation.onNpcDispatcher.instances.containsKey("$id:${def.stringId}") || Operation.onNpcDispatcher.instances.containsKey(id)

    override fun hasApproach() = Approachable.onNpcDispatcher.instances.containsKey("$id:${def.stringId}") || Approachable.onNpcDispatcher.instances.containsKey(id)

    override fun operate() = Events.events.launch { Operation.operate(player, id, Item.EMPTY, slot, target) }

    override fun approach() = Events.events.launch { Approachable.approach(player, id, Item.EMPTY, slot, target) }
}

data class InteractInterfaceOnObject(
    val player: Player,
    val target: GameObject,
    val id: String,
    val slot: Int,
    val def: ObjectDefinition,
) : InteractionType {
    override fun hasOperate() = Operation.onObjectDispatcher.instances.containsKey("$id:${def.stringId}") || Operation.onObjectDispatcher.instances.containsKey(id)

    override fun hasApproach() = Approachable.onObjectDispatcher.instances.containsKey("$id:${def.stringId}") || Approachable.onObjectDispatcher.instances.containsKey(id)

    override fun operate() = Events.events.launch { Operation.operate(player, id, Item.EMPTY, slot, target) }

    override fun approach() = Events.events.launch { Approachable.approach(player, id, Item.EMPTY, slot, target) }
}

data class InteractInterfaceOnFloorItem(
    val player: Player,
    val target: FloorItem,
    val id: String,
    val slot: Int,
) : InteractionType {
    override fun hasOperate() = Operation.onObjectDispatcher.instances.containsKey("$id:${target.id}") || Operation.onObjectDispatcher.instances.containsKey(id)

    override fun hasApproach() = Approachable.onObjectDispatcher.instances.containsKey("$id:${target.id}") || Approachable.onObjectDispatcher.instances.containsKey(id)

    override fun operate() = Events.events.launch { Operation.operate(player, id, Item.EMPTY, slot, target) }

    override fun approach() = Events.events.launch { Approachable.approach(player, id, Item.EMPTY, slot, target) }
}