package data.telegram

import domain.Distributor
import org.telegram.telegrambots.meta.api.objects.Message

fun Message.toDomain(): Distributor.Input.CreateMessage {
    val message = this
    return Distributor.Input.CreateMessage(
        createdAt = date.toLong(),
        author = from.userName,
        action = null, // todo
        modifiers = listOf(), // todo rework
        attachments = message.toAttachment(),
    )
}

private fun Message.toAttachment(): List<Distributor.Attachment> {
    val result = mutableListOf<Distributor.Attachment>()
    if (hasText())
        Distributor.Attachment.Text(
            type = Distributor.Attachment.Text.Type.PLAIN,
            value = text,
        ).run(result::add)

    return result
}