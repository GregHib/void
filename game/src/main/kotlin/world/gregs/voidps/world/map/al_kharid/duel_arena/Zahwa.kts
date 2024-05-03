package world.gregs.voidps.world.map.al_kharid.duel_arena

import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

npcOperate("Talk-to", "zahwa") {
    when (random.nextInt(0, 6)) {
        0 -> {
            player<Cheerful>("Hi!")
            player<Cheerful>("Hi!")
        }
        1 -> {
            player<Uncertain>("Are you alright?")
            npc<Frustrated>("Do I look alright?!")
        }
        2 -> {
            player<Uncertain>("Are you alright?")
            npc<Cheerful>("Yeh. The nurses here are...wonderful!")
        }
        3 -> {
            player<Cheerful>("Hi!")
            npc<Frustrated>("I could've 'ad 'im!")
            player<Surprised>("Er...")
            npc<Frustrated>("I was robbed!")
            player<RollEyes>("Right.")
            npc<Frustrated>("It was rigged I tell you!")
            player<RollEyes>("Uh huh.")
            npc<Frustrated>("Leave me alone!")
        }
        4-> {
            player<Uncertain>("Are you alright?")
            npc<Surprised>("NURSE!")
        }
        5-> {
            player<Uncertain>("Are you alright?")
            npc<Frustrated>("It's just a flesh wound!")
        }
        6-> {
            player<Uncertain>("Are you alright?")
            npc<Sad>("Can't....go....on!")
            npc<Sad>("Leave me behind!")
            player<Uncertain>("I'll leave you here, OK?")
            npc<Uncertain>("Oh. OK.")
        }
        7 -> {
            player<Cheerful>("Hi!")
            player<Sad>("Ughhhh....")
        }
    }
}