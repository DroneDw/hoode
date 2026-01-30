@file:OptIn(androidx.camera.core.ExperimentalGetImage::class)

package com.example.mybalaka.screens.organizer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.mybalaka.network.TicketScanApi
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketScannerScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val vibrator = context.getSystemService(Vibrator::class.java)

    var isScanning by remember { mutableStateOf(true) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var resultColor by remember { mutableStateOf(Color.Transparent) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Scan Ticket") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            if (!hasCameraPermission) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Camera permission required to scan tickets",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                        Text("Grant Permission")
                    }
                }
            } else {

                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->

                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                        cameraProviderFuture.addListener({

                            val cameraProvider = cameraProviderFuture.get()

                            val preview = Preview.Builder().build().apply {
                                setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val analysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(
                                    ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                                )
                                .build()

                            analysis.setAnalyzer(
                                cameraExecutor,
                                TicketAnalyzer(
                                    isScanning = { isScanning },
                                    onScanResult = { success, message ->
                                        resultMessage = message
                                        resultColor = if (success)
                                            Color(0xFF4CAF50)
                                        else
                                            Color(0xFFF44336)

                                        vibrator?.let {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                it.vibrate(
                                                    VibrationEffect.createOneShot(
                                                        200,
                                                        if (success)
                                                            VibrationEffect.DEFAULT_AMPLITUDE
                                                        else
                                                            255
                                                    )
                                                )
                                            } else {
                                                @Suppress("DEPRECATION")
                                                it.vibrate(200)
                                            }
                                        }

                                        isScanning = false
                                    }
                                )
                            )

                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                analysis
                            )

                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    }
                )

                if (isScanning) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            "Point camera at QR code",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                resultMessage?.let { msg ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(resultColor.copy(alpha = 0.95f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                msg,
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    resultMessage = null
                                    isScanning = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = resultColor
                                )
                            ) {
                                Icon(Icons.Default.Refresh, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Scan Next Ticket")
                            }
                        }
                    }
                }
            }
        }
    }
}

class TicketAnalyzer(
    private val isScanning: () -> Boolean,
    private val onScanResult: (Boolean, String) -> Unit
) : ImageAnalysis.Analyzer {

    private val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient()
    private var lastScanTime = 0L

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {

        if (!isScanning()) {
            imageProxy.close()
            return
        }

        val now = System.currentTimeMillis()
        if (now - lastScanTime < 2000) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                val qr = barcodes.firstOrNull()?.rawValue
                if (qr != null) {
                    lastScanTime = now
                    TicketScanApi.scanTicket(qr, onScanResult)
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
