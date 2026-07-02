package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ExpiryStatusFilter {
    ALL,
    SAFE, // Green (> 5 days)
    EXPIRING_SOON, // Yellow (1-5 days)
    EXPIRED // Red (<= 0 days)
}

class ExpiryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ProductRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ProductRepository(database.productDao())
    }

    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeProducts: StateFlow<List<Product>> = repository.activeProducts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UI state for search and filtering
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")
    val selectedStatusFilter = MutableStateFlow(ExpiryStatusFilter.ALL)

    // Selected screen tab
    val currentTab = MutableStateFlow(0)

    // Filtered active products
    val filteredProducts: StateFlow<List<Product>> = combine(
        activeProducts,
        searchQuery,
        selectedCategory,
        selectedStatusFilter
    ) { products, query, category, statusFilter ->
        products.filter { product ->
            val matchesQuery = product.name.contains(query, ignoreCase = true) ||
                    product.notes.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || product.category == category

            val days = product.getDaysRemaining()
            val matchesStatus = when (statusFilter) {
                ExpiryStatusFilter.ALL -> true
                ExpiryStatusFilter.SAFE -> days > 5
                ExpiryStatusFilter.EXPIRING_SOON -> days in 1..5
                ExpiryStatusFilter.EXPIRED -> days <= 0
            }

            matchesQuery && matchesCategory && matchesStatus
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // AI Recipes state
    private val _recipeState = MutableStateFlow<RecipeUiState>(RecipeUiState.Idle)
    val recipeState: StateFlow<RecipeUiState> = _recipeState.asStateFlow()

    fun insertProduct(product: Product) {
        viewModelScope.launch {
            repository.insert(product)
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            repository.update(product)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.delete(product)
        }
    }

    fun consumeProduct(product: Product) {
        viewModelScope.launch {
            repository.update(product.copy(isConsumed = true))
        }
    }

    fun generateRecipes() {
        viewModelScope.launch {
            _recipeState.value = RecipeUiState.Loading
            val itemsForRecipes = activeProducts.value.filter { it.getDaysRemaining() <= 7 }
            if (itemsForRecipes.isEmpty()) {
                _recipeState.value = RecipeUiState.Error("No products are expiring soon (<= 7 days remaining). Add some products with nearing expiration dates to get recipe suggestions!")
                return@launch
            }

            try {
                val suggestions = RecipeService.getRecipeSuggestions(itemsForRecipes)
                _recipeState.value = RecipeUiState.Success(suggestions)
            } catch (e: Exception) {
                _recipeState.value = RecipeUiState.Error(e.message ?: "An unexpected error occurred while generating recipes.")
            }
        }
    }

    fun resetRecipeState() {
        _recipeState.value = RecipeUiState.Idle
    }
}

sealed interface RecipeUiState {
    object Idle : RecipeUiState
    object Loading : RecipeUiState
    data class Success(val recipes: List<RecipeSuggestion>) : RecipeUiState
    data class Error(val message: String) : RecipeUiState
}
