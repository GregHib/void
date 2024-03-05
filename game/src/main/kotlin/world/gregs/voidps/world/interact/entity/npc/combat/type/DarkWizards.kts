package world.gregs.voidps.world.interact.entity.npc.combat.type

import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.npcCombatSwing
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.Spell
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell

npcCombatSwing("dark_wizard_water*", priority = Priority.HIGHEST) { npc ->
    npc.spell = if (!random.nextBoolean() && Spell.canDrain(target, "confuse")) "confuse" else "water_strike"
}

npcCombatSwing("dark_wizard_earth*", priority = Priority.HIGHEST) { npc ->
    npc.spell = if (!random.nextBoolean() && Spell.canDrain(target, "weaken")) "weaken" else "earth_strike"
}