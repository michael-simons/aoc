import java.security.MessageDigest

fun digest(bytes: ByteArray): ByteArray = MessageDigest.getInstance("MD5").digest(bytes)

private val HEX_DIGITS = "0123456789abcdef".toCharArray()

fun encode(bytes: ByteArray): String =
    bytes.fold("", { str, b -> str + HEX_DIGITS[(b.toInt() shr 4) and 0xf] + HEX_DIGITS[b.toInt() and 0xf] })

fun findHashStartingWith(numDigits: Int, input: String = "iwrupvqb"): Long {
    val prefix = "0".repeat(numDigits)
    return generateSequence(0L) { it + 1 }
        .filter { i ->
            (input + i)
                .toByteArray()
                .run(::digest)
                .run(::encode)
                .startsWith(prefix)
        }
        .first()
}

println("Star one ${findHashStartingWith(5)}")
println("Star two ${findHashStartingWith(6)}")
