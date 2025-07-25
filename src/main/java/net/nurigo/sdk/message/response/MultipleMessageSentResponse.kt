package net.nurigo.sdk.message.response

import kotlinx.serialization.Serializable
import net.nurigo.sdk.message.model.Count

@Serializable
data class MultipleMessageSentResponse(
    var groupId: String? = null,
    var messageId: String? = null,
    var accountId: String? = null,
    var statusMessage: String? = null,
    var statusCode: String? = null,
    var to: String? = null,
    var from: String? = null,
    var type: String? = null,
    var country: String? = null,
    var count: Count? = null
)
