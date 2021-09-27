package world.gregs.voidps.world.interact.entity.player.combat.magic.ancient

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.utility.inject
import world.gregs.voidps.utility.toTicks
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.proj.shoot
import java.util.concurrent.TimeUnit

val definitions: SpellDefinitions by inject()

fun isSpell(spell: String) = spell.startsWith("miasmic_")

on<CombatSwing>({ player -> !swung() && isSpell(player.spell) }, Priority.LOW) { player: Player ->
    val spell = player.spell
    player.setAnimation("${spell}_cast")
    player.setGraphic("${spell}_cast")
    player.shoot(spell, target)
    if (player.hit(target) != -1) {
        val seconds: Int = definitions.get(spell)["effect_seconds"]
        target.start("miasmic", TimeUnit.SECONDS.toTicks(seconds))
    }
    delay = 5
}

fun meleeOrRanged(type: String) = type == "range" || type == "melee"

on<CombatSwing>({ delay != null && delay!! > 0 && it.hasEffect("miasmic") && meleeOrRanged(getWeaponType(it, it.weapon)) }, Priority.LOWEST) { _: Player ->
    delay = delay!! * 2
}