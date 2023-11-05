package data.telegram


import domain.Chat
import domain.Distributor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

class ChatRepositoryBase(
    name: String,
    token: String,
) : Chat.Repository {

    private val rawInputFlow = MutableSharedFlow<Distributor.Input>()


    private val _eventFlow = MutableSharedFlow<Distributor.Event>()
    override val eventFlow = _eventFlow.asSharedFlow()

    override suspend fun write(input: Distributor.Input): Result<Unit> {
        rawInputFlow.emit(input)
        return Result.success(Unit)
    }

    init {
        val bot = BotBase(
            name = name,
            token = token,
            inputFlow = rawInputFlow,
            eventFlow = _eventFlow,
        )
        TelegramBotsApi(DefaultBotSession::class.java).registerBot(bot)
    }


}

private class BotBase(
    private val name: String,
    token: String,
    val inputFlow: MutableSharedFlow<Distributor.Input>,
    val eventFlow: Flow<Distributor.Event>,
) : TelegramLongPollingBot(token) {

    override fun getBotUsername() = name

    val scope = CoroutineScope(Dispatchers.Unconfined)

    init {
        eventFlow
            .filterIsInstance<Distributor.Event.NewMessage>()
            .map(::toMessagePart)
            .onEach { parts ->
                parts.forEach(::execute)
            }
            .launchIn(scope)
    }

    override fun onUpdateReceived(update: Update?) {
        update ?: return
        if (update.hasMessage().not()) return
        scope.launch {
            inputFlow.emit(update.message.toDomain())
        }
    }
}
