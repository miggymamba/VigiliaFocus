package com.miguelrivera.vigiliafocus.platform

/**
 * Android implementation of [PlatformTimer].
 *
 * Stubbed for VF1.S3 scaffold. Full coroutine-based countdown
 * implemented in VF2.S3.
 */
actual class PlatformTimer actual constructor() {
    actual fun start(durationSeconds: Int, onTick: (remaining: Int) -> Unit, onFinish: () -> Unit) {
        TODO("Implemented in VF2.S3")
    }

    actual fun pause() {
        TODO("Implemented in VF2.S3")
    }

    actual fun reset() {
        TODO("Implemented in VF2.S3")
    }
}