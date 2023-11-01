import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import mergerapi.*


fun main(args: Array<String>) = runBlocking {
    println("Hello World!")
   val stream = qwe()
//    stream.onNext()


    println("end")
    delay(1000*10)
}

fun qwe(): StreamObserver<CreateMessageRequest> {
    val channel = ManagedChannelBuilder
        .forAddress("localhost", 9000)
        .usePlaintext()
//        .enableRetry()
        .build()

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