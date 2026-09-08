package com.jhutchings87.jame360.data.nzbget

import com.jhutchings87.jame360.data.model.ServerProfile
import com.jhutchings87.jame360.data.nzbget.model.JsonRpcError
import com.jhutchings87.jame360.data.nzbget.model.JsonRpcRequest
import com.jhutchings87.jame360.data.nzbget.model.NzbGetGroup
import com.jhutchings87.jame360.data.nzbget.model.NzbGetHistoryItem
import com.jhutchings87.jame360.data.nzbget.model.NzbGetStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class NzbGetApiException(message: String) : IOException(message)

/**
 * NZBGet exposes a single JSON-RPC 2.0 endpoint (`/jsonrpc`) where every call is
 * distinguished by `method`, so this is a thin hand-rolled client rather than a
 * Retrofit interface. Reference: https://nzbget.net/api/
 */
class NzbGetClient(
    private val profile: ServerProfile,
    private val okHttpClient: OkHttpClient,
    private val json: Json
) {
    private val rpcUrl = "${profile.baseUrl}/jsonrpc"

    private suspend fun call(method: String, params: List<JsonElement> = emptyList()): JsonElement =
        withContext(Dispatchers.IO) {
            val requestJson = json.encodeToString(JsonRpcRequest.serializer(), JsonRpcRequest(method = method, params = params))
            val body = requestJson.toRequestBody("application/json".toMediaType())

            val requestBuilder = Request.Builder().url(rpcUrl).post(body)
            if (profile.username.isNotBlank()) {
                requestBuilder.header("Authorization", Credentials.basic(profile.username, profile.password))
            }

            okHttpClient.newCall(requestBuilder.build()).execute().use { response ->
                val bodyString = response.body?.string()
                    ?: throw IOException("Empty response from NZBGet (HTTP ${response.code})")
                if (!response.isSuccessful) {
                    throw IOException("NZBGet HTTP ${response.code}: $bodyString")
                }

                val root = json.parseToJsonElement(bodyString).jsonObject
                root["error"]?.let { errorElement ->
                    if (errorElement != JsonNull) {
                        val error = json.decodeFromJsonElement(JsonRpcError.serializer(), errorElement)
                        throw NzbGetApiException(error.message)
                    }
                }
                root["result"] ?: throw NzbGetApiException("Malformed NZBGet response: missing result")
            }
        }

    suspend fun status(): NzbGetStatus =
        json.decodeFromJsonElement(NzbGetStatus.serializer(), call("status"))

    suspend fun listGroups(): List<NzbGetGroup> =
        json.decodeFromJsonElement(ListSerializer(NzbGetGroup.serializer()), call("listgroups"))

    suspend fun history(includeHidden: Boolean = false): List<NzbGetHistoryItem> =
        json.decodeFromJsonElement(
            ListSerializer(NzbGetHistoryItem.serializer()),
            call("history", listOf(JsonPrimitive(includeHidden)))
        )

    suspend fun pauseDownload(): Boolean = call("pausedownload").jsonPrimitive.boolean
    suspend fun resumeDownload(): Boolean = call("resumedownload").jsonPrimitive.boolean

    private suspend fun editQueue(command: String, ids: List<Int>, offset: Int = 0, text: String = ""): Boolean {
        val params = listOf(
            JsonPrimitive(command),
            JsonPrimitive(offset),
            JsonPrimitive(text),
            JsonArray(ids.map { JsonPrimitive(it) })
        )
        return call("editqueue", params).jsonPrimitive.boolean
    }

    suspend fun pauseGroup(nzbId: Int): Boolean = editQueue("GroupPause", listOf(nzbId))
    suspend fun resumeGroup(nzbId: Int): Boolean = editQueue("GroupResume", listOf(nzbId))
    suspend fun deleteGroup(nzbId: Int): Boolean = editQueue("GroupDelete", listOf(nzbId))
}
