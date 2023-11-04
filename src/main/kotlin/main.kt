
import config.Config
import di.KoinModules
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin


fun main() = runBlocking {
    startKoin {
        modules(
            KoinModules.main(cfg =  Config.default),
            KoinModules.baseRepositories,
        )
    }
}
