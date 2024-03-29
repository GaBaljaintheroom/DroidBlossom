package com.droidblossom.archive.domain.usecase.friend

import android.util.Log
import com.droidblossom.archive.domain.repository.FriendRepository
import com.droidblossom.archive.util.onException
import com.droidblossom.archive.util.onFail
import com.droidblossom.archive.util.onSuccess
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FriendsRequestsPageUseCase @Inject constructor(
    private val repository: FriendRepository
) {
    suspend operator fun invoke(size: Int, createdAt: String) =
        flow {
            try {
                emit(repository.getFriendsRequestsPage(size, createdAt).onSuccess {

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