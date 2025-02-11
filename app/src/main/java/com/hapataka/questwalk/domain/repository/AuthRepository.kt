package com.hapataka.questwalk.domain.repository

interface AuthRepository {
    suspend fun registerByIdAndPw(email: String, pw: String): Result<Boolean>
    suspend fun loginByIdAndPw(email: String, pw: String): Result<Boolean>
    suspend fun getCurrentUserId(): String?
    suspend fun logout(): Result<Unit>
    suspend fun reauthCurrentUser(pw: String): Result<Unit>
    suspend fun dropOutCurrentUser(): Result<Unit>
}