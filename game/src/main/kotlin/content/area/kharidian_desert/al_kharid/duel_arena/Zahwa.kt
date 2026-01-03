package content.area.kharidian_desert.al_kharid.duel_arena

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class Zahwa : Script {

    init {
        npcOperate("Talk-to", "zahwa") {
            when (random.nextInt(0, 6)) {
                0 -> {
                    player<Happy>("Hi!")
                    player<Happy>("Hi!")
                }
                1 -> {
                    player<Confused>("Are you alright?")
                    npc<Frustrated>("Do I look alright?!")
                }
                2 -> {
                    player<Confused>("Are you alright?")
                    npc<Happy>("Yeh. The nurses here are...wonderful!")
                }
                3 -> {
                    player<Happy>("Hi!")
                    npc<Frustrated>("I could've 'ad 'im!")
                    player<Shock>("Er...")
                    npc<Frustrated>("I was robbed!")
                    player<Bored>("Right.")
                    npc<Frustrated>("It was rigged I tell you!")
                    player<Bored>("Uh huh.")
                    npc<Frustrated>("Leave me alone!")
                }
                4 -> {
                    player<Confused>("Are you alright?")
                    npc<Shock>("NURSE!")
                }
                5 -> {
                    player<Confused>("Are you alright?")
                    npc<Frustrated>("It's just a flesh wound!")
                }
                6 -> {
                    player<Confused>("Are you alright?")
                    npc<Disheartened>("Can't....go....on!")
                    npc<Disheartened>("Leave me behind!")
                    player<Confused>("I'll leave you here, OK?")
                    npc<Confused>("Oh. OK.")
                }
                7 -> {
                    player<Happy>("Hi!")
                    player<Disheartened>("Ughhhh....")
                }
            }
        }
    }
}
