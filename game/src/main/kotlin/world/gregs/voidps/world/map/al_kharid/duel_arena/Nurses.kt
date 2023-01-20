package world.gregs.voidps.world.map.al_kharid.duel_arena

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.turn
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.sound.playSound

internal suspend fun NPCOption.fighters() {
    player("uncertain", "Do you see a lot of injured fighters?")
    npc("talking", """
        Yes I do. Thankfully we can cope with almost anything.
	    Jaraah really is a wonderful surgeon, his methods are a
	    little unorthodox but he gets the job done.
    """)
    npc("talking", """
	    I shouldn't tell you this but his nickname is 'The
	    Butcher'.
	""")
    player("uncertain", "That's reassuring.")
}

internal suspend fun NPCOption.heal() {
    npc.turn(player)
    val heal = player.levels.getMax(Skill.Constitution)
    if (player.levels.get(Skill.Constitution) < heal) {
        npc.setAnimation("pick_pocket")
        player.playSound("heal")
        player.levels.restore(Skill.Constitution, heal)
        player.message("You feel a little better.")
        return
    }
    npc("cheerful", "You look healthy to me!")
}

internal suspend fun NPCOption.often() {
    player("uncertain", "Do you come here often?")
    npc("cheerful", "I work here, so yes!")
    npc("laugh", "You're silly!")
}