package content.area.misthalin.lumbridge.thieves_guild

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has

class DarrenLightfinger : Script {
    init {
        // https://runescape.wiki/w/Transcript:Guildmaster_Darren_Lightfinger#After_Buyers_and_Cellars,_without_24_Thieving
        // TODO "Can we try out that training dummy again?" needs the Mark I training dummy
        // TODO with 24 Thieving he starts the From Tiny Acorns caper instead
        npcOperate("Talk-to", "guildmaster_darren_3") {
            npc<Happy>("Ah, $name, there you are. How is the shadowy world of crime doing up there?")
            player<Happy>("Very shady, thank you.")
            choice {
                guild()
                option<Quiz>("So what's new around here?") {
                    npc<Happy>("We have a training room for you to try out, like all good guilds should have! Talk to the trainers about the hanky points system I've had them set up.")
                    npc<Neutral>("I'm also looking for a caper to be done by someone with proven skill in thievery.")
                    if (!has(Skill.Thieving, 24)) {
                        statement("The Thieves' Guild will have more work for you at level 24 Thieving.")
                    }
                }
                option<Neutral>("Sorry, I was just leaving.")
            }
        }
    }

    private fun ChoiceOption.guild() {
        option<Quiz>("How's the guild coming along these days?") {
            npc<Happy>("Thanks to your efforts we have a real training room now, although we're somewhat short on staff. Still, from tiny acorns grow mighty oaks...which then get cut down by adventurers.")
            anythingElse()
        }
    }

    private suspend fun Player.anythingElse() {
        npc<Neutral>("Anything else I can do for you?")
        choice {
            guild()
            option<Neutral>("Sorry, I was just leaving.")
        }
    }
}
