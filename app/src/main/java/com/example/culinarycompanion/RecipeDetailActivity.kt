package com.example.culinarycompanion


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.myrecipeapp.ui.theme.MyRecipeAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RecipeDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val recipeId = intent.getIntExtra("recipeId", -1)
        val recipeDao = AppDatabase.getInstance(this).recipeDao()
        val recipe = recipeDao.getRecipeById(recipeId)

        if (recipeId == -1 || recipe == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            MyRecipeAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RecipeDetailScreen(
                        recipeId = recipeId,
                        onEditClick = {
                            val intent = Intent(this, EditRecipeActivity::class.java)
                            intent.putExtra("recipeId", recipeId)
                            startActivity(intent)
                        },
                        onDeleteConfirmed = {
                            GlobalScope.launch(Dispatchers.IO) {
                                recipeDao.deleteRecipe(recipe)
                            }
                            Toast.makeText(this, "Recipe deleted", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RecipeDetailScreen(
    recipeId: Int,
    onEditClick: () -> Unit,
    onDeleteConfirmed: () -> Unit
) {
    val context = LocalContext.current
    val recipeDao = AppDatabase.getInstance(context).recipeDao()
    var recipe by remember { mutableStateOf(recipeDao.getRecipeById(recipeId)) }
    var showDialog by remember { mutableStateOf(false) }

    // Reload recipe from DB (useful after editing)
    LaunchedEffect(Unit) {
        recipe = recipeDao.getRecipeById(recipeId)
    }

    if (recipe == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Recipe not found", color = Color.Red)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding(),
        verticalArrangement = Arrangement.Top
    ) {
        // Image
        Image(
            painter = recipe!!.imageUri.takeIf { it.isNotEmpty() }?.let {
                rememberAsyncImagePainter(Uri.parse(it))
            } ?: painterResource(id = R.drawable.food),
            contentDescription = "Recipe Image",
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.CenterHorizontally),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Title
        Text(
            text = recipe!!.title,
            style = MaterialTheme.typography.headlineSmall
        )

        // Category
        Text(
            text = "Category: ${recipe!!.category}",
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Ingredients
        Text("Ingredients:", style = MaterialTheme.typography.titleMedium)
        Text(recipe!!.ingredients)

        Spacer(modifier = Modifier.height(12.dp))

        // Instructions
        Text("Instructions:", style = MaterialTheme.typography.titleMedium)
        Text(recipe!!.instructions)

        Spacer(modifier = Modifier.height(24.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = onEditClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
            ) {
                Text("Edit", color = Color.White)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
            ) {
                Text("Delete", color = Color.White)
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete this recipe?") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    onDeleteConfirmed()
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}
