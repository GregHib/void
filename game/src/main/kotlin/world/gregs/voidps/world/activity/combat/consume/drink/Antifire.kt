package world.gregs.voidps.world.activity.combat.consume.drink

import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.entity.character.player.Player

val Player.antifire: Boolean
    get() = getVar("antifire", 0) > 0

val Player.superAntifire: Boolean
    get() = getVar("super_antifire", 0) > 0