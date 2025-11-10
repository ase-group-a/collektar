package exceptions

open class RateLimitException(
    override val message: String? = "Rate limited",
    val retryAfterSeconds: Long = 1L
) : RuntimeException(message)
