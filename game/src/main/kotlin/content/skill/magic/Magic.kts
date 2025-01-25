package content.skill.magic

import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.combat.characterCombatSwing
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import content.skill.magic.spell.Spell
import content.skill.magic.book.modern.teleBlock
import content.skill.magic.spell.removeSpellItems
import content.skill.magic.spell.spell
import content.entity.proj.shoot

val spellDefinitions: SpellDefinitions by inject()

combatSwing(style = "blaze") { player ->
    if (!castSpell(player, target)) {
        cancel()
    }
}

characterCombatSwing(style = "magic") { source ->
    if (!castSpell(source, target)) {
        cancel()
    }
}

fun castSpell(source: Character, target: Character): Boolean {
    if (source.spell.isNotBlank() && source is Player && !source.removeSpellItems(source.spell)) {
        source.clear("autocast")
        return false
    }
    val spell = source.spell
    val definition = spellDefinitions.get(spell)
    var time = -1
    if (definition.contains("projectiles")) {
        val projectiles: List<Map<String, Any>> = definition["projectiles"]
        for (projectile in projectiles) {
            val id = projectile.getValue("id") as String
            val delay = projectile["delay"] as? Int
            val curve = projectile["curve"] as? Int
            val end = projectile["end_height"] as? Int
            val flightTime = source.shoot(id = id, target = target, delay = delay, curve = curve, endHeight = end)
            if (time == -1) {
                time = flightTime
            }
        }
    } else {
        time = source.shoot(id = spell, target = target)
    }
    if (source.weapon.def["weapon_type", ""] == "salamander" && source.spell.isBlank()) {
        source.anim("salamander_scorch")
        source.gfx("salamander_blaze")
        time = 0
    } else {
        val staff = source.weapon.def["category", ""] == "staff"
        val animation: String = if (staff && definition.contains("animation_staff")) definition["animation_staff"] else definition["animation", ""]
        val graphic: String = if (staff && definition.contains("graphic_staff")) definition["graphic_staff"] else definition["graphic", ""]
        source.anim(animation)
        source.gfx(graphic)
    }
    val damage = source.hit(target, delay = if (time == -1) 64 else time)
    if (damage != -1) {
        if (definition.contains("drain_multiplier")) {
            Spell.drain(source, target, spell)
        } else if (definition.contains("block_ticks")) {
            val duration: Int = definition["block_ticks"]
            (source as? Player)?.teleBlock(target, duration)
        }
    }
    source.clear("spell")
    if (!source.contains("autocast")) {
        source.queue.clearWeak()
    }
    return true
}