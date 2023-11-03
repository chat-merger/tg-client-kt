package data

import domain.Distributor
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.runBlocking
import mergerapi.BaseServiceGrpc
import mergerapi.Request
import mergerapi.Response

class DistributorRepositoryBase(
    host: String,
    port: Int,
) : Distributor.Repository {
    private val stream: StreamObserver<Request>
    private val flow: MutableSharedFlow<Distributor.Event> = MutableSharedFlow()

    init {
        val channel = createChannel(host, port)
        val stub = BaseServiceGrpc.newStub(channel)
        val observer = ResponseObserver(flow)
        stream = stub.connect(observer)
    }

    override suspend fun eventFlow(): Result<Flow<Distributor.Event>> {
        TODO("Not yet implemented")
    }

    override suspend fun write(input: Distributor.Input): Result<Unit> {

    }
}

private fun createChannel(host: String, port: Int): ManagedChannel? {
    return ManagedChannelBuilder
        .forAddress(host, port)
        .usePlaintext()
        .build()
}

private class ResponseObserver(
    private val receiver: MutableSharedFlow<Distributor.Event>,
) : StreamObserver<Response> {

    override fun onNext(p0: Response?) {
        runBlocking{ receiver.emit(Distributor.Event.Delete) }
        println("p0?.eventCase =>\n ${p0?.eventCase}")
    }

    override fun onError(p0: Throwable?) {
        println("емое ошибка: $p0")
    }

    override fun onCompleted() {
        println("класс, комплитд")
    }
}