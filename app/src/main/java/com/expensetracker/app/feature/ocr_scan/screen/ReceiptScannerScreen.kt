package com.expensetracker.app.feature.ocr_scan.screen

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expensetracker.app.data.mlkit.ParsedReceipt
import com.expensetracker.app.feature.ocr_scan.ReceiptScannerUiState
import com.expensetracker.app.feature.ocr_scan.ReceiptScannerViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import java.io.File
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ReceiptScannerScreen(
    onNavigateToAddTransaction: (amount: Double?, note: String?) -> Unit = { _, _ -> },
    viewModel: ReceiptScannerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    // Create temp file URI for camera capture
    val tempImageUri = remember { createTempImageUri(context) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.processImage(tempImageUri, context)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.processImage(it, context) }
    }

    when (val s = state) {
        is ReceiptScannerUiState.Camera -> {
            CameraScreen(
                hasCameraPermission = cameraPermission.status.isGranted,
                showPermissionRationale = cameraPermission.status.shouldShowRationale,
                onRequestPermission = { cameraPermission.launchPermissionRequest() },
                onCapture = {
                    if (cameraPermission.status.isGranted) {
                        cameraLauncher.launch(tempImageUri)
                    } else {
                        cameraPermission.launchPermissionRequest()
                    }
                },
                onPickFromGallery = { galleryLauncher.launch("image/*") },
            )
        }

        is ReceiptScannerUiState.Processing -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Scanning receipt...",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }

        is ReceiptScannerUiState.Result -> {
            ResultScreen(
                parsed = s.parsed,
                onUseResult = { amount, note ->
                    onNavigateToAddTransaction(amount, note)
                },
                onRetry = { viewModel.reset() },
            )
        }

        is ReceiptScannerUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp),
                ) {
                    Text(
                        text = "Scan Failed",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = s.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.reset() }) { Text("Try Again") }
                }
            }
        }
    }
}

@Composable
private fun CameraScreen(
    hasCameraPermission: Boolean,
    showPermissionRationale: Boolean,
    onRequestPermission: () -> Unit,
    onCapture: () -> Unit,
    onPickFromGallery: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Scan Receipt",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Take a photo or pick from gallery to automatically extract transaction details",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (showPermissionRationale) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Camera permission is needed to scan receipts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = onCapture) {
                Icon(Icons.Default.CameraAlt, null, Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(if (hasCameraPermission) "Take Photo" else "Allow Camera")
            }
            OutlinedButton(onClick = onPickFromGallery) {
                Icon(Icons.Default.Image, null, Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Gallery")
            }
        }
    }
}

@Composable
private fun ResultScreen(
    parsed: ParsedReceipt,
    onUseResult: (amount: Double?, note: String?) -> Unit,
    onRetry: () -> Unit,
) {
    val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    var merchant by remember { mutableStateOf(parsed.merchant) }
    var amountStr by remember { mutableStateOf(parsed.amount?.toLong()?.toString() ?: "") }
    var dateStr by remember { mutableStateOf(parsed.date.format(fmt)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = "Receipt Scanned",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))

        val confidenceText = when {
            parsed.confidence >= 0.8f -> "High confidence"
            parsed.confidence >= 0.5f -> "Medium confidence"
            else -> "Low confidence - please verify"
        }
        Text(
            text = confidenceText,
            style = MaterialTheme.typography.labelMedium,
            color = when {
                parsed.confidence >= 0.8f -> MaterialTheme.colorScheme.primary
                parsed.confidence >= 0.5f -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.error
            },
        )

        Spacer(Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    label = { Text("Merchant") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = dateStr,
                    onValueChange = { dateStr = it },
                    label = { Text("Date") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(onClick = onRetry, modifier = Modifier.weight(1f)) {
                Text("Scan Again")
            }
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull()
                    onUseResult(amount, merchant.takeIf { it.isNotBlank() })
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("Use This")
            }
        }
    }
}

private fun createTempImageUri(context: Context): Uri {
    val tempFile = File.createTempFile("receipt_", ".jpg", context.cacheDir)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        tempFile,
    )
}
