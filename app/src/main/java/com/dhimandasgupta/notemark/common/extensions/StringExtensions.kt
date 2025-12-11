package com.dhimandasgupta.notemark.common.extensions

// Compile regex patterns once
private val EMAIL_REGEX = """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""".toRegex()
private val USERNAME_REGEX = """^[a-zA-Z0-9_-]{3,20}$""".toRegex()
private val PASSWORD_REGEX = """^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[@$!%*?&]).{8,}""".toRegex()
private val SPACE_REGEX = """\s+""".toRegex()

fun String.isValidEmail(): Boolean {
    // The regex pattern for a practical email validation
    return this.matches(EMAIL_REGEX)
}

/**
 * A simple username validation.
 * Allows alphanumeric chars, underscore, and hyphen. Length 3-20.
 */
fun String.isUsernameValid(): Boolean {
    return this.matches(USERNAME_REGEX)
}

/**
 * Checks if the String is a strong password based on the defined regex.
 *
 * A strong password requires:
 * - At least 8 characters
 * - At least one lowercase letter
 * - At least one uppercase letter
 * - At least one digit
 * - At least one special character from the set @$!%*?&
 *
 * @return True if the string meets the criteria, false otherwise.
 */
fun String.isValidPassword(): Boolean {
    // Regex explanation:
    // ^                  # start-of-string
    // (?=.*[a-z])        # a lowercase letter must occur at least once
    // (?=.*[A-Z])        # an uppercase letter must occur at least once
    // (?=.*[0-9])        # a digit must occur at least once
    // (?=.*[@$!%*?&])    # a special character must occur at least once
    // .{8,}              # anything, at least eight places long
    return this.matches(PASSWORD_REGEX)
}


/**
 * Formats a user's full name into a short, typically two-letter, representation.
 *
 * This function processes a name string and returns a formatted version based on the following rules:
 * - Trims leading and trailing whitespace from the input string.
 * - If the trimmed string is empty, returns an empty string.
 * - Splits the name into parts based on whitespace.
 * - If there are two or more name parts (e.g., "John Doe", "Jane Anne Doe"):
 *     - Takes the first character of the first part and the first character of the last part.
 *     - Converts both characters to uppercase.
 *     - Concatenates them (e.g., "John Doe" -> "JD", "Jane Anne Doe" -> "JE").
 * - If there is only one name part (e.g., "John", "Al"):
 *     - If the single name part has 2 or fewer characters, it's returned in uppercase (e.g., "Al" -> "AL").
 *     - If the single name part has more than 2 characters, the first two characters are returned in uppercase (e.g., "John" -> "JO").
 * - In the unlikely event that no name parts are found after trimming and splitting (which should be handled by the initial empty check),
 *   it returns an empty string as a fallback.
 *
 * Examples:
 * - "   John Doe   " -> "JD"
 * - "SingleName" -> "SI"
 * - "   Al   " -> "AL"
 * - "Mary Anne Smith" -> "MH"
 * - "  " -> ""
 * - "X" -> "X"
 *
 * @return A formatted string representing the user's initials or a shortened version of their name,
 *         or an empty string if the input is blank or cannot be processed.
 */
fun String.formatUserName(): String {
    val nameParts = this.split(SPACE_REGEX).filter { it.isNotEmpty() } // Split by whitespace and remove empty parts

    return when {
        nameParts.size >= 2 -> {
            // First char of the first part + First char of the last part
            "${nameParts.first().first().uppercaseChar()}${nameParts.last().first().uppercaseChar()}"
        }
        nameParts.isNotEmpty() -> { // Single word name
            val singleName = nameParts.first()
            if (singleName.length <= 2) {
                singleName.uppercase()
            } else {
                singleName.take(n = 2).uppercase()
            }
        }
        else -> "" // Should not happen if trim().ifEmpty{} is used, but good for safety
    }
}