import di.KoinModules
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin


fun main(args: Array<String>) = runBlocking {
    startKoin {
        modules(
            KoinModules.main,
            KoinModules.testRepositories,
//            KoinModules.baseRepositories,
        )
    }
    val stream = qwe()
    delay(1000 * 100)
}
