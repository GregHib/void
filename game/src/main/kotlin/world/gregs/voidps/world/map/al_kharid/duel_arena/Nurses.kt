package world.gregs.voidps.world.map.al_kharid.duel_arena

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.world.interact.dialogue.Cheerful
import world.gregs.voidps.world.interact.dialogue.Laugh
import world.gregs.voidps.world.interact.dialogue.Talking
import world.gregs.voidps.world.interact.dialogue.Uncertain
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.sound.playSound

internal suspend fun NPCOption.fighters() {
    player<Uncertain>("Do you see a lot of injured fighters?")
    npc<Talking>("""
        Yes I do. Thankfully we can cope with almost anything.
	    Jaraah really is a wonderful surgeon, his methods are a
	    little unorthodox but he gets the job done.
    """)
    npc<Talking>("""
	    I shouldn't tell you this but his nickname is 'The
	    Butcher'.
	""")
    player<Uncertain>("That's reassuring.")
}

internal suspend fun NPCOption.heal() {
    npc.face(player)
    val heal = player.levels.getMax(Skill.Constitution)
    if (player.levels.get(Skill.Constitution) < heal) {
        npc.setAnimation("pick_pocket")
        player.playSound("heal")
        player.levels.restore(Skill.Constitution, heal)
        player.message("You feel a little better.")
        return
    }
    npc<Cheerful>("You look healthy to me!")
}

internal suspend fun NPCOption.often() {
    player<Uncertain>("Do you come here often?")
    npc<Cheerful>("I work here, so yes!")
    npc<Laugh>("You're silly!")
}