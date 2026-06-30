package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.skill.summoning.follower
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile

class LavaTitan : Script {
    init {
        npcOperate("Interact", "lava_titan_familiar") { (target) ->
            if (target != follower) {
                return@npcOperate
            }
            choice {
                option("Talk-to") {
                    talk()
                }
                option("Teleport to Lava Maze") {
                    choice("Are you sure you want to teleport here? It's very high wilderness.") {
                        option("Yes. I'm sure.") {
                            tele(Tile(3030, 3838, 0))
                        }
                        option("Nevermind. That sounds dangerous.")
                    }
                }
            }
        }
    }

    private suspend fun Player.talk() {
        player<Happy>("Isn't it a lovely day, Titan?")
        npc<Neutral>("It is quite beautiful. The perfect sort of day for a limerick. Perhaps, I could tell you one?")
        player<Happy>("That sounds splendid.")
        npc<Neutral>("There once was a bard of Edgeville,")
        npc<Neutral>("Whose limericks were quite a thrill,")
        npc<Neutral>("He wrote this one here,")
        npc<Neutral>("His best? Nowhere near,")
        npc<Neutral>("But at least half a page it did fill.")
    }
}
