package com.moali.qrcodescannerkmm.core.shared.ui

import android.Manifest
import android.content.pm.PackageManager
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.MeteringPointFactory
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.moali.qrcodescannerkmm.core.qrcode.QrCodeAnalyzer


@Composable
actual fun CameraPreview(
    visible: Boolean,
    onCodeScanner: (String) -> Unit,
) {
    val context = LocalContext.current
    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(context)
    }

    val hasPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val lifeCycleOwner= LocalLifecycleOwner.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasPermission.value = isGranted
        }
    )

    LaunchedEffect(key1 = true) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    if (hasPermission.value) {
        if (visible) {
            Column (
                modifier = Modifier.fillMaxSize()
            ) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .background(Color.Black),
                    factory = { contextA ->
                        val previewView = PreviewView(contextA)
                        val cameraProvider = cameraProviderFuture.get()
                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build()
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .apply {
                                setAnalyzer(
                                    ContextCompat.getMainExecutor(contextA),
                                    QrCodeAnalyzer { result ->
                                        // Here, you can handle the scanned QR code result
                                        onCodeScanner(result)
                                    }
                                )
                            }

                        try {
                            // Unbind all use cases before binding new ones
                            cameraProvider.unbindAll()
                            // Bind the camera use cases
                            val camera=cameraProvider.bindToLifecycle(
                                lifeCycleOwner,
                                cameraSelector,
                                imageAnalysis
                            )
                            previewView.setOnTouchListener { view, motionEvent ->
                                return@setOnTouchListener when (motionEvent.action) {
                                    MotionEvent.ACTION_DOWN -> {
                                        true
                                    }
                                    MotionEvent.ACTION_UP -> {
                                        val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                                            previewView.width.toFloat(), previewView.height.toFloat()
                                        )
                                        val autoFocusPoint = factory.createPoint(motionEvent.x, motionEvent.y)
                                        try {
                                            camera.cameraControl.startFocusAndMetering(
                                                FocusMeteringAction.Builder(
                                                    autoFocusPoint,
                                                    FocusMeteringAction.FLAG_AF
                                                ).apply {
                                                    //focus only when the user tap the preview
                                                    disableAutoCancel()
                                                }.build()
                                            )
                                        } catch (e: CameraInfoUnavailableException) {

                                        }
                                        true
                                    }
                                    else -> false // Unhandled event.
                                }
                            }

                            // Build the preview use case
                            val preview = Preview.Builder()
                                .build()
                                .apply {
                                    setSurfaceProvider(previewView.surfaceProvider)
                                }

                            // Bind the preview use case
                            cameraProvider.bindToLifecycle(
                                lifeCycleOwner,
                                cameraSelector,
                                preview
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        previewView
                    }
                )

                Text(
                    text = "Please scan QR Code",
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .background(Color.White)
                )
            }
        } else {
            return
        }
    }
}



