package world.gregs.voidps.world.map.varrock

import world.gregs.voidps.engine.event.CharacterContext
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.npc.minimumCanoeLevel

npcOperate("Talk-To", "tarquin") {
    player<Neutral>("Hello there.")
    npc<RollEyes>("Hello old bean. Is there something I can help you with?")
    choice {
        option<Neutral>("Who are you?") {
            npc<RollEyes>("My name is Tarquin Marjoribanks.")
            npc<Neutral>("I'd be surprised if you haven't already heard of me?")
            player<Quiz>("Why would I have heard of you Mr. Marjoribanks?")
            npc<Frustrated>("It's pronounced 'Marchbanks'!")
            npc<Neutral>("You should know of me because I am a member of the royal family of Misthalin!")
            player<Quiz>("Are you related to King Roald?")
            npc<Happy>("Oh yes! Quite closely actually")
            npc<Neutral>("I'm his 4th cousin, once removed on his mothers side.")
            player<Uncertain>("Er... Okay. What are you doing here then?")
            npc<Neutral>("I'm canoeing on the river! It's enormous fun! Would you like to know how?")
            choice {
                option("Yes") {
                    canoeing()
                }
                option("No") {
                    player<Neutral>("No thanks, not right now.")
                }
            }
        }
        option("Can you teach me about Canoeing?") {
            canoeing()
        }
    }
}

suspend fun CharacterContext<Player>.canoeing() {
    if (minimumCanoeLevel()) {
        return
    }
    npc<Happy>("It's really quite simple to make. Just walk down to that tree on the bank and chop it down.")
    npc<Happy>("When you have done that you can shape the log further with your axe to make a canoe.")
    npc<Happy>("My personal favourite is the Stable Dugout canoe. A finer craft you'll never see old bean!")
    npc<Happy>("A Stable Dugout canoe will take you pretty much the length of the Lum river.")
    npc<RollEyes>("Of course there are other canoes.")
    when (player.levels.get(Skill.Woodcutting)) {
        in 12..26 -> {
            npc<Surprised>("Further up river, near the Barbarian Village, I saw some darned fool 'canoeing' on a log!")
            npc<RollEyes>("Unfortunately, you don't have the skill to create anything more than one of those logs. I dare say it will only get 1 stop down the river!")
            npc<RollEyes>("Still, I'm sure it will satisfy one such as yourself.")
            player<Frustrated>("What's that supposed to mean?")
            npc<Frustrated>("Do not profane the royal house of Varrock by engaging me in further discourse you knave!")
            player<Surprised>("Pfft! I doubt he even knows the King!")
        }
        in 27..41 -> {
            npc<Happy>("You seem to be quite handy with an axe though!")
            npc<RollEyes>("I'm sure you can build a Dugout canoe. Not as fine as a Stable Dugout but it will carry you 2 stops on the river.")
            npc<RollEyes>("I should imagine it would suit your limited means.")
            player<Frustrated>("What do you mean when you say 'limited means'?")
            npc<Surprised>("Well, you're just an itinerant adventurer!")
            npc<Frustrated>("What possible reason would you have for cluttering up my river with your inferior water craft!")
        }
        in 42..56 -> {
            npc<Happy>("Ah! Perfect! You can make a Stable Dugout canoe! One of those will carry you to any civilised place on the river.")
            npc<Neutral>("If you were of good pedigree I'd let you join my boat club. You seem to be one of those vagabond adventurers though.")
            player<Frustrated>("Charming!")
            npc<Frustrated>("Be off with you rogue!")
        }
        else -> {
            npc<Happy>("My personal favourite is the Stable Dugout canoe. A finer craft you'll never see old bean!")
            npc<Happy>("A Stable Dugout canoe will take you pretty much the length of the Lum river.")
            npc<RollEyes>("Of course there are other canoes.")
            npc<Surprised>("Well ... erm. You seem to be able to make a Waka!")
            player<Happy>("Sounds fun, what's a Waka.")
            npc<Neutral>("I've only ever seen one man on the river who uses a Waka. A big, fearsome looking fellow up near Edgeville.")
            npc<Quiz>("People say he was born in the Wilderness and that he is looking for a route back.")
            player<Surprised>("Is that true!")
            npc<RollEyes>("How should I know? I would not consort with such a base fellow!")
        }
    }
}