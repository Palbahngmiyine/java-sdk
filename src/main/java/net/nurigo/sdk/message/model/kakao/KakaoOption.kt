package net.nurigo.sdk.message.model.kakao

import kotlinx.serialization.Serializable

@Serializable
data class KakaoOption(
    /**
     * 비즈니스 채널 ID
     */
    var pfId: String? = null,

    /**
     * 알림톡 템플릿 ID
     */
    var templateId: String? = null,

    /**
     * 변수처리를 위한 배열 값
     * 예) "#{변수1}": "1234"
     */
    var variables: MutableMap<String, String>? = null,

    /**
     * 대체 발송 여부
     * 기본값: 대체발송 허용(false)
     */
    var disableSms: Boolean = false,

    /**
     * 이미지 알림톡 발송을 위한 ImageId
     */
    var imageId: String? = null,

    /**
     * 광고 발송 여부
     */
    var adFlag: Boolean = false,

    /**
     * 친구톡 버튼
     */
    var buttons: List<KakaoButton>? = null
)