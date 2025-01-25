package content.area.fremennik_province.rellekka

import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.event.interfaceClose
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.player.sex
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyColour
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyPart
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.world.interact.entity.npc.shop.openShop
import content.area.asgarnia.falador.openDressingRoom

val enums: EnumDefinitions by inject()

npcOperate("Talk-to", "yrsa") {
    npc<Pleased>("Hi. You wanted to buy some clothes? Or did you want to makeover your shoes?")
    choice {
        option<Pleased>("I'd like to buy some clothes.") {
            player.openShop("yrsas_shoe_store")
        }
        option<Pleased>("I'd like to change my shoes.") {
            startShoeShopping()
        }
        option<Talk>("Neither, thanks.") {
            npc<Talk>("As you wish.")
        }
    }
}

npcOperate("Change-shoes", "yrsa") {
    startShoeShopping()
}

suspend fun Interaction<Player>.startShoeShopping() {
    player.closeDialogue()
    if (player.equipped(EquipSlot.Weapon).isNotEmpty() || player.equipped(EquipSlot.Shield).isNotEmpty()) {
        npc<Afraid>("I don't feel comfortable showing you shoes when you are wielding something. Please remove what you are holding first.")
        return
    }
    if (player.equipped(EquipSlot.Feet).isNotEmpty()) {
        npc<Quiz>("You can't try on shoes with those on your feet.")
        return
    }
    openDressingRoom("yrsas_shoe_store")
}

interfaceClose("yrsas_shoe_store") { player ->
    player.softTimers.stop("dressing_room")
}

interfaceOpen("yrsas_shoe_store") { player ->
    player.interfaces.sendText(id, "confirm_text", "Change")
    player.interfaceOptions.unlockAll(id, "styles", 0 until 40)
    val colours = enums.get("colour_shoes")
    player.interfaceOptions.unlockAll(id, "colours", 0 until colours.length * 2)
    player["makeover_shoes"] = player.body.getLook(BodyPart.Feet)
    player["makeover_colour_shoes"] = player.body.getColour(BodyColour.Feet)
}

interfaceOption(component = "styles", id = "yrsas_shoe_store") {
    val value = enums.get("look_shoes_${player.sex}").getInt(itemSlot / 2)
    player["makeover_shoes"] = value
}

interfaceOption(component = "colours", id = "yrsas_shoe_store") {
    player["makeover_colour_shoes"] = enums.get("colour_shoes").getInt(itemSlot / 2)
}

interfaceOption(component = "confirm", id = "yrsas_shoe_store") {
    player.body.setLook(BodyPart.Feet, player["makeover_shoes", 0])
    player.body.setColour(BodyColour.Feet, player["makeover_colour_shoes", 0])
    player.flagAppearance()
    player.closeMenu()
    npc<Happy>("yrsa", "Hey, They look great!")
}
