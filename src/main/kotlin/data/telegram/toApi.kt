package data.telegram

import Res
import domain.Distributor
import org.telegram.telegrambots.meta.api.methods.send.*
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.bots.AbsSender

val APP_TAG = "tg_cl"

data class MessagePart(
    val groupId: String,
    val modifiers: List<Modifier>,
    val content: Content,
) {
    enum class Modifier {
        Silent, Spoiler
    }

    sealed interface Content {
        class Header(
            val app: String,
            val author: String?,
            val replyTo: ReplayedMessage?, // is string, class or Content case?
        ) : Content {
            data class ReplayedMessage(
                val name: String?,
                val text: String?,
            )
        }

        // dont have inner header
        data class Media(
            val url: String,
            val type: Type,
            val caption: String? = null,
            val header: Header? = null,
        ) : Content {
            enum class Type { Audio, Video, File, Photo, }
        }

        class Sticker(val url: String) : Content

        // contains header
        class Text(val value: String, val header: Header? = null) : Content
    }
}

fun toPartModifier(modifier: Distributor.Modifier): MessagePart.Modifier? =
    when (modifier) {
        Distributor.Modifier.Silent -> MessagePart.Modifier.Silent
        Distributor.Modifier.Spoiler -> MessagePart.Modifier.Spoiler
        else -> null
    }


inline fun <reified T, Y> List<Y>.separate(): Pair<T?, List<Y>> =
    partition { it is T }
        .let { it.first.firstOrNull() as? T to it.second }

fun Distributor.Event.NewMessage.createHeaderContent(replyTo: MessagePart.Content.Header.ReplayedMessage? = null): MessagePart.Content.Header {
    return MessagePart.Content.Header(
        app = APP_TAG,
        author = author?.name,
        replyTo = replyTo
    )
}

fun toMessagePart(event: Distributor.Event.NewMessage): List<MessagePart> {
    val parts = mutableListOf<MessagePart>()
    val partModifiers = event.modifiers.mapNotNull(::toPartModifier)


    val (replayed, attachments) = event.attachments.separate<Distributor.Attachment.ReplyMessage, Distributor.Attachment>()


    val caption = if (attachments.filter {
            it is Distributor.Attachment.Text || it is Distributor.Attachment.Resource
        }.size > 1)
        attachments
            .filterIsInstance<Distributor.Attachment.Text>()
            .firstOrNull()
            ?.value
            ?.run(::Caption)
    else null

    fun addToParts(content: MessagePart.Content) =
        MessagePart(
            groupId = event.id,
            modifiers = partModifiers,
            content = content
        ).run(parts::add)

    val header = replayed
        ?.toHeaderReplayedMessage()
        .run(event::createHeaderContent)
        .let { content ->
            if (event.headerMustBeInner()) {
                addToParts(content)
                content
            } else null
        }

    fun createMediaContent(type: MessagePart.Content.Media.Type, url: String) =
        MessagePart.Content.Media(
            type = type,
            url = url,
            caption = caption?.takeOrNull(),
            header = header
        )

    attachments.forEach {
        val content = when (it) {
            is Distributor.Attachment.Resource -> {
                val type = it.type.toContentMediaType()
                if (type == null)
                    MessagePart.Content.Text(value = "$type", header = header)
                else
                    createMediaContent(type = type, url = it.url)
            }

            is Distributor.Attachment.Text ->
                MessagePart.Content.Text(value = it.value, header = header)

            is Distributor.Attachment.ReplyMessage -> null
        }
        content?.run(::addToParts)
    }

    return parts
}

class Caption(text: String) {
    private var text: String? = text
    fun takeOrNull() = text.also { text = null }
}


fun Distributor.Attachment.ReplyMessage.toHeaderReplayedMessage() =
    MessagePart.Content.Header.ReplayedMessage(
        name = author?.name,
        text = content.run {
            if (length > 10) dropLast(length - 10) else this
        }
    )


fun Distributor.Attachment.Resource.Type.toContentMediaType() =
    when (this) {
        Distributor.Attachment.Resource.Type.UNKNOWN,
        Distributor.Attachment.Resource.Type.WEB_PAGE,
        Distributor.Attachment.Resource.Type.STICKER -> null

        Distributor.Attachment.Resource.Type.AUDIO -> MessagePart.Content.Media.Type.Audio
        Distributor.Attachment.Resource.Type.VIDEO -> MessagePart.Content.Media.Type.Video
        Distributor.Attachment.Resource.Type.FILE -> MessagePart.Content.Media.Type.File
        Distributor.Attachment.Resource.Type.PHOTO -> MessagePart.Content.Media.Type.Photo
    }

fun Distributor.Event.NewMessage.headerMustBeInner(): Boolean {
    return if (attachments.size > 1) false
    else if (attachments.isEmpty()) true
    else {
        when (val it = attachments.firstOrNull()) {
            is Distributor.Attachment.ReplyMessage,
            is Distributor.Attachment.Text,
            null -> true

            is Distributor.Attachment.Resource -> {
                when (it.type) {
                    Distributor.Attachment.Resource.Type.WEB_PAGE,
                    Distributor.Attachment.Resource.Type.AUDIO,
                    Distributor.Attachment.Resource.Type.VIDEO,
                    Distributor.Attachment.Resource.Type.FILE,
                    Distributor.Attachment.Resource.Type.PHOTO -> true

                    Distributor.Attachment.Resource.Type.UNKNOWN,
                    Distributor.Attachment.Resource.Type.STICKER -> false
                }
            }

        }

    }
}


fun MessagePart.Content.Header.ReplayedMessage.toText() =
    """
        ${name?.let { "> *$it*: " } ?: ""} _${text ?: "? <replayed>"}_ 
    """.trimIndent()

fun MessagePart.Content.Header.toText() =
    """
        a: *$app*
        ${author?.let { "u: *$it*" } ?: ""}
        ${replyTo?.toText()}
    """.trimIndent()

fun AbsSender.execute(part: MessagePart) {
   when (part.content) {
        is MessagePart.Content.Header ->
            SendMessage().apply {
                text = part.content.toText()
                parseMode = Res.Val.Markdown
                disableNotification = part.modifiers.contains(MessagePart.Modifier.Silent)
            }.run(::execute)

        is MessagePart.Content.Media -> when (part.content.type) {
            MessagePart.Content.Media.Type.Audio -> SendAudio().apply {
                caption = part.content.caption
                disableNotification = part.modifiers.contains(MessagePart.Modifier.Silent)
                parseMode = Res.Val.Markdown
                audio = InputFile(part.content.url)
            }.run(::execute)

            MessagePart.Content.Media.Type.Video -> SendVideo().apply {
                caption = part.content.caption
                disableNotification = part.modifiers.contains(MessagePart.Modifier.Silent)
                parseMode = Res.Val.Markdown
                video = InputFile(part.content.url)
            }.run(::execute)

            MessagePart.Content.Media.Type.File -> SendDocument().apply {
                caption = part.content.caption
                disableNotification = part.modifiers.contains(MessagePart.Modifier.Silent)
                parseMode = Res.Val.Markdown
                document = InputFile(part.content.url)
            }.run(::execute)

            MessagePart.Content.Media.Type.Photo -> SendPhoto().apply {
                caption = part.content.caption
                disableNotification = part.modifiers.contains(MessagePart.Modifier.Silent)
                parseMode = Res.Val.Markdown
                photo = InputFile(part.content.url)
            }.run(::execute)
        }

        is MessagePart.Content.Sticker ->
            SendSticker().apply {
                this.disableNotification = part.modifiers.contains(MessagePart.Modifier.Silent)
                this.sticker = InputFile(part.content.url)
            }.run(::execute)

        is MessagePart.Content.Text ->
            SendMessage().apply {
                val mbSpoileredText = part.content.value.let {
                    if (part.modifiers.contains(MessagePart.Modifier.Spoiler))
                        "||$it||" else it
                }
                text = """
                    ${part.content.header?.toText() ?: ""}
                    $mbSpoileredText
                """.trimIndent()
                parseMode = Res.Val.Markdown
                disableNotification = part.modifiers.contains(MessagePart.Modifier.Silent)
            }.run(::execute)
    }
}

//
//fun Distributor.Event.NewMessage.toApiMessage2(replayedMessage: Message? = null): SendMessage {
//    val msg = SendMessage()
//
//    attachments.forEach { attachment ->
//        when (attachment) {
//            is Distributor.Attachment.ReplyMessage ->
//                if (replayedMessage != null)
//                    msg.addReplayedPrefix(replayedMessage)
//
//            is Distributor.Attachment.Text -> {
//                msg.text = attachment.value
//                if (modifiers.hasSpoiler())
//                    msg.transformTextToSpoilered()
//            }
//
//            else -> Unit
//        }
//    }
//
//    modifiers.forEach { modifier ->
//        when (modifier) {
//            Distributor.Modifier.Silent -> msg.disableNotification()
//            Distributor.Modifier.Spoiler -> Unit // handled in `attachments` block
//            is Distributor.Modifier.Timer -> Unit // unsupported in telegram api
//        }
//    }
//
//    TODO()
//}
//
//fun Distributor.Attachment.Resource.Type.toApi(): Any {
//    val domain = this
//
//    when (domain) {
//        domain.Distributor.Attachment.Resource.Type.UNKNOWN -> TODO()
//        domain.Distributor.Attachment.Resource.Type.WEB_PAGE -> TODO()
//        domain.Distributor.Attachment.Resource.Type.AUDIO -> TODO()
//        domain.Distributor.Attachment.Resource.Type.VIDEO -> TODO()
//        domain.Distributor.Attachment.Resource.Type.FILE -> TODO()
//        domain.Distributor.Attachment.Resource.Type.PHOTO -> TODO()
//        domain.Distributor.Attachment.Resource.Type.STICKER -> TODO()
//    }
//}
//
//
//private fun List<Distributor.Modifier>.hasSpoiler(): Boolean =
//    contains(Distributor.Modifier.Spoiler)
//
//private fun SendMessage.addReplayedPrefix(replayedMessage: Message) {
//    val prefix = prefixOfReplayedMsg(replayedMessage)
//    text = "$prefix\n$text"
//}
//
//private fun SendMessage.transformTextToSpoilered() {
//    parseMode = "MarkdownV2"
//    text = "||${text}||"
//}
//
//private fun prefixOfReplayedMsg(replayedMessage: Message): String {
//    return """
//    reply to:${replayedMessage.from.userName}
//    in: ${formatDate(replayedMessage.date.toLong())}
//    ${replayedMessage.text.let { if (it.isNotBlank()) "part:$it" else "" }}
//    """.trimIndent()
//}
//
//private val firstApiFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
//private fun formatDate(epoch: Long): String {
//    val instant = Instant.ofEpochSecond(epoch)
//    return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(firstApiFormat)
//}