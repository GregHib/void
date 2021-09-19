package world.gregs.voidps.world.script

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCClick
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerClick
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.getComponentId
import world.gregs.voidps.engine.entity.definition.getComponentOrNull
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.utility.get

/**
 * Helper functions to make fake instruction calls in [WorldMock] tests
 */

fun Player.interfaceOption(
    name: String,
    component: String,
    option: String = "",
    optionIndex: Int = getOptionIndex(name, component, option) ?: -1,
    item: Item = Item("", -1),
    slot: Int = -1
) {
    val definitions: InterfaceDefinitions = get()
    val id = definitions.get(name).getComponentId(component) ?: -1
    events.emit(InterfaceOption(definitions.getId(name), name, id, component, optionIndex, option, item, slot))
}

private fun getOptionIndex(name: String, componentName: String, option: String): Int? {
    val definitions: InterfaceDefinitions = get()
    val definition = definitions.get(name)
    val component = definition.getComponentOrNull(componentName) ?: return null
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

fun Player.npcOption(npc: NPC, option: String) {
    val click = NPCClick(npc, option)
    events.emit(click)
    if (!click.cancel) {
        events.emit(NPCOption(npc, option, false))
    }
}