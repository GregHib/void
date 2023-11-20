package world.gregs.voidps.world.interact.entity.combat

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCLevels
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.hit.*
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack

val npcDefinitions: NPCDefinitions by inject()
val eventHandler: EventHandlerStore by inject()

on<Command>({ prefix == "maxhit" }) { player: Player ->
    val debug = player["debug", false]
    player["debug"] = false
    val parts = content.split(" ")
    val npcName = if (content.isBlank() || parts.isEmpty()) "rat" else parts.first()
    val spell = if (parts.size < 2) "wind_rush" else parts[1]
    val weapon = player.equipped(EquipSlot.Weapon)
    player.message("Max Hit (target=$npcName, spell=$spell)")
    val rangeMax = Damage.maximum(player, "range", weapon)
    val meleeMax = Damage.maximum(player, "melee", weapon)
    val magicMax = Damage.maximum(player, "magic", weapon, spell)
    player.message("Ranged: $rangeMax Melee: $meleeMax Magic: $magicMax")
    player.message("Hit Chance")
    val target = NPC(npcName).apply {
        def = npcDefinitions.get(npcName)
        eventHandler.populate(this)
        levels.link(events, NPCLevels(def))
        levels.clear()
    }
    val rangeChance = Hit.chance(player, target, "range", weapon)
    val meleeChance = Hit.chance(player, target, "melee", weapon)
    val magicChance = Hit.chance(player, target, "magic", weapon)
    player.message("Ranged: $rangeChance Melee: $meleeChance Magic: $magicChance")
    player["debug"] = debug
}

val logger = InlineLogger()

val Character.charName: String
    get() = (this as? Player)?.name ?: (this as NPC).id

on<CombatSwing>({ it["debug", false] || target["debug", false] }, Priority.HIGHEST) { character: Character ->
    val player = if (character["debug", false] && character is Player) character else target as Player
    player.message("---- Swing (${character.charName}) -> (${target.charName}) -----")
}

on<HitRatingModifier>({ debug(it, target) }, Priority.LOWEST) { character: Character ->
    val player = if (character["debug", false] && character is Player) character else target as Player
    val message = "${if (offense) "Offensive" else "Defensive"} rating: $rating ($type)"
    player.message(message)
    logger.debug { message }
}

on<HitChanceModifier>({ debug(it, target) }, Priority.LOWEST) { character: Character ->
    val player = if (character["debug", false] && character is Player) character else target as Player
    val message =
        "Hit chance: $chance ($type, ${if (type == "magic") character.spell else if (weapon.isEmpty()) "unarmed" else weapon.id}${if (character is Player && character.specialAttack) ", special" else ""})"
    player.message(message)
    logger.debug { message }
}

on<CombatHit>({ debug(source, it) }, Priority.LOWEST) { character: Character ->
    val player = if (character["debug", false] && character is Player) character else source as Player
    val message = "Damage: $damage ($type, ${if (weapon.isEmpty()) "unarmed" else weapon.id})"
    player.message(message)
    logger.debug { message }
}

fun debug(player: Character, target: Character?) = player["debug", false] || target?.get("debug", false) == true