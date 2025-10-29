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

data class InteractItemOnPlayer(
    val player: Player,
    val target: Player,
    val id: String,
    val item: Item,
    val slot: Int,
) : InteractionType {
    override fun hasOperate() = Operation.onPlayerBlocks.containsKey(item.id)

    override fun hasApproach() = Approachable.onPlayerBlocks.containsKey(item.id)

    override fun operate() = Events.events.launch { Operation.operate(player, id, slot, item, target) }

    override fun approach() = Events.events.launch { Approachable.approach(player, id, slot, item, target) }
}

data class InteractItemOnNPC(
    val player: Player,
    val target: NPC,
    val id: String,
    val item: Item,
    val slot: Int,
    val def: NPCDefinition,
) : InteractionType {
    override fun hasOperate() = Operation.onNpcBlocks.containsKey("${item.id}:${def.stringId}") || Operation.onNpcBlocks.containsKey(def.stringId)

    override fun hasApproach() = Approachable.onNpcBlocks.containsKey("${item.id}:${def.stringId}") || Approachable.onNpcBlocks.containsKey(def.stringId)

    override fun operate() = Events.events.launch { Operation.operate(player, id, item, slot, target) }

    override fun approach() = Events.events.launch { Approachable.approach(player, id, item, slot, target) }
}

data class InteractItemOnObject(
    val player: Player,
    val target: GameObject,
    val id: String,
    val item: Item,
    val slot: Int,
    val def: ObjectDefinition,
) : InteractionType {
    override fun hasOperate() = Operation.onObjectBlocks.containsKey("${item.id}:${def.stringId}") || Operation.onObjectBlocks.containsKey(def.stringId)

    override fun hasApproach() = Approachable.onObjectBlocks.containsKey("${item.id}:${def.stringId}") || Approachable.onObjectBlocks.containsKey(def.stringId)

    override fun operate() = Events.events.launch { Operation.operate(player, id, item, slot, target) }

    override fun approach() = Events.events.launch { Approachable.approach(player, id, item, slot, target) }
}

data class InteractItemOnFloorItem(
    val player: Player,
    val target: FloorItem,
    val id: String,
    val item: Item,
    val slot: Int,
) : InteractionType {
    override fun hasOperate() = Operation.onObjectBlocks.containsKey("${item.id}:${target.id}") || Operation.onObjectBlocks.containsKey(target.id)

    override fun hasApproach() = Approachable.onObjectBlocks.containsKey("${item.id}:${target.id}") || Approachable.onObjectBlocks.containsKey(target.id)

    override fun operate() = Events.events.launch { Operation.operate(player, id, item, slot, target) }

    override fun approach() = Events.events.launch { Approachable.approach(player, id, item, slot, target) }
}