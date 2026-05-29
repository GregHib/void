package content.quest.free.the_knights_sword

import WorldTest
import content.quest.quest
import dialogueContinue
import dialogueOption
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.type.Tile

class TheKnightsSwordTest : WorldTest() {
    override var loadNpcs: Boolean = true

    @Test
    fun `Unable to start with missing skills`() {
        val player = createPlayer(Tile(3208, 3215, 0))

        // <10 mining
    }

    @Test
    fun `Complete the quest`() {
        val player = createPlayer(Tile(3208, 3215, 0))

        // 10 mining
        // give 2 iron bars, redberry pie, pickaxe or blurite ore

        // start in falador castle
        // squire
        // talk to
        // tick
        //
        // 1
        //
        // 2
        //
        // 1
        //
        // 1
        //

        // check dialog not null
        // check quest step

        // teleport to reldo
        // reldo
        // talk
        // tick
        //
        // 4
        //

        // check dialog not null
        // check quest step

        // teleport to thurgo
        // talk
        //
        // 2
        //

        // talk
        //
        // 1
        //

        // check dialog not null
        // check no more redberry pie
        // check quest step

        // teleport to squire
        // talk
        // tick
        //

        // teleport to cupboard
        // interact open
        // interact search
        //

        // check dialog not null
        // check portrait in invent
        // check quest step

        // teleport to thrugo
        // talk
        //
        // 1
        //

        // check dialog not null
        // check no portrait in invent
        // check quest step

        // teleport to asgarnia ice dungeon
        // mine blurite ore

        // check dialog not null
        // check blurite in invent
        // check quest step

        // teleport to thurgo
        // talk
        // tick
        //
        // 1
        //

        // check dialog not null
        // check no ore and no iron bars in invent
        // check sword in invent
        // check quest step

        // teleport to squire
        // talk
        // tick
        //

        // check dialog not null
        // check no sword in invent
        // completed quest
        // check smithing exp
    }

    private fun Player.fastForwardDialogue() {
        assertNotNull(dialogue)
        require(suspension is Suspension.Continue)
        while (suspension is Suspension.Continue) {
            dialogueContinue()
        }
    }

    private fun Player.selectDialogueOption(option: Int) {
        assertNotNull(dialogue)
        require(suspension is Suspension.IntEntry)
        dialogueOption("line$option")
    }
}
