package content.quest.member.fairy_tale_part_2.fairy_ring

import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader

object FairyRingCodesLoader : Loader<FairyRingCodes> {
    val instance = FairyRingCodes()
    override val name = "fairy_ring_codes"
    override val file = listOf("definitions.fairyCodes")

    override fun load(context: LoadContext): FairyRingCodes = FairyRingCodes().load(context.paths(this).single())

    override fun install(state: FairyRingCodes) = instance.copyFrom(state)
}
