package content.area.kharidian_desert.al_kharid.duel_arena

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.interact.TargetInteraction
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import content.entity.player.dialogue.Chuckle
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Uncertain
import content.entity.player.dialogue.type.PlayerChoice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.sound.playSound

internal suspend fun PlayerChoice.fighters(): Unit = option<Uncertain>("Do you see a lot of injured fighters?") {
    npc<Neutral>("Yes I do. Thankfully we can cope with almost anything. Jaraah really is a wonderful surgeon, his methods are a little unorthodox but he gets the job done.")
    npc<Neutral>("I shouldn't tell you this but his nickname is 'The Butcher'.")
    player<Uncertain>("That's reassuring.")
}

internal suspend fun TargetInteraction<Player, NPC>.heal() {
    target.face(player)
    val heal = player.levels.getMax(Skill.Constitution)
    if (player.levels.get(Skill.Constitution) < heal) {
        target.anim("pick_pocket")
        player.playSound("heal")
        player.levels.restore(Skill.Constitution, heal)
        player.message("You feel a little better.")
        return
    }
    npc<Happy>("You look healthy to me!")
}

internal suspend fun PlayerChoice.often(): Unit = option<Uncertain>("Do you come here often?") {
    npc<Happy>("I work here, so yes!")
    npc<Chuckle>("You're silly!")
}
