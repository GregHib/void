package content.area.asgarnia.falador

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.Tile

class ZandarHorfyre : Script {

    init {
        teleportLand("magic") {
            if (!get("zandar_horfyre_banish", false)) {
                return@teleportLand
            }
            clear("zandar_horfyre_banish")
            queue("zandar_horfyre_banish_dialogue") {
                player<Frustrated>("Zamorak curse that mage!")
                player<Laugh>("Actually, I guess he already has!")
            }
        }

        npcOperate("Talk-to", "zandar_horfyre_falador") { (target) ->
            player<Quiz>("Who are you?")
            npc<Angry>("My name is Zandar Horfyre. You, Player, are trespassing in my tower, not to mention attacking my students! I thank you to leave immediately!")
            choice("Select an option") {
                option<Neutral>("Okay, I was going anyway.") {
                    npc<Angry>("Good! And don't forget to close the door behind you!")
                }
                option<Neutral>("No, I think I'll stay for a bit.") {
                    npc<Angry>("Actually, that wasn't an invitation. I've tried being polite, now we'll do it the hard way!")
                    queue("zandar_horfyre_banish") {
                        target.anim("teleport_other")
                        target.gfx("teleport_other_casting")
                        delay(2)
                        gfx("teleport_other_impact")
                        anim("teleport_other_impact")
                        sound("teleport_modern")
                        delay(2)
                        gfx("teleport_other_impact")
                        set("zandar_horfyre_banish", true)
                        Teleport.teleport(this, Tile(3217, 3176), "magic", force = true)
                    }
                }
            }
        }
    }
}
