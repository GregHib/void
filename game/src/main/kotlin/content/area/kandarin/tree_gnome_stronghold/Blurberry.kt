package content.area.kandarin.tree_gnome_stronghold

import content.entity.combat.hit.damage
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.miniquest.alfred_grimhands_barcrawl.barCrawlDrink
import content.quest.miniquest.alfred_grimhands_barcrawl.onBarCrawl
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

class Blurberry : Script {

    init {
        npcOperate("Talk-to", "blurberry") { (target) ->
            player<Neutral>("Hello.")
            npc<Idle>("Well hello there traveller. If you're looking for a cocktail the barman will happily make you one.")
            npc<Idle>("Or if you're looking for some training in cocktail making, then I'm your gnome! Aluft Gianne jnr. is looking for gnome cooks and bartenders to help in his new venture, so it's a useful skill to have.")
            choice {
                option<Neutral>("No thanks, I prefer to stay this side of the bar.")
                if (onBarCrawl(target)) {
                    option("I'm doing Alfred Grimhand's barcrawl.") {
                        barCrawl(target)
                    }
                }
            }
        }

        itemOnNPCOperate("barcrawl_card", "blurberry") { (target) ->
            if (containsVarbit("barcrawl_signatures", "fire_toad_blast")) {
                return@itemOnNPCOperate
            }
            barCrawl(target)
        }
    }

    suspend fun Player.barCrawl(target: NPC) = barCrawlDrink(
        target,
        start = { npc<Happy>("Ah, you've come to the best stop on your list! I'll give you my famous Fire Toad Blast! It'll cost you 10 coins.") },
        effects = { damage(10) },
    )
}
