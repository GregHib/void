package content.area.kharidian_desert.al_kharid

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.EvilLaugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.proj.shoot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound

class Sorceress : Script {
    init {
        npcOperate("Talk-to", "sorceress") { (target) ->
            npc<Quiz>("Who are you and what do you want?")
            choice {
                option<Angry>("None of your business!") {
                    player<Neutral>("I go where I like and do what I like.")
                    npc<Angry>("Not in my house. Be gone!")
                    curse(target)
                }
                option<EvilLaugh>("I'm here to kill you!") {
                    npc<Angry>("I think not!")
                    curse(target)
                }
                option<Neutral>("Can I have some sq'irks please?") {
                    npc<Neutral>("What do you want them for?")
                    player<Neutral>("Someone asked me to bring them some.")
                    npc<Quiz>("Who?")
                    player<Neutral>("Osman")
                    npc<Neutral>("In that case I'm sorry, but you can't. I have had a falling out with him recently and would rather not oblige him.")
                }
                option<Neutral>("I'm just passing by.")
            }
        }
    }

    private suspend fun Player.curse(target: NPC) {
        target.say("Be gone, intruder!")
        sound("curse_cast")
        sound("curse_impact", delay = 100)
        target.gfx("curse_cast")
        gfx("curse_impact", delay = 100)
        target.shoot("curse", this)
        delay(3)
        gfx("puff", delay = 10)
        sound("smoke_puff", delay = 10)
        delay(1)
        tele(3321, 3143, 0)
    }
}
