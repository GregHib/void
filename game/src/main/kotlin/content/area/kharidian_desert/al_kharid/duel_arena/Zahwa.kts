package content.area.kharidian_desert.al_kharid.duel_arena

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.type.random

npcOperate("Talk-to", "zahwa") {
    when (random.nextInt(0, 6)) {
        0 -> {
            player<Happy>("Hi!")
            player<Happy>("Hi!")
        }
        1 -> {
            player<Uncertain>("Are you alright?")
            npc<Frustrated>("Do I look alright?!")
        }
        2 -> {
            player<Uncertain>("Are you alright?")
            npc<Happy>("Yeh. The nurses here are...wonderful!")
        }
        3 -> {
            player<Happy>("Hi!")
            npc<Frustrated>("I could've 'ad 'im!")
            player<Surprised>("Er...")
            npc<Frustrated>("I was robbed!")
            player<RollEyes>("Right.")
            npc<Frustrated>("It was rigged I tell you!")
            player<RollEyes>("Uh huh.")
            npc<Frustrated>("Leave me alone!")
        }
        4 -> {
            player<Uncertain>("Are you alright?")
            npc<Surprised>("NURSE!")
        }
        5 -> {
            player<Uncertain>("Are you alright?")
            npc<Frustrated>("It's just a flesh wound!")
        }
        6 -> {
            player<Uncertain>("Are you alright?")
            npc<Sad>("Can't....go....on!")
            npc<Sad>("Leave me behind!")
            player<Uncertain>("I'll leave you here, OK?")
            npc<Uncertain>("Oh. OK.")
        }
        7 -> {
            player<Happy>("Hi!")
            player<Sad>("Ughhhh....")
        }
    }
}
