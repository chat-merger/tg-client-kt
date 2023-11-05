package data.merger

import data.merger.mapping.toEvent
import data.merger.mapping.toRequest
import domain.Distributor
import io.grpc.Channel
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mergerapi.BaseServiceGrpcKt
import mergerapi.Request
import mergerapi.Response


class DistributorRepositoryBase(
    host: String,
    port: Int,
) : Distributor.Repository {
    //    private val stream: StreamObserver<ResponseKt>
    private val rawRequestFlow: MutableSharedFlow<Request> = MutableSharedFlow()
    private val rawResponseFlow: Flow<Response>

    private val _eventFlow = MutableSharedFlow<Distributor.Event>()
    override val eventFlow = _eventFlow

    init {
        val channel = createChannel(host, port)
        val stub = BaseServiceGrpcKt.BaseServiceCoroutineStub(channel)
        rawResponseFlow = stub.connect(requests = rawRequestFlow)
        CoroutineScope(Dispatchers.Unconfined).launch {
            rawResponseFlow.collect { response ->
                response.toEvent()?.let { event ->
                    _eventFlow.emit(event)
                }
            }
        }
    }

    override suspend fun write(input: Distributor.Input): Result<Unit> {
        val request = input.toRequest()
        rawRequestFlow.emit(request)
        return Result.success(Unit)
    }
}

private fun createChannel(host: String, port: Int): Channel {
    return ManagedChannelBuilder
        .forAddress(host, port)
        .usePlaintext()
        .build()
}

private class ResponseObserver(
    private val receiver: MutableSharedFlow<Distributor.Event>,
) : StreamObserver<Response> {

    override fun onNext(p0: Response?) {
        runBlocking { receiver.emit(Distributor.Event.Delete) }
        println("p0?.eventCase =>\n ${p0?.eventCase}")
    }

    override fun onError(p0: Throwable?) {
        println("емое ошибка: $p0")
    }

    override fun onCompleted() {
        println("класс, комплитд")
    }
}