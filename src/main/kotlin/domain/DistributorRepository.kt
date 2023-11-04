package domain

import kotlinx.coroutines.flow.SharedFlow

object Distributor {
    interface Repository {
        val eventFlow: SharedFlow<Event>
        suspend fun write(input: Input): Result<Unit>
    }

    sealed interface Input {
        data object Edit : Input
        data object Delete : Input
        data class CreateMessage(
            val createdAt: Long,
            val author: String?,
            val action: Action?,
            val modifiers: List<Modifier>,
            val attachments: List<Attachment>,
        ) : Input
    }


    sealed interface Event {
        data object Edit : Event
        data object Delete : Event
        data class NewMessage(
            val id: String,
            val createdAt: Long,
            val clientName: String,
            val author: String?,
            val action: Action?,
            val modifiers: List<Modifier>,
            // editable:
            val attachments: List<Attachment>,
        ) : Event
    }

    sealed interface Modifier {
        data object Silent : Modifier
        data object Spoiler : Modifier
        data class Timer(val expiresAt: Long) : Modifier
    }

    sealed interface Attachment {
        data class Text(val type: Type, val value: String) : Attachment {
            enum class Type {
                PLAIN, MARKDOWN
            }
        }

        data class Resource(val type: Type, val url: String) : Attachment {
            enum class Type {
                UNKNOWN,
                WEB_PAGE,
                AUDIO,
                VIDEO,
                FILE,
                PHOTO,
                STICKER,
            }
        }

        data class ReplyMessage(
            val createdAt: Long,
            val author: Author?,
            val content: String,
        ) : Attachment
    }

    data class Author(
        val id: String,
        val name: String,
    )

    enum class Action {
        DELETE_MEMBER, JOIN_MEMBER,
    }
}
