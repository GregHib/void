import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.client.instruction.InstructionHandlers
import world.gregs.voidps.engine.client.ui.InterfaceSwitch
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.dialogue.ContinueDialogue
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.client.ui.interact.ItemOnItem
import world.gregs.voidps.engine.client.ui.interact.ItemOnNPC
import world.gregs.voidps.engine.client.ui.interact.ItemOnObject
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.client.instruction.*
import world.gregs.voidps.type.Tile

/**
 * Helper functions to make fake instruction calls in [WorldTest] tests
 */

fun Player.itemOption(
    option: String,
    item: String = "",
    id: String = "inventory",
    component: String = "inventory",
    optionIndex: Int = getOptionIndex(id, component, option) ?: getItemOptionIndex(item, option) ?: -1,
    inventory: String = "inventory",
    slot: Int = inventories.inventory(inventory).indexOf(item)
) {
    Assertions.assertTrue(hasOpen(id)) { "Player $this doesn't have interface $id open" }
    val item = inventories.inventory(inventory).getOrNull(slot) ?: Item(item)
    val definition = get<InterfaceDefinitions>().getComponent(id, component)!!
    get<InstructionHandlers>().interactInterface.validate(this, InteractInterface(InterfaceDefinition.id(definition.id), InterfaceDefinition.componentId(definition.id), item.def.id, slot, optionIndex))
}

fun Player.interfaceOption(
    id: String,
    component: String,
    option: String = "",
    optionIndex: Int = getOptionIndex(id, component, option) ?: -1,
    item: Item = Item("", -1),
    slot: Int = -1
) {
    Assertions.assertTrue(hasOpen(id)) { "Player $this doesn't have interface $id open" }
    val definition = get<InterfaceDefinitions>().getComponent(id, component)!!
    get<InstructionHandlers>().interactInterface.validate(this, InteractInterface(InterfaceDefinition.id(definition.id), InterfaceDefinition.componentId(definition.id), item.def.id, slot, optionIndex))
}

fun Player.skillCreation(
    item: String,
    amount: Int = 1
) {
    Assertions.assertTrue(hasOpen("dialogue_skill_creation")) { "Player $this doesn't have interface dialogue_skill_creation open" }
    set("skill_creation_amount", amount)
    var index = -1
    for (i in 0 until 10) {
        val name = get<String>("skill_creation_name_$i") ?: continue
        if (item == name) {
            index = i
        }
    }
    get<InstructionHandlers>().enterInt.invoke(EnterInt(index), this)
}

fun Player.interfaceUse(
    id: String,
    inventory: String = "",
    fromItem: Item = Item("", -1),
    toItem: Item = Item("", -1),
    fromSlot: Int = -1,
    toSlot: Int = -1
) {
    Assertions.assertTrue(hasOpen(id)) { "Player $this doesn't have interface $id open" }
    emit(
        ItemOnItem(
            fromItem = fromItem,
            toItem = toItem,
            fromSlot = fromSlot,
            toSlot = toSlot,
            fromInventory = inventory,
            toInventory = inventory
        )
    )
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
    emit(
        InterfaceSwitch(
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
        )
    )
}

fun Player.equipItem(
    item: String,
    slot: Int = inventory.indexOf(item)
) {
    interfaceOption("inventory", "inventory", "Wield", item = Item(item, 1), slot = slot, optionIndex = Item(item).def.options.indexOf("Wield"))
}

fun Player.dialogueOption(
    component: String,
    option: Int = -1,
    id: String = dialogue!!
) = runTest {
    emit(ContinueDialogue(id, component, option))
}

fun Player.dialogueContinue(repeat: Int = 1) {
    repeat(repeat) {
        dialogueOption("continue")
    }
}

private fun getItemOptionIndex(item: String, option: String): Int? {
    val definitions: ItemDefinitions = get()
    val definition = definitions.getOrNull(item) ?: return null
    return definition.options.indexOf(option)
}

private fun getOptionIndex(id: String, componentId: String, option: String): Int? {
    val definitions: InterfaceDefinitions = get()
    val component = definitions.getComponent(id, componentId) ?: return null
    var options: Array<String?>? = component.options
    if (options != null) {
        val indexOf = options.indexOf(option)
        if (indexOf != -1) {
            return indexOf
        }
    }
    options = component.getOrNull("options") ?: return null
    val indexOf = options.indexOf(option)
    if (indexOf == -1) {
        return null
    }
    return indexOf
}

fun Player.playerOption(player: Player, option: String) = runTest {
    instructions.send(InteractPlayer(player.index, options.indexOf(option)))
}

fun Player.walk(toTile: Tile) = runTest {
    instructions.send(Walk(toTile.x, toTile.y))
}

fun Player.itemOnObject(obj: GameObject, itemSlot: Int, inventory: String = "inventory") {
    val item = inventories.inventory(inventory)[itemSlot]
    emit(ItemOnObject(this, obj, item, itemSlot, inventory))
}

fun Player.itemOnNpc(npc: NPC, itemSlot: Int, inventory: String = "inventory") {
    val item = inventories.inventory(inventory)[itemSlot]
    emit(ItemOnNPC(this, npc, item, itemSlot, inventory))
}

fun Player.itemOnItem(
    firstSlot: Int,
    secondSlot: Int,
    firstInventory: String = "inventory",
    secondInventory: String = firstInventory,
) {
    val one = inventories.inventory(firstInventory)
    val two = inventories.inventory(secondInventory)
    emit(
        ItemOnItem(
            one[firstSlot],
            two[secondSlot],
            firstSlot,
            secondSlot,
            firstInventory,
            secondInventory
        )
    )
}

fun Player.npcOption(npc: NPC, option: String) = npcOption(npc, npc.def.options.indexOf(option))

fun Player.npcOption(npc: NPC, option: Int) = runTest {
    instructions.send(InteractNPC(npc.index, option + 1))
}

fun Player.objectOption(gameObject: GameObject, option: String = "", optionIndex: Int? = null) = runTest {
    val def = get<ObjectDefinitions>().get(gameObject.intId)
    instructions.send(InteractObject(gameObject.intId, gameObject.tile.x, gameObject.tile.y, (optionIndex ?: def.optionsIndex(option)) + 1))
}

fun Player.floorItemOption(floorItem: FloorItem, option: String) = runTest {
    instructions.send(InteractFloorItem(floorItem.def.id, floorItem.tile.x, floorItem.tile.y, floorItem.def.floorOptions.indexOf(option)))
}

fun Inventory.set(index: Int, id: String, amount: Int = 1) = transaction { set(index, Item(id, amount)) }

fun Player.containsMessage(message: String) = messages.any { it.contains(message) }

val Player.messages: List<String>
    get() = get("messages", emptyList())
