package world.gregs.voidps.world.map.falador

import org.rsmod.game.pathfinder.LineValidator
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.mode.move.hasLineOfSight
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.hasItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.world.interact.dialogue.Angry
import world.gregs.voidps.world.interact.dialogue.Talking
import world.gregs.voidps.world.interact.dialogue.Uncertain
import world.gregs.voidps.world.interact.dialogue.Unsure
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.dialogue.type.statement
import world.gregs.voidps.world.interact.entity.sound.playSound
import java.util.concurrent.TimeUnit

val floorItems: FloorItems by inject()
val npcs: NPCs by inject()
val lineValidator: LineValidator by inject()

on<ObjectOption>({ operate && target.id == "cupboard_the_knights_sword_closed" && option == "Open" }) { player: Player ->
    player.playSound("cupboard_open")
    target.replace("cupboard_the_knights_sword_opened", ticks = TimeUnit.MINUTES.toTicks(3))
}

on<ObjectOption>({ operate && target.id == "cupboard_the_knights_sword_opened" && option == "Shut" }) { player: Player ->
    player.playSound("cupboard_close")
    target.replace("cupboard_the_knights_sword_closed")
}

on<ObjectOption>({ operate && target.id == "cupboard_the_knights_sword_opened" && option == "Search" }) { player: Player ->
    when (player["the_knights_sword", "unstarted"]) {
        "cupboard", "blurite_sword" -> {
            val sirVyvin = npcs[player.tile.regionLevel].firstOrNull { it.id == "sir_vyvin" }
            if (sirVyvin != null && lineValidator.hasLineOfSight(sirVyvin, player)) {
                player.talkWith(sirVyvin)
                npc<Angry>("""
                    HEY! Just WHAT do you THINK you are
                     DOING??? STAY OUT of MY cupboard!
                """)
                return@on
            }
            if (player.hasItem("portrait")) {
                statement("There is just a load of junk in here.")
            } else {
                statement("You find a small portrait in here which you take.")
                if (player.inventory.isFull()) {
                    floorItems.add(player.tile, "portrait", disappearTicks = 300, owner = player)
                    return@on
                }
                player.inventory.add("portrait")
            }
        }
        else -> statement("There is just a load of junk in here.")
    }
}

on<NPCOption>({ operate && target.id == "sir_vyvin" && option == "Talk-to" }) { player: Player ->
    player<Talking>("Hello.")
    npc<Talking>("Greetings traveller.")
    choice {
        option<Unsure>("Do you have anything to trade?") {
            npc<Talking>("No, I'm sorry.")
        }
        option<Unsure>("Why are there so many knights in this city?") {
            npc<Talking>("""
                We are the White Knights of Falador. We are the most
                powerful order of knights in the land. We are helping
                the king Vallance rule the kingdom as he is getting old
                and tired.
            """)
        }
        option("Can I just distract you for a minute?") {
            player<Talking>("""
                Can I just talk to you very slowly for a few minutes,
                while I distract you, so that my friend over there can
                do something while you're busy being distracted by me?
            """)
            npc<Uncertain>("... ...what?")
            npc<Uncertain>("""
                I'm... not sure what you're asking me... you want to
                join the White Knights?
            """)
            player<Talking>("Nope. I'm just trying to distract you.")
            npc<Uncertain>("... ...you are very odd.")
            player<Talking>("So can I distract you some more?")
            npc<Uncertain>("... ...I don't think I want to talk to you anymore.")
            player<Talking>("Ok. My work here is done. 'Bye!")
        }
    }
}