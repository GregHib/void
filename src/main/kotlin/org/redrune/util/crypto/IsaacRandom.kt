package org.redrune.util.crypto

/**
 *
 *
 * An implementation of the [ISAAC](http://www.burtleburtle.net/bob/rand/isaacafa.html) psuedorandom number
 * generator.
 *
 *
 * <pre>
 * ------------------------------------------------------------------------------
 * Rand.java: By Bob Jenkins.  My random number generator, ISAAC.
 * rand.init() -- initialize
 * rand.val()  -- get a random value
 * MODIFIED:
 * 960327: Creation (addition of randinit, really)
 * 970719: use context, not global variables, for internal state
 * 980224: Translate to Java
 * ------------------------------------------------------------------------------
</pre> *
 *
 *
 * This class has been changed to be more conformant to Java and javadoc conventions.
 *
 *
 * @author Bob Jenkins
 */
class IsaacRandom(seed: IntArray) {
    /**
     * The results given to the user.
     */
    private val results = IntArray(SIZE)
    /**
     * The internal state.
     */
    private val state = IntArray(SIZE)
    /**
     * The count through the results in the results array.
     */
    private var count = SIZE
    /**
     * The accumulator.
     */
    private var accumulator = 0
    /**
     * The last result.
     */
    private var last = 0
    /**
     * The counter.
     */
    private var counter = 0

    /**
     * Generates 256 results.
     */
    private fun isaac() {
        var i: Int
        var j: Int
        var x: Int
        var y: Int
        last += ++counter
        i = 0
        j = SIZE / 2
        while (i < SIZE / 2) {
            x = state[i]
            accumulator = accumulator xor accumulator shl 13
            accumulator += state[j++]
            y = state[x and MASK shr 2] + accumulator + last
            state[i] = y
            last = state[y shr LOG_SIZE and MASK shr 2] + x
            results[i++] = last
            x = state[i]
            accumulator = accumulator xor accumulator ushr 6
            accumulator += state[j++]
            y = state[x and MASK shr 2] + accumulator + last
            state[i] = y
            last = state[y shr LOG_SIZE and MASK shr 2] + x
            results[i++] = last
            x = state[i]
            accumulator = accumulator xor accumulator shl 2
            accumulator += state[j++]
            y = state[x and MASK shr 2] + accumulator + last
            state[i] = y
            last = state[y shr LOG_SIZE and MASK shr 2] + x
            results[i++] = last
            x = state[i]
            accumulator = accumulator xor accumulator ushr 16
            accumulator += state[j++]
            y = state[x and MASK shr 2] + accumulator + last
            state[i] = y
            last = state[y shr LOG_SIZE and MASK shr 2] + x
            results[i++] = last
        }
        j = 0
        while (j < SIZE / 2) {
            x = state[i]
            accumulator = accumulator xor accumulator shl 13
            accumulator += state[j++]
            y = state[x and MASK shr 2] + accumulator + last
            state[i] = y
            last = state[y shr LOG_SIZE and MASK shr 2] + x
            results[i++] = last
            x = state[i]
            accumulator = accumulator xor accumulator ushr 6
            accumulator += state[j++]
            y = state[x and MASK shr 2] + accumulator + last
            state[i] = y
            last = state[y shr LOG_SIZE and MASK shr 2] + x
            results[i++] = last
            x = state[i]
            accumulator = accumulator xor accumulator shl 2
            accumulator += state[j++]
            y = state[x and MASK shr 2] + accumulator + last
            state[i] = y
            last = state[y shr LOG_SIZE and MASK shr 2] + x
            results[i++] = last
            x = state[i]
            accumulator = accumulator xor accumulator ushr 16
            accumulator += state[j++]
            y = state[x and MASK shr 2] + accumulator + last
            state[i] = y
            last = state[y shr LOG_SIZE and MASK shr 2] + x
            results[i++] = last
        }
    }

    /**
     * Initializes this random number generator.
     */
    private fun init() {
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
            a = a xor b shl 11
            d += a
            b += c
            b = b xor c ushr 2
            e += b
            c += d
            c = c xor d shl 8
            f += c
            d += e
            d = d xor e ushr 16
            g += d
            e += f
            e = e xor f shl 10
            h += e
            f += g
            f = f xor g ushr 4
            a += f
            g += h
            g = g xor h shl 8
            b += g
            h += a
            h = h xor a ushr 9
            c += h
            a += b
            ++i
        }
        i = 0
        while (i < SIZE) {
            /* fill in mem[] with messy stuff */a += results[i]
            b += results[i + 1]
            c += results[i + 2]
            d += results[i + 3]
            e += results[i + 4]
            f += results[i + 5]
            g += results[i + 6]
            h += results[i + 7]
            a = a xor b shl 11
            d += a
            b += c
            b = b xor c ushr 2
            e += b
            c += d
            c = c xor d shl 8
            f += c
            d += e
            d = d xor e ushr 16
            g += d
            e += f
            e = e xor f shl 10
            h += e
            f += g
            f = f xor g ushr 4
            a += f
            g += h
            g = g xor h shl 8
            b += g
            h += a
            h = h xor a ushr 9
            c += h
            a += b
            state[i] = a
            state[i + 1] = b
            state[i + 2] = c
            state[i + 3] = d
            state[i + 4] = e
            state[i + 5] = f
            state[i + 6] = g
            state[i + 7] = h
            i += 8
        }
        i = 0
        while (i < SIZE) {
            a += state[i]
            b += state[i + 1]
            c += state[i + 2]
            d += state[i + 3]
            e += state[i + 4]
            f += state[i + 5]
            g += state[i + 6]
            h += state[i + 7]
            a = a xor b shl 11
            d += a
            b += c
            b = b xor c ushr 2
            e += b
            c += d
            c = c xor d shl 8
            f += c
            d += e
            d = d xor e ushr 16
            g += d
            e += f
            e = e xor f shl 10
            h += e
            f += g
            f = f xor g ushr 4
            a += f
            g += h
            g = g xor h shl 8
            b += g
            h += a
            h = h xor a ushr 9
            c += h
            a += b
            state[i] = a
            state[i + 1] = b
            state[i + 2] = c
            state[i + 3] = d
            state[i + 4] = e
            state[i + 5] = f
            state[i + 6] = g
            state[i + 7] = h
            i += 8
        }
        isaac()
    }

    /**
     * Gets the next random value.
     *
     * @return The next random value.
     */
    fun nextInt(): Int {
        if (0 == count--) {
            isaac()
            count = SIZE - 1
        }
        return results[count]
    }

    companion object {
        /**
         * The golden ratio.
         */
        private const val GOLDEN_RATIO = -0x61c88647
        /**
         * The log of the size of the result and state arrays.
         */
        private const val LOG_SIZE = java.lang.Long.BYTES
        /**
         * The size of the result and states arrays.
         */
        private const val SIZE = 1 shl LOG_SIZE
        /**
         * A mask for pseudo-random lookup.
         */
        private const val MASK = SIZE - 1 shl 2
    }

    /**
     * Creates the random number generator with the specified seed.
     *
     * @param seed The seed.
     */
    init {
        val length = Math.min(seed.size, results.size)
        System.arraycopy(seed, 0, results, 0, length)
        init()
    }
}