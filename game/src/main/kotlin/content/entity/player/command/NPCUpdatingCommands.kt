package content.entity.player.command

import content.entity.effect.transform
import content.entity.player.dialogue.sendChat
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.GraphicDefinitions
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.colourOverlay
import world.gregs.voidps.engine.entity.character.flagHits
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setTimeBar
import world.gregs.voidps.engine.suspend.pauseButton
import world.gregs.voidps.network.login.protocol.encode.npcDialogueHead
import world.gregs.voidps.network.login.protocol.visual.update.HitSplat
import world.gregs.voidps.type.Delta

class NPCUpdatingCommands : Script {

    init {
        adminCommand("npc_tfm", stringArg("transform-id", autofill = NPCDefinitions.ids.keys)) { args ->
            val npc = NPCs.at(tile.addY(1)).first()
            npc.transform(args[0])
        }

        adminCommand("npc_turn", intArg("delta-x"), intArg("delta-y")) { args ->
            val npc = NPCs.at(tile.addY(1)).first()
            npc.face(Delta(args[0].toInt(), args[1].toInt()))
        }

        adminCommand("npc_anim", stringArg("anim-id", autofill = AnimationDefinitions.ids.keys)) { args ->
            val npc = NPCs.at(tile.addY(1)).first()
            npc.anim(args[0]) // 863
        }

        adminCommand(
            "chathead",
            stringArg("npc-id", autofill = NPCDefinitions.ids.keys),
            stringArg("anim-id", autofill = AnimationDefinitions.ids.keys),
            stringArg("text", optional = true),
            desc = "Test an npc chat head with an animation (name, expression_ name or id)",
        ) { args ->
            val npc = NPCDefinitions.get(args[0])
            val animation = AnimationDefinitions.getOrNull("expression_${args[1]}") ?: AnimationDefinitions.get(args[1])
            val id = "dialogue_npc_chat1"
            if (!open(id)) {
                return@adminCommand
            }
            InterfaceDefinitions.getComponent(id, "head")?.let { client?.npcDialogueHead(it.id, npc.id) }
            interfaces.sendChat(id, "head", animation.id, npc.name, listOf(args.getOrNull(2) ?: "${args[0]} - ${args[1]} (${animation.id})"))
            pauseButton()
            close(id)
        }

        adminCommand("npc_overlay") { _ ->
            val npc = NPCs.at(tile.addY(1)).first()
            npc.colourOverlay(-2108002746, 10, 100)
        }

        adminCommand("npc_chat", stringArg("message", optional = true)) { args ->
            val npc = NPCs.at(tile.addY(1)).first()
            npc.say(args.getOrNull(0) ?: "Testing")
        }

        adminCommand("npc_gfx", stringArg("gfx-id", autofill = GraphicDefinitions.ids.keys)) { args ->
            val npc = NPCs.at(tile.addY(1)).first()
            npc.gfx(args[0]) // 93
        }

        adminCommand("npc_hit") { _ ->
            val npc = NPCs.at(tile.addY(1)).first()
            npc.visuals.hits.add(HitSplat(10, HitSplat.Mark.Healed, npc.levels.getPercent(Skill.Constitution, fraction = 255.0).toInt()))
            npc.flagHits()
        }

        adminCommand("npc_time") { _ ->
            val npc = NPCs.at(tile.addY(1)).first()
            npc.setTimeBar(true, 0, 60, 1)
        }

        adminCommand("npc_watch") { _ ->
            val npc = NPCs.at(tile.addY(1)).first()
            npc.watch(this)
        }

        adminCommand("npc_crawl") { _ ->
            val npc = NPCs.at(tile.addY(1)).first()
            //    npc.def["crawl"] = true
            //    npc.walkTo(npc.tile)
            //    npc.movement.steps.add(Tile(npc.tile.x, npc.tile.y + 1))
        }

        adminCommand("npc_run") { _ ->
            val npc = NPCs.at(tile.addY(1)).first()
            npc.running = true
            //    npc.walkTo(npc.tile)
            //    npc.movement.steps.add(Tile(npc.tile.x, npc.tile.y + 2))
        }
    }
}
