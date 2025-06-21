package content.skill.summoning

import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.objectOperate

val pouchInterfaceId = 672
val pouchComponentId = 16

val scrollInterfaceId = 666
val scrollComponentId = 16

val width = 8
val height = 10
val slotLength = 78

objectOperate("Infuse-pouch") {
    openPouchCraftingInterface(player)
}

interfaceOption("Transform Scrolls", "scroll_creation_tab", "summoning_pouch_creation") {
    openScrollCraftingInterface(player)
}

interfaceOption("Infuse Pouches", "pouch_creation_tab", "summoning_scroll_creation") {
    openPouchCraftingInterface(player)
}

fun openPouchCraftingInterface(player: Player) {
    player.interfaces.open("summoning_pouch_creation")
    player.sendScript(
        "populate_summoning_pouch_creation",
        InterfaceDefinition.pack(pouchInterfaceId, pouchComponentId),
        width,
        height,
        1,
        slotLength,
        "Infuse<col=FF9040>",
        "Infuse-5<col=FF9040>",
        "Infuse-10<col=FF9040>",
        "Infuse-X<col=FF9040>",
        "Infuse-All<col=FF9040>",
        "List<col=FF9040>"
    )
    player.interfaceOptions.unlockAll("summoning_pouch_creation", "pouches", 0..400)
}

fun openScrollCraftingInterface(player: Player) {
    player.interfaces.open("summoning_scroll_creation")
    player.sendScript(
        "populate_summoning_scroll_creation",
        InterfaceDefinition.pack(scrollInterfaceId, scrollComponentId),
        width,
        height,
        1,
        slotLength,
        "Transform<col=FF9040>",
        "Transform-5<col=FF9040>",
        "Transform-10<col=FF9040>",
        "Transform-X<col=FF9040>",
        "Transform-All<col=FF9040>"
    )
    player.interfaceOptions.unlockAll("summoning_scroll_creation", "scrolls", 0..400)
}