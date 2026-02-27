package content.area.misthalin.draynor_village.wise_old_man

import content.entity.combat.killer
import content.entity.effect.transform
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound

class ThingUnderTheBed : Script {
    init {
        npcOperate("Kick", "bed_draynor") { (target) ->
            watch(target)
            anim("unarmed_kick")
            sound("unarmed_kick")
            delay(1)
            target.anim("bed_block")
            clearWatch()
            if (get("wise_old_man_npc", "") != "thing_under_the_bed") {
                return@npcOperate
            }
            if (get("wise_old_man_remaining", 0) == 0) {
                player<Happy>("I think it's already dead. Maybe the Wise Old Man will reward me now?")
                return@npcOperate
            }
            target.say("Gurrhh!")
            jingle("wise_old_man")
            delay(1)
            target.transform("thing_under_the_bed")
            target.interactPlayer(this, "Attack")
        }

        npcDeath("bed_draynor") {
            val killer = killer
            if (killer is Player) {
                killer.dec("wise_old_man_remaining")
                killer.inc("wise_old_man_bed_kills")
            }
        }
    }
}
