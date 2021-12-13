package world.gregs.voidps.world.interact.entity.player.combat.range.ammo

import world.gregs.voidps.engine.entity.item.Item

fun isBowOrCrossbow(item: Item) = item.id.endsWith("bow") || item.id == "seercull" || item.id.endsWith("longbow_sighted")