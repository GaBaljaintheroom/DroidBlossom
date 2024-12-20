package com.droidblossom.archive.data.dto.common

import com.droidblossom.archive.presentation.model.mypage.CapsuleData

data class CapsuleBasicInfoResponseDto (
    val capsuleId : Long,
    val skinUrl : String,
    val dueDate : String?,
    val createdAt : String,
    val title : String,
    val isOpened : Boolean,
    val capsuleType : String,
){
    fun toUIModel() = CapsuleData(
        capsuleId = this.capsuleId,
        capsuleSkinUrl = this.skinUrl,
        createdDate = this.createdAt,
        dueDate = this.dueDate,
        isOpened = this.isOpened,
        title = this.title,
        capsuleType = this.capsuleType
    )
}