package world.gregs.voidps.world.interact.entity.npc.combat

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.spell
import world.gregs.voidps.world.interact.entity.player.combat.magic.canDrain
import kotlin.random.Random

on<CombatSwing>({ npc -> !swung() && npc.id.startsWith("dark_wizard_water") }, Priority.HIGHEST) { npc: NPC ->
    npc.spell = if (!Random.nextBoolean() && canDrain(target, "confuse")) "confuse" else "water_strike"
}

on<CombatSwing>({ npc -> !swung() && npc.id.startsWith("dark_wizard_earth") }, Priority.HIGHEST) { npc: NPC ->
    npc.spell = if (!Random.nextBoolean() && canDrain(target, "weaken")) "weaken" else "earth_strike"
}