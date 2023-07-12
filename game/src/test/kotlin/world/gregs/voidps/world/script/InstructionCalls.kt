package world.gregs.voidps.world.script

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.InterfaceSwitch
import world.gregs.voidps.engine.client.ui.dialogue.ContinueDialogue
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.client.ui.interact.ItemOnItem
import world.gregs.voidps.engine.client.ui.interact.ItemOnObject
import world.gregs.voidps.engine.contain.Inventory
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.instruct.*
import world.gregs.voidps.type.Tile

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
    inventory: String = ""
) {
    Assertions.assertTrue(hasOpen(id)) { "Player $this doesn't have interface $id open" }
    events.emit(InterfaceOption(this, id = id, component = component, optionIndex = optionIndex, option = option, item = item, itemSlot = slot, inventory = inventory))
}
fun Player.interfaceUse(
    id: String,
    component: String,
    inventory: String = "",
    fromItem: Item = Item("", -1),
    toItem: Item = Item("", -1),
    fromSlot: Int = -1,
    toSlot: Int = -1
) {
    Assertions.assertTrue(hasOpen(id)) { "Player $this doesn't have interface $id open" }
    events.emit(ItemOnItem(
        fromItem = fromItem,
        toItem = toItem,
        fromSlot = fromSlot,
        toSlot = toSlot,
        fromInterface = id,
        fromComponent = component,
        toInterface = id,
        toComponent = component,
        fromInventory = inventory,
        toInventory = inventory
    ))
}

fun Player.interfaceSwitch(
    id: String,
    component: String,
    inventory: String = "",
    fromItem: Item = Item("", -1),
    toItem: Item = Item("", -1),
    fromSlot: Int = -1,
    toSlot: Int = -1
) {
    Assertions.assertTrue(hasOpen(id)) { "Player $this doesn't have interface $id open" }
    events.emit(InterfaceSwitch(
        id = id,
        component = component,
        fromItem = fromItem,
        fromSlot = fromSlot,
        fromInventory = inventory,
        toId = id,
        toComponent = component,
        toItem = toItem,
        toSlot = toSlot,
        toInventory = inventory
    ))
}

fun Player.equipItem(
    item: String,
    slot: Int = inventory.indexOf(item)
) {
    interfaceOption("inventory", "inventory", "Wield", item = Item(item, 1), slot = slot, optionIndex = Item(item).def.options.indexOf("Wield"))
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
    val component = definitions.getComponent(id, componentId) ?: return null
    val options: Array<String> = component.getOrNull("options") ?: return null
    return options.indexOf(option)
}

fun Player.playerOption(player: Player, option: String) = runTest {
    instructions.emit(InteractPlayer(player.index, player.options.indexOf(option)))
}

fun Player.walk(toTile: Tile) = runTest {
    instructions.emit(Walk(toTile.x, toTile.y))
}

fun Player.itemOnObject(obj: GameObject, itemSlot: Int, id: String, component: String = "inventory", inventory: String = "inventory") {
    val item = inventories.inventory(inventory)[itemSlot]
    events.emit(ItemOnObject(this, obj, id, component, item, itemSlot, inventory))
}

fun Player.itemOnItem(firstSlot: Int, secondSlot: Int, firstInventory: String = "inventory", firstComponent: String = "inventory", secondInventory: String = firstInventory, secondComponent: String = firstComponent) {
    val one = inventories.inventory(firstInventory)
    val two = inventories.inventory(secondInventory)
    events.emit(ItemOnItem(
        one[firstSlot],
        two[secondSlot],
        firstSlot,
        secondSlot,
        firstInventory,
        firstComponent,
        secondInventory,
        secondComponent,
        firstInventory,
        secondInventory
    ))
}

fun Player.npcOption(npc: NPC, option: String) = runTest {
    instructions.emit(InteractNPC(npc.index, npc.def.options.indexOf(option) + 1))
}

fun Player.objectOption(gameObject: GameObject, option: String) = runTest {
    val def = get<ObjectDefinitions>().get(gameObject.intId)
    instructions.emit(InteractObject(gameObject.intId, gameObject.tile.x, gameObject.tile.y, def.optionsIndex(option) + 1))
}

fun Player.floorItemOption(floorItem: FloorItem, option: String) = runTest {
    instructions.emit(InteractFloorItem(floorItem.def.id, floorItem.tile.x, floorItem.tile.y, floorItem.def.floorOptions.indexOf(option)))
}

fun Inventory.set(index: Int, id: String, amount: Int = 1) = transaction { set(index, Item(id, amount, def = ItemDefinition.EMPTY))  }