package world.gregs.voidps.world.interact.entity.player.combat.magic

import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.Spell
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.modern.teleBlock
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell
import world.gregs.voidps.world.interact.entity.proj.shoot

val spellDefinitions: SpellDefinitions by inject()

combatSwing(style = "blaze") { player ->
    if (!castSpell(player, target)) {
        cancel()
    }
}

combatSwing(style = "magic") { player ->
    if (!castSpell(player, target)) {
        cancel()
    }
}

fun castSpell(player: Player, target: Character): Boolean {
    if (player.spell.isNotBlank() && !Spell.removeRequirements(player, player.spell)) {
        player.clear("autocast")
        return false
    }
    val spell = player.spell
    val definition = spellDefinitions.get(spell)
    var time = -1
    if (definition.contains("projectiles")) {
        val projectiles: List<Map<String, Any>> = definition["projectiles"]
        for (projectile in projectiles) {
            val id = projectile.getValue("id") as String
            val delay = projectile["delay"] as? Int
            val curve = projectile["curve"] as? Int
            val end = projectile["end_height"] as? Int
            val flightTime = player.shoot(id = id, target = target, delay = delay, curve = curve, endHeight = end)
            if (time == -1) {
                time = flightTime
            }
        }
    } else {
        time = player.shoot(id = spell, target = target)
    }
    if (player.weapon.def["weapon_type", ""] == "salamander" && player.spell.isBlank()) {
        player.setAnimation("salamander_scorch")
        player.setGraphic("salamander_blaze")
        time = 0
    } else {
        val staff = player.weapon.def["category", ""] == "staff"
        val animation: String = if (staff && definition.contains("animation_staff")) definition["animation_staff"] else definition["animation", ""]
        val graphic: String = if (staff && definition.contains("graphic_staff")) definition["graphic_staff"] else definition["graphic", ""]
        player.setAnimation(animation)
        player.setGraphic(graphic)
    }
    val damage = player.hit(target, delay = if (time == -1) 64 else time)
    if (damage != -1) {
        if (definition.contains("drain_multiplier")) {
            Spell.drain(player, target, spell)
        } else if (definition.contains("block_ticks")) {
            val duration: Int = definition["block_ticks"]
            (player as? Player)?.teleBlock(target, duration)
        }
    }
    player.clear("spell")
    if (!player.contains("autocast")) {
        player.queue.clearWeak()
    }
    return true
}