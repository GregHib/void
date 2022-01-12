package world.gregs.voidps.world.interact.entity.player.combat.range.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier

fun isWeaponOutlier(special: Boolean, id: String?): Boolean = (special && id?.startsWith("magic") == true || id == "seercull" || id == "rune_thrownaxe") || id == "ogre_bow"

on<HitDamageModifier>({ type == "range" && isWeaponOutlier(special, weapon?.id) }, Priority.HIGH) { player: Player ->
    damage = 0.5 + (player.levels.get(Skill.Ranged) + 10) * strengthBonus / 64
    if (weapon?.id == "rune_thrownaxe" || (weapon?.id == "magic_shortbow" && target is Player)) {
        damage += 1.0
    }
}