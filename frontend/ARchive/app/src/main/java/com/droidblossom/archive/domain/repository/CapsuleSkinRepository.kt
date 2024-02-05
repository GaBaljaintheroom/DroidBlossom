package com.droidblossom.archive.domain.repository

import com.droidblossom.archive.data.dto.capsule_skin.request.CapsuleSkinsPageRequestDto
import com.droidblossom.archive.domain.model.capsule_skin.CapsuleSkinsPageResponse
import com.droidblossom.archive.util.RetrofitResult

interface CapsuleSkinRepository {

    suspend fun getCapsuleSkinPage(request: CapsuleSkinsPageRequestDto) : RetrofitResult<CapsuleSkinsPageResponse>
}