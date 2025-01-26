package content.skill.magic.book.modern

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC
import content.skill.slayer.undead
import content.entity.combat.combatPrepare
import content.skill.magic.spell.spell

combatPrepare(style = "magic") { player ->
    if (target is NPC && player.spell == "crumble_undead" && !target.undead) {
        player.clear("autocast")
        player.message("This spell only affects skeletons, zombies, ghosts and shades")
        cancel()
    }
}