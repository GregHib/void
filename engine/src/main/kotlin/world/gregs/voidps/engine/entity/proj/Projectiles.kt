package world.gregs.voidps.engine.entity.proj

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import world.gregs.voidps.engine.entity.BatchList

class Projectiles(
    override val chunks: MutableMap<Int, MutableList<Projectile>> = Int2ObjectOpenHashMap()
) : BatchList<Projectile>