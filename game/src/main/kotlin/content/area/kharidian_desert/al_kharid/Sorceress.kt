package content.area.kharidian_desert.al_kharid

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.EvilLaugh
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Surprised
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.Upset
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.proj.shoot
import content.entity.sound.sound
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.queue.softQueue

@Script
class Sorceress {
    init {
        npcOperate("Talk-to", "sorceress") {
            // TODO expressions + anims
            npc<Talk>("Who are you and what do you want?")
            choice {
                option<Talk>("None of your business!") {
                    player<Quiz>("I go where I like and do what I like.")
                    npc<Talk>("Not in my house. Be gone!")
                    target.say("Be gone, intruder!")
                    delay()
                    player.tele(3321, 3143)
                }
                option<Angry>("I'm here to kill you!") {
                    npc<Talk>("I think not!")
                    target.say("Be gone, intruder!")
                    delay()
                    player.tele(3321, 3143)
                }
                option<Quiz>("Can I have some sq'irks please?") {
                    npc<Quiz>("What do you want them for?")
                    player<Talk>("Someone asked me to bring them some.")
                    npc<Quiz>("Who?")
                    player<Talk>("Osman")
                    npc<Talk>("In that case I'm sorry, but you can't. I have had a falling out with him recently and would rather not oblige him.")
                }
                option<Talk>("I'm just passing by.")
            }
        }
    }
}