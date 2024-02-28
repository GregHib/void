package world.gregs.voidps.world.map.al_kharid.duel_arena

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.mode.interact.TargetNPCContext
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.PlayerChoice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.sound.playSound

internal suspend fun PlayerChoice.fighters(): Unit = option<Uncertain>("Do you see a lot of injured fighters?") {
    npc<Talking>("Yes I do. Thankfully we can cope with almost anything. Jaraah really is a wonderful surgeon, his methods are a little unorthodox but he gets the job done.")
    npc<Talking>("I shouldn't tell you this but his nickname is 'The Butcher'.")
    player<Uncertain>("That's reassuring.")
}

internal suspend fun TargetNPCContext.heal() {
    target.face(player)
    val heal = player.levels.getMax(Skill.Constitution)
    if (player.levels.get(Skill.Constitution) < heal) {
        target.setAnimation("pick_pocket")
        player.playSound("heal")
        player.levels.restore(Skill.Constitution, heal)
        player.message("You feel a little better.")
        return
    }
    npc<Cheerful>("You look healthy to me!")
}

internal suspend fun PlayerChoice.often(): Unit = option<Uncertain>("Do you come here often?") {
    npc<Cheerful>("I work here, so yes!")
    npc<Chuckle>("You're silly!")
}
