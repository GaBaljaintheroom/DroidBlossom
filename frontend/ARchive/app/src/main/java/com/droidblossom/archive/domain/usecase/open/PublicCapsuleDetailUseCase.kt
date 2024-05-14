package com.droidblossom.archive.domain.usecase.open

import android.util.Log
import com.droidblossom.archive.domain.repository.PublicRepository
import com.droidblossom.archive.util.onException
import com.droidblossom.archive.util.onFail
import com.droidblossom.archive.util.onSuccess
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PublicCapsuleDetailUseCase @Inject constructor(
    private val repository: PublicRepository
) {
    suspend operator fun invoke(capsuleId :Long) =
        flow {
            try {
                emit(repository.getPublicCapsuleDetail(capsuleId).onSuccess {

                }.onFail {

                }.onException {
                    throw Exception(it)
                })
            } catch (e: Exception) {
                Log.d("예외확인", "$e")
                e.printStackTrace()
            }
        }
}