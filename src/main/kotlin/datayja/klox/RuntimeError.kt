package datayja.klox

import java.io.Serial

class RuntimeError(
    val token: Token,
    message: String?,
) : RuntimeException(message) {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -2933078107803801197L
    }
}
