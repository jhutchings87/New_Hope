package com.jhutchings87.jame360.data.nzbget.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class JsonRpcRequest(
    val method: String,
    val params: List<JsonElement> = emptyList(),
    val id: Int = 1,
    val jsonrpc: String = "2.0"
)

@Serializable
data class JsonRpcResponse<T>(
    val result: T? = null,
    val error: JsonRpcError? = null,
    val id: Int = 0
)

@Serializable
data class JsonRpcError(
    val code: Int = 0,
    val message: String = "Unknown NZBGet error"
)

/** Subset of NZBGet's `status` RPC response. Field names match the NZBGet API exactly. */
@Serializable
data class NzbGetStatus(
    @SerialName("RemainingSizeMB") val remainingSizeMB: Long = 0,
    @SerialName("DownloadedSizeMB") val downloadedSizeMB: Long = 0,
    @SerialName("DownloadRate") val downloadRateBps: Long = 0,
    @SerialName("DownloadPaused") val downloadPaused: Boolean = false,
    @SerialName("FreeDiskSpaceMB") val freeDiskSpaceMB: Long = 0,
    @SerialName("ServerStandBy") val serverStandBy: Boolean = true,
    @SerialName("PostJobCount") val postJobCount: Int = 0,
    @SerialName("UpTimeSec") val upTimeSec: Long = 0
)

/** One row from NZBGet's `listgroups` RPC (an active/queued download). */
@Serializable
data class NzbGetGroup(
    @SerialName("NZBID") val nzbId: Int,
    @SerialName("NZBName") val name: String,
    @SerialName("Status") val status: String,
    @SerialName("FileSizeMB") val fileSizeMB: Long = 0,
    @SerialName("RemainingSizeMB") val remainingSizeMB: Long = 0,
    @SerialName("DownloadedSizeMB") val downloadedSizeMB: Long = 0,
    @SerialName("PausedSizeMB") val pausedSizeMB: Long = 0,
    @SerialName("Health") val health: Int = 1000,
    @SerialName("Category") val category: String = ""
) {
    val progressFraction: Float
        get() {
            if (fileSizeMB <= 0) return 0f
            val done = fileSizeMB - remainingSizeMB
            return (done.toFloat() / fileSizeMB.toFloat()).coerceIn(0f, 1f)
        }

    val isPaused: Boolean get() = pausedSizeMB > 0 && pausedSizeMB >= remainingSizeMB
}

/** One row from NZBGet's `history` RPC (a finished/failed download). */
@Serializable
data class NzbGetHistoryItem(
    @SerialName("NZBID") val nzbId: Int,
    @SerialName("Name") val name: String,
    @SerialName("Status") val status: String,
    @SerialName("Kind") val kind: String = "NZB",
    @SerialName("FileSizeMB") val fileSizeMB: Long = 0,
    @SerialName("HistoryTime") val historyTimeEpochSec: Long = 0
)
