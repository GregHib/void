package rs.dusk.core.io.crypto

/**
 *
 *  An implementation of an ISAAC cipher. See [
 * http://en.wikipedia.org/wiki/ISAAC_(cipher)](http://en.wikipedia.org/wiki/ISAAC_(cipher)) for more information.
 *
 *  This implementation is based on the one written by Bob Jenkins, which is
 * available at [
 * http://www.burtleburtle.net/bob/java/rand/Rand.java](http://www.burtleburtle.net/bob/java/rand/Rand.java).
 * @author Graham Edgecombe
 */

class IsaacCipher(seed : IntArray) {
	/**
	 * The count through the results.
	 */
	private var count = 0
	
	/**
	 * The results.
	 */
	private val results = IntArray(SIZE)
	
	/**
	 * The internal memory state.
	 */
	private val memory = IntArray(SIZE)
	
	/**
	 * The accumulator.
	 */
	private var a = 0
	
	/**
	 * The last result.
	 */
	private var b = 0
	
	/**
	 * The counter.
	 */
	private var c = 0
	
	val seed : IntArray
	
	/**
	 * Gets the next value.
	 * @return The next value.
	 */
	fun nextInt() : Int {
		if (count-- == 0) {
			isaac()
			count = SIZE - 1
		}
		return results[count]
	}
	
	/**
	 * Generates 256 results.
	 */
	fun isaac() {
		var i : Int
		var j : Int
		var x : Int
		var y : Int
		b += ++c
		i = 0
		j = SIZE / 2
		while (i < SIZE / 2) {
			x = memory[i]
			a = a xor (a shl 13)
			a += memory[j++]
			y = memory[x and MASK shr 2] + a + b
			memory[i] = y
			b = memory[y shr SIZE_LOG and MASK shr 2] + x
			results[i++] = b
			x = memory[i]
			a = a xor (a ushr 6)
			a += memory[j++]
			y = memory[x and MASK shr 2] + a + b
			memory[i] = y
			b = memory[y shr SIZE_LOG and MASK shr 2] + x
			results[i++] = b
			x = memory[i]
			a = a xor (a shl 2)
			a += memory[j++]
			y = memory[x and MASK shr 2] + a + b
			memory[i] = y
			b = memory[y shr SIZE_LOG and MASK shr 2] + x
			results[i++] = b
			x = memory[i]
			a = a xor (a ushr 16)
			a += memory[j++]
			y = memory[x and MASK shr 2] + a + b
			memory[i] = y
			b = memory[y shr SIZE_LOG and MASK shr 2] + x
			results[i++] = b
		}
		j = 0
		while (j < SIZE / 2) {
			x = memory[i]
			a = a xor (a shl 13)
			a += memory[j++]
			y = memory[x and MASK shr 2] + a + b
			memory[i] = y
			b = memory[y shr SIZE_LOG and MASK shr 2] + x
			results[i++] = b
			x = memory[i]
			a = a xor (a ushr 6)
			a += memory[j++]
			y = memory[x and MASK shr 2] + a + b
			memory[i] = y
			b = memory[y shr SIZE_LOG and MASK shr 2] + x
			results[i++] = b
			x = memory[i]
			a = a xor (a shl 2)
			a += memory[j++]
			y = memory[x and MASK shr 2] + a + b
			memory[i] = y
			b = memory[y shr SIZE_LOG and MASK shr 2] + x
			results[i++] = b
			x = memory[i]
			a = a xor (a ushr 16)
			a += memory[j++]
			y = memory[x and MASK shr 2] + a + b
			memory[i] = y
			b = memory[y shr SIZE_LOG and MASK shr 2] + x
			results[i++] = b
		}
	}
	
	/**
	 * Initialises the ISAAC.
	 * @param flag Flag indicating if we should perform a second pass.
	 */
	fun init(flag : Boolean) {
		var i : Int
		var a : Int
		var b : Int
		var c : Int
		var d : Int
		var e : Int
		var f : Int
		var g : Int
		var h : Int
		h = RATIO
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
		count = SIZE
	}
	
	companion object {
		/**
		 * The golden ratio.
		 */
		const val RATIO = -0x61c88647
		
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
	
	/**
	 * Creates the ISAAC cipher.
	 * @param seed The seed.
	 */
	init {
		for (i in seed.indices) {
			results[i] = seed[i]
		}
		init(true)
		this.seed = seed
	}
}