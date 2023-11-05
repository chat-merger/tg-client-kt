
import config.Config
import di.KoinModules
import org.koin.core.context.startKoin

fun main() {
    val cfg = Config(
        mergerHost = "localhost",
        mergerPort = 9000,
        botToken = System.getenv("BOT_TOKEN"),
        botName = System.getenv("BOT_NAME"),
    )
    startKoin {
        modules(
            KoinModules.core(cfg = cfg),
            KoinModules.baseRepositories,
        )
    }
    readln()
}

