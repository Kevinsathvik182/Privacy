package com.example.crypto

data class CipherStep(
    val originalChar: Char,
    val charIndex: Int,
    val halfValue: Double,
    val token: String,
    val isAlpha: Boolean
)

object HalfNumberCipher {

    /**
     * Converts raw text to Half-Number Cipher.
     * Example: "HI" -> "4.0 4.5"
     * A=1 (0.5), B=2 (1.0), C=3 (1.5) ... H=8 (4.0), I=9 (4.5) ... Z=26 (13.0)
     * Space is represented as "/"
     */
    fun encrypt(text: String): String {
        if (text.isEmpty()) return ""
        val tokens = mutableListOf<String>()

        for (char in text) {
            when {
                char.isLetter() -> {
                    val upper = char.uppercaseChar()
                    val index = upper - 'A' + 1 // 1 to 26
                    val half = index / 2.0
                    // Format with 1 decimal place (e.g., 4.0, 4.5)
                    tokens.add(String.format(java.util.Locale.US, "%.1f", half))
                }
                char == ' ' -> {
                    tokens.add("/")
                }
                char.isDigit() -> {
                    val digitVal = char - '0'
                    tokens.add("#${String.format(java.util.Locale.US, "%.1f", digitVal / 2.0)}")
                }
                else -> {
                    // Punctuation is preserved as a distinct symbol token
                    tokens.add(char.toString())
                }
            }
        }
        return tokens.joinToString(" ")
    }

    /**
     * Converts Half-Number Cipher back to plain text.
     * Example: "4.0 4.5" -> "HI"
     * "4.0" -> 4.0 * 2 = 8 -> 'H'
     * "4.5" -> 4.5 * 2 = 9 -> 'I'
     * "/" -> ' '
     */
    fun decrypt(cipherText: String): String {
        if (cipherText.isBlank()) return ""
        val tokens = cipherText.trim().split(Regex("\\s+"))
        val result = StringBuilder()

        for (token in tokens) {
            when {
                token == "/" -> {
                    result.append(' ')
                }
                token.startsWith("#") -> {
                    // Digit token
                    val numStr = token.removePrefix("#")
                    val halfVal = numStr.toDoubleOrNull()
                    if (halfVal != null) {
                        val digit = (halfVal * 2.0).toInt()
                        if (digit in 0..9) {
                            result.append(digit)
                        } else {
                            result.append(token)
                        }
                    } else {
                        result.append(token)
                    }
                }
                token.toDoubleOrNull() != null -> {
                    val halfVal = token.toDouble()
                    val letterIndex = (halfVal * 2.0).toInt() // 1 to 26
                    if (letterIndex in 1..26) {
                        val char = ('A' + letterIndex - 1)
                        result.append(char)
                    } else {
                        result.append("[$token]")
                    }
                }
                else -> {
                    // Preserved punctuation/symbols
                    result.append(token)
                }
            }
        }
        return result.toString()
    }

    /**
     * Generates a step-by-step mathematical breakdown of the cipher transformation.
     */
    fun analyzeTransformation(text: String): List<CipherStep> {
        val steps = mutableListOf<CipherStep>()
        for (char in text) {
            when {
                char.isLetter() -> {
                    val upper = char.uppercaseChar()
                    val index = upper - 'A' + 1
                    val half = index / 2.0
                    val token = String.format(java.util.Locale.US, "%.1f", half)
                    steps.add(CipherStep(char, index, half, token, true))
                }
                char == ' ' -> {
                    steps.add(CipherStep(' ', 0, 0.0, "/", false))
                }
                char.isDigit() -> {
                    val digitVal = char - '0'
                    val half = digitVal / 2.0
                    val token = "#${String.format(java.util.Locale.US, "%.1f", half)}"
                    steps.add(CipherStep(char, digitVal, half, token, false))
                }
                else -> {
                    steps.add(CipherStep(char, -1, -1.0, char.toString(), false))
                }
            }
        }
        return steps
    }
}
