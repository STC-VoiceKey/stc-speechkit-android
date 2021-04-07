package ru.speechpro.stcspeechkit

import android.annotation.SuppressLint
import android.content.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.speechpro.android.session.session_library.SessionClientFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import ru.speechpro.stcspeechkit.common.BASE_URL
import ru.speechpro.stcspeechkit.common.SESSION_BASE_URL
import ru.speechpro.stcspeechkit.data.network.*
import ru.speechpro.stcspeechkit.util.AppInfo.getVersionCode
import ru.speechpro.stcspeechkit.util.AppInfo.getVersionName
import ru.speechpro.stcspeechkit.util.Logger
import ru.speechpro.stcspeechkit.util.ServerUrl
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

/**
 * Singleton class contains methods for initializing services, enabled logging and receiving version
 *
 * @author Alexander Grigal
 */
@SuppressLint("StaticFieldLeak")
object STCSpeechKit {

    private val TAG = STCSpeechKit::class.java.simpleName

    /**
     * Retrieves the application context.
     *
     * @return the application context if is initialized
     * @throws UninitializedPropertyAccessException if is not initialized
     */
    lateinit var applicationContext: Context
        private set

    /**
     * Retrieves the username.
     *
     * @return the username if is initialized
     * @throws UninitializedPropertyAccessException if is not initialized
     */
    lateinit var username: String
        private set

    /**
     * Retrieves the password.
     *
     * @return the password if is initialized
     * @throws UninitializedPropertyAccessException if is not initialized
     */
    lateinit var password: String
        private set

    /**
     * Retrieves the domain id.
     *
     * @return the domain id if is initialized
     * @throws UninitializedPropertyAccessException if is not initialized
     */
    var domainId: Int by Delegates.notNull()
        private set

    /**
     * Retrieves the recognize service version v1.
     *
     * @return the recognize service if is initialized
     */
    var recognizeService: RecognizeApi
        private set

    /**
     * Retrieves the recognize service version v2.
     *
     * @return the recognize service if is initialized
     */
    var recognizeV2Service: RecognizeV2Api
        private set

    /**
     * Retrieves the synthesize service.
     *
     * @return the synthesize service if is initialized
     */
    var synthesizeService: SynthesizeApi
        private set

    /**
     * Retrieves the diarization service.
     *
     * @return the diarization service if is initialized
     */
    var diarizationService: DiarizationApi
        private set

    /**
     * Retrieves the anti spoofing service.
     *
     * @return the anti spoofing service if is initialized
     */
    var antiSpoofingService: AntiSpoofingApi
        private set

    /**
     * Retrieves the anti spoofing service.
     *
     * @return the anti spoofing service if is initialized
     */
    var sessionClient: SessionClientFactory.SessionClient
        private set

    init {
        val okHttpClient = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request()

                    val t1 = System.nanoTime()
                    Logger.print(TAG, String.format("Sending request %s %s on %s%n%s",
                            request.method(), request.url(), chain.connection(), request.headers()))

                    Logger.print(TAG, bodyToString(request))

                    val response = chain.proceed(request)

                    val t2 = System.nanoTime()
                    Logger.print(TAG, String.format("Received response for %s in %.1fms%n%s",
                            response.request().url(), (t2 - t1) / 1e6, response.headers()))

                    Logger.print(TAG, String.format("Received response code %d and message %s",
                            response.code(), response.message()))

                    val bodyStr = response.body()!!.string()
                    Logger.print(TAG, bodyStr)

                    response.newBuilder()
                            .body(ResponseBody.create(response.body()!!.contentType(), bodyStr))
                            .build()
                }
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .build()

        val retrofit = Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(ServerUrl.getBaseUrl())
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .addConverterFactory(JacksonConverterFactory.create(ObjectMapper()))
                .build()

        recognizeService = retrofit.create(RecognizeApi::class.java)
        recognizeV2Service = retrofit.create(RecognizeV2Api::class.java)
        synthesizeService = retrofit.create(SynthesizeApi::class.java)
        diarizationService = retrofit.create(DiarizationApi::class.java)
        antiSpoofingService = retrofit.create(AntiSpoofingApi::class.java)
        sessionClient = SessionClientFactory.get(ServerUrl.getSessionUrl(), true)
    }

    /**
     * Set application context, username, password and domain id.
     * You must call before using the services.
     *
     * @param applicationContext application context
     * @param username username
     * @param password password
     * @param domainId domain Id
     */
    fun init(applicationContext: Context, username: String, password: String, domainId: Int) {
        Logger.print(TAG, "init: $username $password $domainId")

        this.applicationContext = applicationContext
        this.username = username
        this.password = password
        this.domainId = domainId
    }

    /**
     * @param isEnabled flag to enable logging
     */
    fun setLogging(isEnabled: Boolean) {
        Logger.isEnabled = isEnabled
    }

    /**
     * Retrieves version name and version code
     *
     * @return version name and version code
     */
    fun getVersion(): String {
        return """${getVersionName(applicationContext)} (${getVersionCode(applicationContext)})"""
    }

    private fun bodyToString(request: Request): String {
        try {
            val npe = request.newBuilder().build()
            val buffer = Buffer()
            npe.body()!!.writeTo(buffer)
            return buffer.readUtf8()
        } catch (ioe: IOException) {
            return "parse error"
        } catch (npe: NullPointerException) {
            return "no body"
        }
    }
}