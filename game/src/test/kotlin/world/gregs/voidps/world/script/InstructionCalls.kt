package world.gregs.voidps.world.script

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.dialogue.ContinueDialogue
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnInterface
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnObject
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.definition.getComponentOrNull
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.network.instruct.*

/**
 * Helper functions to make fake instruction calls in [WorldTest] tests
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
    Assertions.assertTrue(hasOpen(id)) { "Player $this doesn't have interface $id open" }
    events.emit(InterfaceOption(this, id = id, component = component, optionIndex = optionIndex, option = option, item = item, itemSlot = slot, container = container))
}

fun Player.equipItem(
    item: String,
    slot: Int = inventory.indexOf(item)
) {
    interfaceOption("inventory", "container", "Wield", item = Item(item, 1), slot = slot, optionIndex = Item(item).def.options.indexOf("Wield"))
}

fun Player.dialogueOption(
    id: String,
    component: String,
    type: String = "make",
    option: Int = -1
) {
    events.emit(ContinueDialogue(id, component, /*type, */option))
}

private fun getOptionIndex(id: String, componentId: String, option: String): Int? {
    val definitions: InterfaceDefinitions = get()
    val definition = definitions.get(id)
    val component = definition.getComponentOrNull(componentId) ?: return null
    val options: Array<String> = component.getOrNull("options") ?: return null
    return options.indexOf(option)
}

@OptIn(ExperimentalCoroutinesApi::class)
fun Player.playerOption(player: Player, option: String) = runTest {
    instructions.emit(InteractPlayer(player.index, player.options.indexOf(option)))
}

@OptIn(ExperimentalCoroutinesApi::class)
fun Player.walk(toTile: Tile) = runTest {
    instructions.emit(Walk(toTile.x, toTile.y))
}

fun Player.itemOnObject(obj: GameObject, itemSlot: Int, id: String, component: String = "container", container: String = "inventory") {
    val item = containers.container(container)[itemSlot]
    events.emit(InterfaceOnObject(this, obj, id, component, item, itemSlot, container))
}

fun Player.itemOnItem(firstSlot: Int, secondSlot: Int, firstContainer: String = "inventory", firstComponent: String = "container", secondContainer: String = firstContainer, secondComponent: String = firstComponent) {
    val one = containers.container(firstContainer)
    val two = containers.container(secondContainer)
    events.emit(InterfaceOnInterface(
        one[firstSlot],
        two[secondSlot],
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

@OptIn(ExperimentalCoroutinesApi::class)
fun Player.npcOption(npc: NPC, option: String) = runTest {
    instructions.emit(InteractNPC(npc.index, npc.def.options.indexOf(option) + 1))
}

@OptIn(ExperimentalCoroutinesApi::class)
fun Player.objectOption(gameObject: GameObject, option: String) = runTest {
    val def = get<ObjectDefinitions>().get(gameObject.id)
    instructions.emit(InteractObject(def.id, gameObject.tile.x, gameObject.tile.y, def.optionsIndex(option) + 1))
}

@OptIn(ExperimentalCoroutinesApi::class)
fun Player.floorItemOption(floorItem: FloorItem, option: String) = runTest {
    instructions.emit(InteractFloorItem(floorItem.def.id, floorItem.tile.x, floorItem.tile.y, floorItem.def.floorOptions.indexOf(option)))
}

fun Container.set(index: Int, id: String, amount: Int = 1) = transaction { set(index, Item(id, amount, def = ItemDefinition.EMPTY))  }