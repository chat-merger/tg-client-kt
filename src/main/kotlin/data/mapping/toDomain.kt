package data.mapping

import domain.Distributor
import mergerapi.*


fun Response.toEvent(): Distributor.Event? =
    when (eventCase) {
        Response.EventCase.ON_CREATE_MSG -> onCreateMsg.toDomain()
        Response.EventCase.ON_DELETE_MSG -> onDeleteMsg.toDomain()
        Response.EventCase.ON_EDIT_MSG -> onEditMsg.toDomain()
        else -> null
    }

private fun EditMessageEvent.toDomain() = Distributor.Event.Edit
private fun DeleteMessageEvent.toDomain() = Distributor.Event.Delete


private fun NewMessageEvent.toDomain() =
    Distributor.Event.NewMessage(
        id = id,
        createdAt = createdAt.seconds,
        clientName = clientName,
        author = authorCase.name,
        action = if (NewMessageEvent.ActionCase.ACTION_VALUE == actionCase)
            actionValue.toDomain() else null,
        modifiers = modifiersList.mapNotNull { it.toDomain() },
        attachments = attachmentsList.mapNotNull { it.toDomain() }
    )


fun Modifier.toDomain() =
    when (valueCase) {
        Modifier.ValueCase.SILENT -> Distributor.Modifier.Silent
        Modifier.ValueCase.SPOILER -> Distributor.Modifier.Spoiler
        Modifier.ValueCase.TIMER -> Distributor.Modifier.Timer(timer.seconds)
        else -> null
    }

fun Attachment.toDomain() =
    when (valueCase) {
        Attachment.ValueCase.TEXT -> text.toDomain()
        Attachment.ValueCase.RESOURCE -> resource.toDomain()
        Attachment.ValueCase.MESSAGE -> message.toDomain()
        else -> null
    }

fun Attachment.Message.toDomain() =
    Distributor.Attachment.ReplyMessage(
        createdAt = createdAt.seconds,
        author = null,
        content = content
    )

fun Attachment.Resource.toDomain() =
    Distributor.Attachment.Resource(
        type = type.toDomain()!!,
        url = url
    )

fun Attachment.Resource.Type.toDomain() =
    when (this) {
        Attachment.Resource.Type.UNKNOWN -> Distributor.Attachment.Resource.Type.UNKNOWN
        Attachment.Resource.Type.WEB_PAGE -> Distributor.Attachment.Resource.Type.WEB_PAGE
        Attachment.Resource.Type.AUDIO -> Distributor.Attachment.Resource.Type.AUDIO
        Attachment.Resource.Type.VIDEO -> Distributor.Attachment.Resource.Type.VIDEO
        Attachment.Resource.Type.FILE -> Distributor.Attachment.Resource.Type.FILE
        Attachment.Resource.Type.PHOTO -> Distributor.Attachment.Resource.Type.PHOTO
        Attachment.Resource.Type.STICKER -> Distributor.Attachment.Resource.Type.STICKER
        else -> null
    }

fun Attachment.Text.toDomain() =
    Distributor.Attachment.Text(
        type = type.toDomain()!!,
        value = value
    )

fun Attachment.Text.Type.toDomain() =
    when (this) {
        Attachment.Text.Type.PLAIN -> Distributor.Attachment.Text.Type.PLAIN
        Attachment.Text.Type.MARKDOWN -> Distributor.Attachment.Text.Type.MARKDOWN
        else -> null
    }

fun Action.toDomain(): Distributor.Action? =
    when (this) {
        Action.DELETE_MEMBER -> Distributor.Action.DELETE_MEMBER
        Action.JOIN_MEMBER -> Distributor.Action.JOIN_MEMBER
        Action.UNRECOGNIZED -> null
    }
