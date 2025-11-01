package content.area.misthalin.draynor_village

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class DraynorJailGuard : Script {

    init {
        npcOperate("Talk-to", "draynor_jail_guard*") {
            player<Quiz>("Hi. Who are you guarding here?")
            npc<Angry>("Can't say. It's all very secret. You should get out of here. I am not supposed to talk while I guard.")
            choice {
                option("Hey, chill out. I won't cause you trouble.") {
                    player<Happy>("Hey, chill out, I won't cause you trouble. I was just wondering what you do to relax.")
                    npc<Talk>("You never relax with these people, but it's a good career for a young man.")
                }
                option<Talk>("I had better leave, I don't want trouble.") {
                    npc<Talk>("Thanks, I appreciate that. Talking on duty can be punished by having your mouth stitched up. These are tough people, make no mistake.")
                }
            }
        }
    }
}
