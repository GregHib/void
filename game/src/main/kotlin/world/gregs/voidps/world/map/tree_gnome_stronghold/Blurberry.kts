package world.gregs.voidps.world.map.tree_gnome_stronghold

import world.gregs.voidps.engine.client.ui.interact.itemOnNPCOperate
import world.gregs.voidps.engine.entity.character.mode.interact.TargetInteraction
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.world.activity.quest.mini.barCrawlDrink
import world.gregs.voidps.world.activity.quest.mini.barCrawlFilter
import world.gregs.voidps.world.interact.dialogue.HappyOld
import world.gregs.voidps.world.interact.dialogue.NeutralOld
import world.gregs.voidps.world.interact.dialogue.Talk
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.combat.hit.damage

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
    effects = { player.damage(10) }
)