package world.gregs.voidps.world.interact.entity.player.combat.range

import world.gregs.voidps.engine.entity.character.Character

var Character.ammo: String
    get() = get("ammo", "")
    set(value) = set("ammo", value)