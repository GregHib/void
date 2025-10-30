package content.skill.magic.book.modern

import content.entity.combat.combatPrepare
import content.skill.magic.spell.spell
import content.skill.slayer.undead
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC

class CrumbleUndead : Script {

    init {
        combatPrepare(style = "magic") { player ->
            if (target is NPC && player.spell == "crumble_undead" && !target.undead) {
                player.clear("autocast")
                player.message("This spell only affects skeletons, zombies, ghosts and shades")
                cancel()
            }
        }
    }
}
