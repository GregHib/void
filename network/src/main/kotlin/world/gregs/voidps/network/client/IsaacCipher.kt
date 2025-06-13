package world.gregs.voidps.network.client

/**
 * An implementation of an ISAAC cipher. See
 * [http://en.wikipedia.org/wiki/ISAAC_(cipher)](http://en.wikipedia.org/wiki/ISAAC_(cipher)) for more information.
 *
 * This implementation is based on the one written by Bob Jenkins, which is
 * available at [http://www.burtleburtle.net/bob/java/rand/Rand.java](http://www.burtleburtle.net/bob/java/rand/Rand.java).
 * @author Graham
 */
class IsaacCipher(seed: IntArray) {
    private var resultCount = 0
    private val results = IntArray(SIZE)

    /**
     * The internal memory state.
     */
    private val memory = IntArray(SIZE)

    private var accumulator = 0
    private var lastResult = 0
    private var counter = 0
    private val seed: IntArray

    init {
        for (i in seed.indices) {
            results[i] = seed[i]
        }
        init(true)
        this.seed = seed
    }

    fun nextInt(): Int {
        if (resultCount-- == 0) {
            isaac()
            resultCount = SIZE - 1
        }
        return results[resultCount]
    }

    /**
     * Generates 256 results.
     */
    private fun isaac() {
        var x: Int
        var y: Int
        lastResult += ++counter
        var i = 0
        var j = SIZE / 2
        while (i < SIZE / 2) {
            x = memory[i]
            accumulator = accumulator xor (accumulator shl 13)
            accumulator += memory[j++]
            y = memory[x and MASK shr 2] + accumulator + lastResult
            memory[i] = y
            lastResult = memory[y shr SIZE_LOG and MASK shr 2] + x
            results[i++] = lastResult
            x = memory[i]
            accumulator = accumulator xor (accumulator ushr 6)
            accumulator += memory[j++]
            y = memory[x and MASK shr 2] + accumulator + lastResult
            memory[i] = y
            lastResult = memory[y shr SIZE_LOG and MASK shr 2] + x
            results[i++] = lastResult
            x = memory[i]
            accumulator = accumulator xor (accumulator shl 2)
            accumulator += memory[j++]
            y = memory[x and MASK shr 2] + accumulator + lastResult
            memory[i] = y
            lastResult = memory[y shr SIZE_LOG and MASK shr 2] + x
            results[i++] = lastResult
            x = memory[i]
            accumulator = accumulator xor (accumulator ushr 16)
            accumulator += memory[j++]
            y = memory[x and MASK shr 2] + accumulator + lastResult
            memory[i] = y
            lastResult = memory[y shr SIZE_LOG and MASK shr 2] + x
            results[i++] = lastResult
        }
        j = 0
        while (j < SIZE / 2) {
            x = memory[i]
            accumulator = accumulator xor (accumulator shl 13)
            accumulator += memory[j++]
            y = memory[x and MASK shr 2] + accumulator + lastResult
            memory[i] = y
            lastResult = memory[y shr SIZE_LOG and MASK shr 2] + x
            results[i++] = lastResult
            x = memory[i]
            accumulator = accumulator xor (accumulator ushr 6)
            accumulator += memory[j++]
            y = memory[x and MASK shr 2] + accumulator + lastResult
            memory[i] = y
            lastResult = memory[y shr SIZE_LOG and MASK shr 2] + x
            results[i++] = lastResult
            x = memory[i]
            accumulator = accumulator xor (accumulator shl 2)
            accumulator += memory[j++]
            y = memory[x and MASK shr 2] + accumulator + lastResult
            memory[i] = y
            lastResult = memory[y shr SIZE_LOG and MASK shr 2] + x
            results[i++] = lastResult
            x = memory[i]
            accumulator = accumulator xor (accumulator ushr 16)
            accumulator += memory[j++]
            y = memory[x and MASK shr 2] + accumulator + lastResult
            memory[i] = y
            lastResult = memory[y shr SIZE_LOG and MASK shr 2] + x
            results[i++] = lastResult
        }
    }

    /**
     * Initialises the ISAAC.
     * @param flag Flag indicating if we should perform a second pass.
     */
    fun init(flag: Boolean) {
        var i: Int
        var a: Int
        var b: Int
        var c: Int
        var d: Int
        var e: Int
        var f: Int
        var g: Int
        var h: Int
        h = GOLDEN_RATIO
        g = h
        f = g
        e = f
        d = e
        c = d
        b = c
        a = b
        i = 0
        while (i < 4) {
            a = a xor (b shl 11)
            d += a
            b += c
            b = b xor (c ushr 2)
            e += b
            c += d
            c = c xor (d shl 8)
            f += c
            d += e
            d = d xor (e ushr 16)
            g += d
            e += f
            e = e xor (f shl 10)
            h += e
            f += g
            f = f xor (g ushr 4)
            a += f
            g += h
            g = g xor (h shl 8)
            b += g
            h += a
            h = h xor (a ushr 9)
            c += h
            a += b
            ++i
        }
        i = 0
        while (i < SIZE) {
            if (flag) {
                a += results[i]
                b += results[i + 1]
                c += results[i + 2]
                d += results[i + 3]
                e += results[i + 4]
                f += results[i + 5]
                g += results[i + 6]
                h += results[i + 7]
            }
            a = a xor (b shl 11)
            d += a
            b += c
            b = b xor (c ushr 2)
            e += b
            c += d
            c = c xor (d shl 8)
            f += c
            d += e
            d = d xor (e ushr 16)
            g += d
            e += f
            e = e xor (f shl 10)
            h += e
            f += g
            f = f xor (g ushr 4)
            a += f
            g += h
            g = g xor (h shl 8)
            b += g
            h += a
            h = h xor (a ushr 9)
            c += h
            a += b
            memory[i] = a
            memory[i + 1] = b
            memory[i + 2] = c
            memory[i + 3] = d
            memory[i + 4] = e
            memory[i + 5] = f
            memory[i + 6] = g
            memory[i + 7] = h
            i += 8
        }
        if (flag) {
            i = 0
            while (i < SIZE) {
                a += memory[i]
                b += memory[i + 1]
                c += memory[i + 2]
                d += memory[i + 3]
                e += memory[i + 4]
                f += memory[i + 5]
                g += memory[i + 6]
                h += memory[i + 7]
                a = a xor (b shl 11)
                d += a
                b += c
                b = b xor (c ushr 2)
                e += b
                c += d
                c = c xor (d shl 8)
                f += c
                d += e
                d = d xor (e ushr 16)
                g += d
                e += f
                e = e xor (f shl 10)
                h += e
                f += g
                f = f xor (g ushr 4)
                a += f
                g += h
                g = g xor (h shl 8)
                b += g
                h += a
                h = h xor (a ushr 9)
                c += h
                a += b
                memory[i] = a
                memory[i + 1] = b
                memory[i + 2] = c
                memory[i + 3] = d
                memory[i + 4] = e
                memory[i + 5] = f
                memory[i + 6] = g
                memory[i + 7] = h
                i += 8
            }
        }
        isaac()
        resultCount = SIZE
    }

    companion object {
        const val GOLDEN_RATIO = -0x61c88647

        /**
         * The log of the size of the results and memory arrays.
         */
        const val SIZE_LOG = 8

        /**
         * The size of the results and memory arrays.
         */
        const val SIZE = 1 shl SIZE_LOG

        /**
         * For pseudorandom lookup.
         */
        const val MASK = SIZE - 1 shl 2
    }
}
