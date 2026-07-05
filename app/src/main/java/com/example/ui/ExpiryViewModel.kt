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
    private val prefs = application.getSharedPreferences("wastenot_prefs", android.content.Context.MODE_PRIVATE)

    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean("is_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _username = MutableStateFlow(prefs.getString("username", "") ?: "")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _email = MutableStateFlow(prefs.getString("email", "") ?: "")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _phoneNumber = MutableStateFlow(prefs.getString("phone_number", "") ?: "")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private val _businessType = MutableStateFlow(prefs.getString("business_type", "Individual") ?: "Individual")
    val businessType: StateFlow<String> = _businessType.asStateFlow()

    val isOwner: StateFlow<Boolean> = combine(_username, _email, _phoneNumber) { user, mail, phone ->
        mail.trim().equals("catalin.barbu92@gmail.com", ignoreCase = true) ||
        phone.trim() == "0784799711" ||
        user.lowercase().contains("owner") ||
        user.lowercase().contains("catalin")
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun login(user: String, emailVal: String, phone: String, businessTypeVal: String = "Individual") {
        prefs.edit().apply {
            putBoolean("is_logged_in", true)
            putString("username", user)
            putString("email", emailVal)
            putString("phone_number", phone)
            putString("business_type", businessTypeVal)
            apply()
        }
        _username.value = user
        _email.value = emailVal
        _phoneNumber.value = phone
        _businessType.value = businessTypeVal
        _isLoggedIn.value = true
    }

    fun updateBusinessType(newType: String) {
        prefs.edit().putString("business_type", newType).apply()
        _businessType.value = newType
    }

    fun logout() {
        prefs.edit().clear().apply()
        _username.value = ""
        _email.value = ""
        _phoneNumber.value = ""
        _businessType.value = "Individual"
        _isLoggedIn.value = false
    }

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

    private val _cart = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val cart: StateFlow<Map<Int, Int>> = _cart.asStateFlow()

    fun addToCart(productId: Int, amount: Int = 1) {
        val current = _cart.value.toMutableMap()
        current[productId] = (current[productId] ?: 0) + amount
        _cart.value = current
    }

    fun removeFromCart(productId: Int) {
        val current = _cart.value.toMutableMap()
        val qty = current[productId] ?: 0
        if (qty > 1) {
            current[productId] = qty - 1
        } else {
            current.remove(productId)
        }
        _cart.value = current
    }

    fun clearCart() {
        _cart.value = emptyMap()
    }

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
                ExpiryStatusFilter.EXPIRING_SOON -> days in 3..5
                ExpiryStatusFilter.EXPIRED -> days <= 2
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
