package world.gregs.voidps.world.activity.skill.slayer

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.hit.HitDamageModifier
import world.gregs.voidps.world.interact.entity.combat.hit.HitRatingModifier
import kotlin.math.floor

on<HitRatingModifier>({ offense }, priority = Priority.HIGH) { player: Player ->
    rating = floor(rating * getSlayerMultiplier(player, target, type, false))
}

on<HitDamageModifier>(priority = Priority.HIGHER) { player: Player ->
    damage = (damage * getSlayerMultiplier(player, target, type, true)).toInt()
}

fun getSlayerMultiplier(player: Player, target: Character, type: String, damage: Boolean): Double {
    if (type == "melee" && target is NPC && target.undead) {
        when (player.equipped(EquipSlot.Amulet).id) {
            "salve_amulet_e" -> return 1.2
            "salve_amulet" -> return 7.0 / 6.0
        }
    }
    if (!player.hasSlayerTask || !player.isTask(target)) {
        return 1.0
    }
    val helm = player.equipped(EquipSlot.Hat).id
    if (type == "melee" && (helm.startsWith("black_mask") || helm.startsWith("slayer_helmet"))) {
        return 7.0 / 6.0
    }
    if (type == "range" && (helm == "focus_sight" || helm.startsWith("full_slayer_helmet"))) {
        return 1.15
    }
    if (damage && type == "magic" && (helm == "hexcrest" || helm.startsWith("full_slayer_helmet"))) {
        return 1.15
    }
    return 1.0
}