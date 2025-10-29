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
    override fun hasOperate() = Operation.onPlayerBlocks.containsKey(id)

    override fun hasApproach() = Approachable.onPlayerBlocks.containsKey(id)

    override fun operate() = Events.events.launch { Operation.operate(player, id, slot, Item.EMPTY, target) }

    override fun approach() = Events.events.launch { Approachable.approach(player, id, slot, Item.EMPTY, target) }
}

data class InteractInterfaceOnNPC(
    val player: Player,
    val target: NPC,
    val id: String,
    val slot: Int,
    val def: NPCDefinition,
) : InteractionType {
    override fun hasOperate() = Operation.onNpcBlocks.containsKey("$id:${def.stringId}") || Operation.onNpcBlocks.containsKey(id)

    override fun hasApproach() = Approachable.onNpcBlocks.containsKey("$id:${def.stringId}") || Approachable.onNpcBlocks.containsKey(id)

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
    override fun hasOperate() = Operation.onObjectBlocks.containsKey("$id:${def.stringId}") || Operation.onObjectBlocks.containsKey(id)

    override fun hasApproach() = Approachable.onObjectBlocks.containsKey("$id:${def.stringId}") || Approachable.onObjectBlocks.containsKey(id)

    override fun operate() = Events.events.launch { Operation.operate(player, id, Item.EMPTY, slot, target) }

    override fun approach() = Events.events.launch { Approachable.approach(player, id, Item.EMPTY, slot, target) }
}

data class InteractInterfaceOnFloorItem(
    val player: Player,
    val target: FloorItem,
    val id: String,
    val slot: Int,
) : InteractionType {
    override fun hasOperate() = Operation.onObjectBlocks.containsKey("$id:${target.id}") || Operation.onObjectBlocks.containsKey(id)

    override fun hasApproach() = Approachable.onObjectBlocks.containsKey("$id:${target.id}") || Approachable.onObjectBlocks.containsKey(id)

    override fun operate() = Events.events.launch { Operation.operate(player, id, Item.EMPTY, slot, target) }

    override fun approach() = Events.events.launch { Approachable.approach(player, id, Item.EMPTY, slot, target) }
}