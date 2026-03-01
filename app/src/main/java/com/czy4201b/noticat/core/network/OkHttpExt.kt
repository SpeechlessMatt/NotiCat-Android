package com.czy4201b.noticat.core.network

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 让 OkHttp 支持协程挂起，不再阻塞线程
 */
suspend fun Call.await(): Response = suspendCancellableCoroutine { continuation ->
    enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            continuation.resume(response)
        }

        override fun onFailure(call: Call, e: IOException) {
            // 协程如果取消了，就没必要把异常抛回去了
            if (continuation.isCancelled) return
            continuation.resumeWithException(e)
        }
    })

    continuation.invokeOnCancellation {
        try {
            cancel()
        } catch (ex: Throwable) { /* 忽略取消时的异常 */ }
    }
}