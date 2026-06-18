package com.example.printxpress;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    LinearLayout cardCart, cardSupport, cardGuidelines, cardAbout, categoriesContainer, featuredProductsContainer;
    EditText etSearch;
    TextView tvPromotionBanner, tvCustomerMiniInfo, tvWelcome;
    ImageView imgProfile;

    FirebaseAuth auth;
    FirebaseFirestore db;

    private final List<CategoryData> allCategories = new ArrayList<>();
    private final List<ProductSearchData> allProducts = new ArrayList<>();

    private String customerName = "Customer";
    private String customerEmail = "";
    private String customerPhone = "";

    private static class CategoryData {
        String categoryName;
        String description;

        CategoryData(String categoryName, String description) {
            this.categoryName = categoryName;
            this.description = description;
        }
    }

    private static class ProductSearchData {
        String productName;
        String description;
        String categoryName;
        String material;
        String size;

        ProductSearchData(String productName, String description, String categoryName, String material, String size) {
            this.productName = productName;
            this.description = description;
            this.categoryName = categoryName;
            this.material = material;
            this.size = size;
        }
    }

    private static class FeaturedProductData {
        String productName;
        String description;
        String categoryName;
        String material;
        String size;
        int basePrice;

        FeaturedProductData(String productName, String description, String categoryName, String material, String size, int basePrice) {
            this.productName = productName;
            this.description = description;
            this.categoryName = categoryName;
            this.material = material;
            this.size = size;
            this.basePrice = basePrice;
        }
    }

    private void addDefaultCategoriesIfNeeded() {
        addCategoryIfMissing("Business Cards", "Professional cards for businesses and individuals");
        addCategoryIfMissing("Flyers", "Promotional flyers for events, offers and campaigns");
        addCategoryIfMissing("Posters", "High quality posters for advertising and announcements");
        addCategoryIfMissing("Banners", "Large format banners for shops, events and promotions");
        addCategoryIfMissing("T-Shirts", "Custom printed T-shirts for personal and business use");
        addCategoryIfMissing("Mugs", "Personalized mug printing for gift and branding");
        addCategoryIfMissing("Stickers", "Custom stickers for packing, labels and decoration");
        addCategoryIfMissing("Invitations", "Wedding, birthday and event invitation card printing");
        addCategoryIfMissing("Brochures", "Folded brochures for company and product information");
        addCategoryIfMissing("Certificates", "Professional certificate printing for event and courses");
        addCategoryIfMissing("ID Cards", "Staff, student and membership ID card printing");
        addCategoryIfMissing("Letterheads", "Branded letterheads for official business communication");
        addCategoryIfMissing("Envelopes", "Custom printed envelopes for business communication");
        addCategoryIfMissing("Calendars", "Personalized wall and desk calendar printing");
        addCategoryIfMissing("Booklets", "Multi-page booklet printing for reports and guides");
    }

    private void addCategoryIfMissing(String categoryName, String description) {
        String newName = normalize(categoryName);

        for (CategoryData category : allCategories) {
            if (normalize(category.categoryName).equals(newName)) {
                return;
            }
        }

        allCategories.add(new CategoryData(categoryName, description));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        cardCart = findViewById(R.id.cardCart);
        cardSupport = findViewById(R.id.cardSupport);
        cardGuidelines = findViewById(R.id.cardGuidelines);
        cardAbout = findViewById(R.id.cardAbout);
        categoriesContainer = findViewById(R.id.categoriesContainer);
        featuredProductsContainer = findViewById(R.id.featuredProductsContainer);
        etSearch = findViewById(R.id.etSearch);
        tvPromotionBanner = findViewById(R.id.tvPromotionBanner);
        tvCustomerMiniInfo = findViewById(R.id.tvCustomerMiniInfo);
        tvWelcome = findViewById(R.id.tvWelcome);
        imgProfile = findViewById(R.id.imgProfile);

        setupSearch();
        loadCustomerProfile();
        loadCategoriesFromFirestore();
        loadProductsForSearch();
        loadFeaturedProducts();
        loadLatestPromotion();

        cardCart.setOnClickListener(v -> openScreen(CartActivity.class));
        cardSupport.setOnClickListener(v -> openScreen(CustomerSupportActivity.class));
        cardGuidelines.setOnClickListener(v -> openScreen(GuidelinesActivity.class));
        cardAbout.setOnClickListener(v -> openScreen(AboutActivity.class));
        imgProfile.setOnClickListener(v -> showCustomerProfileDialog());
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCategories(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void openScreen(Class<?> activityClass) {
        startActivity(new Intent(HomeActivity.this, activityClass));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void loadCustomerProfile() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        customerEmail = currentUser.getEmail() == null ? "" : currentUser.getEmail();

        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        String email = documentSnapshot.getString("email");
                        String phone = documentSnapshot.getString("phone");

                        if (fullName != null && !fullName.trim().isEmpty()) {
                            customerName = fullName.trim();
                        }

                        if (email != null && !email.trim().isEmpty()) {
                            customerEmail = email.trim();
                        }

                        if (phone != null && !phone.trim().isEmpty()) {
                            customerPhone = phone.trim();
                        }
                    }

                    updateProfileMiniText();
                })
                .addOnFailureListener(e -> updateProfileMiniText());
    }

    private void updateProfileMiniText() {
        tvWelcome.setText("Welcome,");

        if (customerName == null || customerName.trim().isEmpty() || customerName.equals("Customer")) {
            tvCustomerMiniInfo.setText("PrintXpress Customer");
        } else {
            tvCustomerMiniInfo.setText(customerName);
        }
    }


    private TextView createProfileRow(String label, String value) {
        TextView row = new TextView(this);
        row.setText(label + "\n" + value);
        row.setTextSize(15);
        row.setTextColor(0xFF1F2937);
        row.setLineSpacing(2, 1.0f);
        row.setPadding(dp(14), dp(10), dp(14), dp(10));
        row.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_search_box));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(10), 0, 0);
        row.setLayoutParams(params);

        return row;
    }

    private void showCustomerProfileDialog() {
        String name = customerName == null || customerName.trim().isEmpty() ? "Customer" : customerName;
        String email = customerEmail == null || customerEmail.trim().isEmpty() ? "Not available" : customerEmail;
        String phone = customerPhone == null || customerPhone.trim().isEmpty() ? "Not available" : customerPhone;

        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(dp(22), dp(20), dp(22), dp(16));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        ImageView profileIcon = new ImageView(this);
        profileIcon.setImageResource(R.drawable.ic_profile_user);
        profileIcon.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_icon_box));
        profileIcon.setPadding(dp(12), dp(12), dp(12), dp(12));
        header.addView(profileIcon, new LinearLayout.LayoutParams(dp(58), dp(58)));

        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams titleBoxParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        titleBoxParams.setMargins(dp(14), 0, 0, 0);

        TextView title = new TextView(this);
        title.setText("Customer Profile");
        title.setTextSize(22);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(0xFF123E91);

        TextView subtitle = new TextView(this);
        subtitle.setText("Logged in account details");
        subtitle.setTextSize(13);
        subtitle.setTextColor(0xFF6B7280);
        subtitle.setPadding(0, dp(3), 0, 0);

        titleBox.addView(title);
        titleBox.addView(subtitle);
        header.addView(titleBox, titleBoxParams);

        dialogLayout.addView(header);
        dialogLayout.addView(createProfileRow("Name", name));
        dialogLayout.addView(createProfileRow("Email", email));
        dialogLayout.addView(createProfileRow("Mobile Number", phone));

        LinearLayout actionRow = new LinearLayout(this);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);
        actionRow.setGravity(Gravity.CENTER_VERTICAL);
        actionRow.setPadding(0, dp(18), 0, 0);

        Button btnLogoutDialog = new Button(this);
        btnLogoutDialog.setText("Logout");
        btnLogoutDialog.setAllCaps(false);
        btnLogoutDialog.setTextColor(0xFFFFFFFF);
        btnLogoutDialog.setTypeface(null, Typeface.BOLD);
        btnLogoutDialog.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_danger_button));

        Button btnCloseDialog = new Button(this);
        btnCloseDialog.setText("Close");
        btnCloseDialog.setAllCaps(false);
        btnCloseDialog.setTextColor(0xFF123E91);
        btnCloseDialog.setTypeface(null, Typeface.BOLD);
        btnCloseDialog.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_link_button));

        LinearLayout.LayoutParams logoutParams = new LinearLayout.LayoutParams(0, dp(52), 1f);
        logoutParams.setMargins(0, 0, dp(8), 0);

        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(0, dp(52), 1f);
        closeParams.setMargins(dp(8), 0, 0, 0);

        actionRow.addView(btnLogoutDialog, logoutParams);
        actionRow.addView(btnCloseDialog, closeParams);
        dialogLayout.addView(actionRow);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogLayout)
                .create();

        btnCloseDialog.setOnClickListener(v -> dialog.dismiss());

        btnLogoutDialog.setOnClickListener(v -> {
            dialog.dismiss();
            auth.signOut();
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }

    private void loadLatestPromotion() {
        db.collection("promotions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        tvPromotionBanner.setText("Up to 20% OFF\nPersonalized printing offers\nShop now with PrintXpress");
                        return;
                    }

                    QueryDocumentSnapshot latestDocument = null;
                    long latestCreatedAt = Long.MIN_VALUE;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Long createdAtLong = document.getLong("createdAt");
                        long createdAt = createdAtLong == null ? 0 : createdAtLong;

                        if (latestDocument == null || createdAt > latestCreatedAt) {
                            latestDocument = document;
                            latestCreatedAt = createdAt;
                        }
                    }

                    if (latestDocument == null) {
                        tvPromotionBanner.setText("Up to 20% OFF\nPersonalized printing offers\nShop now with PrintXpress");
                        return;
                    }

                    String title = latestDocument.getString("title");
                    String description = latestDocument.getString("description");
                    String validUntil = latestDocument.getString("validUntil");

                    Long discountLong = latestDocument.getLong("discount");
                    int discount = discountLong == null ? 0 : discountLong.intValue();

                    if (title == null || title.isEmpty()) title = "Special Offer";
                    if (description == null || description.isEmpty()) description = "Printing discount available";
                    if (validUntil == null || validUntil.isEmpty()) validUntil = "Limited time";

                    tvPromotionBanner.setText(title + "\n" + discount + "% OFF - " + description + "\nValid until: " + validUntil);
                })
                .addOnFailureListener(e -> tvPromotionBanner.setText("Special Offer\nCheck our latest printing offers"));
    }

    private void loadCategoriesFromFirestore() {
        categoriesContainer.removeAllViews();
        categoriesContainer.addView(createMessageText("Loading categories..."));

        allCategories.clear();
        addDefaultCategoriesIfNeeded();
        Collections.sort(allCategories, (c1, c2) -> getCategoryOrder(c1.categoryName) - getCategoryOrder(c2.categoryName));
        filterCategories(etSearch.getText().toString());

        db.collection("categories")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String categoryName = document.getString("categoryName");
                            String description = document.getString("description");

                            if (categoryName == null || categoryName.trim().isEmpty()) {
                                continue;
                            }

                            if (description == null || description.trim().isEmpty()) {
                                description = "View printing products";
                            }

                            addCategoryIfMissing(categoryName.trim(), description.trim());
                        }
                    }

                    Collections.sort(allCategories, (c1, c2) -> getCategoryOrder(c1.categoryName) - getCategoryOrder(c2.categoryName));
                    filterCategories(etSearch.getText().toString());
                })
                .addOnFailureListener(e -> {
                    Collections.sort(allCategories, (c1, c2) -> getCategoryOrder(c1.categoryName) - getCategoryOrder(c2.categoryName));
                    filterCategories(etSearch.getText().toString());
                    Toast.makeText(this, "Using default categories", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadProductsForSearch() {
        allProducts.clear();

        db.collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allProducts.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String productName = document.getString("productName");
                        String description = document.getString("description");
                        String categoryName = document.getString("categoryName");
                        String material = document.getString("material");
                        String size = document.getString("size");

                        if (productName == null) productName = "";
                        if (description == null) description = "";
                        if (categoryName == null) categoryName = "";
                        if (material == null) material = "";
                        if (size == null) size = "";

                        allProducts.add(new ProductSearchData(
                                productName.trim(),
                                description.trim(),
                                categoryName.trim(),
                                material.trim(),
                                size.trim()
                        ));
                    }

                    filterCategories(etSearch.getText().toString());
                });
    }


    private void filterCategories(String query) {
        if (allCategories.isEmpty()) {
            addDefaultCategoriesIfNeeded();
        }

        String searchText = normalize(query);
        List<CategoryData> filteredList = new ArrayList<>();

        if (searchText.isEmpty()) {
            filteredList.addAll(allCategories);
        } else {
            for (CategoryData category : allCategories) {
                if (matchesCategoryName(category.categoryName, searchText)) {
                    filteredList.add(category);
                }
            }
        }

        categoriesContainer.removeAllViews();

        if (filteredList.isEmpty()) {
            categoriesContainer.addView(createMessageText("No category found for " + query + "."));
            return;
        }

        addCategoryGrid(filteredList);
    }

    private boolean matchesCategoryName(String categoryName, String searchText) {
        String category = normalize(categoryName);
        String query = normalize(searchText);

        if (category.isEmpty() || query.isEmpty()) {
            return false;
        }

        if (category.contains(query) || query.contains(category)) {
            return true;
        }

        for (String alias : getCategoryAliases(categoryName)) {
            String normalizedAlias = normalize(alias);
            if (normalizedAlias.contains(query) || query.contains(normalizedAlias)) {
                return true;
            }

            if (isCloseMatch(normalizedAlias, query)) {
                return true;
            }
        }

        return isCloseMatch(category, query);
    }

    private String[] getCategoryAliases(String categoryName) {
        String category = normalize(categoryName);

        if (category.contains("business")) return new String[]{"business", "businesscard", "businesscards", "card", "cards"};
        if (category.contains("flyer")) return new String[]{"flyer", "flyers", "leaflet", "leaflets"};
        if (category.contains("poster")) return new String[]{"poster", "posters"};
        if (category.contains("banner")) return new String[]{"banner", "banners", "baner", "baners"};
        if (category.contains("tshirt") || category.contains("shirt")) return new String[]{"tshirt", "tshirts", "tshirt", "shirt", "shirts", "t shirt", "t shirts"};
        if (category.contains("mug")) return new String[]{"mug", "mugs", "cup", "cups"};
        if (category.contains("sticker")) return new String[]{"sticker", "stickers", "stiker", "stikers"};
        if (category.contains("invitation")) return new String[]{"invitation", "invitations", "invite", "invites", "invitationcard"};
        if (category.contains("brochure")) return new String[]{"brochure", "brochures", "brocher", "brochers"};
        if (category.contains("certificate")) return new String[]{"certificate", "certificates", "cert", "certs"};
        if (category.contains("idcard")) return new String[]{"id", "idcard", "idcards", "identity", "identitycard"};
        if (category.contains("letterhead")) return new String[]{"letterhead", "letterheads", "letter", "letters"};
        if (category.contains("envelope")) return new String[]{"envelope", "envelopes", "envelop", "envelops", "cover", "covers"};
        if (category.contains("calendar")) return new String[]{"calendar", "calendars", "calender", "calenders"};
        if (category.contains("booklet")) return new String[]{"booklet", "booklets", "book", "books"};

        return new String[]{categoryName};
    }

    private boolean isCloseMatch(String first, String second) {
        first = normalize(first);
        second = normalize(second);

        if (first.isEmpty() || second.isEmpty()) {
            return false;
        }

        String singularFirst = singularize(first);
        String singularSecond = singularize(second);

        if (singularFirst.contains(singularSecond) || singularSecond.contains(singularFirst)) {
            return true;
        }

        int maxLength = Math.max(singularFirst.length(), singularSecond.length());
        int allowedMistakes = maxLength <= 5 ? 1 : 2;

        return getEditDistance(singularFirst, singularSecond) <= allowedMistakes;
    }

    private String singularize(String value) {
        if (value == null) return "";

        String text = normalize(value);

        if (text.endsWith("ies") && text.length() > 3) {
            return text.substring(0, text.length() - 3) + "y";
        }

        if (text.endsWith("sses") && text.length() > 4) {
            return text.substring(0, text.length() - 2);
        }

        if (text.endsWith("es") && text.length() > 3) {
            String withoutEs = text.substring(0, text.length() - 2);
            if (withoutEs.endsWith("ch") || withoutEs.endsWith("sh") || withoutEs.endsWith("x") || withoutEs.endsWith("s")) {
                return withoutEs;
            }
        }

        if (text.endsWith("s") && text.length() > 1) {
            return text.substring(0, text.length() - 1);
        }

        return text;
    }

    private int getEditDistance(String first, String second) {
        if (first == null) first = "";
        if (second == null) second = "";

        int[][] dpTable = new int[first.length() + 1][second.length() + 1];

        for (int i = 0; i <= first.length(); i++) {
            dpTable[i][0] = i;
        }

        for (int j = 0; j <= second.length(); j++) {
            dpTable[0][j] = j;
        }

        for (int i = 1; i <= first.length(); i++) {
            for (int j = 1; j <= second.length(); j++) {
                int cost = first.charAt(i - 1) == second.charAt(j - 1) ? 0 : 1;

                dpTable[i][j] = Math.min(
                        Math.min(dpTable[i - 1][j] + 1, dpTable[i][j - 1] + 1),
                        dpTable[i - 1][j - 1] + cost
                );
            }
        }

        return dpTable[first.length()][second.length()];
    }

    private String normalize(String value) {
        if (value == null) return "";

        return value.toLowerCase()
                .replace("&", "and")
                .replaceAll("[^a-z0-9]", "")
                .trim();
    }

    private TextView createMessageText(String message) {
        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextSize(15);
        textView.setTextColor(0xFF1F2937);
        textView.setPadding(16, 16, 16, 16);
        textView.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_white_card));
        return textView;
    }

    private void addCategoryGrid(List<CategoryData> categoryList) {
        categoriesContainer.removeAllViews();

        for (CategoryData category : categoryList) {
            View card = createCategoryTile(category.categoryName, category.description);

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    dp(98),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, dp(14), 0);
            categoriesContainer.addView(card, cardParams);
        }
    }

    private View createCategoryTile(String categoryName, String description) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setClickable(true);
        card.setPadding(0, 0, 0, 0);

        ImageView image = new ImageView(this);
        image.setImageResource(getCategoryImageResource(categoryName));
        image.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_circle_light));
        image.setPadding(dp(8), dp(8), dp(8), dp(8));
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        card.addView(image, new LinearLayout.LayoutParams(dp(74), dp(74)));

        TextView title = new TextView(this);
        title.setText(makeCategoryTitleForHome(categoryName));
        title.setTextSize(13);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(0xFF111827);
        title.setGravity(Gravity.CENTER);
        title.setMaxLines(2);
        title.setPadding(0, dp(8), 0, 0);
        card.addView(title);

        card.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProductListActivity.class);
            intent.putExtra("categoryName", categoryName);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        return card;
    }

    private String makeCategoryTitleForHome(String categoryName) {
        if (categoryName == null) return "Category";

        if (categoryName.equalsIgnoreCase("Business Cards")) return "Business Card";
        if (categoryName.equalsIgnoreCase("Flyers")) return "Flyers";
        if (categoryName.equalsIgnoreCase("Posters")) return "Banners & Posters";
        if (categoryName.equalsIgnoreCase("Banners")) return "Banners";
        if (categoryName.equalsIgnoreCase("T-Shirts")) return "Clothing & Bags";
        if (categoryName.equalsIgnoreCase("Mugs")) return "Gifts & Stationery";
        if (categoryName.equalsIgnoreCase("Stickers")) return "Stickers";
        if (categoryName.equalsIgnoreCase("Invitations")) return "Invitations";
        if (categoryName.equalsIgnoreCase("Brochures")) return "Brochures";
        if (categoryName.equalsIgnoreCase("Certificates")) return "Certificates";
        if (categoryName.equalsIgnoreCase("ID Cards")) return "ID Cards";
        if (categoryName.equalsIgnoreCase("Letterheads")) return "Letterheads";
        if (categoryName.equalsIgnoreCase("Envelopes")) return "Envelopes";
        if (categoryName.equalsIgnoreCase("Calendars")) return "Calendars";
        if (categoryName.equalsIgnoreCase("Booklets")) return "Booklets";

        return categoryName;
    }

    private String makeShortDescription(String description) {
        if (description == null) return "Printing products";
        String value = description.trim();
        if (value.length() <= 48) return value;
        return value.substring(0, 45) + "...";
    }


    private void loadFeaturedProducts() {
        if (featuredProductsContainer == null) {
            return;
        }

        featuredProductsContainer.removeAllViews();
        featuredProductsContainer.addView(createMessageText("Loading featured products..."));

        db.collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    featuredProductsContainer.removeAllViews();

                    List<FeaturedProductData> productList = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String productName = document.getString("productName");
                        String description = document.getString("description");
                        String categoryName = document.getString("categoryName");
                        String material = document.getString("material");
                        String size = document.getString("size");
                        Long basePriceLong = document.getLong("basePrice");
                        int basePrice = basePriceLong == null ? 0 : basePriceLong.intValue();

                        if (productName == null || productName.trim().isEmpty()) productName = "Printing Product";
                        if (description == null || description.trim().isEmpty()) description = "Professional printing service";
                        if (categoryName == null || categoryName.trim().isEmpty()) categoryName = "Business Cards";
                        if (material == null || material.trim().isEmpty()) material = "Standard";
                        if (size == null || size.trim().isEmpty()) size = "Custom";

                        productList.add(new FeaturedProductData(
                                productName.trim(),
                                description.trim(),
                                categoryName.trim(),
                                material.trim(),
                                size.trim(),
                                basePrice
                        ));
                    }

                    if (productList.isEmpty()) {
                        addDefaultFeaturedProducts();
                        return;
                    }

                    Collections.sort(productList, (p1, p2) -> getCategoryOrder(p1.categoryName) - getCategoryOrder(p2.categoryName));

                    int limit = Math.min(productList.size(), 4);
                    addFeaturedProductGrid(productList.subList(0, limit));
                })
                .addOnFailureListener(e -> {
                    featuredProductsContainer.removeAllViews();
                    addDefaultFeaturedProducts();
                });
    }

    private void addDefaultFeaturedProducts() {
        List<FeaturedProductData> defaultList = new ArrayList<>();
        defaultList.add(new FeaturedProductData("Premium Business Card", "High quality business cards", "Business Cards", "Premium Glossy", "90mm x 55mm", 1200));
        defaultList.add(new FeaturedProductData("A5 Promotional Flyer", "Colour flyer printing", "Flyers", "Gloss Paper", "A5", 500));
        defaultList.add(new FeaturedProductData("Custom Printed T-Shirt", "Personalized T-shirt printing", "T-Shirts", "Cotton", "S / M / L / XL", 1800));
        defaultList.add(new FeaturedProductData("Personalized Photo Mug", "Custom mug printing", "Mugs", "Ceramic", "Standard Mug", 1200));
        addFeaturedProductGrid(defaultList);
    }

    private void addFeaturedProductGrid(List<FeaturedProductData> productList) {
        featuredProductsContainer.removeAllViews();

        LinearLayout currentRow = null;

        for (int i = 0; i < productList.size(); i++) {
            if (i % 2 == 0) {
                currentRow = new LinearLayout(this);
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                currentRow.setWeightSum(2f);

                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                rowParams.setMargins(0, 0, 0, dp(14));
                currentRow.setLayoutParams(rowParams);
                featuredProductsContainer.addView(currentRow);
            }

            View card = createFeaturedProductCard(productList.get(i));

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
            );
            cardParams.setMargins(i % 2 == 0 ? 0 : dp(7), 0, i % 2 == 0 ? dp(7) : 0, 0);

            if (currentRow != null) {
                currentRow.addView(card, cardParams);
            }
        }
    }

    private View createFeaturedProductCard(FeaturedProductData product) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(10), dp(10), dp(10), dp(12));
        card.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_white_card));
        card.setClickable(true);
        card.setElevation(3f);

        FrameLayout imageFrame = new FrameLayout(this);
        imageFrame.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_icon_box));
        imageFrame.setPadding(dp(4), dp(4), dp(4), dp(4));

        ImageView productImage = new ImageView(this);
        productImage.setImageResource(getCategoryImageResource(product.categoryName));
        productImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageFrame.addView(productImage, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        TextView badge = new TextView(this);
        badge.setText("10% off");
        badge.setTextSize(11);
        badge.setTextColor(0xFF111827);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_discount_badge));
        FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(dp(64), dp(28));
        badgeParams.setMargins(dp(6), dp(6), 0, 0);
        imageFrame.addView(badge, badgeParams);

        card.addView(imageFrame, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(120)
        ));

        TextView title = new TextView(this);
        title.setText(product.productName);
        title.setTextSize(15);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(0xFF111827);
        title.setMaxLines(2);
        title.setPadding(0, dp(10), 0, 0);
        card.addView(title);

        TextView price = new TextView(this);
        price.setText("Rs. " + product.basePrice + " only");
        price.setTextSize(13);
        price.setTypeface(null, Typeface.BOLD);
        price.setTextColor(0xFFFF9800);
        price.setPadding(0, dp(3), 0, 0);
        card.addView(price);

        TextView rating = new TextView(this);
        rating.setText("★ 4.9");
        rating.setTextSize(12);
        rating.setTextColor(0xFF6B7280);
        rating.setPadding(0, dp(4), 0, 0);
        card.addView(rating);

        card.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProductDetailsActivity.class);
            intent.putExtra("categoryName", product.categoryName);
            intent.putExtra("productName", product.productName);
            intent.putExtra("productPrice", "Starting from Rs. " + product.basePrice);
            intent.putExtra("description", product.description);
            intent.putExtra("material", product.material);
            intent.putExtra("size", product.size);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        return card;
    }

    private int getCategoryOrder(String categoryName) {
        if (categoryName == null) return 999;
        String name = categoryName.toLowerCase();
        if (name.equals("business cards")) return 1;
        if (name.equals("flyers")) return 2;
        if (name.equals("posters")) return 3;
        if (name.equals("banners")) return 4;
        if (name.equals("t-shirts")) return 5;
        if (name.equals("mugs")) return 6;
        if (name.equals("stickers")) return 7;
        if (name.equals("invitations")) return 8;
        if (name.equals("brochures")) return 9;
        if (name.equals("certificates")) return 10;
        if (name.equals("id cards")) return 11;
        if (name.equals("letterheads")) return 12;
        if (name.equals("envelopes")) return 13;
        if (name.equals("calendars")) return 14;
        if (name.equals("booklets")) return 15;
        return 999;
    }

    private int getCategoryImageResource(String categoryName) {
        if (categoryName == null) return R.drawable.cat_business_cards;

        String name = categoryName.toLowerCase();

        if (name.contains("business")) return R.drawable.cat_business_cards;
        if (name.contains("flyer")) return R.drawable.cat_flyers;
        if (name.contains("poster")) return R.drawable.cat_posters;
        if (name.contains("banner")) return R.drawable.cat_banners;
        if (name.contains("shirt")) return R.drawable.cat_tshirts;
        if (name.contains("mug")) return R.drawable.cat_mugs;
        if (name.contains("sticker")) return R.drawable.cat_stickers;
        if (name.contains("invitation")) return R.drawable.cat_invitations;
        if (name.contains("brochure")) return R.drawable.cat_brochures;
        if (name.contains("certificate")) return R.drawable.cat_certificates;
        if (name.contains("id")) return R.drawable.cat_id_cards;
        if (name.contains("letter")) return R.drawable.cat_letterheads;
        if (name.contains("envelope")) return R.drawable.cat_envelopes;
        if (name.contains("calendar")) return R.drawable.cat_calendars;
        if (name.contains("booklet")) return R.drawable.cat_booklets;

        return R.drawable.cat_business_cards;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCustomerProfile();
        loadCategoriesFromFirestore();
        loadProductsForSearch();
        loadFeaturedProducts();
        loadLatestPromotion();
    }
}
