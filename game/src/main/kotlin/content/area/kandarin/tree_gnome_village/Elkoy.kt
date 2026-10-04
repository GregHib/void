package content.area.kandarin.tree_gnome_village

import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player

class Elkoy : Script {
    init {
        npcOperate("Talk-to", "elkoy_tree_gnome_village*") { (target) ->
            val outside = target.id == "elkoy_tree_gnome_village" || target.id == "elkoy_tree_gnome_village_3"
            if (get("tree_gnome_village", "unstarted") != "completed") {
                player<Idle>("Hello there.")
                npc<Idle>(if (outside) "Hello, welcome to our maze. I'm Elkoy the tree gnome." else "Hello, welcome to our village. I'm Elkoy the tree gnome.")
                player<Idle>(if (outside) "I haven't heard of your sort." else "I haven't heard of your sort before.")
                npc<Idle>("There's not many of us left. Once you could find tree gnomes anywhere in the world, now we hide in small groups to avoid capture.")
                player<Idle>("Capture by whom?")
                npc<Idle>("Tree gnomes have been hunted for so called 'fun' since as long as I can remember.")
                npc<Idle>("Our main threat nowadays are General Khazard's troops. They know no mercy, but are also very dense. They'll never find their way through our maze.")
                npc<Idle>("Have fun.")
            } else {
                player<Idle>("Hello Elkoy.")
                npc<Idle>(if (outside) "Hi there, I hope life is treating you well. Would you like me to show you the way to the village?" else "Hi there, I hope life is treating you well. Would you like me to show you the way out of the village?")
                choice {
                    option("Yes please.") {
                        player<Idle>("Yes please.")
                        guide(outside)
                    }
                    option(if (outside) "No thanks Elkoy." else "Not now, thanks.") {
                        player<Idle>(if (outside) "No thanks Elkoy." else "Not now, thanks.")
                        if (outside) npc<Idle>("Ok then, take care.")
                    }
                }
            }
        }
        npcOperate("Follow", "elkoy_tree_gnome_village*") { (target) ->
            if (questStage("tree_gnome_village") == 0) return@npcOperate
            guide(target.id == "elkoy_tree_gnome_village" || target.id == "elkoy_tree_gnome_village_3")
        }
    }

    private suspend fun Player.guide(outside: Boolean) {
        message("Elkoy guides you through the maze.")
        if (outside) tele(2515, 3160) else tele(2504, 3190)
        npc<Idle>(if (outside) "Here we are. Feel free to have a look around." else "Here we are. Have a safe journey.")
    }
}
