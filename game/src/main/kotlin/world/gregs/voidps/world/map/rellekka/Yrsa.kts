import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.*
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.event.InterfaceClosed
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.visual.update.player.BodyColour
import world.gregs.voidps.network.visual.update.player.BodyPart
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.npc.shop.OpenShop

val enums: EnumDefinitions by inject()

on<NPCOption>({ npc.id == "yrsa" && option == "Talk-to" }) { player: Player ->
    player.talkWith(npc) {
        npc("happy", """
            Hi. You wanted to buy some clothes? Or
            did you want to makeover your shoes?
        """)
        val choice = choice("""
            I'd like to buy some clothes.
            I'd like to change my shoes.
            Neither, thanks.
        """)
        when (choice) {
            1 -> {
                player("happy", "I'd like to buy some clothes.")
                player.events.emit(OpenShop("yrsas_shoe_store"))
            }
            2 -> {
                player("happy", "I'd like to change my shoes.")
                startShoeShopping(player, npc)
            }
            3 -> {
                player("talk", "Neither, thanks.")
                npc("talk", "As you wish.")
            }
        }
    }
}

on<NPCOption>({ npc.id == "yrsa" && option == "Change-shoes" }) { player: Player ->
    startShoeShopping(player, npc)
}

fun startShoeShopping(player: Player, npc: NPC) {
    player.dialogues.clear()
    if (player.equipped(EquipSlot.Weapon).isNotEmpty() || player.equipped(EquipSlot.Shield).isNotEmpty()) {
        player.talkWith(npc) {
            npc("afraid", """
                I don't feel comfortable showing you shoes when you are
                wielding something. Please remove what you are holding
                first.
            """)
        }
        return
    }
    if (player.equipped(EquipSlot.Feet).isNotEmpty()) {
        player.talkWith(npc) {
            npc("unsure", "You can't try on shoes with those on your feet.")
        }
        return
    }
    player.action(ActionType.Makeover) {
        try {
            delay(1)
            player.setGraphic("dressing_room_start")
            delay(1)
            player.open("yrsas_shoe_store")
            while (isActive) {
                player.setGraphic("dressing_room")
                delay(1)
            }
        } finally {
            player.close("yrsas_shoe_store")
            player.setGraphic("dressing_room_finish")
            player.flagAppearance()
            withContext(NonCancellable) {
                delay(1)
            }
        }
    }
}

on<InterfaceClosed>({ id == "yrsas_shoe_store" }) { player: Player ->
    player.action.cancel(ActionType.Makeover)
}

val maleEnum = 1136
val femaleEnum = 1137
val colourEnum = 3297

on<InterfaceOpened>({ id == "yrsas_shoe_store" }) { player: Player ->
    player.interfaces.sendText(id, "confirm_text", "Change")
    player.interfaceOptions.unlockAll(id, "styles", 0 until 40)
    val colours = enums.get(colourEnum)
    player.interfaceOptions.unlockAll(id, "colours", 0 until colours.length * 2)
    player.setVar("makeover_shoes", player.body.getLook(BodyPart.Feet))
    player.setVar("makeover_colour_shoes", player.body.getColour(BodyColour.Feet))
}

on<InterfaceOption>({ id == "yrsas_shoe_store" && component == "styles" }) { player: Player ->
    val index = itemSlot / 2
    val enumId = if (player.male) maleEnum else femaleEnum
    val value = enums.get(enumId).getInt(index)
    player.setVar("makeover_shoes", value)
}

on<InterfaceOption>({ id == "yrsas_shoe_store" && component == "colours" }) { player: Player ->
    player.setVar("makeover_colour_shoes", enums.get(colourEnum).getInt(itemSlot / 2))
}

on<InterfaceOption>({ id == "yrsas_shoe_store" && component == "confirm" }) { player: Player ->
    player.body.setLook(BodyPart.Feet, player.getVar("makeover_shoes"))
    player.body.setColour(BodyColour.Feet, player.getVar("makeover_colour_shoes"))
    player.flagAppearance()
    player.closeInterface()
    player.dialogue {
        npc("yrsa", "cheerful", "Hey, They look great!")
    }
}
