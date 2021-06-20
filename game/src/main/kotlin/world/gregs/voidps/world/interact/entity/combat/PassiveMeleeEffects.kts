import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import kotlin.math.floor
import kotlin.random.Random

fun isTzhaarWeapon(weapon: String?) = weapon != null && (weapon == "toktz-xil-ak" || weapon == "tzhaar-ket-om" || weapon == "tzhaar-ket-em" || weapon == "toktz-xil-ek")

on<HitDamageModifier>({ player -> type == "melee" && isTzhaarWeapon(weapon?.name) && player.equipped(EquipSlot.Amulet).name == "berserker_necklace" }, Priority.LOW) { _: Player ->
    damage = floor(damage * 1.20)
}

fun isDemon(target: Character?): Boolean = target != null

on<HitDamageModifier>({ type == "melee" && weapon?.name == "dark_light" && isDemon(target) }, Priority.LOW) { _: Player ->
    damage = floor(damage * 1.60)
}

fun isKalphite(target: Character?): Boolean = target != null

on<HitDamageModifier>({ type == "melee" && weapon?.name == "keris" && isKalphite(target) }, Priority.LOW) { _: Player ->
    damage = floor(damage * if (Random.nextDouble() < 0.51) 3.0 else 1.0 + 1.0 / 3.0)
}

fun isShade(target: Character?): Boolean = target != null

on<HitDamageModifier>({ type == "melee" && weapon?.name == "gadderhammer" && isShade(target) }, Priority.LOW) { _: Player ->
    damage = floor(damage * if (Random.nextDouble() < 0.05) 2.0 else 1.25)
}

on<HitDamageModifier>({ player -> type == "melee" && weapon?.name?.startsWith("dharoks_greataxe") == true && player.hasEffect("dharoks_set") }, Priority.LOW) { player: Player ->
    val lost = player.levels.getMax(Skill.Constitution) - player.levels.get(Skill.Constitution) / 100.0
    val max = player.levels.getMax(Skill.Constitution) / 100.0
    damage = floor(damage * (1 + lost * max))
}