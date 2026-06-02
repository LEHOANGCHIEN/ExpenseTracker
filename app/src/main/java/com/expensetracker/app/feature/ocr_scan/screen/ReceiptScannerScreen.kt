package com.expensetracker.app.feature.ocr_scan.screen

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.expensetracker.app.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expensetracker.app.domain.model.Category
import com.expensetracker.app.feature.ocr_scan.ReceiptResultData
import com.expensetracker.app.feature.ocr_scan.ReceiptScannerUiState
import com.expensetracker.app.feature.ocr_scan.ReceiptScannerViewModel
import com.expensetracker.app.feature.ocr_scan.screen.component.CameraPreview
import com.expensetracker.app.feature.ocr_scan.screen.component.OcrResultSheet
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScannerScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToAddTransaction: (amount: Double?, note: String?, categoryId: Long?) -> Unit = { _, _, _ -> },
    viewModel: ReceiptScannerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.processImage(it, context) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ocr_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { paddingValues ->
    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
    when (val s = state) {
        is ReceiptScannerUiState.Camera -> {
            if (cameraPermission.status.isGranted) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CameraPreview(
                        onImageCaptured = { uri -> viewModel.processImage(uri, context) },
                        onError = { /* handled silently; user can retry */ },
                        modifier = Modifier.fillMaxSize(),
                    )
                    // Gallery button overlaid at bottom-left
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 24.dp, bottom = 40.dp),
                    ) {
                        Icon(Icons.Default.Image, null, Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Gallery")
                    }
                }
            } else {
                PermissionScreen(
                    showRationale = cameraPermission.status.shouldShowRationale,
                    onRequestPermission = { cameraPermission.launchPermissionRequest() },
                    onPickFromGallery = { galleryLauncher.launch("image/*") },
                )
            }
        }

        is ReceiptScannerUiState.Processing -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Reading receipt…",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }

        is ReceiptScannerUiState.Result -> {
            // Show background (previous camera view or blank) + result sheet
            Box(modifier = Modifier.fillMaxSize())
            OcrResultSheet(
                data = s.data,
                suggestedCategory = null, // categories resolved in NavHost via categoryId
                sheetState = sheetState,
                onDismiss = { scope.launch { sheetState.hide() } },
                onAmountChanged = viewModel::onAmountEdited,
                onNoteChanged = viewModel::onNoteEdited,
                onDateChanged = viewModel::onDateEdited,
                onUseThis = {
                    val amount = s.data.editedAmount.toDoubleOrNull()
                    val note = s.data.editedNote.takeIf { it.isNotBlank() }
                    onNavigateToAddTransaction(amount, note, s.data.suggestedCategoryId)
                },
                onScanAgain = { viewModel.reset() },
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
                    Button(onClick = viewModel::reset) { Text("Try Again") }
                }
            }
        }
    }
    }   // Box
    }   // Scaffold
}

@Composable
private fun PermissionScreen(
    showRationale: Boolean,
    onRequestPermission: () -> Unit,
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
            text = "Take a photo or pick from gallery to automatically extract transaction details.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (showRationale) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Camera permission is required to scan receipts.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = onRequestPermission) {
                Icon(Icons.Default.CameraAlt, null, Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Allow Camera")
            }
            OutlinedButton(onClick = onPickFromGallery) {
                Icon(Icons.Default.Image, null, Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Gallery")
            }
        }
    }
}
