package world.gregs.voidps.engine.client.ui.event

import world.gregs.voidps.engine.entity.character.player.Player

data class CommandContext(val player: Player, val args: List<String>, val content: String)