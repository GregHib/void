package world.gregs.voidps

import kotlin.random.Random

open class FakeRandom : Random() {
    override fun nextBits(bitCount: Int): Int = 0
}