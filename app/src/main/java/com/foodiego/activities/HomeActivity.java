package com.foodiego.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.foodiego.R;
import com.foodiego.adapters.CategoryAdapter;
import com.foodiego.adapters.FoodAdapter;
import com.foodiego.databinding.ActivityHomeBinding;
import com.foodiego.models.Food;

import java.util.ArrayList;
import java.util.List;

/**
 * Home Screen Dashboard Activity.
 * Displays custom toolbar, horizontal categories list, and a vertical list of popular food items loaded from a dummy repository.
 */
public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupCategoriesRecyclerView();
        setupPopularFoodsRecyclerView();
    }

    private void setupToolbar() {
        // Toolbar Profile Action Click Handler
        binding.imgToolbarProfile.setOnClickListener(v -> 
            Toast.makeText(this, "Profile Settings - Coming Soon!", Toast.LENGTH_SHORT).show()
        );

        // Logo Click Handler
        binding.imgToolbarLogo.setOnClickListener(v -> 
            Toast.makeText(this, "FoodieGo - Your Food Partner", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupCategoriesRecyclerView() {
        List<CategoryAdapter.Category> categories = new ArrayList<>();
        categories.add(new CategoryAdapter.Category("Pizza", R.drawable.ic_pizza));
        categories.add(new CategoryAdapter.Category("Burger", R.drawable.ic_burger));
        categories.add(new CategoryAdapter.Category("Pasta", R.drawable.ic_pasta));
        categories.add(new CategoryAdapter.Category("Sandwich", R.drawable.ic_sandwich));
        categories.add(new CategoryAdapter.Category("Drinks", R.drawable.ic_drinks));
        categories.add(new CategoryAdapter.Category("Dessert", R.drawable.ic_dessert));

        CategoryAdapter categoryAdapter = new CategoryAdapter(categories, category -> 
            Toast.makeText(this, "Selected Category: " + category.getName(), Toast.LENGTH_SHORT).show()
        );

        binding.rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvCategories.setAdapter(categoryAdapter);
    }

    private void setupPopularFoodsRecyclerView() {
        List<Food> popularFoods = new ArrayList<>();
        
        // 10 Detailed Premium Dummy Food Items
        popularFoods.add(new Food(
                "Pizza Margherita",
                "Classic mozzarella, fresh basil, olive oil, and house marinara sauce.",
                "https://images.unsplash.com/photo-1604382355076-af4b0eb60143?q=80&w=600&auto=format&fit=crop",
                "₹199"
        ));
        popularFoods.add(new Food(
                "Cheese Burger",
                "Double-layered prime beef patty, cheddar cheese, fresh veggies, and secret house sauce.",
                "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?q=80&w=600&auto=format&fit=crop",
                "₹149"
        ));
        popularFoods.add(new Food(
                "White Sauce Pasta",
                "Penne pasta tossed in rich, creamy parmesan cheese sauce with sauteed garlic mushrooms.",
                "https://images.unsplash.com/photo-1645112411341-6c4fd023714a?q=80&w=600&auto=format&fit=crop",
                "₹249"
        ));
        popularFoods.add(new Food(
                "Veggie Club Sandwich",
                "Triple decker toasted sandwich loaded with fresh cucumber, tomatoes, lettuce, and premium cheddar.",
                "https://images.unsplash.com/photo-1525351484163-7529414344d8?q=80&w=600&auto=format&fit=crop",
                "₹119"
        ));
        popularFoods.add(new Food(
                "Caramel Macchiato",
                "Rich espresso combined with milk and sweet vanilla syrup, finished with a caramel drizzle.",
                "https://images.unsplash.com/photo-1572286258217-40142c1c6a70?q=80&w=600&auto=format&fit=crop",
                "₹179"
        ));
        popularFoods.add(new Food(
                "Double Chocolate Muffin",
                "Rich, moist chocolate muffin filled with premium Belgian dark chocolate chips.",
                "https://images.unsplash.com/photo-1607958996333-41aef7caefaa?q=80&w=600&auto=format&fit=crop",
                "₹99"
        ));
        popularFoods.add(new Food(
                "Paneer Tikka Wrap",
                "Grilled spicy cottage cheese cubes wrapped in a soft tortilla wrap with crisp onions and mint mayo.",
                "https://images.unsplash.com/photo-1626700051175-6518c4793f4f?q=80&w=600&auto=format&fit=crop",
                "₹159"
        ));
        popularFoods.add(new Food(
                "Garlic Bread with Cheese",
                "Crispy freshly baked artisanal baguette slices topped with garlic herb butter and melted mozzarella.",
                "https://images.unsplash.com/photo-1573140247632-f8fd74997d5c?q=80&w=600&auto=format&fit=crop",
                "₹129"
        ));
        popularFoods.add(new Food(
                "Chocolate Lava Cake",
                "Hot chocolate soufflé cake with a rich and gooey molten Belgian chocolate center.",
                "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?q=80&w=600&auto=format&fit=crop",
                "₹139"
        ));
        popularFoods.add(new Food(
                "Tropical Mango Smoothie",
                "A creamy and refreshing tropical blend of sweet Alphonso mangoes, fresh banana, and Greek yogurt.",
                "https://images.unsplash.com/photo-1553530666-ba11a7da3888?q=80&w=600&auto=format&fit=crop",
                "₹149"
        ));

        FoodAdapter foodAdapter = new FoodAdapter(popularFoods);
        binding.rvPopularFoods.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPopularFoods.setAdapter(foodAdapter);
    }
}
