import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.network.instruct.Command
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack

on<Command>({ prefix == "maxhit" }) { player: Player ->
    val debug = player["debug", false]
    player["debug"] = false
    val weapon = player.equipped(EquipSlot.Weapon)
    player.message("Max hit")
    player.message("Ranged: ${getMaximumHit(player, null, "range", weapon)} Melee: ${getMaximumHit(player, null, "melee", weapon)} Magic: ${getMaximumHit(player, null, "spell", null)}")
    player.message("Hit chance")
    player.message("Ranged: ${hitChance(player, null, "range", weapon)} Melee: ${hitChance(player, null, "melee", weapon)} Magic: ${hitChance(player, null, "spell", null)}")
    player["debug"] = debug
}

val logger = InlineLogger()

val Character.name: String
    get() = (this as? Player)?.name ?: (this as NPC).name

on<CombatSwing>({ player -> player["debug", false] }, Priority.HIGHEST) { player: Player ->
    player.message("---- Swing (${player.name}) -> (${target.name}) -----")
}

on<HitEffectiveLevelModifier>({ player -> player["debug", false] }, Priority.LOWEST) { player: Player ->
    val message = "${if (accuracy) "Accuracy" else "Damage"} effective level: $level (${skill.name.toLowerCase()})"
    player.message(message)
    logger.debug { message }
}

on<HitEffectiveLevelOverride>({ player -> player["debug", false] }, Priority.LOWEST) { player: Player ->
    val message = "${if (defence) "Defender" else "Attacker"} effective level: $level ($type)"
    player.message(message)
    logger.debug { message }
}

on<HitRatingModifier>({ player -> player["debug", false] }, Priority.LOWEST) { player: Player ->
    val message = "${if (offense) "Offensive" else "Defensive"} rating: $rating ($type)"
    player.message(message)
    logger.debug { message }
}

on<HitChanceModifier>({ player -> player["debug", false] }, Priority.LOWEST) { player: Player ->
    val message = "Hit chance: $chance ($type, ${weapon?.name}${if (player.specialAttack) ", special" else ""})"
    player.message(message)
    logger.debug { message }
}

on<HitDamageModifier>({ player -> player["debug", false] }, Priority.LOWEST) { player: Player ->
    val message = "Max damage: $damage ($type, $strengthBonus str, ${weapon?.name}${if (player.specialAttack) ", special" else ""})"
    player.message(message)
    logger.debug { message }
}

on<CombatDamage>({ player -> player["debug", false] }, Priority.LOWEST) { player: Player ->
    val message = "Damage ${(target as? Player)?.name ?: (target as NPC).name}: $damage ($type, ${weapon?.name})"
    player.message(message)
    logger.debug { message }
}
