package world.gregs.voidps.world.interact.entity.player.combat.armour

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.type.random
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.player.combat.consume.drink.antifire
import world.gregs.voidps.world.interact.entity.player.combat.consume.drink.superAntifire
import world.gregs.voidps.world.activity.skill.summoning.isFamiliar
import world.gregs.voidps.world.interact.entity.combat.hit.HitDamageModifier
import kotlin.math.floor

on<HitDamageModifier>({ type == "dragonfire" && it.isFamiliar }, Priority.HIGHISH) { _: NPC ->
    damage = (damage * 0.7).toInt()
}

on<HitDamageModifier>({ type == "dragonfire" }, Priority.HIGHISH) { player: Player ->
    val metal = target is NPC && (target.id.contains("bronze") || target.id.contains("iron") || target.id.contains("steel"))
    var multiplier = 1.0

    val shield = player.equipped(EquipSlot.Shield).id
    if (shield == "anti_dragon_shield" || shield.startsWith("dragonfire_shield")) {
        multiplier -= if (metal) 0.6 else 0.8
        player.message("Your shield absorbs most of the dragon's fiery breath!", ChatType.Filter)
    }

    if (player.antifire || player.superAntifire) {
        multiplier -= if (player.superAntifire) 1.0 else 0.5
    }

    if (multiplier > 0.0) {
        val black = target is NPC && target.id.contains("black")
        if (!metal && !black && random.nextDouble() <= 0.1) {
            multiplier -= 0.1
            player.message("You manage to resist some of the dragon fire!", ChatType.Filter)
        } else {
            player.message("You're horribly burnt by the dragon fire!", ChatType.Filter)
        }
    }

    damage = (damage * multiplier.coerceAtLeast(0.0)).toInt()
}

fun fireResistantShield(shield: String) = shield == "elemental_shield" || shield == "mind_shield" || shield == "body_shield" || shield == "dragonfire_shield"

on<HitDamageModifier>({ type == "icy_breath" && fireResistantShield(it.equipped(EquipSlot.Shield).id) }, Priority.HIGHISH) { _: Player ->
    damage = 100
}
