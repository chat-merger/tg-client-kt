import config.Config
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mergerapi.BaseServiceGrpc
import mergerapi.CreateMessageRequest
import mergerapi.MsgBody


data class MergerStream<T>(
    val stream: StreamObserver<T>,
    val status: Status,
) {
    sealed interface Status {
        object Ok : Status
        data class Error(val err: Throwable) : Status
    }
}

class MergerClient(private val cfg: Config) {

    //    val streams = ConcurrentHashMap<Int, MergerStream<*>>()
    val newMergerStream: StreamObserver<CreateMessageRequest> = MergerStream()

    private fun createChannel() = ManagedChannelBuilder
        .forAddress(cfg.mergerHost, cfg.mergerPort)
        .usePlaintext()
        .build()

    init {
        val channel = createChannel()
        val stub = BaseServiceGrpc.newStub(channel)
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            qwe().
        }
    }


    fun qwe(): StreamObserver<CreateMessageRequest> {
        val channel = createChannel()

        val stub = BaseServiceGrpc.newStub(channel)

        return stub.createMessage(
            object : StreamObserver<MsgBody> {
                override fun onNext(p0: MsgBody?) {
                    println("новое сообщение!!!: \n$p0")
                }

                override fun onError(p0: Throwable?) {
                    println("емое ошибка: $p0")
                }

                override fun onCompleted() {
                    println("класс, комплитд")
                }
            }
        )
    }

    fun <T> createObserver() = object : StreamObserver<T> {
        override fun onNext(p0: T?) {
            println("новое сообщение!!!: \n$p0")
        }

        override fun onError(p0: Throwable?) {
            println("емое ошибка: $p0")
        }

        override fun onCompleted() {
            println("класс, комплитд")
        }
    }
}

