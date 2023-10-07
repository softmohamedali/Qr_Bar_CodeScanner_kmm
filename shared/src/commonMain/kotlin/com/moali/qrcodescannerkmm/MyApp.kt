package com.moali.qrcodescannerkmm

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.moali.qrcodescannerkmm.core.shared.ui.CameraPreview
import kotlinx.coroutines.launch


@Composable
fun MyApp (){

    val scaffoldState = rememberScaffoldState()
    val scope= rememberCoroutineScope ()
    Scaffold (
        modifier = Modifier.fillMaxSize(),
        scaffoldState
    ){
        CameraPreview(
            visible = true,
            onCodeScanner = {
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(
                        "Code Result =>${it}"
                    )
                }
            }
        )
    }
}