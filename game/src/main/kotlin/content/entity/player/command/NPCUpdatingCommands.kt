package content.entity.player.command

import content.entity.effect.transform
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.GraphicDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.colourOverlay
import world.gregs.voidps.engine.entity.character.flagHits
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setTimeBar
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.login.protocol.visual.update.HitSplat
import world.gregs.voidps.type.Delta

@Script
class NPCUpdatingCommands {

    val npcs: NPCs by inject()
    val definitions: NPCDefinitions by inject()
    val animationDefinitions: AnimationDefinitions by inject()
    val graphicDefinitions: GraphicDefinitions by inject()

    init {
        adminCommand("npc_tfm", stringArg("transform-id", autofill = definitions.ids.keys)) { player, args ->
            val npc = npcs[player.tile.addY(1)].first()
            npc.transform(args[0])
        }

        adminCommand("npc_turn", intArg("delta-x"), intArg("delta-y")) { player, args ->
            val npc = npcs[player.tile.addY(1)].first()
            npc.face(Delta(args[0].toInt(), args[1].toInt()))
        }

        adminCommand("npc_anim", stringArg("anim-id", autofill = animationDefinitions.ids.keys)) { player, args ->
            val npc = npcs[player.tile.addY(1)].first()
            npc.anim(args[0]) // 863
        }

        adminCommand("npc_overlay") { player, _ ->
            val npc = npcs[player.tile.addY(1)].first()
            npc.colourOverlay(-2108002746, 10, 100)
        }

        adminCommand("npc_chat", stringArg("message", optional = true)) { player, args ->
            val npc = npcs[player.tile.addY(1)].first()
            npc.say(args.getOrNull(0) ?: "Testing")
        }

        adminCommand("npc_gfx", stringArg("gfx-id", autofill = graphicDefinitions.ids.keys)) { player, args ->
            val npc = npcs[player.tile.addY(1)].first()
            npc.gfx(args[0]) // 93
        }

        adminCommand("npc_hit") { player, _ ->
            val npc = npcs[player.tile.addY(1)].first()
            npc.visuals.hits.add(HitSplat(10, HitSplat.Mark.Healed, npc.levels.getPercent(Skill.Constitution, fraction = 255.0).toInt()))
            npc.flagHits()
        }

        adminCommand("npc_time") { player, _ ->
            val npc = npcs[player.tile.addY(1)].first()
            npc.setTimeBar(true, 0, 60, 1)
        }

        adminCommand("npc_watch") { player, _ ->
            val npc = npcs[player.tile.addY(1)].first()
            npc.watch(player)
        }

        adminCommand("npc_crawl") { player, _ ->
            val npc = npcs[player.tile.addY(1)].first()
            //    npc.def["crawl"] = true
            //    npc.walkTo(npc.tile)
            //    npc.movement.steps.add(Tile(npc.tile.x, npc.tile.y + 1))
        }

        adminCommand("npc_run") { player, _ ->
            val npc = npcs[player.tile.addY(1)].first()
            npc.running = true
            //    npc.walkTo(npc.tile)
            //    npc.movement.steps.add(Tile(npc.tile.x, npc.tile.y + 2))
        }
    }
}
