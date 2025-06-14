package world.gregs.voidps.cache.secure

import java.math.BigInteger
import java.nio.ByteBuffer
import java.security.SecureRandom

object RSA {

    /**
     * Encrypt/decrypts bytes with key and modulus
     */
    fun crypt(data: ByteArray, modulus: BigInteger, exponent: BigInteger): ByteArray = BigInteger(data).modPow(exponent, modulus).toByteArray()

    /**
     * Encrypt/decrypts [ByteBuffer] with key and modulus
     */
    fun crypt(data: ByteBuffer, modulus: BigInteger, key: BigInteger): ByteBuffer = ByteBuffer.wrap(BigInteger(data.array()).modPow(key, modulus).toByteArray())

    @JvmStatic
    fun main(args: Array<String>) {
        generateRsa()
    }

    /**
     * Generates rsa values
     */
    private fun generateRsa() {
        val bits = 1024
        val random = SecureRandom()

        var p: BigInteger
        var q: BigInteger
        var phi: BigInteger
        var modulus: BigInteger
        var publicKey: BigInteger
        var privateKey: BigInteger

        do {
            p = BigInteger.probablePrime(bits / 2, random)
            q = BigInteger.probablePrime(bits / 2, random)
            phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE))

            modulus = p.multiply(q)
            publicKey = BigInteger("65537")
            privateKey = publicKey.modInverse(phi)
        } while (modulus.bitLength() != bits || privateKey.bitLength() != bits || phi.gcd(publicKey) != BigInteger.ONE)

        println("modulus: ${modulus.toString(16)}")
        println("public key: ${publicKey.toString(16)}")
        println("private key: ${privateKey.toString(16)}")
    }
}
