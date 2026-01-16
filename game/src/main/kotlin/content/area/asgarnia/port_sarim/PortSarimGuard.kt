package content.area.asgarnia.port_sarim

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.PatrolDefinitions
import world.gregs.voidps.engine.entity.character.mode.Patrol
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.type.random

class PortSarimGuard(
    val patrols: PatrolDefinitions,
) : Script {

    init {
        npcSpawn("port_sarim_guard_6") {
            val patrol = patrols.get("port_sarim_guard")
            mode = Patrol(this, patrol.waypoints)
        }

        npcOperate("Talk-to", "port_sarim_guard_sleeping") {
            npc<Asleep>(
                "port_sarim_guard_6",
                when (random.nextInt(4)) {
                    0 -> "Mmmm... big pint of beer... kebab..."
                    1 -> "Guh... mwww... zzzzz..."
                    2 -> "Hmph... heh heh heh..."
                    else -> "Mmmmmm... donuts..."
                },
            )
            player<Idle>("Maybe I should let him sleep.")
        }

        npcOperate("Talk-to", "port_sarim_guard_6") {
            npc<Angry>("HALT! Who goes there?")
            choice {
                option<Happy>("Don't worry, I'm not going to cause trouble.") {
                    npc<Idle>("But you shouldn't be here - be off with you!")
                    player<Idle>("I was going anyway.")
                }
                option<Happy>("I am $name the Mighty!") {
                    npc<Idle>("Mighty? You look like another of those silly adventurers who thinks they're the bee's knees just because they've done a few lousy quests!")
                    player<Idle>("Well it sounds better than sitting on this rooftop all day looking at trees!")
                    npc<Angry>("I'll have you know it's a very important job guarding this jail!")
                    npc<Idle>("If anyone comes sneaking in here to mess around with the prisoners, the lads downstairs will make mincemeat of them, and I'll be here to pick them off if they try to escape.")
                    player<Quiz>("You mean people aren't meant to be able to shoot the prisoners in the cells?")
                    npc<Idle>("Yes, that's right.")
                    player<Laugh>("Okay, it's been nice talking to you.")
                }
                option<Happy>("No-one, there's no-one here.") {
                    npc<Idle>("What? I can see you!")
                    player<Happy>("No, you're just imagining it. Perhaps you've been up here in the sun for too long?")
                    npc<Confused>("So who am I talking to?")
                    player<Happy>("Oh dear, you've started talking to yourself. That's a common sign that you're going mad!")
                    npc<Confused>("But... but... you're standing right there...")
                    statement("Maybe you should leave him alone now.")
                }
            }
        }
    }
}
