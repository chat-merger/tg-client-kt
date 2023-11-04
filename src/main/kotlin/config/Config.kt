package config

data class Config(
    val mergerHost: String,
    val mergerPort: Int,
    val botToken: String,
    val botName: String,
)
