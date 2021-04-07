package ru.speechpro.stcspeechkit.data.network

import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.*
import ru.speechpro.stcspeechkit.domain.models.*

/**
 * @author Victoria Prusakova
 */
interface RecognizeV2Api {

    // models
    @GET ("vkasr/rest/v2/models")
    fun getAllModels(@Header("X-Session-Id") sessionId: String): Deferred<Response<List<ModelResponse>>>

    // recognize
    @PUT ("vkasr/rest/v2/recognizer/simple")
    fun recognizeText(
        @Header("X-Session-Id") sessionId: String,
        @Body request: RecognizeV2Request
    ): Deferred<Response<RecognizeV2Response>>

    @PUT ("vkasr/rest/v2/recognizer/multichannel")
    fun recognizeMultichannel(
            @Header("X-Session-Id") sessionId: String,
            @Body request: RecognizeV2Request
    ): Deferred<Response<List<RecognizeMultichannelResponse>>>

    @PUT ("vkasr/rest/v2/recognizer/words")
    fun recognizeWords(
        @Header("X-Session-Id") sessionId: String,
        @Body request: RecognizeV2Request
    ): Deferred<Response<List<RecognizeWordResponse>>>

    @POST ("vkasr/rest/v2/transaction/ws")
    fun startWebsocketTransaction(
        @Header("X-Session-Id") sessionId: String,
        @Body request: StartTransactionRequest
    ): Deferred<Response<StreamResponse>>

    @POST ("vkasr/rest/v2/transaction/buffer")
    fun startBufferTransaction(
            @Header("X-Session-Id") sessionId: String,
            @Body request: StartTransactionRequest
    ): Deferred<Response<Void>>

    @PUT ("vkasr/rest/v2/transaction/sample")
    fun recognizeInBufferTransaction(
            @Header("X-Session-Id") sessionId: String,
            @Body request: Audio
    ): Deferred<Response<Void>>

    @DELETE ("vkasr/rest/v2/transaction")
    fun closeTransaction(
            @Header("X-Session-Id") sessionId: String,
            @Header("X-Transaction-Id") transactionId: String
    ): Deferred<Response<RecognizeV2Response>>
}