package content.area.kandarin.tree_gnome_stronghold

import content.entity.combat.hit.damage
import content.entity.player.dialogue.HappyOld
import content.entity.player.dialogue.NeutralOld
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.miniquest.alfred_grimhands_barcrawl.barCrawlDrink
import content.quest.miniquest.alfred_grimhands_barcrawl.barCrawlFilter
import world.gregs.voidps.engine.client.ui.interact.itemOnNPCOperate
import world.gregs.voidps.engine.entity.character.mode.interact.TargetInteraction
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.noInterest

npcOperate("Talk-to", "blurberry") {
    player<Talk>("Hello.")
    npc<NeutralOld>("Well hello there traveller. If you're looking for a cocktail the barman will happily make you one.")
    npc<NeutralOld>("Or if you're looking for some training in cocktail making, then I'm your gnome! Aluft Gianne jnr. is looking for gnome cooks and bartenders to help in his new venture, so it's a useful skill to have.")
    choice {
        option<Talk>("No thanks, I prefer to stay this side of the bar.")
        option("I'm doing Alfred Grimhand's barcrawl.", filter = barCrawlFilter) {
            barCrawl()
        }
    }
}

itemOnNPCOperate("barcrawl_card", "blurberry") {
    if (player.containsVarbit("barcrawl_signatures", "fire_toad_blast")) {
        player.noInterest() // TODO proper message
        return@itemOnNPCOperate
    }
    barCrawl()
}

suspend fun TargetInteraction<Player, NPC>.barCrawl() = barCrawlDrink(
    start = { npc<HappyOld>("Ah, you've come to the best stop on your list! I'll give you my famous Fire Toad Blast! It'll cost you 10 coins.") },
    effects = { player.damage(10) },
)
