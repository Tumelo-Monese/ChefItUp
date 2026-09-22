package com.chefitup.app.data.local

import com.chefitup.app.domain.model.Ingredient
import com.chefitup.app.domain.model.InstructionStep
import com.chefitup.app.domain.model.NutritionItem
import com.chefitup.app.domain.model.RecipeDetail
import com.chefitup.app.domain.model.RecipeSummary

/**
 * Offline / API-failure fallback catalogue (South Africa–friendly demo recipes).
 */
object MockRecipes {

    val details: List<RecipeDetail> = listOf(
        recipe(
            id = "mock-1",
            title = "Chicken Peri-Peri",
            imageUrl = "https://images.unsplash.com/photo-1598103442097-8b74394b95c6",
            minutes = 45,
            servings = 4,
            difficulty = "Medium",
            rating = 4.7,
            cuisines = listOf("South African", "Portuguese"),
            dishTypes = listOf("main course", "dinner"),
            summary = "Fiery grilled chicken with homemade peri-peri sauce — a braai favourite.",
            ingredients = listOf(
                ing("chicken pieces", 1.2, "kg"),
                ing("red chillies", 6.0, ""),
                ing("garlic cloves", 4.0, ""),
                ing("lemon juice", 60.0, "ml"),
                ing("olive oil", 3.0, "tbsp"),
                ing("paprika", 1.0, "tsp"),
                ing("salt", 1.0, "tsp")
            ),
            steps = listOf(
                "Blend chillies, garlic, lemon, oil, paprika and salt into a marinade.",
                "Coat chicken and marinate for at least 30 minutes.",
                "Grill or braai over medium heat until cooked through, basting often.",
                "Rest 5 minutes and serve with lemon wedges."
            ),
            nutrition = listOf(cal(420.0), protein(38.0), carbs(4.0), fat(28.0))
        ),
        recipe(
            id = "mock-2",
            title = "Bobotie",
            imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c",
            minutes = 70,
            servings = 6,
            difficulty = "Medium",
            rating = 4.8,
            vegetarian = false,
            cuisines = listOf("South African", "Cape Malay"),
            dishTypes = listOf("main course", "dinner"),
            summary = "Spiced minced meat topped with a golden egg custard — Cape Malay classic.",
            ingredients = listOf(
                ing("beef mince", 500.0, "g"),
                ing("onion", 1.0, ""),
                ing("curry powder", 2.0, "tbsp"),
                ing("raisins", 50.0, "g"),
                ing("milk", 250.0, "ml"),
                ing("eggs", 2.0, ""),
                ing("bread slices", 2.0, ""),
                ing("chutney", 2.0, "tbsp")
            ),
            steps = listOf(
                "Soak bread in half the milk. Soften onion in a pan.",
                "Brown mince with curry powder, then stir in raisins, chutney and soaked bread.",
                "Spread mixture in a baking dish.",
                "Beat eggs with remaining milk, pour over, and bake at 180°C until set."
            ),
            nutrition = listOf(cal(380.0), protein(28.0), carbs(22.0), fat(20.0))
        ),
        recipe(
            id = "mock-3",
            title = "Chakalaka & Pap",
            imageUrl = "https://images.unsplash.com/photo-1512621776951-a57141f2eefd",
            minutes = 35,
            servings = 4,
            difficulty = "Easy",
            rating = 4.5,
            vegetarian = true,
            vegan = true,
            glutenFree = true,
            dairyFree = true,
            cuisines = listOf("South African"),
            dishTypes = listOf("side dish", "lunch", "dinner"),
            summary = "Spicy vegetable relish served with creamy maize meal pap.",
            ingredients = listOf(
                ing("maize meal", 250.0, "g"),
                ing("water", 1.0, "l"),
                ing("onion", 1.0, ""),
                ing("carrot", 2.0, ""),
                ing("baked beans", 1.0, "can"),
                ing("tomato", 2.0, ""),
                ing("curry powder", 1.0, "tbsp"),
                ing("chilli", 1.0, "")
            ),
            steps = listOf(
                "Cook pap: whisk maize meal into boiling water and simmer until thick.",
                "Sauté onion, carrot and chilli, then add curry powder.",
                "Stir in tomatoes and baked beans; simmer 10 minutes.",
                "Serve chakalaka over hot pap."
            ),
            nutrition = listOf(cal(310.0), protein(10.0), carbs(58.0), fat(5.0))
        ),
        recipe(
            id = "mock-4",
            title = "Bunny Chow",
            imageUrl = "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38",
            minutes = 50,
            servings = 4,
            difficulty = "Medium",
            rating = 4.6,
            cuisines = listOf("South African", "Indian"),
            dishTypes = listOf("main course", "lunch"),
            summary = "Hollowed loaf filled with fragrant Durban-style curry.",
            ingredients = listOf(
                ing("chicken thighs", 600.0, "g"),
                ing("onion", 1.0, ""),
                ing("curry powder", 2.0, "tbsp"),
                ing("potato", 2.0, ""),
                ing("tomato", 2.0, ""),
                ing("white bread loaf", 1.0, ""),
                ing("garlic", 3.0, "cloves"),
                ing("ginger", 1.0, "tsp")
            ),
            steps = listOf(
                "Brown chicken, then soften onion, garlic and ginger.",
                "Add curry powder, tomatoes and diced potato with a splash of water.",
                "Simmer until chicken is tender and sauce thickens.",
                "Hollow bread quarters, fill with curry and top with the bread lid."
            ),
            nutrition = listOf(cal(510.0), protein(32.0), carbs(48.0), fat(22.0))
        ),
        recipe(
            id = "mock-5",
            title = "Creamy Mushroom Pasta",
            imageUrl = "https://images.unsplash.com/photo-1621996346565-e3dbc646d9a9",
            minutes = 25,
            servings = 3,
            difficulty = "Easy",
            rating = 4.4,
            vegetarian = true,
            cuisines = listOf("Italian"),
            dishTypes = listOf("main course", "dinner"),
            summary = "Weeknight pasta with garlic mushrooms in a silky cream sauce.",
            ingredients = listOf(
                ing("pasta", 300.0, "g"),
                ing("mushrooms", 250.0, "g"),
                ing("cream", 200.0, "ml"),
                ing("garlic cloves", 3.0, ""),
                ing("parmesan", 40.0, "g"),
                ing("butter", 1.0, "tbsp"),
                ing("parsley", 2.0, "tbsp")
            ),
            steps = listOf(
                "Cook pasta in salted water until al dente; reserve some pasta water.",
                "Sauté mushrooms and garlic in butter until golden.",
                "Pour in cream, simmer briefly, then toss with pasta and parmesan.",
                "Loosen with pasta water if needed and finish with parsley."
            ),
            nutrition = listOf(cal(560.0), protein(18.0), carbs(62.0), fat(26.0))
        ),
        recipe(
            id = "mock-6",
            title = "Avocado Toast with Eggs",
            imageUrl = "https://images.unsplash.com/photo-1525351484163-7529414344d8",
            minutes = 15,
            servings = 2,
            difficulty = "Easy",
            rating = 4.3,
            vegetarian = true,
            glutenFree = false,
            cuisines = listOf("American"),
            dishTypes = listOf("breakfast", "brunch"),
            summary = "Crispy toast topped with smashed avocado and soft eggs.",
            ingredients = listOf(
                ing("sourdough slices", 2.0, ""),
                ing("avocado", 1.0, ""),
                ing("eggs", 2.0, ""),
                ing("lemon juice", 1.0, "tsp"),
                ing("chilli flakes", 0.5, "tsp"),
                ing("salt", 0.5, "tsp"),
                ing("olive oil", 1.0, "tsp")
            ),
            steps = listOf(
                "Toast the bread until golden.",
                "Mash avocado with lemon, salt and chilli flakes.",
                "Fry or poach eggs to your liking.",
                "Spread avocado on toast, top with eggs and a drizzle of oil."
            ),
            nutrition = listOf(cal(340.0), protein(14.0), carbs(28.0), fat(20.0))
        ),
        recipe(
            id = "mock-7",
            title = "Beef Potjie",
            imageUrl = "https://images.unsplash.com/photo-1467003909585-2f8a72700288",
            minutes = 120,
            servings = 6,
            difficulty = "Hard",
            rating = 4.9,
            cuisines = listOf("South African"),
            dishTypes = listOf("main course", "dinner"),
            summary = "Slow-cooked layered stew in a cast-iron potjie — perfect for weekends.",
            ingredients = listOf(
                ing("beef shin", 1.0, "kg"),
                ing("onion", 2.0, ""),
                ing("carrot", 3.0, ""),
                ing("potato", 4.0, ""),
                ing("beef stock", 500.0, "ml"),
                ing("tomato paste", 2.0, "tbsp"),
                ing("bay leaves", 2.0, ""),
                ing("thyme", 1.0, "tsp")
            ),
            steps = listOf(
                "Brown beef in the potjie over coals or a low flame.",
                "Layer onions, carrots and potatoes; do not stir.",
                "Add stock, tomato paste and herbs. Cover tightly.",
                "Simmer gently 1.5–2 hours until meat is falling apart."
            ),
            nutrition = listOf(cal(450.0), protein(40.0), carbs(30.0), fat(18.0))
        ),
        recipe(
            id = "mock-8",
            title = "Malva Pudding",
            imageUrl = "https://images.unsplash.com/photo-1488477181946-6428a0291777",
            minutes = 55,
            servings = 8,
            difficulty = "Medium",
            rating = 4.8,
            vegetarian = true,
            cuisines = listOf("South African"),
            dishTypes = listOf("dessert"),
            summary = "Sticky apricot sponge soaked in warm cream sauce.",
            ingredients = listOf(
                ing("flour", 250.0, "g"),
                ing("sugar", 200.0, "g"),
                ing("eggs", 2.0, ""),
                ing("apricot jam", 2.0, "tbsp"),
                ing("milk", 200.0, "ml"),
                ing("butter", 100.0, "g"),
                ing("cream", 250.0, "ml"),
                ing("bicarbonate of soda", 1.0, "tsp")
            ),
            steps = listOf(
                "Cream butter and sugar, beat in eggs and jam.",
                "Fold in flour, bicarb and milk; bake at 180°C until golden.",
                "Heat cream, butter and sugar for the sauce.",
                "Pour hot sauce over the warm pudding and rest before serving."
            ),
            nutrition = listOf(cal(390.0), protein(5.0), carbs(52.0), fat(18.0))
        ),
        recipe(
            id = "mock-9",
            title = "Vegetable Stir-Fry Rice",
            imageUrl = "https://images.unsplash.com/photo-1603133872878-684f208fb84b",
            minutes = 20,
            servings = 3,
            difficulty = "Easy",
            rating = 4.2,
            vegetarian = true,
            vegan = true,
            dairyFree = true,
            cuisines = listOf("Asian"),
            dishTypes = listOf("main course", "lunch"),
            summary = "Quick wok rice with colourful veggies — great for leftover rice.",
            ingredients = listOf(
                ing("cooked rice", 400.0, "g"),
                ing("mixed vegetables", 300.0, "g"),
                ing("soy sauce", 3.0, "tbsp"),
                ing("garlic", 2.0, "cloves"),
                ing("ginger", 1.0, "tsp"),
                ing("sesame oil", 1.0, "tsp"),
                ing("spring onion", 2.0, "")
            ),
            steps = listOf(
                "Heat oil in a wok; stir-fry garlic and ginger briefly.",
                "Add vegetables and cook until crisp-tender.",
                "Add rice and soy sauce; toss on high heat.",
                "Finish with sesame oil and spring onion."
            ),
            nutrition = listOf(cal(320.0), protein(8.0), carbs(58.0), fat(6.0))
        )
    )

    val summaries: List<RecipeSummary> = details.map { it.toSummary() }

    fun detailById(id: String): RecipeDetail? = details.firstOrNull { it.id == id }

    fun search(query: String): List<RecipeSummary> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return summaries
        return details.filter { detail ->
            detail.title.lowercase().contains(q) ||
                detail.cuisines.any { it.lowercase().contains(q) } ||
                detail.dishTypes.any { it.lowercase().contains(q) } ||
                detail.ingredients.any { it.name.lowercase().contains(q) }
        }.map { it.toSummary() }
    }

    fun byIngredients(ingredients: List<String>): List<RecipeSummary> {
        val wanted = ingredients.map { it.trim().lowercase() }.filter { it.isNotEmpty() }
        if (wanted.isEmpty()) return summaries
        return details.map { detail ->
            val names = detail.ingredients.map { it.name.lowercase() }
            val used = wanted.count { w -> names.any { it.contains(w) || w.contains(it) } }
            val missed = (detail.ingredients.size - used).coerceAtLeast(0)
            detail.toSummary().copy(
                usedIngredientCount = used,
                missedIngredientCount = missed,
                matchedIngredientsLabel = "$used of ${detail.ingredients.size} ingredients"
            )
        }.sortedByDescending { it.usedIngredientCount ?: 0 }
    }

    fun random(number: Int): List<RecipeSummary> =
        summaries.shuffled().take(number.coerceAtLeast(1))

    private fun recipe(
        id: String,
        title: String,
        imageUrl: String,
        minutes: Int,
        servings: Int,
        difficulty: String,
        rating: Double,
        vegetarian: Boolean = false,
        vegan: Boolean = false,
        glutenFree: Boolean = false,
        dairyFree: Boolean = false,
        cuisines: List<String>,
        dishTypes: List<String>,
        summary: String,
        ingredients: List<Ingredient>,
        steps: List<String>,
        nutrition: List<NutritionItem>
    ): RecipeDetail = RecipeDetail(
        id = id,
        title = title,
        imageUrl = imageUrl,
        readyInMinutes = minutes,
        servings = servings,
        difficulty = difficulty,
        rating = rating,
        vegetarian = vegetarian,
        vegan = vegan,
        glutenFree = glutenFree,
        dairyFree = dairyFree,
        cuisines = cuisines,
        dishTypes = dishTypes,
        summary = summary,
        ingredients = ingredients,
        instructions = steps.mapIndexed { index, step -> InstructionStep(index + 1, step) },
        nutrition = nutrition
    )

    private fun ing(name: String, amount: Double, unit: String) = Ingredient(
        name = name,
        amount = amount,
        unit = unit,
        original = listOf(
            if (amount % 1.0 == 0.0) amount.toInt().toString() else amount.toString(),
            unit,
            name
        ).filter { it.isNotBlank() }.joinToString(" ")
    )

    private fun cal(v: Double) = NutritionItem("Calories", v, "kcal")
    private fun protein(v: Double) = NutritionItem("Protein", v, "g")
    private fun carbs(v: Double) = NutritionItem("Carbohydrates", v, "g")
    private fun fat(v: Double) = NutritionItem("Fat", v, "g")
}
