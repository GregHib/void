package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character

val Character.inPvp: Boolean
    get() = get("in_pvp", false)

val Character.inWilderness: Boolean
    get() = get("in_wilderness", false)

val Character.inMultiCombat: Boolean
    get() = contains("in_multi_combat")

val Character.inSingleCombat: Boolean
    get() = !inMultiCombat