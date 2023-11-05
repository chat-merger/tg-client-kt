
import config.Config
import data.merger.DistributorRepositoryBase
import data.telegram.ChatRepositoryBase
import domain.Distributor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

fun main() {
    val cfg = Config(
        mergerHost = "localhost",
        mergerPort = 9000,
        botToken = System.getenv("BOT_TOKEN"),
        botName = System.getenv("BOT_NAME"),
    )
    val distr = DistributorRepositoryBase(
        host = cfg.mergerHost,
        port = cfg.mergerPort,
    )
    val chat = ChatRepositoryBase(
        name = cfg.botName,
        token = cfg.botToken,
    )
    val scope = CoroutineScope(Dispatchers.Unconfined)

    chat.eventFlow.onEach {
        distr.write(it.toInput())
    }.launchIn(scope)
    distr.eventFlow.onEach {
        chat.write(it.toInput())
    }.launchIn(scope)

    readln()
}


fun Distributor.Event.toInput(): Distributor.Input {
    return when (this) {
        Distributor.Event.Delete -> TODO()
        Distributor.Event.Edit -> TODO()
        is Distributor.Event.NewMessage -> Distributor.Input.CreateMessage(
            createdAt = createdAt,
            author = author?.name,
            action = action,
            modifiers = modifiers,
            attachments = attachments,
        )
    }
}
