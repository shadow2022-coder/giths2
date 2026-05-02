package com.hastakala.shop.ui.inventory

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hastakala.shop.data.repository.InventoryItem
import com.hastakala.shop.data.repository.ProductCardModel
import com.hastakala.shop.data.repository.ProductForm
import com.hastakala.shop.ui.components.AppSearchField
import com.hastakala.shop.ui.components.EmptyState
import com.hastakala.shop.ui.components.ProductGridCard
import com.hastakala.shop.ui.components.ProductImage
import com.hastakala.shop.ui.components.SectionTitle
import com.hastakala.shop.ui.components.StockChip
import com.hastakala.shop.ui.components.SummaryCard
import com.hastakala.shop.util.CurrencyUtils

@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showStockEditor by remember { mutableStateOf<InventoryItem?>(null) }
    var pendingVariantRemoval by remember { mutableStateOf<InventoryItem?>(null) }
    var pendingProductRemoval by remember { mutableStateOf<ProductCardModel?>(null) }
    var selectedTab by rememberSaveable { mutableStateOf("products") }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val filteredProducts = uiState.products.filter { it.matchesSearch(searchQuery) }
    val filteredInventory = uiState.inventory.filter { it.matchesSearch(searchQuery) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            viewModel.updateImageUri(uri.toString())
        }
    }

    LaunchedEffect(uiState.message, uiState.error) {
        val text = uiState.message ?: uiState.error
        if (!text.isNullOrBlank()) {
            snackbarHostState.showSnackbar(text)
            viewModel.clearFeedback()
        }
    }

    LaunchedEffect(selectedTab) {
        searchQuery = ""
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (selectedTab == "products") {
                if (uiState.hasDraft && !uiState.isEditorVisible) {
                    ExtendedFloatingActionButton(
                        onClick = viewModel::resumeEditor,
                        icon = { Icon(Icons.Filled.Edit, contentDescription = "Continue draft") },
                        text = { Text("Continue Draft") }
                    )
                } else {
                    FloatingActionButton(onClick = viewModel::startNewProduct) {
                        Icon(Icons.Filled.Add, contentDescription = "Add product")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionTitle(
                title = "Inventory",
                subtitle = "Manage products, photos, stock, and low-stock limits."
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Products",
                    value = uiState.products.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Low stock",
                    value = uiState.inventory.count { it.isLowStock }.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedTab == "products",
                    onClick = { selectedTab = "products" },
                    label = { Text("Products") }
                )
                FilterChip(
                    selected = selectedTab == "stock",
                    onClick = { selectedTab = "stock" },
                    label = { Text("Stock") }
                )
            }
            AppSearchField(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                label = if (selectedTab == "products") "Search products" else "Search stock"
            )

            if (uiState.hasDraft && !uiState.isEditorVisible) {
                DraftResumeCard(
                    title = uiState.editingForm?.name?.takeIf { it.isNotBlank() } ?: "New product draft",
                    onResume = viewModel::resumeEditor,
                    onDiscard = viewModel::discardDraft
                )
            }

            if (selectedTab == "products") {
                if (uiState.products.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            title = "No products yet",
                            body = "Add a product with colors and a photo to start selling."
                        )
                    }
                } else if (filteredProducts.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            title = "No product matches",
                            body = "Try a different product, category, or color."
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        modifier = Modifier.weight(1f),
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 96.dp)
                    ) {
                        items(filteredProducts, key = { it.id }) { product ->
                            ProductGridCard(
                                product = product,
                                onClick = { viewModel.editProduct(product.id) },
                                onDelete = { pendingProductRemoval = product }
                            )
                        }
                    }
                }
            } else {
                if (uiState.inventory.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            title = "No stock yet",
                            body = "Add products with colors to start tracking stock."
                        )
                    }
                } else if (filteredInventory.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            title = "No stock matches",
                            body = "Try a different product or color name."
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 96.dp)
                    ) {
                        items(filteredInventory.size) { index ->
                            val item = filteredInventory[index]
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = item.productName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${item.color} • ${CurrencyUtils.format(item.basePrice)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            StockChip("In stock", item.stock.toString(), item.isLowStock)
                                            StockChip("Need", item.effectiveThreshold.toString(), item.isLowStock)
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(onClick = { showStockEditor = item }) {
                                            Icon(Icons.Filled.Edit, contentDescription = "Edit stock")
                                        }
                                        IconButton(onClick = { pendingVariantRemoval = item }) {
                                            Icon(
                                                Icons.Filled.Delete,
                                                contentDescription = "Remove color",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (uiState.isEditorVisible) {
            uiState.editingForm?.let { form ->
            ProductEditorDialog(
                form = form,
                onDismiss = viewModel::minimizeEditor,
                onPickImage = { imagePicker.launch(arrayOf("image/*")) },
                onNameChange = viewModel::updateName,
                onCategoryChange = viewModel::updateCategory,
                onBasePriceChange = viewModel::updateBasePrice,
                onVariantColorChange = viewModel::updateVariantColor,
                onVariantStockChange = viewModel::updateVariantStock,
                onVariantThresholdChange = viewModel::updateVariantThreshold,
                onAddVariant = viewModel::addVariant,
                onRemoveVariant = viewModel::removeVariant,
                onSave = viewModel::saveProduct,
                onDelete = viewModel::deleteCurrentProduct,
                canSave = uiState.canSaveForm,
                onDiscard = viewModel::discardDraft
            )
        }
        }

        showStockEditor?.let { item ->
            StockEditorDialog(
                item = item,
                onDismiss = { showStockEditor = null },
                onDelete = {
                    showStockEditor = null
                    pendingVariantRemoval = item
                },
                onSave = { stock, threshold ->
                    viewModel.updateStockNow(item.variantId, stock)
                    viewModel.updateThresholdNow(item.variantId, threshold)
                    showStockEditor = null
                }
            )
        }

        pendingVariantRemoval?.let { item ->
            AlertDialog(
                onDismissRequest = { pendingVariantRemoval = null },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteVariantNow(item.variantId)
                            if (showStockEditor?.variantId == item.variantId) {
                                showStockEditor = null
                            }
                            pendingVariantRemoval = null
                        }
                    ) {
                        Text("Remove")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingVariantRemoval = null }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Remove ${item.color}?") },
                text = {
                    Text(
                        "This deletes the selected stock color. If it is the last color, the whole product is removed too."
                    )
                }
            )
        }

        pendingProductRemoval?.let { product ->
            AlertDialog(
                onDismissRequest = { pendingProductRemoval = null },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteProductNow(product.id)
                            pendingProductRemoval = null
                        }
                    ) {
                        Text("Remove")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingProductRemoval = null }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Remove ${product.name}?") },
                text = {
                    Text("Use this for products that are not selling well anymore.")
                }
            )
        }
    }
}

private fun ProductCardModel.matchesSearch(query: String): Boolean {
    if (query.isBlank()) return true
    return name.contains(query, ignoreCase = true) ||
        category.contains(query, ignoreCase = true) ||
        variants.any { it.color.contains(query, ignoreCase = true) }
}

private fun InventoryItem.matchesSearch(query: String): Boolean {
    if (query.isBlank()) return true
    return productName.contains(query, ignoreCase = true) ||
        color.contains(query, ignoreCase = true)
}

@Composable
private fun ProductEditorDialog(
    form: ProductForm,
    onDismiss: () -> Unit,
    onPickImage: () -> Unit,
    onNameChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onBasePriceChange: (String) -> Unit,
    onVariantColorChange: (Int, String) -> Unit,
    onVariantStockChange: (Int, String) -> Unit,
    onVariantThresholdChange: (Int, String) -> Unit,
    onAddVariant: () -> Unit,
    onRemoveVariant: (Int) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    canSave: Boolean,
    onDiscard: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 760.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SectionTitle(
                        title = if (form.id == 0L) "Add product" else "Edit product",
                        subtitle = "Keep it simple for fast billing."
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Back to list")
                        }
                        TextButton(onClick = onDiscard) {
                            Text(if (form.id == 0L) "Discard draft" else "Close draft")
                        }
                    }
                    ProductImage(
                        imageUri = form.imageUri,
                        contentDescription = form.name.ifBlank { "Product" },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onPickImage, modifier = Modifier.weight(1f)) {
                            Text(if (form.imageUri.isNullOrBlank()) "Choose Photo" else "Change Photo")
                        }
                        if (!form.imageUri.isNullOrBlank()) {
                            TextButton(onClick = { onPickImage() }) {
                                Text("Replace")
                            }
                        }
                    }
                    OutlinedTextField(
                        value = form.name,
                        onValueChange = onNameChange,
                        label = { Text("Product name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = form.category,
                        onValueChange = onCategoryChange,
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = form.basePrice,
                        onValueChange = onBasePriceChange,
                        label = { Text("Base price") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    Text(
                        text = "Variants",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    form.variants.forEachIndexed { index, variant ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Color ${index + 1}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (form.variants.size > 1) {
                                        IconButton(onClick = { onRemoveVariant(index) }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Remove variant")
                                        }
                                    }
                                }
                                OutlinedTextField(
                                    value = variant.color,
                                    onValueChange = { onVariantColorChange(index, it) },
                                    label = { Text("Color") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = variant.stock,
                                        onValueChange = { onVariantStockChange(index, it) },
                                        label = { Text("Stock") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    OutlinedTextField(
                                        value = variant.manualThreshold,
                                        onValueChange = { onVariantThresholdChange(index, it) },
                                        label = { Text("Threshold") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }
                            }
                        }
                    }
                    OutlinedButton(onClick = onAddVariant, modifier = Modifier.fillMaxWidth()) {
                        Text("Add Color")
                    }
                    Text(
                        text = "Tip: Product name, base price, and at least one color are required.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!canSave) {
                        Text(
                            text = "Fill product name, base price, and one color to enable Save.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = onDismiss
                        ) {
                            Text("Back")
                        }
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = onSave,
                            enabled = canSave
                        ) {
                            Text("Save Product")
                        }
                    }
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onDiscard
                    ) {
                        Text(if (form.id == 0L) "Discard Draft" else "Close Draft")
                    }
                    if (form.id != 0L) {
                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onDelete
                        ) {
                            Text("Delete Product")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DraftResumeCard(
    title: String,
    onResume: () -> Unit,
    onDiscard: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Draft saved: $title",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "You can go anywhere in Inventory and return to finish this later.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onResume) {
                    Text("Continue")
                }
                OutlinedButton(onClick = onDiscard) {
                    Text("Discard")
                }
            }
        }
    }
}

@Composable
private fun StockEditorDialog(
    item: InventoryItem,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onSave: (Int, Int) -> Unit
) {
    var stockText by rememberSaveable(item.variantId) { mutableStateOf(item.stock.toString()) }
    var thresholdText by rememberSaveable(item.variantId) { mutableStateOf(item.manualThreshold.takeIf { it > 0 }?.toString().orEmpty()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionTitle(
                    title = "${item.productName} • ${item.color}",
                    subtitle = "Auto threshold: ${item.autoThreshold}"
                )
                OutlinedTextField(
                    value = stockText,
                    onValueChange = { stockText = it.filter(Char::isDigit) },
                    label = { Text("Stock") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = thresholdText,
                    onValueChange = { thresholdText = it.filter(Char::isDigit) },
                    label = { Text("Manual threshold") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickAdjustButton(label = "-1") {
                        val current = stockText.toIntOrNull() ?: item.stock
                        stockText = (current - 1).coerceAtLeast(0).toString()
                    }
                    QuickAdjustButton(label = "+1") {
                        val current = stockText.toIntOrNull() ?: item.stock
                        stockText = (current + 1).toString()
                    }
                    QuickAdjustButton(label = "+5") {
                        val current = stockText.toIntOrNull() ?: item.stock
                        stockText = (current + 5).toString()
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            stockText = "0"
                        }
                    ) {
                        Text("Set Stock to 0")
                    }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onSave(
                                stockText.toIntOrNull() ?: item.stock,
                                thresholdText.toIntOrNull() ?: 0
                            )
                        }
                    ) {
                        Text("Save")
                    }
                }
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDelete
                ) {
                    Text("Remove This Color")
                }
            }
        }
    }
}

@Composable
private fun QuickAdjustButton(
    label: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.wrapContentHeight()
    ) {
        Text(label)
    }
}
