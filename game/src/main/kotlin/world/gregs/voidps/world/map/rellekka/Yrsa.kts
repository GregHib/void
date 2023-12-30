package world.gregs.voidps.world.map.rellekka

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.event.InterfaceClosed
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.player.sex
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.visual.update.player.BodyColour
import world.gregs.voidps.network.visual.update.player.BodyPart
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.entity.npc.shop.openShop
import world.gregs.voidps.world.map.falador.openDressingRoom

val enums: EnumDefinitions by inject()

on<NPCOption>({ operate && target.id == "yrsa" && option == "Talk-to" }) { player: Player ->
    npc<Happy>("Hi. You wanted to buy some clothes? Or did you want to makeover your shoes?")
    choice {
        option<Happy>("I'd like to buy some clothes.") {
            player.openShop("yrsas_shoe_store")
        }
        option<Happy>("I'd like to change my shoes.") {
            startShoeShopping()
        }
        option<Talk>("Neither, thanks.") {
            npc<Talk>("As you wish.")
        }
    }
}

on<NPCOption>({ operate && target.id == "yrsa" && option == "Change-shoes" }) { player: Player ->
    startShoeShopping()
}

suspend fun CharacterContext.startShoeShopping() {
    player.closeDialogue()
    if (player.equipped(EquipSlot.Weapon).isNotEmpty() || player.equipped(EquipSlot.Shield).isNotEmpty()) {
        npc<Afraid>("I don't feel comfortable showing you shoes when you are wielding something. Please remove what you are holding first.")
        return
    }
    if (player.equipped(EquipSlot.Feet).isNotEmpty()) {
        npc<Unsure>("You can't try on shoes with those on your feet.")
        return
    }
    openDressingRoom("yrsas_shoe_store")
}

on<InterfaceClosed>({ id == "yrsas_shoe_store" }) { player: Player ->
    player.softTimers.stop("dressing_room")
}

on<InterfaceOpened>({ id == "yrsas_shoe_store" }) { player: Player ->
    player.interfaces.sendText(id, "confirm_text", "Change")
    player.interfaceOptions.unlockAll(id, "styles", 0 until 40)
    val colours = enums.get("colour_shoes")
    player.interfaceOptions.unlockAll(id, "colours", 0 until colours.length * 2)
    player["makeover_shoes"] = player.body.getLook(BodyPart.Feet)
    player["makeover_colour_shoes"] = player.body.getColour(BodyColour.Feet)
}

on<InterfaceOption>({ id == "yrsas_shoe_store" && component == "styles" }) { player: Player ->
    val value = enums.get("look_shoes_${player.sex}").getInt(itemSlot / 2)
    player["makeover_shoes"] = value
}

on<InterfaceOption>({ id == "yrsas_shoe_store" && component == "colours" }) { player: Player ->
    player["makeover_colour_shoes"] = enums.get("colour_shoes").getInt(itemSlot / 2)
}

on<InterfaceOption>({ id == "yrsas_shoe_store" && component == "confirm" }) { player: Player ->
    player.body.setLook(BodyPart.Feet, player["makeover_shoes", 0])
    player.body.setColour(BodyColour.Feet, player["makeover_colour_shoes", 0])
    player.flagAppearance()
    player.closeMenu()
    npc<Cheerful>("yrsa", "Hey, They look great!")
}
