package content.area.kharidian_desert.al_kharid

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.EvilLaugh
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.proj.shoot
import content.entity.sound.sound
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script

@Script
class Sorceress {
    init {
        npcOperate("Talk-to", "sorceress") {
            npc<Quiz>("Who are you and what do you want?")
            choice {
                option<Angry>("None of your business!") {
                    player<Talk>("I go where I like and do what I like.")
                    npc<Angry>("Not in my house. Be gone!")
                    curse()
                }
                option<EvilLaugh>("I'm here to kill you!") {
                    npc<Angry>("I think not!")
                    curse()
                }
                option<Talk>("Can I have some sq'irks please?") {
                    npc<Talk>("What do you want them for?")
                    player<Talk>("Someone asked me to bring them some.")
                    npc<Quiz>("Who?")
                    player<Talk>("Osman")
                    npc<Talk>("In that case I'm sorry, but you can't. I have had a falling out with him recently and would rather not oblige him.")
                }
                option<Talk>("I'm just passing by.")
            }
        }
    }

    private suspend fun NPCOption<Player>.curse() {
        target.say("Be gone, intruder!")
        player.sound("curse_cast")
        player.sound("curse_impact", delay = 100)
        target.gfx("curse_cast")
        player.gfx("curse_impact", delay = 100)
        target.shoot("curse", player)
        delay(3)
        player.gfx("puff", delay = 10)
        player.sound("smoke_puff", delay = 10)
        delay(1)
        player.tele(3321, 3143, 0)
    }
}
