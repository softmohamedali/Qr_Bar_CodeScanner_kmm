package com.moali.qrcodescannerkmm.core.shared.ui

import androidx.compose.runtime.Composable


@Composable
expect fun CameraPreview(
    visible: Boolean,
    onCodeScanner: (String) -> Unit,
)