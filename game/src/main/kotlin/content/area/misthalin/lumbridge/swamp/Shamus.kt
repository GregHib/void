package content.area.misthalin.lumbridge.swamp

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.quest
import content.skill.woodcutting.Hatchet
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactNpc
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit

class Shamus : Script {
    init {
        npcOperate("Talk-to", "shamus") { (shamus) ->
            npc<Angry>("Ay yer big elephant! Yer've caught me, to be sure! What would an elephant like yer be wanting wid ol' Shamus then?")
            when (quest("lost_city")) {
                "unstarted" -> {
                    player<Confused>("I'm not sure.")
                    npc<Neutral>("Well you'll have to be catchin' me again when yer are, elephant!")
                    NPCs.remove(shamus)
                    statement("The leprechaun magically disappears.")
                }
                "started" -> {
                    player<Neutral>("I want to find Zanaris.")
                    npc<Neutral>("Zanaris is it now? Well well well... Yer'll be needing to be going to that funny little shed out there in the swamp, so you will.")
                    player<Confused>("...but... I thought... Zanaris was a city...?")
                    npc<Neutral>("Aye that it is!")
                    choice {
                        option("How does it fit in a shed then?") {
                            player<Quiz>("...How does it fit in a shed then?")
                            npc<Angry>("Ah yer stupid elephant! The city isn't IN the shed! The doorway to the shed is being a portal to Zanaris, so it is.")
                            player<Quiz>("So I just walk into the shed and end up in Zanaris then?")
                            dramenStaff(shamus)
                        }
                        option<Neutral>("I've been in that shed, I didn't see a city.") {
                            dramenStaff(shamus)
                        }
                    }
                }
                else -> {
                    choice {
                        option<Confused>("I'm not sure.") {
                            npc<Angry>("Ha! Look at yer! Look at the stupid elephant who tries to go catching a leprechaun when he don't even be knowing what he wants!")
                            NPCs.remove(shamus)
                            statement("The leprechaun magically disappears.")
                        }
                        option<Quiz>("How do I get to Zanaris again?") {
                            npc<Angry>("Yer stupid elephant! I'll tell yer again! Yer need to be entering the shed in the middle of the swamp while holding a dramenwood staff! Yer can make the Dramen staff")
                            npc<Angry>("from a dramen tree branch, and there's a Dramen tree on Entrana! Now leave me alone yer great elephant!")
                            NPCs.remove(shamus)
                            statement("The leprechaun magically disappears.")
                        }
                    }
                }
            }
        }

        objectOperate("Chop", "lost_city_tree") {
            val hatchet = Hatchet.best(this)
            if (hatchet == null) {
                message("You do not have a hatchet which you have the woodcutting level to use.")
                return@objectOperate
            }
            var shamus = NPCs.findOrNull(tile.regionLevel, "shamus")
            if (shamus != null) {
                talkWith(shamus)
                npc<Angry>("Hey! Yer big elephant! Don't go choppin' down me house, now!")
                return@objectOperate
            }
            shamus = NPCs.add("shamus", Tile(3139, 3211))
            shamus.softQueue("shamus_despawn", TimeUnit.SECONDS.toTicks(60)) {
                NPCs.remove(shamus)
            }
            talkWith(shamus)
            interactNpc(shamus, "Talk-to")
        }
    }

    private suspend fun Player.dramenStaff(shamus: NPC) {
        npc<Neutral>("Oh, was I fergetting to say? Yer need to be carrying a Dramenwood staff to be getting there! Otherwise Yer'll just be ending up in the shed.")
        player<Quiz>("So where would I get a staff?")
        npc<Neutral>("Dramenwood staffs are crafted from branches of the Dramen tree, so they are. I hear there's a Dramen tree over on the island of Entrana in a cave")
        npc<Neutral>("or some such. There would probably be a good place for an elephant like yer to be starting looking I reckon.")
        set("lost_city", "find_staff")
        npc<Angry>("The monks are running a ship from Port Sarim to Entrana, I hear too. Now leave me alone yer elephant!")
        NPCs.remove(shamus)
        statement("The leprechaun magically disappears.")
    }
}
