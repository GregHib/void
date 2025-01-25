package content.area.asgarnia.falador

import org.rsmod.game.pathfinder.LineValidator
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.mode.move.hasLineOfSight
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.world.activity.quest.quest
import content.entity.player.dialogue.Frustrated
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Uncertain
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.world.interact.entity.sound.playSound
import java.util.concurrent.TimeUnit

val floorItems: FloorItems by inject()
val npcs: NPCs by inject()
val lineValidator: LineValidator by inject()

objectOperate("Open", "cupboard_the_knights_sword_closed") {
    player.playSound("cupboard_open")
    target.replace("cupboard_the_knights_sword_opened", ticks = TimeUnit.MINUTES.toTicks(3))
}

objectOperate("Shut", "cupboard_the_knights_sword_opened") {
    player.playSound("cupboard_close")
    target.replace("cupboard_the_knights_sword_closed")
}

objectOperate("Search", "cupboard_the_knights_sword_opened") {
    when (player.quest("the_knights_sword")) {
        "cupboard", "blurite_sword" -> {
            val sirVyvin = npcs[player.tile.regionLevel].firstOrNull { it.id == "sir_vyvin" }
            if (sirVyvin != null && lineValidator.hasLineOfSight(sirVyvin, player)) {
                player.talkWith(sirVyvin)
                npc<Frustrated>("HEY! Just WHAT do you THINK you are DOING??? STAY OUT of MY cupboard!")
                return@objectOperate
            }
            if (player.holdsItem("portrait")) {
                statement("There is just a load of junk in here.")
            } else {
                statement("You find a small portrait in here which you take.")
                if (player.inventory.isFull()) {
                    floorItems.add(player.tile, "portrait", disappearTicks = 300, owner = player)
                    return@objectOperate
                }
                player.inventory.add("portrait")
            }
        }
        else -> statement("There is just a load of junk in here.")
    }
}

npcOperate("Talk-to", "sir_vyvin") {
    player<Neutral>("Hello.")
    npc<Neutral>("Greetings traveller.")
    choice {
        option<Quiz>("Do you have anything to trade?") {
            npc<Neutral>("No, I'm sorry.")
        }
        option<Quiz>("Why are there so many knights in this city?") {
            npc<Neutral>("We are the White Knights of Falador. We are the most powerful order of knights in the land. We are helping the king Vallance rule the kingdom as he is getting old and tired.")
        }
        option("Can I just distract you for a minute?") {
            player<Neutral>("Can I just talk to you very slowly for a few minutes, while I distract you, so that my friend over there can do something while you're busy being distracted by me?")
            npc<Uncertain>("... ...what?")
            npc<Uncertain>("I'm... not sure what you're asking me... you want to join the White Knights?")
            player<Neutral>("Nope. I'm just trying to distract you.")
            npc<Uncertain>("... ...you are very odd.")
            player<Neutral>("So can I distract you some more?")
            npc<Uncertain>("... ...I don't think I want to talk to you anymore.")
            player<Neutral>("Ok. My work here is done. 'Bye!")
        }
    }
}