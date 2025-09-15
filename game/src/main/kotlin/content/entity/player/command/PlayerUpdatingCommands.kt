package content.entity.player.command

import content.bot.isBot
import content.entity.proj.shoot
import net.pearx.kasechange.toScreamingSnakeCase
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.commandAlias
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.playerCommand
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.GraphicDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.colourOverlay
import world.gregs.voidps.engine.entity.character.flagExactMovement
import world.gregs.voidps.engine.entity.character.player.*
import world.gregs.voidps.engine.entity.character.setTimeBar
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Direction

@Script
class PlayerUpdatingCommands {

    val players: Players by inject()
    val animationDefinitions: AnimationDefinitions by inject()
    val graphicDefinitions: GraphicDefinitions by inject()
    val npcDefinitions: NPCDefinitions by inject()

    init {
        adminCommand("kill", desc = "Remove all bots") { _, _ ->
            val it = players.iterator()
            val remove = mutableListOf<Player>()
            while (it.hasNext()) {
                val p = it.next()
                if (p.isBot) {
                    remove.add(p)
                }
            }
            for (bot in remove) {
                players.remove(bot)
            }
        }

        playerCommand("players", desc = "Get the total and local player counts") { player, _ ->
            player.message("Players: ${players.size}, ${player.viewport?.players?.localCount}")
        }

        adminCommand("anim", stringArg("anim-id", autofill = animationDefinitions.ids.keys), desc = "Perform animation (-1 to clear)") { player, args ->
            when (args[0]) {
                "-1", "" -> player.clearAnim()
                else -> player.anim(args[0], override = true) // 863
            }
        }

        adminCommand("emote", stringArg("emote-id"), desc = "Perform render emote (-1 to clear)") { player, args ->
            when (args[0]) {
                "-1", "" -> player.clearRenderEmote()
                else -> player.renderEmote(args[0])
            }
        }

        adminCommand("gfx", stringArg("gfx-id", autofill = graphicDefinitions.ids.keys), desc = "Perform graphic effect (-1 to clear)") { player, args ->
            when (args[0]) {
                "-1", "" -> player.clearGfx()
                else -> player.gfx(args[0]) // 93
            }
        }

        adminCommand("proj", stringArg("gfx-id", autofill = graphicDefinitions.ids.keys), desc = "Shoot projectile (-1 to clear)") { player, args ->
            player.shoot(args[0], player.tile.add(0, 5), delay = 0, flightTime = 400)
        }
        commandAlias("proj", "shoot")

        adminCommand("overlay") { player, args ->
            player.colourOverlay(-2108002746, 10, 100)
        }

        adminCommand("exact_move", intArg("start-x", optional = true), intArg("start-y", optional = true), intArg("end-x", optional = true), intArg("end-y", optional = true)) { player, args ->
            val move = player.visuals.exactMovement
            move.startX = args.getOrNull(0)?.toIntOrNull() ?: -4
            move.startY = args.getOrNull(0)?.toIntOrNull() ?: 2
            move.startDelay = 0
            move.endX = args.getOrNull(0)?.toIntOrNull() ?: 0
            move.endY = args.getOrNull(0)?.toIntOrNull() ?: 0
            move.endDelay = 100
            move.direction = Direction.EAST.ordinal
            player.flagExactMovement()
        }
        commandAlias("move", "exact_move")

        adminCommand("time") { player, args ->
            player.setTimeBar(true, 0, 60, 1)
        }

        adminCommand("face", intArg("delta-x"), intArg("delta-y"), desc = "Turn player to face a direction or delta coordinate") { player, args ->
            if (args[0].contains(" ")) {
                val parts = args[0].split(" ")
                player.face(Delta(parts[0].toInt(), parts[1].toInt()))
            } else {
                val direction = Direction.valueOf(args[0].toScreamingSnakeCase())
                player.face(direction.delta)
            }
        }

        adminCommand("skill_level", intArg("level"), desc = "Set the current displayed skill level") { player, args ->
            player.skillLevel = args[0].toInt()
        }

        adminCommand("combat_level", intArg("level"), desc = "Set the current displayed combat level") { player, args ->
            player.combatLevel = args[0].toInt()
        }

        adminCommand("toggle_skill_level", desc = "Toggle skill level display") { player, args ->
            player.toggleSkillLevel()
        }

        adminCommand("summoning_level", intArg("level"), desc = "Set the current summoning combat level") { player, args ->
            player.summoningCombatLevel = args[0].toInt()
        }
    }
}
