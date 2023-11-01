package config

data class Config(
    val mergerHost: String,
    val mergerPort: Int,
) {
    companion object {
        val default = Config(
            mergerHost = "localhost",
            mergerPort = 9000
        )
    }
}

