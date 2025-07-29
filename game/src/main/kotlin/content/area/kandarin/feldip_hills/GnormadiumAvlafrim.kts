package content.area.kandarin.feldip_hills

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.ChoiceBuilder
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.quest.questCompleted
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player

npcOperate("Talk-to", "gnormadium_avlafrim") {
    if (!player.questCompleted("one_small_favour")) {
        npc<Talk>("Hello! Don't get in the way around here, we've got a lot of work to do!")
        menu()
        return@npcOperate
    }
    choice {
        howsWork()
        canIFly()
        bye()
    }
}

npcOperate("Glider", "gnormadium_avlafrim") {
    if (!player.questCompleted("one_small_favour")) {
        player.message("You need to have completed One Small Favour quest to travel with the glider from here.") // TODO proper message
        return@npcOperate
    }
    player["glider_location"] = "lemantolly_undri"
    player.open("glider_map")
}

fun ChoiceBuilder<NPCOption<Player>>.howsWork() {
    option<Talk>("Hello, how's the work going?") {
        npc<Talk>("Getting there now, thanks to your help!")
        choice {
            canIFly()
            bye()
        }
    }
}

fun ChoiceBuilder<NPCOption<Player>>.canIFly() {
    option<Talk>("Can I take a flight in the glider?") {
        npc<Happy>("Sure, go ahead.")
        player["glider_location"] = "gandius"
        player.open("glider_map")
    }
}

fun ChoiceBuilder<NPCOption<Player>>.bye() {
    option<Happy>("Have a nice day.") {
        npc<Happy>("You too, human.")
    }
}

fun ChoiceBuilder<NPCOption<Player>>.helloThere() {
    option<Talk>("Hello there, what are you working on?") {
        npc<Talk>("Well, it's quite exciting... we're extending the glider network to include the Feldip Hills so that people can come and look at these curious ogres.")
        npc<Talk>("They are impressive creatures aren't they - quite mystified they are by the technology we gnomes have invented.")
        menu()
    }
}

fun ChoiceBuilder<NPCOption<Player>>.whatAreYouWorkingOn() {
    option<Talk>("What are you working on?") {
        npc<Talk>("Well, people will be able to visit this area, once construction has finished, by using the most advanced network of glider routes in the whole of Gielinor... in fact, the only network of glider routes!")
        menu()
    }
}

fun ChoiceBuilder<NPCOption<Player>>.anythingICanDo() {
    option<Talk>("Is there anything I can do?") {
        npc<Talk>("Not really, I'm afraid, it's probably all far too technical for someone like you.")
        menu()
    }
}

fun ChoiceBuilder<NPCOption<Player>>.thanks() {
    option<Talk>("Okay, thanks.")
}

suspend fun NPCOption<Player>.menu() {
    choice {
        helloThere()
        whatAreYouWorkingOn()
        anythingICanDo()
        thanks()
    }
}