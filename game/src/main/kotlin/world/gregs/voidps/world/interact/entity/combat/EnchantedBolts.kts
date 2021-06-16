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

fun isUndead(target: Character?): Boolean = target != null

// TODO other staves and npcs
fun isFirey(target: Character?): Boolean = target is Player && target.equipped(EquipSlot.Weapon).name == "staff_of_fire"
fun isWatery(target: Character?): Boolean = target is Player && target.equipped(EquipSlot.Weapon).name == "staff_of_water"

on<HitDamageModifier>({ player -> type == "range" && player.hasEffect("life_leech") && !isUndead(target) }, Priority.HIGH) { _: Player ->
    damage = floor(damage * 1.20)
}

on<HitDamageModifier>({ player -> type == "range" && player.hasEffect("armour_piercing") }, Priority.HIGH) { _: Player ->
    damage = floor(damage * 1.15)
}

on<HitDamageModifier>({ player -> type == "range" && player.hasEffect("lucky_lightning") }, Priority.LOW) { player: Player ->
    damage += floor(player.levels.get(Skill.Range) * 0.1)
}

on<HitDamageModifier>({ player -> type == "range" && player.hasEffect("sea_curse") && !isWatery(target) }, Priority.LOW) { player: Player ->
    damage += floor(player.levels.get(Skill.Range) * if (isFirey(target)) 1.0 / 15.0 else 0.05)
}

on<HitDamageModifier>({ player -> type == "range" && player.hasEffect("dragons_breath") && !isFirey(target) && target?.hasEffect("anti-fire") != true }, Priority.LOW) { player: Player ->
    damage = floor(damage * (player.levels.get(Skill.Range) * 0.2))
}

on<HitDamageModifier>({ player -> type == "range" && player.hasEffect("blood_forfeit") }, Priority.LOW) { player: Player ->
    damage = floor(player.levels.get(Skill.Constitution) * 0.2)
}