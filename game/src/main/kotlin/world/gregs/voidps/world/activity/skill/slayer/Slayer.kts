import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.visual.EquipSlot
import world.gregs.voidps.world.activity.skill.slayer.hasSlayerTask
import world.gregs.voidps.world.activity.skill.slayer.isTask
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import world.gregs.voidps.world.interact.entity.combat.HitRatingModifier
import kotlin.math.floor

on<HitRatingModifier>({ offense }, priority = Priority.HIGH) { player: Player ->
    rating = floor(rating * getSlayerMultiplier(player, target, type, false))
}

on<HitDamageModifier>(priority = Priority.HIGHER) { player: Player ->
    damage = floor(damage * getSlayerMultiplier(player, target, type, true))
}

fun getSlayerMultiplier(player: Player, target: Character?, type: String, damage: Boolean): Double {
    if (!player.hasSlayerTask || !player.isTask(target)) {
        return 1.0
    }
    val helm = player.equipped(EquipSlot.Hat).id
    if (type == "melee") {
        val amulet = player.equipped(EquipSlot.Amulet).id
        if (amulet == "salve_amulet_e") {
            return 1.2
        }
        if (amulet == "salve_amulet" || helm.startsWith("black_mask") || helm.startsWith("slayer_helmet")) {
            return 1.15
        }
    }

    if (type == "range" && (helm == "focus_sight" || helm.startsWith("full_slayer_helmet"))) {
        return 1.15
    }
    if (damage && type == "magic" && (helm == "hexcrest" || helm.startsWith("full_slayer_helmet"))) {
        return 1.15
    }
    return 1.0
}