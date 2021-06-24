import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatDamage
import world.gregs.voidps.world.interact.entity.combat.CombatHit
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.player.poison
import kotlin.math.floor

on<HitDamageModifier>({ target != null && type == "range" && target.hasEffect("lucky_lightning") }, Priority.LOW) { player: Player ->
    damage += floor(player.levels.get(Skill.Range) * 0.1)
}

on<HitDamageModifier>({ target != null && type == "range" && target.hasEffect("armour_piercing") }, Priority.HIGH) { _: Player ->
    damage = floor(damage * 1.15)
}

// TODO other staves and npcs
fun isFirey(target: Character?): Boolean = target is Player && target.equipped(EquipSlot.Weapon).name == "staff_of_fire"
fun isWatery(target: Character?): Boolean = target is Player && target.equipped(EquipSlot.Weapon).name == "staff_of_water"

on<HitDamageModifier>({ target != null && type == "range" && target.hasEffect("sea_curse") && !isWatery(target) }, Priority.LOW) { player: Player ->
    damage += floor(player.levels.get(Skill.Range) * if (isFirey(target)) 1.0 / 15.0 else 0.05)
}

on<CombatHit>({ char -> type == "range" && char.hasEffect("dragons_breath") && !isFirey(char) }) { character: Character ->
    hit(character, source, source.levels.get(Skill.Range) * 2, "dragonfire", weapon)
}

on<HitDamageModifier>({ target != null && type == "range" && target.hasEffect("blood_forfeit") }, Priority.LOW) { player: Player ->
    damage = floor(player.levels.get(Skill.Constitution) * 0.2)
}

fun isUndead(target: Character?): Boolean = target != null

on<HitDamageModifier>({ target != null && type == "range" && target.hasEffect("life_leech") && !isUndead(target) }, Priority.HIGH) { _: Player ->
    damage = floor(damage * 1.20)
}

on<CombatDamage>({ char -> type == "range" && char.hasEffect("life_leech") }) { player: Player ->
    val heal = damage / 4
    player.levels.restore(Skill.Constitution, heal)
}

on<CombatHit>({ char -> type == "range" && char.hasEffect("earths_fury") }) { character: Character ->
    character.movement.frozen = true
    delay(character, 5) {
        character.movement.frozen = false
    }
}

on<CombatHit>({ char -> type == "range" && char.hasEffect("down_to_earth") }) { character: Character ->
    character.levels.drain(Skill.Magic, 1)
}

on<CombatHit>({ char -> type == "range" && char.hasEffect("clear_mind") }) { character: Character ->
    val amount = floor(source["range", 0] * 0.05).toInt()
    character.levels.drain(Skill.Prayer, amount)
    source.levels.restore(Skill.Prayer, amount / 2)
}

on<CombatHit>({ char -> type == "range" && char.hasEffect("magical_poison") }) { character: Character ->
    if (!character.hasEffect("poison")) {
        character.poison(source, 50)
    }
}

on<CombatDamage>({ char -> type == "range" && char.hasEffect("blood_forfeit") }) { player: Player ->
    val drain = floor(player.levels.get(Skill.Constitution) * 0.1).toInt()
    player.levels.drain(Skill.Constitution, drain)
}