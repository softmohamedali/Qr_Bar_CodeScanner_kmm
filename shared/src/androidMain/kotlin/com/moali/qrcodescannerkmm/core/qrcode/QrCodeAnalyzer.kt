package com.moali.qrcodescannerkmm.core.qrcode

import android.graphics.ImageFormat
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.integration.android.IntentIntegrator
import java.nio.ByteBuffer


class QrCodeAnalyzer(
    var onQrCodeScanned:(String)->Unit
) : ImageAnalysis.Analyzer
{
    private  val supportedImageFormate= listOf(
        ImageFormat.YUV_420_888,
        ImageFormat.YUV_422_888,
        ImageFormat.YUV_444_888
    )
    override fun analyze(image: ImageProxy) {
        if (image.format in supportedImageFormate){
            val bytes=image.planes.first().buffer.toByteArray()
            val source= PlanarYUVLuminanceSource(
                bytes,
                image.width,
                image.height,
                0,
                0,
                image.width,
                image.height,
                false
            )
            val binaryBitmap= BinaryBitmap(HybridBinarizer(source))
            try {
                val result= MultiFormatReader().apply {
                    setHints(mapOf(
                        DecodeHintType.POSSIBLE_FORMATS to arrayListOf(
                            BarcodeFormat.UPC_EAN_EXTENSION,
                            BarcodeFormat.EAN_13,
                            BarcodeFormat.EAN_8,
                        )
                    ))
                }.decode(binaryBitmap)
                onQrCodeScanned(result.text)
            }catch (e:Exception){
                e.printStackTrace()
            }finally {
                image.close()
            }
        }
    }

    fun ByteBuffer.toByteArray():ByteArray{
        rewind()
        return ByteArray(remaining()).also {
            get(it)
        }

    }

}