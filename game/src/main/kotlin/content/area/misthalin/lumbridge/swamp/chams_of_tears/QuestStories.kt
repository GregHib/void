package content.area.misthalin.lumbridge.swamp.chams_of_tears

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Bored
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import content.quest.quests
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.random

suspend fun Player.randomQuestStory() {
    val choice = quests.filter { questCompleted(it) }.randomOrNull(random)
    when (choice) {
        "cooks_assistant" -> {
            player<Happy>("... and in the end I found all the ingredients, so the Duke of Lumbridge had a birthday cake after all.")
            npc<Happy>("Ah, a happy ending. It would not be good for such an anniversary to go unmarked.")
        }
        "demon_slayer" -> {
            player<Happy>("... So I destroyed the demon Delrith and saved Varrock!")
            npc<Neutral>("I remember Delrith. A most unpleasant character; I am glad he has been dispatched.")
        }
        "dorics_quest" -> {
            player<Neutral>("... So once I had got all the ores he wanted, Doric let me use his anvils.")
            npc<Neutral>("Such a small task hardly seems worthy of the term 'quest'.")
        }
        "gunnars_ground" -> {
            player<Neutral>("...and the chieftain agreed to settle on the land that his ancestors had won.")
            npc<Neutral>("These militant Fremennik think they serve Guthix, but they serve only themselves.")
        }
        "the_restless_ghost" -> {
            player<Neutral>("... and once I returned the skull, the ghost was able to rest.")
            npc<Neutral>("A strange attachment to an item that has no use after one's death.")
        }
        "rune_mysteries" -> {
            player<Neutral>("... So I brought Aubury's notes to Sedridor the Head Wizard, and from then on I was able to mine Rune Essence.")
            npc<Quiz>("So the mortals above have discovered magic once more? Very interesting.")
        }
        "the_knights_sword" -> player<Neutral>("...that was how I found the Imcando Dwarves and got the knight a new sword.")
        "prince_ali_rescue" -> player<Neutral>("... and I had to disguise Prince Ali in order to smuggle him out!")
        "imp_catcher" -> {
            player<Neutral>("... It took some time, but I finally got all four beads back, and Mizgog gave me my reward.")
            npc<Angry>("Imps! I remember the age of great war, when armies of Zamorak's imps bloodied the ankles of the other gods' creatures.")
        }
        "druidic_ritual" -> {
            player<Happy>("... So Kaqemeex taught me how to use the Herblore skill.")
            npc<Neutral>("A generous reward indeed.")
        }
        "plague_city" -> player<Happy>("... and that was how I rescued Elena from West Ardougne.")
        "lost_city" -> player<Shock>("... and when I entered the door carrying the Dramen Staff, I was transported to a whole new world -- a world populated by magical fairies!")
        "enter_the_abyss" -> player<Happy>("I gained access to the Abyss.")
        // TODO more quests
        "fremennik_isles" -> player<Confused>("... so I presented the head of the Troll King to the Burgher and he gave me his own helm!")
        "eyes_of_glouphrie" -> player<Happy>("... the machine revealed that the cute creatures were actually Arposandran spies! I destroyed them, and King Narnode gave me a crystal seed that belonged to Oaknock the Engineer.")
        "the_dig_site" -> {
            player<Happy>("... and the examiner was very impressed that I had discovered an ancient altar of Zaros.")
            npc<Neutral>("Zaros? I had not heard that name for a thousand years even before the start of my sojourn here.")
        }
        "roving_elves" -> player<Shock>("... and when I planted the shard, it grew into a crystal tree!")
        "family_crest" -> player<Happy>("... So all three parts of the family crest were reunited.")
        "heroes_quest" -> player<Happy>("... So after I had retrieved all the items, I became a member of the Heroes' Guild!")
        "in_aid_of_the_myreque" -> player<Happy>("... Veliaf was very impressed by the 'Rod of Ivandis', and I can use it to kill Juves and the Juvinates!")
        "recruitment_drive" -> {
            player<Happy>("... So I figured out their weird puzzles, and the Temple Knights asked me to join as an initiate member!")
            npc<Neutral>("I have heard tales of these 'Temple Knights' from many years past. Be wary, their motivations are not what they might lead you to believe.")
        }
        "shilo_village" -> player<Happy>("... So what Rashiliyia wanted all along was to be reunited with her son in the afterlife.")
        "slug_menace" -> {
            player<Sad>("... when I told Sir Tiffy that I had accidentally freed the Mother Mallum, he promoted me to the rank of Proselyte within the Temple Knights.")
            npc<Neutral>("That was a strange response to such an unfortunate event. But the motives of the Temple Knights have always been incomprehensibly unbalanced.")
        }
        "ghost_ahoy" -> player<Neutral>("... So the people of Port Phasmatys were finally able to rest.")
        "fairy_tale_ii" -> {
            player<Happy>("... the Fairy Queen awoke, and we realised that the Godfather had betrayed her!")
            npc<Bored>("Politics is never straightforward.")
        }
        "jungle_potion" -> player<Neutral>("... and once I had gathered all the herbs, Trufitus Shakaya was able to commune with his gods.")
        "monkey_madness" -> {
            player<Happy>("... So I defeated the Jungle Demon and stopped the Monkeys' plot to take over Karamja.")
            npc<Neutral>("I see they couldn't make a monkey out of you!")
            player<Sad>("Well, actually, I was a monkey for a bit.")
        }
    }
}