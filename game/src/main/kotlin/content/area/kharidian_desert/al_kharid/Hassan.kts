package content.area.kharidian_desert.al_kharid

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

npcOperate("Talk-to", "hassan") {
    npc<Talk>("Greetings! I am Hassan, Chancellor to the Emir of Al Kharid.")
    choice {
        anyHelp()
        tooHot()
        killWarriors()
        option<Talk>("I'd better be off.")
    }
}

fun ChoiceBuilder<NPCOption<Player>>.killWarriors() {
    option<Quiz>("Do you mind if I just kill your warriors?") {
        npc<Uncertain>("Kill our warriors? I assume this is some sort of joke?")
        player<Quiz>("I'll take that as a no. Forget I asked.")
        choice {
            anyHelp()
            tooHot()
            option("I'd better be off.")
        }
    }
}

fun ChoiceBuilder<NPCOption<Player>>.anyHelp() {
    option<Happy>("Can I help you? You must need some help here in the desert.") {
        player["prince_ali_rescue"] = "osman"
        npc<Uncertain>("I need the services of someone, yes. If you are interested, see the spymaster, Osman. I manage the finances here. Come to me when you need payment.")
    }
}

fun ChoiceBuilder<NPCOption<Player>>.tooHot() {
    option<Upset>("It's just too hot here. How can you stand it?") {
        npc<Talk>("We manage, in our humble way. We are a wealthy town and we have water. It cures many thirsts.")
        player.message("The chancellor hands you some water.", type = ChatType.Broadcast)
        player.inventory.add("jug_of_water")
        statement("The chancellor hands you some water.")
        choice {
            anyHelp()
            killWarriors()
            option<Talk>("I'd better be off.")
        }
    }
}