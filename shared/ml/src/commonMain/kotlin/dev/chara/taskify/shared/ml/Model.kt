package dev.chara.taskify.shared.ml

enum class Model(val classes: List<String>) {
    ClassifierGrocery(
        listOf(
            "Fruits",
            "Vegetables",
            "Dairy",
            "Bread & Bakery",
            "Meat & Fish",
            "Meat Alternatives",
            "Canned Goods",
            "Pasta, Rice, & Cereals",
            "Condiments & Sauces",
            "Herbs & Spices",
            "Frozen Foods",
            "Snacks",
            "Drinks",
            "Household & Cleaning Supplies",
            "Personal Care",
            "Pet Care"
        )
    )
}