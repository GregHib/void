package world.gregs.voidps.world.interact.entity.npc.combat.type

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.npcSwing
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.Spell
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell

npcSwing("dark_wizard_water*", Priority.HIGHEST) { npc: NPC ->
    npc.spell = if (!random.nextBoolean() && Spell.canDrain(target, "confuse")) "confuse" else "water_strike"
}

npcSwing("dark_wizard_earth*", Priority.HIGHEST) { npc: NPC ->
    npc.spell = if (!random.nextBoolean() && Spell.canDrain(target, "weaken")) "weaken" else "earth_strike"
}