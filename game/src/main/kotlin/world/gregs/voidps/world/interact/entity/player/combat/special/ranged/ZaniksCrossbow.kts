import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot
import kotlin.random.Random
import kotlin.random.nextInt

fun isCrossbow(weapon: Item?) = weapon != null && weapon.name == "zaniks_crossbow"

fun hasActivePrayer(player: Player) = player.values.temporary.any { (key, value) -> key.startsWith("prayer_") && value == true }

fun hasGodArmour(player: Player) = false

on<HitDamageModifier>({ player -> type == "range" && player.specialAttack && weapon?.name == "zaniks_crossbow" }, Priority.HIGH) { _: Player ->
    if (target is NPC) {
        damage += Random.nextInt(30..150)
    } else if (target is Player && (hasActivePrayer(target) || hasGodArmour(target))) {
        damage += Random.nextInt(0..150)
    }
}

on<CombatDamage>({ isCrossbow(weapon) && special }) { _: Player ->
    target.levels.drain(Skill.Defence, damage / 10)
}

on<CombatSwing>({ player -> !swung() && player.specialAttack && isCrossbow(player.weapon) }, Priority.HIGHISH) { player: Player ->
    if (!drainSpecialEnergy(player, 500)) {
        return@on
    }
    player.setAnimation("zaniks_crossbow_special")
    player.setGraphic("zaniks_crossbow_special")
    player.shoot(name = "zaniks_crossbow_bolt", target = target, delay = 80, height = 43, endHeight = target.height, curve = 8)
    player.hit(target)
    val speed = player.weapon.def.getOrNull("attack_speed") as? Int ?: 4
    delay = if (player.attackType == "rapid") speed - 1 else speed
}