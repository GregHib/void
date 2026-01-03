package content.area.kharidian_desert.al_kharid.duel_arena

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.sound

internal fun ChoiceOption.fighters(): Unit = option<Confused>("Do you see a lot of injured fighters?") {
    npc<Idle>("Yes I do. Thankfully we can cope with almost anything. Jaraah really is a wonderful surgeon, his methods are a little unorthodox but he gets the job done.")
    npc<Idle>("I shouldn't tell you this but his nickname is 'The Butcher'.")
    player<Confused>("That's reassuring.")
}

internal suspend fun Player.heal(target: NPC) {
    target.face(this)
    val heal = levels.getMax(Skill.Constitution)
    if (levels.get(Skill.Constitution) < heal) {
        target.anim("pick_pocket")
        sound("heal")
        levels.restore(Skill.Constitution, heal)
        message("You feel a little better.")
        return
    }
    npc<Happy>("You look healthy to me!")
}

internal fun ChoiceOption.often(): Unit = option<Confused>("Do you come here often?") {
    npc<Happy>("I work here, so yes!")
    npc<Laugh>("You're silly!")
}

class Nurses : Script {

    init {
        npcOperate("Talk-to", "sabreen,a_abla") { (target) ->
            player<Happy>("Hi!")
            npc<Happy>("Hi. How can I help?")
            choice {
                option<Confused>("Can you heal me?") {
                    heal(target)
                }
                fighters()
                often()
            }
        }

        npcOperate("Heal", "sabreen,a_abla") { (target) ->
            heal(target)
        }
    }
}
