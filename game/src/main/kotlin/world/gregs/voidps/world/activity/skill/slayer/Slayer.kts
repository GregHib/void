import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.skill.slayer.hasSlayerTask
import world.gregs.voidps.world.activity.skill.slayer.isTask
import world.gregs.voidps.world.interact.entity.combat.HitChanceModifier
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import kotlin.math.floor

on<HitChanceModifier>({ offense }, priority = Priority.HIGH) { player: Player ->
    chance = floor(chance * getSlayerMultiplier(player, target, skill, false))
}

on<HitDamageModifier>(priority = Priority.HIGHER) { player: Player ->
    damage = floor(damage * getSlayerMultiplier(player, target, skill, true))
}

fun getSlayerMultiplier(player: Player, target: Character?, skill: Skill, damage: Boolean): Double {
    if (!player.hasSlayerTask || !player.isTask(target)) {
        return 1.0
    }
    val helm = player.equipped(EquipSlot.Hat).name
    if (skill == Skill.Strength) {
        val amulet = player.equipped(EquipSlot.Amulet).name
        if (amulet == "salve_amulet_e") {
            return 1.2
        }
        if (amulet == "salve_amulet" || helm.startsWith("black_mask") || helm.startsWith("slayer_helmet")) {
            return 1.15
        }
    }

    if (skill == Skill.Range && (helm == "focus_sight" || helm.startsWith("full_slayer_helmet"))) {
        return 1.15
    }
    if (damage && skill == Skill.Magic && (helm == "hexcrest" || helm.startsWith("full_slayer_helmet"))) {
        return 1.15
    }
    return 1.0
}