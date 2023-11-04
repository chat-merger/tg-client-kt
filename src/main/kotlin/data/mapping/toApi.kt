package data.mapping


import com.google.protobuf.timestamp
import domain.Distributor
import mergerapi.*

fun Distributor.Input.toRequest() = request {
    when (val domain = this@toRequest) {
        is Distributor.Input.Delete -> deleteMsg = domain.toApi()
        is Distributor.Input.Edit -> editMsg = domain.toApi()
        is Distributor.Input.CreateMessage -> createMsg = domain.toApi()
    }
}

fun Distributor.Input.Delete.toApi() = deleteMessageBody {}
fun Distributor.Input.Edit.toApi() = editMessageBody {}

fun Distributor.Input.CreateMessage.toApi() = createMessageBody {
    val domain = this@toApi
    createdAt = domain.createdAt.toTimestamp()
    domain.action?.let { actionValue = it.toApi() }
    domain.modifiers.forEach {
        modifiers.add(it.toApi())
    }
    domain.attachments.forEach {
        attachments.add(it.toApi())
    }
}

fun Distributor.Action.toApi(): Action =
    when (this) {
        Distributor.Action.DELETE_MEMBER -> Action.DELETE_MEMBER
        Distributor.Action.JOIN_MEMBER -> Action.JOIN_MEMBER
    }

fun Distributor.Modifier.toApi() = modifier {
    when (val domain = this@toApi) {
        Distributor.Modifier.Silent -> silent = true
        Distributor.Modifier.Spoiler -> spoiler = true
        is Distributor.Modifier.Timer -> timer = domain.expiresAt.toTimestamp()
    }
}


fun Long.toTimestamp() = timestamp {
    seconds = this@toTimestamp
}

fun Distributor.Attachment.toApi() = attachment {
    when (val domain = this@toApi) {
        is Distributor.Attachment.ReplyMessage -> message = domain.toApi()
        is Distributor.Attachment.Resource -> resource = domain.toApi()
        is Distributor.Attachment.Text -> text = domain.toApi()
    }
}

fun Distributor.Attachment.ReplyMessage.toApi() = AttachmentKt.message {
    val domain = this@toApi
    createdAt = domain.createdAt.toTimestamp()
    domain.author?.let { authorValue = it.toApi() }
    content = domain.content
}

fun Distributor.Author.toApi() = author {
    authorId = id
    authorName = name
}

fun Distributor.Attachment.Text.toApi() = AttachmentKt.text {
    val domain = this@toApi
    type = domain.type.toApi()
    value = domain.value
}

fun Distributor.Attachment.Resource.toApi() = AttachmentKt.resource {
    val domain = this@toApi
    type = domain.type.toApi()
    url = domain.url
}

fun Distributor.Attachment.Text.Type.toApi() =
    when (this) {
        Distributor.Attachment.Text.Type.PLAIN -> Attachment.Text.Type.PLAIN
        Distributor.Attachment.Text.Type.MARKDOWN -> Attachment.Text.Type.MARKDOWN
    }

fun Distributor.Attachment.Resource.Type.toApi() =
    when (this) {
        Distributor.Attachment.Resource.Type.UNKNOWN -> Attachment.Resource.Type.UNKNOWN
        Distributor.Attachment.Resource.Type.WEB_PAGE -> Attachment.Resource.Type.WEB_PAGE
        Distributor.Attachment.Resource.Type.AUDIO -> Attachment.Resource.Type.AUDIO
        Distributor.Attachment.Resource.Type.VIDEO -> Attachment.Resource.Type.VIDEO
        Distributor.Attachment.Resource.Type.FILE -> Attachment.Resource.Type.FILE
        Distributor.Attachment.Resource.Type.PHOTO -> Attachment.Resource.Type.PHOTO
        Distributor.Attachment.Resource.Type.STICKER -> Attachment.Resource.Type.STICKER
    }