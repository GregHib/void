package world.gregs.voidps.world.interact.entity.combat

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack

on<Command>({ prefix == "maxhit" }) { player: Player ->
    val debug = player["debug", false]
    player["debug"] = false
    val weapon = player.equipped(EquipSlot.Weapon)
    player.message("Max hit")
    player.message("Ranged: ${Damage.maximum(player, type = "range", weapon = weapon)} Melee: ${Damage.maximum(player, type = "melee", weapon = weapon)} Magic: ${Damage.maximum(player, type = "magic")}")
    player.message("Hit chance")
    player.message("Ranged: ${Hit.chance(player, type = "range", weapon = weapon)} Melee: ${Hit.chance(player, type = "melee", weapon = weapon)} Magic: ${Hit.chance(player, type = "magic")}")
    player["debug"] = debug
}

val logger = InlineLogger()

val Character.charName: String
    get() = (this as? Player)?.name ?: (this as NPC).id

on<CombatSwing>({ it["debug", false] || target["debug", false] }, Priority.HIGHEST) { character: Character ->
    val player = if (character["debug", false] && character is Player) character else target as Player
    player.message("---- Swing (${character.charName}) -> (${target.charName}) -----")
}

on<HitEffectiveLevelModifier>({ player -> player["debug", false] }, Priority.LOWEST) { player: Player ->
    val message = "${if (accuracy) "Accuracy" else "Damage"} effective level: $level (${skill.name.lowercase()})"
    player.message(message)
    logger.debug { message }
}

on<HitEffectiveLevelOverride>({ debug(it, target) }, Priority.LOWEST) { character: Character ->
    val player = if (character["debug", false] && character is Player) character else target as Player
    val message = "${if (defence) "Defender" else "Attacker"} effective level: $level ($type)"
    player.message(message)
    logger.debug { message }
}

on<HitRatingModifier>({ debug(it, target) }, Priority.LOWEST) { character: Character ->
    val player = if (character["debug", false] && character is Player) character else target as Player
    val message = "${if (offense) "Offensive" else "Defensive"} rating: $rating ($type)"
    player.message(message)
    logger.debug { message }
}

on<HitChanceModifier>({ debug(it, target) }, Priority.LOWEST) { character: Character ->
    val player = if (character["debug", false] && character is Player) character else target as Player
    val message = "Hit chance: $chance ($type, ${if (type == "magic") character.spell else weapon?.id ?: "unarmed"}${if (character is Player && character.specialAttack) ", special" else ""})"
    player.message(message)
    logger.debug { message }
}

on<HitDamageModifier>({ debug(it, target) }, Priority.LOWEST) { character: Character ->
    val player = if (character["debug", false] && character is Player) character else target as Player
    val message = "Max damage: $damage ($type, $strengthBonus str, ${if (type == "magic") character.spell else weapon?.id ?: "unarmed"}${if (player.specialAttack) ", special" else ""})"
    player.message(message)
    logger.debug { message }
}

on<CombatHit>({ debug(source, it) }, Priority.LOWEST) { character: Character ->
    val player = if (character["debug", false] && character is Player) character else source as Player
    val message = "Damage: $damage ($type, ${weapon?.id ?: "unarmed"})"
    player.message(message)
    logger.debug { message }
}

fun debug(player: Character, target: Character?) = player["debug", false] || target?.get("debug", false) == true