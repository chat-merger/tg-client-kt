package domain

import com.google.type.DateTime
import kotlinx.coroutines.flow.Flow

object Distributor {
    interface Repository {
        suspend fun eventFlow(): Result<Flow<Event>>
        suspend fun write(input: Input): Result<Unit>
    }

    sealed interface Input {
        object Edit : Input
        object Delete : Input
        data class NewMessage(
            val timestamp: DateTime,
            val author: String?,
            val action: Action?,
            val modifier: Modifier?,
            val attachments: List<Attachment>,
        ) : Input
    }


    sealed interface Event {
        object Edit : Event
        object Delete : Event
        data class NewMessage(
            val id: Int,
            val timestamp: DateTime,
            val clientName: String,
            val author: String?,
            val action: Action?,
            val modifier: Modifier?,
            // editable:
            val attachments: List<Attachment>,
        ) : Event
    }

    sealed interface Modifier {
        data object Silent : Modifier
        data object Spoiler : Modifier
        data class Timer(val expiresAt: DateTime) : Modifier
    }

    sealed interface Attachment {
        data class Text(val type: Type, val value: String) : Attachment {
            enum class Type {
                PLAIN, MARKDOWN
            }
        }

        data class Resource(val type: Type, val url: String) : Attachment {
            enum class Type {
                UNKNOWN, WEB_PAGE,
                AUDIO, VIDEO, FILE,
                PHOTO, STICKER,
            }
        }

        data class ReplyMessage(
            val timestamp: DateTime,
            val author: String?,
            val content: String,
        ) : Attachment
    }

    enum class Action {
        DELETE_MEMBER, JOIN_MEMBER,
    }
}
