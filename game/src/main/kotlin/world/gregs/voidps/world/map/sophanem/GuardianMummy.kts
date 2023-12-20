package world.gregs.voidps.world.map.sophanem

import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.world.interact.dialogue.Talk
import world.gregs.voidps.world.interact.dialogue.Talking
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.dialogue.type.statement

on<NPCOption>({ operate && target.id == "guardian_mummy" && option == "Talk-to" }) { player: Player ->
    if (player.holdsItem("pharaohs_sceptre")) {
        player<Talk>("This sceptre seems to have run out of charges.")
        npc<Talk>("You shouldn't have that thing in the first place, thief!")
        player<Talk>("""
            If I gave you back some of the artefacts I've taken
            from the tomb, would you recharge the sceptre for me?
        """)
        npc<Talking>("""
            *sigh* Oh alright. But only if the sceptre is fully
            empty, I'm not wasting the King's magic...
        """)
        choice("Recharge the sceptre with...") {
            option("Gold artefacts?") {
                statement("You recharge your sceptre with gold artefacts.")
            }
            option("Stone artefacts?") {
                statement("You recharge your sceptre with stone artefacts.")
            }
            option("Pottery and Ivory artefacts?")
            option("Actually, I'm more interested in plundering the tombs.")
        }
    }
}