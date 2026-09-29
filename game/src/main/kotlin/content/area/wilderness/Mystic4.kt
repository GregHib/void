package content.area.wilderness

import content.entity.player.dialogue.Bored
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile

class Mystic4 : Script {

    init {
        npcOperate("Talk-to", "mystic_4") {
            npc<Bored>("What ya want?")
            mainMenu()
        }

        npcOperate("Teleport", "mystic_4") { (target) ->
            teleportToSpawn(target)
        }
    }

    private suspend fun Player.mainMenu() {
        choice("Select an option") {
            option<Quiz>("What do you do here?") {
                npc<Shifty>("I worship the spirits of the Wilderness, innit? Ya gotta love 'em, gliding silently through the night an' killing everything they see.")
                whatDoYouDoHereMenu()
            }
            option<Scared>("Um...I'll just leave you alone.") {
            }
        }
    }

    private suspend fun Player.whatDoYouDoHereMenu() {
        choice("Select an option") {
            option<Confused>("Isn't that a rather unhealthy attitude?") {
                npc<Bored>("Yeah, well, whatever. Now, what ya want?")
                leaveYouAlone()
            }
            option<Scared>("Um...I'll just leave you alone.") {
                leaveYouAlone()
            }
        }
    }

    private suspend fun Player.leaveYouAlone() {
        player<Scared>("Um... I'll just leave you alone.")
    }

    private suspend fun Player.teleportToSpawn(mystic: NPC) {
        val destination = this["respawn_tile", Tile(Settings["world.home.x", 0], Settings["world.home.y", 0], Settings["world.home.level", 0])]
        face(mystic)
        mystic.face(this)
        mystic.anim("mace_pummel")
        anim("human_death")
        delay(3)
        open("fade_out")
        delay(2)
        tele(destination)
        clearAnim()
        open("fade_in")
    }
}
