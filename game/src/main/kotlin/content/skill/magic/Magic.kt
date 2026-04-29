package content.skill.magic

import content.entity.combat.hit.hit
import content.entity.proj.shoot
import content.skill.magic.book.modern.teleBlock
import content.skill.magic.spell.Spell
import content.skill.magic.spell.removeSpellItems
import content.skill.magic.spell.spell
import content.skill.melee.weapon.weapon
import content.skill.slayer.categories
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound

object Magic {
    fun castSpell(source: Character, target: Character): Boolean {
        if (source.spell.isNotBlank() && source is Player && !source.removeSpellItems(source.spell)) {
            source.clear("autocast")
            return false
        }
        val spell = source.spell
        val row = Rows.get("spells.${spell}")
        val time = time(source, target, row)
        source.anim(animation(source, row))
        source.gfx(graphic(source, row))
        target.sound("${spell}_cast")
        source.sound("${spell}_cast")
        val damage = source.hit(target, delay = if (time == -1) 64 else time)
        if (damage != -1) {
            val percent = row.intOrNull("drain_percent")
            val ticks = row.intOrNull("effect_ticks")
            if (percent != null) {
                Spell.drain(source, target, spell)
            } else if (ticks != null) {
                (source as? Player)?.teleBlock(target, ticks)
            }
        }
        source.clear("spell")
        if (!source.contains("autocast")) {
            source.queue.clearWeak()
        }
        return true
    }

    fun animation(source: Character, row: RowDefinition): String {
        if (source.weapon.def["weapon_type", ""] == "salamander" && source.spell.isBlank()) {
            return "salamander_scorch"
        }
        val staff = source.weapon.def["category", ""] == "staff"
        val anim = row.anim("animation")
        return if (staff) {
            row.animOrNull("animation_staff") ?: anim
        } else {
            anim
        }
    }

    fun graphic(source: Character, row: RowDefinition): String {
        if (source.weapon.def["weapon_type", ""] == "salamander" && source.spell.isBlank()) {
            return "salamander_blaze"
        }
        if (source is Player || source is NPC && source.categories.contains("human")) {
            val staff = source.weapon.def["category", ""] == "staff"
            val anim = row.gfx("graphic")
            return if (staff) {
                row.gfxOrNull("graphic_staff") ?: anim
            } else {
                anim
            }
        } else if (source is NPC) {
            return source.def["combat_gfxs", ""]
        }
        return ""
    }

    fun time(source: Character, target: Character, row: RowDefinition): Int {
        if (source.weapon.def["weapon_type", ""] == "salamander" && source.spell.isBlank()) {
            return 0
        }
        val projectiles = row.rowList("projectiles")
        if (projectiles.isEmpty()) {
            return source.shoot(id = row.rowId, target = target)
        }
        var time = -1
        for (projectile in projectiles) {
            val id = projectile.gfx("gfx")
            val delay = projectile.intOrNull("delay")
            val curve = projectile.intOrNull("curve")
            val end = projectile.intOrNull("end_height")
            val flightTime = if (id == "ice_barrage") {
                target.tile.shoot(id = id, target = target, delay = delay, curve = curve, endHeight = end)
            } else {
                source.shoot(id = id, target = target, delay = delay, curve = curve, endHeight = end)
            }
            if (time == -1) {
                time = flightTime
            }
        }
        return time
    }
}
