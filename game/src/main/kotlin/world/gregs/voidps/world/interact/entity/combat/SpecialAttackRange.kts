import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import world.gregs.voidps.world.interact.entity.combat.specialAttack
import kotlin.math.floor

on<HitDamageModifier>({ player -> skill == Skill.Range && player.specialAttack && player.equipped(EquipSlot.Weapon).name == "dark_bow" }, Priority.HIGH) { player: Player ->
    damage = floor(damage * if (player.equipped(EquipSlot.Ammo).name == "dragon_arrow") 1.50 else 1.30)
}

fun isWeaponOutlier(special: Boolean, name: String): Boolean = (special && name.startsWith("magic") || name == "seercull" || name == "rune_thrownaxe") || name == "ogre_bow"

on<HitDamageModifier>({ player -> skill == Skill.Range && isWeaponOutlier(player.specialAttack, player.equipped(EquipSlot.Weapon).name) }, Priority.HIGH) { player: Player ->
    damage = 0.5 + (player.levels.get(skill) + 10) * strengthBonus / 640
    val weapon = player.equipped(EquipSlot.Weapon).name
    if (weapon == "rune_thrownaxe" || (weapon == "magic_shortbow" && target is Player)) {
        damage += 1.0
    }
}

on<HitDamageModifier>({ skill == Skill.Range && it.specialAttack && it.equipped(EquipSlot.Weapon).name.endsWith("morrigans_throwing_axe") }, Priority.LOW) { player: Player ->
    damage = floor(damage * 1.2)
}