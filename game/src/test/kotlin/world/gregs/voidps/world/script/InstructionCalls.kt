package world.gregs.voidps.world.script

import io.mockk.every
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnInterface
import world.gregs.voidps.engine.entity.character.contain.container
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCClick
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerClick
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.getComponentOrNull
import world.gregs.voidps.engine.entity.item.FloorItem
import world.gregs.voidps.engine.entity.item.FloorItemClick
import world.gregs.voidps.engine.entity.item.FloorItemOption
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectClick
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.sync
import world.gregs.voidps.engine.utility.get

/**
 * Helper functions to make fake instruction calls in [WorldMock] tests
 */

fun Player.interfaceOption(
    id: String,
    component: String,
    option: String = "",
    optionIndex: Int = getOptionIndex(id, component, option) ?: -1,
    item: Item = Item("", -1),
    slot: Int = -1,
    container: String = ""
) {
    events.emit(InterfaceOption(id, component, optionIndex, option, item, slot, container))
}

private fun getOptionIndex(id: String, componentId: String, option: String): Int? {
    val definitions: InterfaceDefinitions = get()
    val definition = definitions.get(id)
    val component = definition.getComponentOrNull(componentId) ?: return null
    val options: Array<String> = component.getOrNull("options") as? Array<String> ?: return null
    return options.indexOf(option)
}

fun Player.playerOption(player: Player, option: String) {
    val click = PlayerClick(player, option)
    events.emit(click)
    if (!click.cancel) {
        events.emit(PlayerOption(player, option, player.options.indexOf(option)))
    }
}

fun Player.itemOnItem(firstSlot: Int, secondSlot: Int, firstContainer: String = "inventory", firstComponent: String = "container", secondContainer: String = firstContainer, secondComponent: String = firstComponent) = sync {
    val one = container(firstContainer)
    val two = container(secondContainer)
    events.emit(InterfaceOnInterface(
        one.getItem(firstSlot),
        two.getItem(secondSlot),
        firstSlot,
        secondSlot,
        firstContainer,
        firstComponent,
        secondContainer,
        secondComponent,
        firstContainer,
        secondContainer
    ))
}

fun Player.npcOption(npc: NPC, option: String) = sync {
    val click = NPCClick(npc, option)
    events.emit(click)
    if (!click.cancel) {
        events.emit(NPCOption(npc, option, false))
    }
}

fun Player.objectOption(gameObject: GameObject, option: String) {
    val click = ObjectClick(gameObject, option)
    events.emit(click)
    if (!click.cancel) {
        events.emit(ObjectOption(gameObject, option, false))
    }
}

fun Player.floorItemOption(floorItem: FloorItem, option: String) {
    val click = FloorItemClick(floorItem, option)
    events.emit(click)
    if (!click.cancel) {
        events.emit(FloorItemOption(floorItem, option, false))
    }
}

fun mockStackableItem(id: Int) {
    every { get<ItemDecoder>().get(id) } returns ItemDefinition(
        id = id,
        stackable = 1
    )
}

fun mockNotedItem(id: Int) {
    every { get<ItemDecoder>().get(id) } returns ItemDefinition(
        id = id,
        stackable = 1,
        noteId = id - 1,
        notedTemplateId = 1234
    )
}

fun mockNotableItem(id: Int) {
    every { get<ItemDecoder>().get(id) } returns ItemDefinition(
        id = id,
        stackable = 1,
        noteId = id + 1
    )
}

fun mockItemExtras(id: Int, extras: Map<String, Any>) {
    every { get<ItemDecoder>().get(id) } returns ItemDefinition(
        id = id,
        extras = extras
    )
}