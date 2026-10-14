package io.github.ppking2897.travelrecord.core.data.remote

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest

/**
 * Android 的 App 身分：套件名稱 + 簽章憑證的 SHA-1 指紋
 *
 * 指紋格式照 Google 文件範例：大寫十六進位、不含冒號。
 * debug 與 release 簽章不同，每台開發機的 debug 簽章也不同，都要加進 key 的限制清單。
 */
class AndroidAppIdentity(context: Context) : AppIdentity {

    override val requestHeaders: Map<String, String> by lazy {
        buildMap {
            put("X-Android-Package", context.packageName)
            signingCertSha1(context)?.let { put("X-Android-Cert", it) }
        }
    }

    private fun signingCertSha1(context: Context): String? {
        val packageManager = context.packageManager
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                .signingInfo?.apkContentsSigners
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES).signatures
        }
        val certificate = signatures?.firstOrNull() ?: return null
        return MessageDigest.getInstance("SHA-1")
            .digest(certificate.toByteArray())
            .joinToString("") { "%02X".format(it) }
    }
}
