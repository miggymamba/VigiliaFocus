package com.miguelrivera.vigiliafocus.platform

/**
 * iOS implementation of [PlatformTimer].
 *
 * Stubbed for VF1.S3 scaffold. Full implementation using
 * `NSTimer` or `DispatchQueue` added in Phase 6.
 */
actual class PlatformTimer actual constructor() {
    actual fun start(durationSeconds: Int, onTick: (remaining: Int) -> Unit, onFinish: () -> Unit) {
        TODO("Implemented in VF6.S2")
    }

    actual fun pause() {
        TODO("Implemented in VF6.S2")
    }

    actual fun reset() {
        TODO("Implemented in VF6.S2")
    }
}