import config.Config
import di.KoinModules
import org.koin.core.context.startKoin
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import java.util.*

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
    val bot = BotBase(
        name = cfg.botName,
        token = cfg.botToken,
    )
    TelegramBotsApi(DefaultBotSession::class.java).registerBot(bot)
}


class BotBase(
    val name: String,
    val token: String,
) : TelegramLongPollingBot(token) {

    override fun getBotUsername() = name

    override fun onUpdateReceived(update: Update?) {
        update ?: return

        println("update received:\n${update}")
        val msg = SendMessage()
        msg.chatId = update.message.chatId.toString()
        msg.text = "m:" + Random().nextFloat() + "\n" + update.message.text
        try {
            execute(msg)
        } catch (_: TelegramApiException) {
        }
    }
}

