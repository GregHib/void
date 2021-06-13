import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import kotlin.math.floor
import kotlin.random.Random

fun isTzhaarWeapon(weapon: Item) = weapon.name == "toktz-xil-ak" || weapon.name == "tzhaar-ket-om" || weapon.name == "tzhaar-ket-em" || weapon.name == "toktz-xil-ek"

on<HitDamageModifier>({ player -> skill == Skill.Strength && isTzhaarWeapon(player.equipped(EquipSlot.Weapon)) && player.equipped(EquipSlot.Amulet).name == "berserker_necklace" }, Priority.LOW) { _: Player ->
    damage = floor(damage * 1.20)
}

fun isDemon(target: Character?): Boolean = target != null

on<HitDamageModifier>({ player -> skill == Skill.Strength && player.equipped(EquipSlot.Weapon).name == "dark_light" && isDemon(target) }, Priority.LOW) { _: Player ->
    damage = floor(damage * 1.60)
}

fun isKalphite(target: Character?): Boolean = target != null

on<HitDamageModifier>({ player -> skill == Skill.Strength && player.equipped(EquipSlot.Weapon).name == "keris" && isKalphite(target) }, Priority.LOW) { _: Player ->
    damage = floor(damage * if (Random.nextDouble() < 0.51) 3.0 else 1.0 + 1.0 / 3.0)
}

fun isShade(target: Character?): Boolean = target != null

on<HitDamageModifier>({ player -> skill == Skill.Strength && player.equipped(EquipSlot.Weapon).name == "gadderhammer" && isShade(target) }, Priority.LOW) { _: Player ->
    damage = floor(damage * if (Random.nextDouble() < 0.05) 2.0 else 1.25)
}

on<HitDamageModifier>({ player -> skill == Skill.Strength && player.equipped(EquipSlot.Weapon).name.startsWith("dharoks_greataxe") && player.hasEffect("dharoks_set") }, Priority.LOW) { player: Player ->
    val lost = player.levels.getMax(Skill.Constitution) - player.levels.get(Skill.Constitution) / 100.0
    val max = player.levels.getMax(Skill.Constitution) / 100.0
    damage = floor(damage * (1 + lost * max))
}

on<HitDamageModifier>({ player -> skill == Skill.Strength && player.equipped(EquipSlot.Weapon).name.endsWith("vestas_longsword") }, Priority.LOW) { _: Player ->
    damage = floor(damage * 1.2)
}

on<HitDamageModifier>({ player -> skill == Skill.Strength && player.equipped(EquipSlot.Weapon).name.endsWith("statiuss_warhammer") }, Priority.LOW) { _: Player ->
    damage = floor(damage * 1.2)
}