package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Product
import com.example.data.RecipeSuggestion
import com.example.ui.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: ExpiryViewModel = viewModel()) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

    if (!isLoggedIn) {
        LoginScreen(viewModel = viewModel)
    } else {
        val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
        val isOwner by viewModel.isOwner.collectAsStateWithLifecycle()
        val activeProducts by viewModel.activeProducts.collectAsStateWithLifecycle()
        val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
        var showAddDialog by remember { mutableStateOf(false) }
        var productToEdit by remember { mutableStateOf<Product?>(null) }

        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.img_woody_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.88f))
            )

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Eco,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.app_name),
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = (-0.5).sp,
                                    fontSize = 20.sp
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                },
                bottomBar = {
                    Column {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            thickness = 1.dp
                        )
                        NavigationBar(
                            containerColor = Color.Transparent,
                            tonalElevation = 0.dp
                        ) {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { viewModel.currentTab.value = 0 },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == 0) Icons.Rounded.Kitchen else Icons.Outlined.Kitchen,
                                        contentDescription = "Pantry"
                                    )
                                },
                                label = { Text("Pantry", fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Medium) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.secondary,
                                    unselectedIconColor = NaturalNeutral,
                                    unselectedTextColor = NaturalNeutral
                                ),
                                modifier = Modifier.testTag("tab_pantry")
                            )
                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { viewModel.currentTab.value = 1 },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == 1) Icons.Rounded.Analytics else Icons.Outlined.Analytics,
                                        contentDescription = "Expiry Board"
                                    )
                                },
                                label = { Text("Board", fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Medium) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.secondary,
                                    unselectedIconColor = NaturalNeutral,
                                    unselectedTextColor = NaturalNeutral
                                ),
                                modifier = Modifier.testTag("tab_board")
                            )
                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { viewModel.currentTab.value = 2 },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == 2) Icons.Rounded.RestaurantMenu else Icons.Outlined.RestaurantMenu,
                                        contentDescription = "Recipes"
                                    )
                                },
                                label = { Text("Recipes", fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Medium) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.secondary,
                                    unselectedIconColor = NaturalNeutral,
                                    unselectedTextColor = NaturalNeutral
                                ),
                                modifier = Modifier.testTag("tab_recipes")
                            )
                            NavigationBarItem(
                                selected = currentTab == 3,
                                onClick = { viewModel.currentTab.value = 3 },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == 3) Icons.Rounded.LocalShipping else Icons.Outlined.LocalShipping,
                                        contentDescription = "Order"
                                    )
                                },
                                label = { Text("Order", fontWeight = if (currentTab == 3) FontWeight.Bold else FontWeight.Medium) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.secondary,
                                    unselectedIconColor = NaturalNeutral,
                                    unselectedTextColor = NaturalNeutral
                                ),
                                modifier = Modifier.testTag("tab_order")
                            )
                        }
                    }
                },
                floatingActionButton = {
                    if (currentTab == 0 && isOwner) {
                        FloatingActionButton(
                            onClick = { showAddDialog = true },
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary,
                            shape = RoundedCornerShape(22.dp),
                            modifier = Modifier
                                .navigationBarsPadding()
                                .shadow(elevation = 6.dp, shape = RoundedCornerShape(22.dp))
                                .border(
                                    width = 4.dp,
                                    color = MaterialTheme.colorScheme.background,
                                    shape = RoundedCornerShape(22.dp)
                                )
                                .testTag("add_product_fab")
                        ) {
                            Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add Product", modifier = Modifier.size(28.dp))
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color.Transparent)
                ) {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = {
                            fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                        },
                        label = "TabContent"
                    ) { tab ->
                        when (tab) {
                            0 -> PantryTab(
                                viewModel = viewModel,
                                onEditProduct = { product ->
                                    if (isOwner) {
                                        productToEdit = product
                                        showAddDialog = true
                                    }
                                }
                            )
                            1 -> ExpiryBoardTab(viewModel = viewModel)
                            2 -> RecipesTab(viewModel = viewModel)
                            3 -> OrderTab(viewModel = viewModel)
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddEditProductDialog(
                product = productToEdit,
                onDismiss = {
                    showAddDialog = false
                    productToEdit = null
                },
                onSave = { name, category, expiryDate, quantity, notes, imageUrl, price, isB2b ->
                    if (productToEdit != null) {
                        viewModel.updateProduct(
                            productToEdit!!.copy(
                                name = name,
                                category = category,
                                expiryDate = expiryDate,
                                quantity = quantity,
                                notes = notes,
                                imageUrl = imageUrl,
                                price = price,
                                isB2bOnly = isB2b
                            )
                        )
                    } else {
                        viewModel.insertProduct(
                            Product(
                                name = name,
                                category = category,
                                expiryDate = expiryDate,
                                quantity = quantity,
                                notes = notes,
                                imageUrl = imageUrl,
                                price = price,
                                isB2bOnly = isB2b
                            )
                        )
                    }
                    showAddDialog = false
                    productToEdit = null
                }
            )
        }
    }
}

@Composable
fun PantryTab(
    viewModel: ExpiryViewModel,
    onEditProduct: (Product) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedStatusFilter by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()
    val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val isOwner by viewModel.isOwner.collectAsStateWithLifecycle()
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val clientType by viewModel.businessType.collectAsStateWithLifecycle()

    val categories = listOf("All", "Dairy & Eggs", "Fruits & Vegetables", "Meat & Seafood", "Bakery", "Pantry Staples", "Beverages", "Other")

    var showProfileDialog by remember { mutableStateOf(false) }
    val username by viewModel.username.collectAsStateWithLifecycle()
    val initials = remember(username) {
        if (username.isBlank()) "UN" else {
            username.trim().split("\\s+".toRegex()).take(2)
                .mapNotNull { if (it.isNotEmpty()) it.first().uppercase() else null }
                .joinToString("")
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar with Avatar Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("Search pantry...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_bar"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    errorBorderColor = MaterialTheme.colorScheme.outline
                ),
                singleLine = true
            )

            // Natural Tones Avatar with Click to Profile
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
                    .clickable { showProfileDialog = true }
                    .testTag("avatar_icon"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        }

        if (showProfileDialog) {
            ProfileDialog(
                viewModel = viewModel,
                onDismiss = { showProfileDialog = false }
            )
        }

        val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
        PantryInsightsDashboard(allProducts = allProducts)

        // Filters row (Categories)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { viewModel.selectedCategory.value = category },
                    label = { Text(category, fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Medium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("category_chip_$category")
                )
            }
        }

        // Status Filter Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExpiryStatusFilter.values().forEach { filter ->
                val label = when (filter) {
                    ExpiryStatusFilter.ALL -> "All Status"
                    ExpiryStatusFilter.SAFE -> "Safe"
                    ExpiryStatusFilter.EXPIRING_SOON -> "Soon"
                    ExpiryStatusFilter.EXPIRED -> "Critical"
                }
                val color = when (filter) {
                    ExpiryStatusFilter.ALL -> MaterialTheme.colorScheme.outline
                    ExpiryStatusFilter.SAFE -> ExpiryGreen
                    ExpiryStatusFilter.EXPIRING_SOON -> ExpiryYellow
                    ExpiryStatusFilter.EXPIRED -> ExpiryRed
                }

                ElevatedFilterChip(
                    selected = selectedStatusFilter == filter,
                    onClick = { viewModel.selectedStatusFilter.value = filter },
                    label = { Text(label, fontSize = 12.sp, fontWeight = if (selectedStatusFilter == filter) FontWeight.Bold else FontWeight.Medium) },
                    leadingIcon = if (filter != ExpiryStatusFilter.ALL) {
                        {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.elevatedFilterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f).testTag("status_chip_${filter.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Product list
        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Kitchen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your Pantry is Empty",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add products to track their expiry dates and minimize food waste!",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        isOwner = isOwner,
                        clientType = clientType,
                        cartQuantity = cart[product.id] ?: 0,
                        onClick = { onEditProduct(product) },
                        onConsume = { viewModel.consumeProduct(product) },
                        onDelete = { viewModel.deleteProduct(product) },
                        onAddToCart = { viewModel.addToCart(it.id) },
                        onRemoveFromCart = { viewModel.removeFromCart(it.id) }
                    )
                }
            }
        }
    }
}

fun getCategoryEmoji(category: String): String {
    return when (category) {
        "Dairy & Eggs" -> "🥛"
        "Fruits & Vegetables" -> "🥗"
        "Meat & Seafood" -> "🥩"
        "Bakery" -> "🍞"
        "Pantry Staples" -> "🥫"
        "Beverages" -> "🧃"
        else -> "🏷️"
    }
}

fun getProductDefaultImage(name: String, category: String): String {
    val lowerName = name.lowercase()
    return when {
        lowerName.contains("milk") -> "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=200&q=80"
        lowerName.contains("egg") -> "https://images.unsplash.com/photo-1506976785307-8732e854ad03?w=200&q=80"
        lowerName.contains("apple") -> "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=200&q=80"
        lowerName.contains("banana") -> "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=200&q=80"
        lowerName.contains("broccoli") -> "https://images.unsplash.com/photo-1584270354949-c26b0d5b4a0c?w=200&q=80"
        lowerName.contains("salmon") || lowerName.contains("fish") -> "https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?w=200&q=80"
        lowerName.contains("steak") || lowerName.contains("beef") || lowerName.contains("chicken") || lowerName.contains("meat") -> "https://images.unsplash.com/photo-1607623814075-e51df1bdc82f?w=200&q=80"
        lowerName.contains("bread") || lowerName.contains("sourdough") || lowerName.contains("bagel") -> "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=200&q=80"
        lowerName.contains("pasta") || lowerName.contains("rice") || lowerName.contains("cereal") -> "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=200&q=80"
        lowerName.contains("juice") || lowerName.contains("beverage") || lowerName.contains("drink") || lowerName.contains("orange") -> "https://images.unsplash.com/photo-1621506289937-a8e4df240d0b?w=200&q=80"
        
        category == "Dairy & Eggs" -> "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=200&q=80"
        category == "Fruits & Vegetables" -> "https://images.unsplash.com/photo-1610832958506-ee5633619141?w=200&q=80"
        category == "Meat & Seafood" -> "https://images.unsplash.com/photo-1607623814075-e51df1bdc82f?w=200&q=80"
        category == "Bakery" -> "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=200&q=80"
        category == "Pantry Staples" -> "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=200&q=80"
        category == "Beverages" -> "https://images.unsplash.com/photo-1527661591475-527312dd65f5?w=200&q=80"
        else -> "https://images.unsplash.com/photo-1542838132-92c53300491e?w=200&q=80"
    }
}

@Composable
fun ProductCard(
    product: Product,
    isOwner: Boolean,
    clientType: String = "Individual",
    cartQuantity: Int = 0,
    onClick: () -> Unit = {},
    onConsume: () -> Unit = {},
    onDelete: () -> Unit = {},
    onAddToCart: (Product) -> Unit = {},
    onRemoveFromCart: (Product) -> Unit = {}
) {
    val daysRemaining = product.getDaysRemaining()
    val (statusColor, statusText, cardBg, textAccentColor) = when {
        daysRemaining <= 2 -> {
            val text = when {
                daysRemaining < 0 -> "Expired ${Math.abs(daysRemaining)} d ago"
                daysRemaining == 0 -> "Expires Today"
                daysRemaining == 1 -> "Expires Tomorrow"
                else -> "In 2 days"
            }
            Quadruple(ExpiryRed, text, ExpiryRedLight, ExpiryRed)
        }
        daysRemaining <= 5 -> {
            val text = "In $daysRemaining days"
            Quadruple(ExpiryYellow, text, ExpiryYellowLight, ExpiryYellow)
        }
        else -> {
            Quadruple(ExpiryGreen, "In $daysRemaining days", ExpiryGreenLight, ExpiryGreen)
        }
    }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isOwner) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Photo with overlaid emoji
            val imageUrl = product.imageUrl?.ifBlank { null } ?: getProductDefaultImage(product.name, product.category)
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("product_image_${product.id}"),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    coil.compose.AsyncImage(
                        model = imageUrl,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Overlay Badge
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .offset(x = 2.dp, y = 2.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getCategoryEmoji(product.category),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main Product Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${product.category}${if (product.quantity.isNotBlank()) " • ${product.quantity}" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (product.price > 0.0) {
                        Text(
                            text = "$${String.format(Locale.getDefault(), "%.2f", product.price)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                if (product.isB2bOnly) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "B2B WHOLESALE ONLY 🏢",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (product.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = product.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right Badges and Quick Actions Column
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                // Warning status tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 9.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Actions row
                if (isOwner) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onConsume,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("action_consume_${product.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Mark Consumed",
                                tint = ExpiryGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("action_delete_${product.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Delete product",
                                tint = ExpiryRed.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    // Customer shopping actions!
                    if (product.isB2bOnly && clientType == "Individual") {
                        Text(
                            text = "B2B Only",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    } else {
                        if (cartQuantity == 0) {
                            Button(
                                onClick = { onAddToCart(product) },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("action_add_cart_${product.id}"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Order", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { onRemoveFromCart(product) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("-", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                
                                Text(
                                    text = "$cartQuantity",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondary)
                                        .clickable { onAddToCart(product) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpiryBoardTab(viewModel: ExpiryViewModel) {
    val activeProducts by viewModel.activeProducts.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()

    val expiredCount = activeProducts.count { it.getDaysRemaining() <= 2 }
    val soonCount = activeProducts.count { it.getDaysRemaining() in 3..5 }
    val safeCount = activeProducts.count { it.getDaysRemaining() > 5 }
    val totalCount = activeProducts.size

    val consumedCount = allProducts.count { it.isConsumed }
    val totalEverAdded = allProducts.size

    val savedRatio = if (totalEverAdded > 0) {
        (consumedCount.toFloat() / totalEverAdded.toFloat() * 100).toInt()
    } else 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Chart & Stats Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Expiry Status Board",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (totalCount > 0) {
                        // Custom Canvas progress segments
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 16.dp.toPx()
                                val size = size.minDimension - strokeWidth
                                val topLeft = Offset(
                                    (this.size.width - size) / 2,
                                    (this.size.height - size) / 2
                                )

                                val expiredAngle = (expiredCount.toFloat() / totalCount) * 360f
                                val soonAngle = (soonCount.toFloat() / totalCount) * 360f
                                val safeAngle = (safeCount.toFloat() / totalCount) * 360f

                                var startAngle = -90f

                                if (expiredCount > 0) {
                                    drawArc(
                                        color = ExpiryRed,
                                        startAngle = startAngle,
                                        sweepAngle = expiredAngle,
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = Size(size, size),
                                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                                    )
                                    startAngle += expiredAngle
                                }

                                if (soonCount > 0) {
                                    drawArc(
                                        color = ExpiryYellow,
                                        startAngle = startAngle,
                                        sweepAngle = soonAngle,
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = Size(size, size),
                                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                                    )
                                    startAngle += soonAngle
                                }

                                if (safeCount > 0) {
                                    drawArc(
                                        color = ExpiryGreen,
                                        startAngle = startAngle,
                                        sweepAngle = safeAngle,
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = Size(size, size),
                                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$totalCount",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Total Items",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatIndicator(label = "Critical", count = expiredCount, color = ExpiryRed, modifier = Modifier.weight(1f))
                            StatIndicator(label = "Soon", count = soonCount, color = ExpiryYellow, modifier = Modifier.weight(1f))
                            StatIndicator(label = "Safe", count = safeCount, color = ExpiryGreen, modifier = Modifier.weight(1f))
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No items in pantry",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Eco-Savings Tracker Card (Styled as a Waste Warrior Banner)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = NaturalBannerBg),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Zero-Waste Progress Tracker",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NaturalDarkGreenText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You saved $consumedCount of $totalEverAdded items added ($savedRatio% utilization rate!). Keep saving food to protect the environment!",
                            style = MaterialTheme.typography.bodySmall,
                            color = NaturalOlive,
                            lineHeight = 16.sp
                        )
                    }
                    // Olive green action button from Waste Warrior Tip banner
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NaturalOlive),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Savings,
                            contentDescription = "Savings Progress",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Chronological timeline section header
        item {
            Text(
                text = "Chronological Expiry Timeline",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }

        if (activeProducts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Add grocery products to see your visual expiration timeline here.",
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(activeProducts) { product ->
                TimelineItem(product = product)
            }
        }
    }
}

@Composable
fun TimelineItem(product: Product) {
    val daysRemaining = product.getDaysRemaining()
    val badgeColor = when {
        daysRemaining <= 0 -> ExpiryRed
        daysRemaining <= 5 -> ExpiryYellow
        else -> ExpiryGreen
    }

    val dateStr = remember(product.expiryDate) {
        SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(product.expiryDate))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = product.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when {
                        daysRemaining < 0 -> "Expired"
                        daysRemaining == 0 -> "Today"
                        daysRemaining == 1 -> "Tomorrow"
                        else -> "$daysRemaining days left"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = badgeColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun StatIndicator(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = color,
                    fontSize = 10.sp
                )
            }
            // Thick bottom accent stripe mimicking border-b-4 from the Natural Tones HTML design
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(color)
            )
        }
    }
}

@Composable
fun RecipesTab(viewModel: ExpiryViewModel) {
    val recipeState by viewModel.recipeState.collectAsStateWithLifecycle()
    val activeProducts by viewModel.activeProducts.collectAsStateWithLifecycle()

    // Items that are expiring soon (<= 7 days left)
    val nearingExpiryItems = remember(activeProducts) {
        activeProducts.filter { it.getDaysRemaining() <= 7 }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "AI Zero-Waste Chef",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Don't let your food go to waste! Let Gemini suggest amazing recipes specifically tailored to use your nearing-expiry ingredients.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ingredients Nearing Expiry",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (nearingExpiryItems.isEmpty()) {
                        Text(
                            text = "Hooray! No ingredients are currently expiring soon (<= 7 days). Keep up the great work!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ExpiryGreen,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "These ingredients need to be used quickly:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            nearingExpiryItems.forEach { product ->
                                val days = product.getDaysRemaining()
                                val color = if (days <= 0) ExpiryRed else ExpiryYellow
                                val text = "${product.name} (${if (days <= 0) "Expired" else "${days}d"})"

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(color.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = text,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = color
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.generateRecipes() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_generate_recipes"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Suggest Recipes Using These Items")
                        }
                    }
                }
            }
        }

        // Recipes Status state handling
        when (val state = recipeState) {
            is RecipeUiState.Idle -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Click the button above to cook zero-waste meal recipes!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            is RecipeUiState.Loading -> {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Consulting Chef Gemini...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Creating delicious meals while minimizing waste!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            is RecipeUiState.Success -> {
                if (state.recipes.isEmpty()) {
                    item {
                        Text("No recipes could be generated for your ingredients. Try adding more pantry variety.")
                    }
                } else {
                    item {
                        Text(
                            text = "Chef Suggestions:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(state.recipes) { recipe ->
                        RecipeCard(recipe = recipe)
                    }
                }
            }
            is RecipeUiState.Error -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ExpiryRedLight)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Warning, contentDescription = null, tint = ExpiryRed)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Recipe Generation Failed",
                                    fontWeight = FontWeight.Bold,
                                    color = ExpiryRed
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ExpiryRed
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeCard(recipe: RecipeSuggestion) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("recipe_card_${recipe.name.replace(" ", "_")}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recipe.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Uses: ${recipe.usedIngredients.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ExpiryGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    // Ingredients
                    Text(
                        text = "Ingredients",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    recipe.usedIngredients.forEach { ingredient ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = ExpiryGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "$ingredient (Saved!)", style = MaterialTheme.typography.bodyMedium, color = ExpiryGreen)
                        }
                    }
                    recipe.otherIngredients.forEach { ingredient ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = ingredient, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Steps
                    Text(
                        text = "Instructions",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    recipe.instructions.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "${index + 1}. ",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(text = step, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Waste reduction tip box
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ExpiryYellowLight),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Eco,
                                contentDescription = null,
                                tint = ExpiryYellow,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Eco-Chef Zero Waste Tip",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = recipe.wasteTip,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductDialog(
    product: Product?,
    onDismiss: () -> Unit,
    onSave: (String, String, Long, String, String, String?, Double, Boolean) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var selectedCategory by remember { mutableStateOf(product?.category ?: "Dairy & Eggs") }
    var quantity by remember { mutableStateOf(product?.quantity ?: "") }
    var notes by remember { mutableStateOf(product?.notes ?: "") }
    var imageUrl by remember { mutableStateOf(product?.imageUrl ?: "") }
    var priceText by remember { mutableStateOf(product?.price?.toString() ?: "0.0") }
    var isB2bOnly by remember { mutableStateOf(product?.isB2bOnly ?: false) }

    val categories = listOf("Dairy & Eggs", "Fruits & Vegetables", "Meat & Seafood", "Bakery", "Pantry Staples", "Beverages", "Other")

    // Date Picker state management
    val calendar = Calendar.getInstance()
    if (product != null) {
        calendar.timeInMillis = product.expiryDate
    } else {
        // Default to +3 days from now
        calendar.add(Calendar.DAY_OF_YEAR, 3)
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = calendar.timeInMillis
    )

    var showDatePicker by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val formattedDate = remember(datePickerState.selectedDateMillis) {
        val millis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(millis))
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("add_edit_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (product != null) "Edit Pantry Item" else "Add New Grocery",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name *") },
                    modifier = Modifier.fillMaxWidth().testTag("input_name"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    )
                )

                // Category Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            IconButton(onClick = { categoryDropdownExpanded = !categoryDropdownExpanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Choose Category")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { categoryDropdownExpanded = true }
                            .testTag("input_category")
                    )
                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Expiry Date Chooser
                OutlinedTextField(
                    value = formattedDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Expiration Date *") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Rounded.CalendarToday, contentDescription = "Choose Date")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                        .testTag("input_expiry_date")
                )

                // Quantity Input
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity (e.g. 2 liters, 500g)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_quantity"),
                    singleLine = true,
                    placeholder = { Text("Optional") }
                )

                // Notes Input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Storage details") },
                    modifier = Modifier.fillMaxWidth().testTag("input_notes"),
                    maxLines = 3,
                    placeholder = { Text("e.g. store in vegetable drawer") }
                )

                // Price Input
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Selling Price ($) *") },
                    modifier = Modifier.fillMaxWidth().testTag("input_price"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    placeholder = { Text("e.g. 4.99") }
                )

                // B2B Wholesale Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isB2bOnly = !isB2bOnly }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isB2bOnly,
                        onCheckedChange = { isB2bOnly = it },
                        modifier = Modifier.testTag("checkbox_b2b_only")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Wholesale / B2B Exclusive",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Only restaurants & business clients can purchase this product",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Image URL Input
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Product Image URL") },
                    modifier = Modifier.fillMaxWidth().testTag("input_image_url"),
                    singleLine = true,
                    placeholder = { Text("Enter custom image URL or select a preset below") },
                    trailingIcon = if (imageUrl.isNotEmpty()) {
                        {
                            IconButton(onClick = { imageUrl = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear image URL")
                            }
                        }
                    } else null
                )

                // Image Presets Selector
                Text(
                    text = "Quick Image & Category Presets",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                val presetImages = listOf(
                    Triple("🥛 Milk", "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=500&q=80", "Dairy & Eggs"),
                    Triple("🥚 Eggs", "https://images.unsplash.com/photo-1506976785307-8732e854ad03?w=500&q=80", "Dairy & Eggs"),
                    Triple("🍎 Apples", "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=500&q=80", "Fruits & Vegetables"),
                    Triple("🥦 Broccoli", "https://images.unsplash.com/photo-1584270354949-c26b0d5b4a0c?w=500&q=80", "Fruits & Vegetables"),
                    Triple("🥩 Steak", "https://images.unsplash.com/photo-1607623814075-e51df1bdc82f?w=500&q=80", "Meat & Seafood"),
                    Triple("🐟 Salmon", "https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?w=500&q=80", "Meat & Seafood"),
                    Triple("🍞 Bread", "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=500&q=80", "Bakery"),
                    Triple("🍊 Juice", "https://images.unsplash.com/photo-1621506289937-a8e4df240d0b?w=500&q=80", "Beverages")
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presetImages) { (label, url, cat) ->
                        val isSelected = imageUrl == url
                        SuggestionChip(
                            onClick = {
                                imageUrl = url
                                selectedCategory = cat
                                if (name.isBlank()) {
                                    name = label.substring(2)
                                }
                            },
                            label = { Text(label) },
                            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.secondary) else null
                        )
                    }
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.testTag("btn_cancel")) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(
                                    name,
                                    selectedCategory,
                                    datePickerState.selectedSelectedDate(),
                                    quantity,
                                    notes,
                                    imageUrl.ifBlank { null },
                                    priceText.toDoubleOrNull() ?: 0.0,
                                    isB2bOnly
                                )
                            }
                        },
                        enabled = name.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_save")
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// Ext helper for date fetching
@OptIn(ExperimentalMaterial3Api::class)
fun DatePickerState.selectedSelectedDate(): Long {
    return this.selectedDateMillis ?: System.currentTimeMillis()
}

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

// ============================================================================
// ADDITIONAL COMPOSABLES: LOGIN, PROFILE, AND ORDER & TRANSPORT SERVICE
// ============================================================================

@Composable
fun LoginScreen(viewModel: ExpiryViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.img_woody_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .safeDrawingPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Brand Logo
            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .size(110.dp)
                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground_asset),
                    contentDescription = "WasteNot Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "WasteNot",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = (-1).sp
            )
            Text(
                text = "Save Food. Eat Well. Live Sustainably.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Create Profile / Sign In",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Username Input
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            usernameError = if (it.isBlank()) "Username cannot be empty" else null
                        },
                        label = { Text("Username") },
                        leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        isError = usernameError != null,
                        supportingText = usernameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth().testTag("login_username"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Password Input
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = if (it.length < 4) "Password must be at least 4 characters" else null
                        },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        isError = passwordError != null,
                        supportingText = passwordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth().testTag("login_password"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Email Address Input
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = when {
                                it.isBlank() -> "Email cannot be empty"
                                !android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches() -> "Invalid email address"
                                else -> null
                            }
                        },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        isError = emailError != null,
                        supportingText = emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth().testTag("login_email"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Phone Number Input
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            phoneError = when {
                                it.isBlank() -> "Phone number cannot be empty"
                                it.length < 7 -> "Invalid phone number"
                                else -> null
                            }
                        },
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        isError = phoneError != null,
                        supportingText = phoneError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth().testTag("login_phone"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            val uErr = if (username.isBlank()) "Username cannot be empty" else null
                            val pErr = if (password.length < 4) "Password must be at least 4 characters" else null
                            val eErr = when {
                                email.isBlank() -> "Email cannot be empty"
                                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email address"
                                else -> null
                            }
                            val phErr = when {
                                phone.isBlank() -> "Phone number cannot be empty"
                                phone.length < 7 -> "Invalid phone number"
                                else -> null
                            }

                            usernameError = uErr
                            passwordError = pErr
                            emailError = eErr
                            phoneError = phErr

                            if (uErr == null && pErr == null && eErr == null && phErr == null) {
                                viewModel.login(username, email, phone)
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_submit_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Text(
                            text = "Get Started",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ProfileDialog(viewModel: ExpiryViewModel, onDismiss: () -> Unit) {
    val username by viewModel.username.collectAsStateWithLifecycle()
    val email by viewModel.email.collectAsStateWithLifecycle()
    val phone by viewModel.phoneNumber.collectAsStateWithLifecycle()

    val initials = remember(username) {
        if (username.isBlank()) "UN" else {
            username.trim().split("\\s+".toRegex()).take(2)
                .mapNotNull { if (it.isNotEmpty()) it.first().uppercase() else null }
                .joinToString("")
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "User Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Clear, contentDescription = "Close Profile")
                    }
                }

                // Avatar Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }

                Text(
                    text = username,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Info Rows
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Email Address", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(email, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Phone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Phone Number", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(phone, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        onDismiss()
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_logout")
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Store Items representation for Ordering
data class StoreItem(
    val name: String,
    val category: String,
    val price: Double,
    val defaultQuantityDesc: String,
    val shelfLifeDays: Int
)

val storeItemsList = listOf(
    StoreItem("Organic Whole Milk", "Dairy & Eggs", 3.49, "1 liter", 7),
    StoreItem("Free-Range Eggs", "Dairy & Eggs", 4.99, "12 pack", 14),
    StoreItem("Fresh Broccoli", "Fruits & Vegetables", 2.29, "500g", 6),
    StoreItem("Fresh Red Apples", "Fruits & Vegetables", 1.99, "1 kg", 10),
    StoreItem("Atlantic Salmon Fillet", "Meat & Seafood", 14.99, "350g", 3),
    StoreItem("Sourdough Bread", "Bakery", 4.50, "1 loaf", 5),
    StoreItem("Whole Wheat Pasta", "Pantry Staples", 1.89, "500g", 180),
    StoreItem("Organic Orange Juice", "Beverages", 3.99, "1 liter", 12)
)

data class TransportService(
    val id: Int,
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val cost: Double,
    val speed: String,
    val ecoImpact: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTab(viewModel: ExpiryViewModel) {
    val userPhone by viewModel.phoneNumber.collectAsStateWithLifecycle()
    val username by viewModel.username.collectAsStateWithLifecycle()
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val activeProducts by viewModel.activeProducts.collectAsStateWithLifecycle()
    val clientType by viewModel.businessType.collectAsStateWithLifecycle()

    val orderedQuantities = remember { mutableStateMapOf<String, Int>() }
    var selectedTransportId by remember { mutableStateOf(0) }
    var deliveryAddress by remember { mutableStateOf("123 Biosphere Circle, Green City") }
    var specialInstructions by remember { mutableStateOf("") }

    // Delivery Stage: -1 = Idle, 0 = Processing, 1 = Packaging, 2 = Transit, 3 = Arrived & Stored
    var deliveryStage by remember { mutableStateOf(-1) }

    // Sync from ViewModel cart to local orderedQuantities
    LaunchedEffect(cart, activeProducts) {
        // Clear previous synced database items to prevent duplicates, but keep static store items
        val databaseProductNames = activeProducts.map { it.name }.toSet()
        databaseProductNames.forEach { name ->
            orderedQuantities.remove(name)
        }
        cart.forEach { (prodId, qty) ->
            val prod = activeProducts.find { it.id == prodId }
            if (prod != null) {
                orderedQuantities[prod.name] = qty
            }
        }
    }

    val transportServices = listOf(
        TransportService(
            id = 0,
            name = "Zero-Emission E-Bike",
            icon = Icons.Rounded.DirectionsBike,
            cost = 3.50,
            speed = "Express Same-Day",
            ecoImpact = "100% Carbon-Free 🌿",
            description = "Delivered via localized green bicycle couriers. Minimal packaging."
        ),
        TransportService(
            id = 1,
            name = "Electric Delivery Van",
            icon = Icons.Rounded.ElectricBolt,
            cost = 4.99,
            speed = "1 - 2 Days",
            ecoImpact = "Low Carbon Fleet ⚡",
            description = "Smart routing via eco-friendly electric transport vehicles."
        ),
        TransportService(
            id = 2,
            name = "Green Offset Truck",
            icon = Icons.Rounded.LocalShipping,
            cost = 2.00,
            speed = "2 - 3 Days",
            ecoImpact = "100% Carbon Offsetting 🌍",
            description = "Optimized routing combined with gold-standard carbon offset projects."
        )
    )

    // Calculate Costs across both database and static store items
    val subtotal = orderedQuantities.entries.sumOf { (name, qty) ->
        val price = activeProducts.find { it.name == name }?.price 
            ?: storeItemsList.find { it.name == name }?.price 
            ?: 0.0
        qty * price
    }
    val shippingFee = if (subtotal > 0) transportServices.firstOrNull { it.id == selectedTransportId }?.cost ?: 0.0 else 0.0
    val totalCost = subtotal + shippingFee

    // Handle simulation progress timer
    if (deliveryStage in 0..2) {
        LaunchedEffect(deliveryStage) {
            kotlinx.coroutines.delay(2000)
            deliveryStage += 1
        }
    }

    // Automatically store items in database once delivery succeeds (stage == 3)
    if (deliveryStage == 3) {
        LaunchedEffect(Unit) {
            // Static store items
            storeItemsList.forEach { item ->
                val quantityCount = orderedQuantities[item.name] ?: 0
                if (quantityCount > 0) {
                    val expiryTimestamp = System.currentTimeMillis() + item.shelfLifeDays * 24L * 60 * 60 * 1000
                    viewModel.insertProduct(
                        Product(
                            name = item.name,
                            category = item.category,
                            expiryDate = expiryTimestamp,
                            quantity = "$quantityCount unit(s)",
                            notes = "Delivered on " + SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date()) + " via " + (transportServices.firstOrNull { it.id == selectedTransportId }?.name ?: "Eco Delivery")
                        )
                    )
                }
            }
            // Live pantry products
            activeProducts.forEach { item ->
                val quantityCount = orderedQuantities[item.name] ?: 0
                if (quantityCount > 0) {
                    val expiryTimestamp = item.expiryDate // Keep original expiry date from owner
                    viewModel.insertProduct(
                        Product(
                            name = item.name,
                            category = item.category,
                            expiryDate = expiryTimestamp,
                            quantity = "$quantityCount x ${item.quantity.ifBlank { "unit" }}",
                            notes = "Purchased from Live Pantry on " + SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date()) + " via " + (transportServices.firstOrNull { it.id == selectedTransportId }?.name ?: "Eco Delivery"),
                            imageUrl = item.imageUrl,
                            price = item.price,
                            isB2bOnly = item.isB2bOnly
                        )
                    )
                    
                    // Mark the owner's original product as consumed (since it has been sold!)
                    viewModel.consumeProduct(item)
                }
            }
            // Clear shopping cart
            viewModel.clearCart()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("order_pantry_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Section
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Eco Grocery & Transport",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Order organic, zero-waste products and request fully carbon-neutral transport directly to your home.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalShipping,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        // Contact Section (Call to order & deliver)
        item {
            val context = LocalContext.current
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contact_section_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Phone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Direct Phone Orders",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Call to custom-order and arrange delivery",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Text(
                        text = "Prefer a direct chat? Call me anytime to order fresh, organic products and coordinate personalized, eco-friendly delivery directly to your doorstep.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
                        lineHeight = 20.sp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "OUR CONTACT NUMBER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "0784799711",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Button(
                            onClick = {
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_DIAL,
                                    android.net.Uri.parse("tel:0784799711")
                                )
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("btn_call_contact_phone")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Call,
                                contentDescription = "Call Now",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Call Now",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Section header: Live Farm-Fresh Pantry Stock
        item {
            val liveCount = activeProducts.count { it.price > 0.0 && (!it.isB2bOnly || clientType != "Individual") }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Farm-Fresh Pantry Stock (Live Listings) 🌿",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (liveCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ExpiryGreenLight)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$liveCount AVAILABLE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ExpiryGreen,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }

        // Database Live Products List
        val liveDbProducts = activeProducts.filter { it.price > 0.0 && (!it.isB2bOnly || clientType != "Individual") }
        if (liveDbProducts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "No direct farm-fresh products are listed yet.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Add some in the Pantry section or check out our standard staples below!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(liveDbProducts) { item ->
                val count = orderedQuantities[item.name] ?: 0
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val imageUrl = item.imageUrl?.ifBlank { null } ?: getProductDefaultImage(item.name, item.category)
                            coil.compose.AsyncImage(
                                model = imageUrl,
                                contentDescription = item.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "${item.category}${if (item.quantity.isNotBlank()) " • ${item.quantity}" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (item.isB2bOnly) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("B2B 🏢", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$${String.format(Locale.getDefault(), "%.2f", item.price)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Plus-Minus controller
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                        ) {
                            IconButton(
                                onClick = {
                                    val newQty = count - 1
                                    if (count > 0) {
                                        orderedQuantities[item.name] = newQty
                                        val currentQty = cart[item.id] ?: 0
                                        if (newQty < currentQty) {
                                            viewModel.removeFromCart(item.id)
                                        }
                                    }
                                },
                                modifier = Modifier.size(32.dp).testTag("btn_minus_db_${item.id}")
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                            }

                            Text(
                                text = count.toString(),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.testTag("count_db_${item.id}")
                            )

                            IconButton(
                                onClick = {
                                    val newQty = count + 1
                                    orderedQuantities[item.name] = newQty
                                    viewModel.addToCart(item.id)
                                },
                                modifier = Modifier.size(32.dp).testTag("btn_plus_db_${item.id}")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Section header: Standard Organic Staples
        item {
            Text(
                text = "Organic Farm-Fresh Staples 🍅",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Product Cards
        items(storeItemsList) { item ->
            val count = orderedQuantities[item.name] ?: 0
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${item.category} • ${item.defaultQuantityDesc}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$${String.format(Locale.getDefault(), "%.2f", item.price)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Plus-Minus controller
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                    ) {
                        IconButton(
                            onClick = { if (count > 0) orderedQuantities[item.name] = count - 1 },
                            modifier = Modifier.size(32.dp).testTag("btn_minus_${item.name.replace(" ", "_")}")
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                        }

                        Text(
                            text = count.toString(),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.testTag("count_${item.name.replace(" ", "_")}")
                        )

                        IconButton(
                            onClick = { orderedQuantities[item.name] = count + 1 },
                            modifier = Modifier.size(32.dp).testTag("btn_plus_${item.name.replace(" ", "_")}")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Section header: Transport Services
        item {
            Text(
                text = "Select Transport Service",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Transport Option Cards
        items(transportServices) { service ->
            val isSelected = selectedTransportId == service.id
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedTransportId = service.id }
                    .testTag("transport_service_${service.id}")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = service.icon,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = service.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$${String.format("%.2f", service.cost)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "${service.speed} • Impact: ${service.ecoImpact}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = service.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Section header: Delivery Details
        item {
            Text(
                text = "Delivery Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Address & instructions card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Contact Info Preview (Non-editable for safety confirmation)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recipient: $username ($userPhone)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedTextField(
                        value = deliveryAddress,
                        onValueChange = { deliveryAddress = it },
                        label = { Text("Delivery Address") },
                        leadingIcon = { Icon(Icons.Rounded.Home, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("delivery_address_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = specialInstructions,
                        onValueChange = { specialInstructions = it },
                        label = { Text("Special Instructions (Optional)") },
                        placeholder = { Text("e.g. Ring bell, leave on porch...") },
                        modifier = Modifier.fillMaxWidth().testTag("delivery_notes_input"),
                        singleLine = false,
                        maxLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Invoice Cost Summary Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Order Receipt",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Products Subtotal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$${String.format("%.2f", subtotal)}", fontWeight = FontWeight.Medium)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Eco Delivery Transport", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$${String.format("%.2f", shippingFee)}", fontWeight = FontWeight.Medium)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Carbon Offset contribution", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Rounded.Eco, contentDescription = null, tint = ExpiryGreen, modifier = Modifier.size(16.dp))
                        }
                        Text("FREE", fontWeight = FontWeight.Bold, color = ExpiryGreen)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Price",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "$${String.format("%.2f", totalCost)}",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            if (subtotal > 0 && deliveryAddress.isNotBlank()) {
                                deliveryStage = 0
                            }
                        },
                        enabled = subtotal > 0 && deliveryAddress.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_submit_order")
                    ) {
                        Icon(Icons.Rounded.LocalShipping, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (subtotal > 0) "Place Order & Dispatch Transport" else "Select Items First",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // Just spacing
        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Delivery progress tracker animation overlay
    if (deliveryStage >= 0) {
        val selectedTransport = transportServices.firstOrNull { it.id == selectedTransportId }
        Dialog(onDismissRequest = { if (deliveryStage == 3) deliveryStage = -1 }) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "Eco Dispatch Tracking",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Big Animated Status Circle
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (deliveryStage) {
                                0 -> Icons.Rounded.Analytics
                                1 -> Icons.Rounded.ShoppingBag
                                2 -> selectedTransport?.icon ?: Icons.Rounded.LocalShipping
                                else -> Icons.Rounded.CheckCircle
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(50.dp)
                        )
                    }

                    // Progress stages indicators
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TrackingStepRow(
                            label = "Order Received & Verified",
                            icon = Icons.Rounded.Check,
                            isActive = deliveryStage >= 0,
                            isCompleted = deliveryStage > 0
                        )

                        TrackingStepRow(
                            label = "Packing Local Organic Ingredients",
                            icon = Icons.Rounded.ShoppingBag,
                            isActive = deliveryStage >= 1,
                            isCompleted = deliveryStage > 1
                        )

                        TrackingStepRow(
                            label = "Dispatched: ${selectedTransport?.name ?: "Eco Cargo"}",
                            icon = selectedTransport?.icon ?: Icons.Rounded.LocalShipping,
                            isActive = deliveryStage >= 2,
                            isCompleted = deliveryStage > 2
                        )

                        TrackingStepRow(
                            label = "Arrived & Logged in Pantry",
                            icon = Icons.Rounded.Kitchen,
                            isActive = deliveryStage >= 3,
                            isCompleted = deliveryStage >= 3
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    if (deliveryStage < 3) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = when (deliveryStage) {
                                0 -> "Validating carbon-neutral coordinates..."
                                1 -> "Eco-friendly packing utilizing biodegradable pulp..."
                                else -> "Courier transit. Zero tailpipe emissions achieved!"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Successfully Delivered! 🎉",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = ExpiryGreen
                            )
                            Text(
                                text = "Ordered ingredients have been securely added to your pantry.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            onClick = {
                                // Reset quantities and close dialog
                                orderedQuantities.clear()
                                deliveryStage = -1
                                // Redirect to Pantry tab
                                viewModel.currentTab.value = 0
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Go to Pantry", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrackingStepRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    isCompleted: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> ExpiryGreen.copy(alpha = 0.15f)
                        isActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.Rounded.Check else icon,
                contentDescription = null,
                tint = when {
                    isCompleted -> ExpiryGreen
                    isActive -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                },
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = when {
                isCompleted -> MaterialTheme.colorScheme.onSurface
                isActive -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            }
        )
    }
}

@Composable
fun PantryInsightsDashboard(
    allProducts: List<Product>
) {
    var expanded by remember { mutableStateOf(false) }
    
    val activeCount = allProducts.count { !it.isConsumed }
    val consumedCount = allProducts.count { it.isConsumed }
    val expiredCount = allProducts.count { !it.isConsumed && it.getDaysRemaining() < 0 }
    val soonCount = allProducts.count { !it.isConsumed && it.getDaysRemaining() in 0..5 }
    
    val totalEnded = consumedCount + expiredCount
    val score = if (totalEnded == 0) 100 else ((consumedCount.toFloat() / totalEnded) * 100).toInt()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { expanded = !expanded }
            .testTag("insights_dashboard_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Analytics,
                        contentDescription = "Insights Icon",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Pantry Eco Insights",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (expanded) "Collapse" else "View Eco Score",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = spring()) + fadeIn(),
                exit = shrinkVertically(animationSpec = spring()) + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Left Circular Canvas Ring
                        Box(
                            modifier = Modifier.size(90.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            val progressColor = if (score >= 80) ExpiryGreen else if (score >= 50) ExpiryYellow else ExpiryRed
                            
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Draw Track
                                drawArc(
                                    color = trackColor,
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                                
                                // Draw Progress
                                drawArc(
                                    color = progressColor,
                                    startAngle = -90f,
                                    sweepAngle = (score / 100f) * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$score%",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "Eco Score",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                                )
                            }
                        }
                        
                        // Right Side Stat items
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(start = 12.dp)
                        ) {
                            StatRow(
                                emoji = "📦",
                                label = "In Pantry",
                                value = "$activeCount items",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            StatRow(
                                emoji = "😋",
                                label = "Saved & Eaten",
                                value = "$consumedCount items",
                                tint = ExpiryGreen
                            )
                            StatRow(
                                emoji = "⚠️",
                                label = "Wasted (Expired)",
                                value = "$expiredCount items",
                                tint = ExpiryRed
                            )
                            StatRow(
                                emoji = "⏰",
                                label = "Expiring Soon",
                                value = "$soonCount items",
                                tint = ExpiryYellow
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Sustainability Insight Text
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🌱",
                                fontSize = 18.sp
                            )
                            val message = when {
                                score >= 85 -> "Outstanding job! Your pantry eco-footprint is incredibly low. Keep rescuing fresh ingredients!"
                                score >= 60 -> "Good work! Try using recipe suggestions to rescue your expiring items before they go to waste."
                                else -> "Let's work together! Set reminders and rescue your expiring products to improve your Eco Score."
                            }
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatRow(
    emoji: String,
    label: String,
    value: String,
    tint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = emoji, fontSize = 14.sp)
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = tint
        )
    }
}

