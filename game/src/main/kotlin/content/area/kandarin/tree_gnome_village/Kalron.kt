package content.area.kandarin.tree_gnome_village

import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Kalron : Script {
    init {
        npcOperate("Talk-to", "kalron_tree_gnome_village") {
            if (get("tree_gnome_village", "unstarted") == "completed") {
                player<Idle>("Hello there, you look lost.")
                npc<Idle>("Are you trying to be funny?")
                player<Idle>("No.")
                npc<Idle>("Hmmm.")
            } else {
                player<Idle>("Hello.")
                npc<Idle>("Gotta find a way out. We built this maze for protection but I can't get used to it. I'm always getting lost.")
            }
        }
    }
}
