package world.gregs.voidps.world.interact.entity.player.combat

fun bowHitDelay(distance: Int) = 1 + (distance + 3) / 6

fun throwHitDelay(distance: Int) = 1 + distance / 6

fun magicHitDelay(distance: Int) = 1 + (distance + 1) / 3

fun darkBowHitDelay(distance: Int) = 1 + (distance + 2) / 3

fun dfsHitDelay(distance: Int) = 2 + (distance + 4) / 6