package content.area.kharidian_desert.al_kharid.duel_arena

import content.entity.player.dialogue.Chuckle
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Uncertain
import content.entity.player.dialogue.type.*
import content.entity.sound.sound
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill

internal fun ChoiceBuilder2.fighters(): Unit = option<Uncertain>("Do you see a lot of injured fighters?") {
    npc<Neutral>("Yes I do. Thankfully we can cope with almost anything. Jaraah really is a wonderful surgeon, his methods are a little unorthodox but he gets the job done.")
    npc<Neutral>("I shouldn't tell you this but his nickname is 'The Butcher'.")
    player<Uncertain>("That's reassuring.")
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

internal fun ChoiceBuilder2.often(): Unit = option<Uncertain>("Do you come here often?") {
    npc<Happy>("I work here, so yes!")
    npc<Chuckle>("You're silly!")
}

class Nurses : Script {

    init {
        npcOperate("Talk-to", "sabreen,a_abla") { (target) ->
            player<Happy>("Hi!")
            npc<Happy>("Hi. How can I help?")
            choice {
                option<Uncertain>("Can you heal me?") {
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
