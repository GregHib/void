package world.gregs.voidps.world.interact.entity.npc.combat.type

import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.npcCombatPrepare
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.Spell
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell

npcCombatPrepare("dark_wizard_water*") { npc ->
    npc.spell = if (!random.nextBoolean() && Spell.canDrain(target, "confuse")) "confuse" else "water_strike"
}

npcCombatPrepare("dark_wizard_earth*") { npc ->
    npc.spell = if (!random.nextBoolean() && Spell.canDrain(target, "weaken")) "weaken" else "earth_strike"
}