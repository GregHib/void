package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.entity.character.player.Player

fun Player.quest(name: String): String = this[name, "unstarted"]

fun Player.questComplete(name: String): Boolean = quest(name) == "completed"