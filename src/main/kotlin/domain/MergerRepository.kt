package domain

import com.google.type.DateTime
import kotlinx.coroutines.flow.StateFlow

interface MergerRepository {
    fun eventsFlow(): StateFlow<MergerEvent>
}

class EditEventBody // :todo
class DeleteEventBody // :todo

sealed interface MergerEvent {
    data class Edit(val body: EditEventBody) : MergerEvent
    data class Delete(val body: DeleteEventBody) : MergerEvent
    data class NewMessage(val msg: Message) : MergerEvent
}

data class Message(
    val id: Int,
    val timestamp: DateTime,
    val clientName: String,
    val author: String?,
    val modifier: Modifier,
    val action: Action?,
    val attachments: List<Attachmnet>,
)

sealed interface Modifier {
    object Silent : Modifier
    object Spoiler : Modifier
    data class Timer(val expiresAt: DateTime) : Modifier
}

sealed interface Attachmnet {
    data class Text(val type: Type, val value: String) : Attachmnet {
        enum class Type {
            PLAIN, MARKDOWN
        }
    }

    data class Resource(val type: Type, val url: String) : Attachmnet {
        enum class Type {
            UNKNOWN, WEB_PAGE,
            AUDIO, VIDEO, FILE,
            PHOTO, STICKER,
        }
    }

    data class Message(
        val timestamp: DateTime,
        val author: String?,
        val content: String,
    ) : Attachmnet
}

enum class Action {
    DELETE_MEMBER, JOIN_MEMBER,
}
