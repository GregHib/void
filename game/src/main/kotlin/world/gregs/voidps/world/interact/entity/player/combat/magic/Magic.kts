package world.gregs.voidps.world.interact.entity.player.combat.magic

import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.combat.characterCombatSwing
import world.gregs.voidps.world.interact.entity.combat.fightStyle
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.Spell
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.modern.teleBlock
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spellBook
import world.gregs.voidps.world.interact.entity.proj.shoot

val definitions: SpellDefinitions by inject()

characterCombatSwing(style = "magic") { character ->
    if (character is Player && character.spell.isNotBlank() && !Spell.removeRequirements(character, character.spell)) {
        character.clear("autocast")
        cancel()
        return@characterCombatSwing
    }
    val book = if (character is Player) character.spellBook else "modern_spellbook"
    val spell = character.spell
    val definition = definitions.get(spell)
    when (book) {
        "modern_spellbook" -> {
            val staff = character.weapon.def["category", ""] == "staff"
            val animation: String = if (staff && definition.contains("animation_staff")) definition["animation_staff"] else definition["animation"]
            val graphic: String = if (staff && definition.contains("graphic_staff")) definition["graphic_staff"] else definition["graphic"]
            character.setAnimation(animation)
            character.setGraphic(graphic)
            if (definition.contains("projectiles")) {
                val projectiles: List<Map<String, Any>> = definition["projectiles"]
                for (projectile in projectiles) {
                    val id = projectile.getValue("id") as String
                    val delay = projectile["delay"] as? Int
                    val curve = projectile["curve"] as? Int
                    val end = projectile["end_height"] as? Int
                    character.shoot(id = id, target = target, delay = delay, curve = curve, endHeight = end)
                }
            } else {
                character.shoot(id = spell, target = target)
            }
            val distance = character.tile.distanceTo(target)
            val damage = character.hit(target, delay = definition["hit_delay", Hit.magicDelay(distance)])
            if (damage != -1) {
                if (definition.contains("drain_multiplier")) {
                    Spell.drain(character, target, spell)
                } else if (definition.contains("block_ticks")) {
                    val duration: Int = definition["block_ticks"]
                    (character as? Player)?.teleBlock(target, duration)
                }
            }
            delay = 5
        }
        "ancient_spellbook" -> {
            if (spell.startsWith("miasmic_")) {
                character.setAnimation("${spell}_cast")
                character.setGraphic("${spell}_cast")
                character.shoot(spell, target)
                character.hit(target)
                delay = 5
            } else {
                character.setAnimation("ancient_spell${if (Spell.isMultiTarget(spell)) "_multi" else ""}")
                character.shoot(spell, target)
                character.hit(target)
                delay = 5
            }
            if (spell.startsWith("miasmic_")) {
                if (delay != null && delay!! > 0 && character.hasClock("miasmic") && (character.fightStyle == "range" || character.fightStyle == "melee")) {
                    delay = delay!! * 2
                }
            }
        }
        "lunar_spellbook" -> {}
        "dungeoneering_spellbook" -> {}
    }
    character.clear("spell")
    if ((delay ?: -1) >= 0) {
        if (!character.contains("autocast")) {
            character.queue.clearWeak()
        }
    }
}