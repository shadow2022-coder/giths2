package com.hastakala.shop.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hastakala.shop.data.repository.InventoryItem
import com.hastakala.shop.data.repository.ProductCardModel
import com.hastakala.shop.data.repository.ProductForm
import com.hastakala.shop.data.repository.ShopRepository
import com.hastakala.shop.data.repository.VariantForm
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class InventoryUiState(
    val products: List<ProductCardModel> = emptyList(),
    val inventory: List<InventoryItem> = emptyList(),
    val editingForm: ProductForm? = null,
    val isEditorVisible: Boolean = false,
    val hasDraft: Boolean = false,
    val canSaveForm: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val shopRepository: ShopRepository
) : ViewModel() {
    private val editingForm = MutableStateFlow<ProductForm?>(null)
    private val editorVisible = MutableStateFlow(false)
    private val message = MutableStateFlow<String?>(null)
    private val error = MutableStateFlow<String?>(null)

    private val contentState = combine(
        shopRepository.observeProducts(),
        shopRepository.observeInventory(),
        editingForm,
        editorVisible
    ) { products, inventory, form, isEditorVisible ->
        InventoryUiState(
            products = products,
            inventory = inventory,
            editingForm = form,
            isEditorVisible = isEditorVisible && form != null,
            hasDraft = form != null,
            canSaveForm = form?.isValid() == true,
        )
    }

    val uiState: StateFlow<InventoryUiState> = combine(contentState, message, error) { content, currentMessage, currentError ->
        content.copy(
            message = currentMessage,
            error = currentError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InventoryUiState()
    )

    fun startNewProduct() {
        editingForm.value = ProductForm()
        editorVisible.value = true
    }

    fun editProduct(productId: Long) {
        viewModelScope.launch {
            editingForm.value = shopRepository.getProductForm(productId)
            editorVisible.value = editingForm.value != null
        }
    }

    fun minimizeEditor() {
        editorVisible.value = false
    }

    fun resumeEditor() {
        if (editingForm.value != null) {
            editorVisible.value = true
        }
    }

    fun discardDraft() {
        editingForm.value = null
        editorVisible.value = false
    }

    fun updateName(name: String) = updateForm { copy(name = name) }

    fun updateCategory(category: String) = updateForm { copy(category = category) }

    fun updateBasePrice(basePrice: String) = updateForm { copy(basePrice = basePrice.normalizeDecimal()) }

    fun updateImageUri(uri: String?) = updateForm { copy(imageUri = uri) }

    fun addVariant() = updateForm {
        copy(variants = variants + VariantForm())
    }

    fun removeVariant(index: Int) = updateForm {
        if (variants.size <= 1) {
            this
        } else {
            copy(variants = variants.filterIndexed { currentIndex, _ -> currentIndex != index })
        }
    }

    fun updateVariantColor(index: Int, color: String) = updateVariant(index) { copy(color = color) }

    fun updateVariantStock(index: Int, stock: String) =
        updateVariant(index) { copy(stock = stock.filterNumeric()) }

    fun updateVariantThreshold(index: Int, threshold: String) =
        updateVariant(index) { copy(manualThreshold = threshold.filterNumeric()) }

    fun saveProduct() {
        val form = editingForm.value ?: return
        viewModelScope.launch {
            runCatching {
                shopRepository.saveProduct(form)
            }.onSuccess {
                message.value = "Product saved"
                error.value = null
                editingForm.value = null
                editorVisible.value = false
            }.onFailure {
                error.value = it.message ?: "Could not save product."
            }
        }
    }

    fun deleteCurrentProduct() {
        val productId = editingForm.value?.id ?: return
        if (productId == 0L) {
            discardDraft()
            return
        }
        viewModelScope.launch {
            runCatching {
                shopRepository.deleteProduct(productId)
            }.onSuccess {
                message.value = "Product deleted"
                error.value = null
                editingForm.value = null
                editorVisible.value = false
            }.onFailure {
                error.value = it.message ?: "Could not delete product."
            }
        }
    }

    fun deleteProductNow(productId: Long) {
        viewModelScope.launch {
            runCatching {
                shopRepository.deleteProduct(productId)
            }.onSuccess {
                message.value = "Product removed"
                error.value = null
                if (editingForm.value?.id == productId) {
                    editingForm.value = null
                    editorVisible.value = false
                }
            }.onFailure {
                error.value = it.message ?: "Could not remove product."
            }
        }
    }

    fun updateStockNow(variantId: Long, newStock: Int) {
        viewModelScope.launch {
            runCatching {
                shopRepository.adjustStock(variantId, newStock)
            }.onFailure {
                error.value = it.message ?: "Could not update stock."
            }
        }
    }

    fun deleteVariantNow(variantId: Long) {
        viewModelScope.launch {
            runCatching {
                shopRepository.deleteVariant(variantId)
            }.onSuccess { removedWholeProduct ->
                message.value = if (removedWholeProduct) {
                    "Last color removed, so the product was removed too"
                } else {
                    "Color removed"
                }
                error.value = null
                val currentForm = editingForm.value
                if (currentForm != null) {
                    val updatedVariants = currentForm.variants.filterNot { it.id == variantId }
                    if (updatedVariants.isEmpty()) {
                        editingForm.value = null
                        editorVisible.value = false
                    } else if (updatedVariants.size != currentForm.variants.size) {
                        editingForm.value = currentForm.copy(variants = updatedVariants)
                    }
                }
            }.onFailure {
                error.value = it.message ?: "Could not remove color."
            }
        }
    }

    fun updateThresholdNow(variantId: Long, threshold: Int) {
        viewModelScope.launch {
            runCatching {
                shopRepository.updateThreshold(variantId, threshold)
            }.onFailure {
                error.value = it.message ?: "Could not update threshold."
            }
        }
    }

    fun clearFeedback() {
        message.value = null
        error.value = null
    }

    private fun updateForm(update: ProductForm.() -> ProductForm) {
        editingForm.value = editingForm.value?.update()
    }

    private fun updateVariant(index: Int, update: VariantForm.() -> VariantForm) {
        updateForm {
            copy(
                variants = variants.mapIndexed { currentIndex, variant ->
                    if (currentIndex == index) {
                        variant.update()
                    } else {
                        variant
                    }
                }
            )
        }
    }

    private fun ProductForm.isValid(): Boolean {
        val hasName = name.trim().isNotEmpty()
        val hasPrice = basePrice.trim().toDoubleOrNull() != null
        val hasVariant = variants.any { it.color.trim().isNotEmpty() }
        return hasName && hasPrice && hasVariant
    }

    private fun String.filterNumeric(): String = filter { it.isDigit() }

    private fun String.normalizeDecimal(): String {
        val filtered = filter { it.isDigit() || it == '.' }
        val firstDot = filtered.indexOf('.')
        return if (firstDot == -1) {
            filtered
        } else {
            buildString {
                append(filtered.substring(0, firstDot + 1))
                append(filtered.substring(firstDot + 1).replace(".", ""))
            }
        }
    }
}
