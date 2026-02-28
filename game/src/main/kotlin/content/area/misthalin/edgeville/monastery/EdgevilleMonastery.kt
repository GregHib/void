package content.area.misthalin.edgeville.monastery

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.queue.queue

class EdgevilleMonastery : Script {
    init {
        objTeleportTakeOff("Climb-up", "monastery_ladder_up") { _, _ ->
            if (!get("edgeville_monastery_order_member", false)) {
                queue("edgeville_monastery_member_dialogue") {
                    npc<Neutral>("abbot_langley", "I'm sorry but only members of our order are allowed in the second level of the monastery.")
                    choice {
                        option<Neutral>("Well can I join your order?") {
                            if (!has(Skill.Prayer, 31)) {
                                npc<Neutral>("abbot_langley", "No. I am sorry, but I feel you are not devout enough.")
                                message("You need a prayer level of 31 to join the order.")
                                return@option
                            }
                            npc<Happy>("abbot_langley", "Ok, I see you are someone suitable for our order. You may join.")
                            set("edgeville_monastery_order_member", true)
                        }
                        option<Sad>("Oh, sorry.")
                    }
                }
                return@objTeleportTakeOff Teleport.CANCEL
            }
            anim("climb_up")
            start("teleport_delay", 2)
            return@objTeleportTakeOff Teleport.CONTINUE
        }
    }
}
