package world.gregs.voidps.engine.entity.list

import org.koin.dsl.module
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.gfx.Graphics
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.entity.proj.Projectiles
import world.gregs.voidps.engine.entity.sound.Sounds

const val MAX_PLAYERS = 0x800// 2048
const val MAX_NPCS = 0x8000// 32768

val entityListModule = module {
    single { NPCs(get(), get(), get(), get()) }
    single { Players() }
    single { Objects() }
    single { FloorItems(get(), get(), get(), get(), get()) }
    single { Projectiles() }
    single { Graphics() }
    single { Sounds() }
}