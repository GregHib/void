package content.minigame.vinesweeper

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.proj.shoot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.timer.CLIENT_TICKS

/**
 * Tool leprechauns (and Teclyn in Lletya) teleport players to Winkin's Farm.
 */
class VinesweeperTeleport : Script {

    companion object {
        // Animation 11705 runs for 162 client cycles; the player leaves once it has finished.
        private const val CAST_TICKS = 6
    }

    init {
        npcOperate("Teleport", "tool_leprechaun*,goth_leprechaun*,teclyn") { (target) ->
            npc<Happy>("Would ye like me to send ye off to Winkin's Farm for a spot of Vinesweeper?")
            choice {
                option<Happy>("Yes please.") {
                    teleportToFarm(target)
                }
                option<Neutral>("No thanks.") {
                }
            }
        }
    }

    private suspend fun Player.teleportToFarm(leprechaun: NPC) {
        set("vinesweeper_return_tile", tile.id)
        leprechaun.face(this)
        leprechaun.say("Avach nimporto!")
        leprechaun.anim("leprechaun_teleport")
        leprechaun.gfx("curse_cast")
        delay(CAST_TICKS)
        val flight = leprechaun.shoot("curse", this)
        sound("curse_all")
        delay(CLIENT_TICKS.toTicks(flight).coerceAtLeast(1))
        tele(Vinesweeper.ARRIVAL_TILE)
        delay(1)
        gfx("curse_impact")
        sound("curse_impact")
        message("The leprechaun sends you to Winkin's Farm.")
    }
}
