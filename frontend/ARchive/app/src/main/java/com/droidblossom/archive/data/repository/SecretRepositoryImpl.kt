package com.droidblossom.archive.data.repository

import com.droidblossom.archive.data.dto.ResponseBody
import com.droidblossom.archive.data.dto.secret.request.SecretCapsuleCreateRequestDto
import com.droidblossom.archive.data.dto.secret.request.SecretCapsuleModifyRequestDto
import com.droidblossom.archive.data.dto.secret.request.SecretCapsulePageRequestDto
import com.droidblossom.archive.data.dto.secret.response.SecretCapsuleCreateResponseDto
import com.droidblossom.archive.data.dto.secret.response.SecretCapsuleDetailResponseDto
import com.droidblossom.archive.data.dto.secret.response.SecretCapsuleModifyResponseDto
import com.droidblossom.archive.data.dto.secret.response.SecretCapsulePageResponseDto
import com.droidblossom.archive.data.source.remote.api.SecretService
import com.droidblossom.archive.domain.model.secret.SecretCapsuleDetail
import com.droidblossom.archive.domain.model.secret.SecretCapsuleModify
import com.droidblossom.archive.domain.model.secret.SecretCapsulePage
import com.droidblossom.archive.domain.repository.SecretRepository
import com.droidblossom.archive.util.RetrofitResult
import com.droidblossom.archive.util.apiHandler
import javax.inject.Inject

class SecretRepositoryImpl @Inject constructor(
    private val api: SecretService
) : SecretRepository {

    override suspend fun getSecretCapsulePage(request: SecretCapsulePageRequestDto): RetrofitResult<SecretCapsulePage> {
        return apiHandler({ api.getSecretCapsulePageApi(request.size, request.capsule_id) }) { response: ResponseBody<SecretCapsulePageResponseDto> -> response.result.toModel() }
    }

    override suspend fun createSecretCapsule(request: SecretCapsuleCreateRequestDto): RetrofitResult<Nothing> {
        return apiHandler({ api.postSecretCapsuleApi(request) }) { response: ResponseBody<SecretCapsuleCreateResponseDto> -> response.result.toModel() }
    }

    override suspend fun getSecretCapsuleDetail(capsuleId: Int): RetrofitResult<SecretCapsuleDetail> {
        return apiHandler({ api.getSecretCapsuleDetailApi(capsuleId) }) { response: ResponseBody<SecretCapsuleDetailResponseDto> -> response.result.toModel() }
    }

    override suspend fun modifySecretCapsule(
        capsuleId: Int,
        request: SecretCapsuleModifyRequestDto
    ): RetrofitResult<SecretCapsuleModify> {
        return apiHandler({ api.modifySecretCapsuleDetailApi(capsuleId , request) }) { response: ResponseBody<SecretCapsuleModifyResponseDto> -> response.result.toModel() }
    }
}