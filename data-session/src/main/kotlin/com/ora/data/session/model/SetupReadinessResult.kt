package com.ora.data.session.model

data class SetupReadinessResult(
    val lightingPassed: Boolean,
    val faceFramingPassed: Boolean,
    val deviceOrientationPassed: Boolean,
    val distancePassed: Boolean,
    val ready: Boolean,
)

