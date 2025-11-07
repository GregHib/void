package content.skill.magic.book.modern

import content.skill.magic.spell.spell
import content.skill.slayer.undead
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC

class CrumbleUndead : Script {

    init {
        combatPrepare(style = "magic") { target ->
            if (target is NPC && spell == "crumble_undead" && !target.undead) {
                clear("autocast")
                message("This spell only affects skeletons, zombies, ghosts and shades")
                false
            } else {
                true
            }
        }
    }
}
