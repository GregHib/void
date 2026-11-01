package content.area.misthalin.wizards_tower.runecrafting_guild

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.minigame.great_orb_project.GreatOrbProject
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class WizardRunecraftingGuild : Script {
    init {
        npcOperate("Talk-to", "wizard_runecrafting_guild") {
            npc<Happy>("Hello!")
            choice {
                option<Happy>("I want to join the orb project!") {
                    npc<Neutral>("You can speak to Wizard Acantha or Wizard Vief if you want to join a particular team. Or if you like I could assign you to whichever team has fewer members.")
                    choice {
                        option<Neutral>("Please assign me to a team.") {
                            join()
                        }
                        option<Neutral>("I'll talk to one of the wizards.") {
                            npc<Happy>("Okay!")
                        }
                    }
                }
                option<Neutral>("Never mind.")
            }
        }

        npcOperate("Join-game", "wizard_runecrafting_guild") {
            join()
        }
    }

    private suspend fun Player.join() {
        if (get("gop_team", "") != "") {
            npc<Neutral>("You're already on a team. Wait for the portal to open and then go through.")
            return
        }
        if (GreatOrbProject.cantJoin(this)) {
            npc<Neutral>("You need your head, right hand and two free inventory slots, or you won't be able to hold the necessary equipment.")
            return
        }
        // TODO if (green count < yellow) green else yellow
        GreatOrbProject.join(this, "yellow")
        npc<Neutral>("I've put you on Wizard Vief's team. You'll need to attract the yellow orbs into the altar.")
    }
}
