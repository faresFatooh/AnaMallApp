package ui.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.mall.anamall.MainActivity;
import com.mall.anamall.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import models.RestaurantMenuItems;
import butterknife.BindView;
import butterknife.ButterKnife;
import ui.cart.CartItemActivity;
import ui.review.ReviewsActivity;

import static android.content.ContentValues.TAG;
import static android.view.View.GONE;

public class MainRestaurantPageActivity extends AppCompatActivity {

    private String uid, mRestaurantUid, mResName, mResDistance, mResPrice, mResDeliveryTime, mResImage, mResNum;
    private FirebaseFirestore db;
    LinearLayoutManager linearLayoutManager;
    private RecyclerView mMenuItemRecyclerView;
    private NestedScrollView mRootView;
    private LottieAnimationView mFavoriteAnim;
    private RecyclerView mRecyclerView;
    private String gender;
    ArrayList<String> list = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_restaurant_page);
        Intent intent = getIntent();
        if (intent != null) {
            mRestaurantUid = intent.getStringExtra("RUID");
            mResName = intent.getStringExtra("NAME");
            mResDistance = intent.getStringExtra("DISTANCE");
            mResPrice = intent.getStringExtra("PRICE");
            mResDeliveryTime = intent.getStringExtra("TIME");
            mResImage = intent.getStringExtra("RES_IMAGE");
            mResNum = intent.getStringExtra("RES_NUM");

        }



        FirebaseFirestore.getInstance().collection("LIST").document(mRestaurantUid).collection("Categorys").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        list.add(document.getId());
                    }
                    gender = list.get(0);
                    init();
                    getMenuItems();
                    RecyclerView.Adapter<RecyclerAdapter.ViewHolder> adapter;
                    LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
                    mRecyclerView = (RecyclerView) findViewById(R.id.reciclar_view);
                    mRecyclerView.setLayoutManager(layoutManager);
                    adapter = new RecyclerAdapter();
                    mRecyclerView.setAdapter(adapter);

                    Log.d("fares", list.toString(), task.getException());
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }

            }
        });

//        Toast.makeText(this, list.toString(), Toast.LENGTH_SHORT).show();

    }

    @SuppressLint("SetTextI18n")
    private void init() {
        mRootView = findViewById(R.id.content1);
        db = FirebaseFirestore.getInstance();

        uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        TextView mResNameToolBar = findViewById(R.id.restaurantTitleToolbar);
        TextView mResNameText = findViewById(R.id.mainResName);
        TextView mReviewsText = findViewById(R.id.reviewText);
        mReviewsText.setOnClickListener(view -> {
            Intent intent = new Intent(this, ReviewsActivity.class);
            intent.putExtra("RUID", mRestaurantUid);
            intent.putExtra("NAME", mResName);
            intent.putExtra("PRICE", mResPrice);
            intent.putExtra("NUM", mResNum);
            startActivity(intent);
        });
        mFavoriteAnim = findViewById(R.id.favoriteAnim);
        checkFavRes();
        mFavoriteAnim.setOnClickListener(view -> {
            if (mFavoriteAnim.getProgress() >= 0.1f) {
                mFavoriteAnim.setSpeed(-1);
                mFavoriteAnim.playAnimation();
                delFavRes();
            } else if (mFavoriteAnim.getProgress() == 0.0f) {
                mFavoriteAnim.setSpeed(1);
                mFavoriteAnim.playAnimation();
                favRes();
            }
        });
        TextView mResDistanceText = findViewById(R.id.mainResDistance);
        TextView mResAvgPriceText = findViewById(R.id.restaurantAvgPrice);
        TextView mResDeliveryTimeText = findViewById(R.id.restaurantDeliveryTime);
        mResNameToolBar.setText(mResName);
        mResNameText.setText(mResName);
        mResDeliveryTimeText.setText(mResDeliveryTime + " mins");
        mResAvgPriceText.setText("\u20b9" + mResPrice);
        mResDistanceText.setText(mResDistance + " kms");
        ImageView mBackBtnView = findViewById(R.id.backBtn);
        mBackBtnView.setOnClickListener(view -> {
            this.onBackPressed();
            this.overridePendingTransition(0, 0);
        });

        mMenuItemRecyclerView = findViewById(R.id.menuItemRecylerView);
        linearLayoutManager = new GridLayoutManager(this, 2);
        mMenuItemRecyclerView.setLayoutManager(linearLayoutManager);

    }

    private void getMenuItems() {

        Query query = db.collection("LIST").document(mRestaurantUid).collection("Categorys").document(gender).collection("name");
        FirestoreRecyclerOptions<RestaurantMenuItems> menuItemModel = new FirestoreRecyclerOptions.Builder<RestaurantMenuItems>()
                .setQuery(query, RestaurantMenuItems.class)
                .build();


        FirestoreRecyclerAdapter<RestaurantMenuItems, MenuItemHolder> adapter = new FirestoreRecyclerAdapter<RestaurantMenuItems, MenuItemHolder>(menuItemModel) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull MenuItemHolder holder, int position, @NonNull RestaurantMenuItems model) {
                if (model.getIs_active().equals("no")) {
                    holder.mItemName.setText(model.getName());
                    holder.mItemCategory.setText(model.getCategory());
                    String specImage = String.valueOf(model.getSpecification());
                    Glide.with(Objects.requireNonNull(getApplicationContext()))
                            .load("https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEjHq2OwxYlK0hykwRA_Bx9oTmuBoyM5jXjAHJaWdazH0QyGGdpjtZnyp6TI72KHOnhN0N4jN_IA6fbN2YpJhZXOhdnYHxRWzZ1STSb5tN8ZS3d4y2TgWrlf5pNtneeeW-H0ImFjFEIROpyvXcPAM2AVm_epEqK_Up2Dg2cTH2-uDAdVQ3EgNiFFlU0L/s320/%D8%B4%D8%A7%D9%8A%20%D8%A7%D9%84%D8%B9%D8%B1%D9%88%D8%B3%D8%A9.jpg").into(holder.foodSpecification);
                    if (!model.getSpecification().equals("0")){
                        holder.mItemPrice.setPaintFlags(holder.mItemPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }else {
                        holder.menuItemPrice2.setVisibility(GONE);
                    }
                    holder.mItemPrice.setText("₪ " + model.getPrice());
                    holder.menuItemPrice2.setText("₪ " + model.getSpecification());
                    holder.mItemAddBtn.setClickable(false);
                    holder.mNotAvailableText.setVisibility(View.VISIBLE);
                } else if (model.getIs_active().equals("yes")) {
                    holder.mItemName.setText(model.getName());
                    holder.menuItemd.setText(model.getDescription());
                    holder.mItemCategory.setText(model.getCategory());
                    String specImage = String.valueOf(model.getSpecification());
                    Glide.with(Objects.requireNonNull(getApplicationContext()))
                            .load("https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEjHq2OwxYlK0hykwRA_Bx9oTmuBoyM5jXjAHJaWdazH0QyGGdpjtZnyp6TI72KHOnhN0N4jN_IA6fbN2YpJhZXOhdnYHxRWzZ1STSb5tN8ZS3d4y2TgWrlf5pNtneeeW-H0ImFjFEIROpyvXcPAM2AVm_epEqK_Up2Dg2cTH2-uDAdVQ3EgNiFFlU0L/s320/%D8%B4%D8%A7%D9%8A%20%D8%A7%D9%84%D8%B9%D8%B1%D9%88%D8%B3%D8%A9.jpg").into(holder.foodSpecification);
                    if (!model.getSpecification().equals("0")){
                        holder.mItemPrice.setPaintFlags(holder.mItemPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }else {
                        holder.menuItemPrice2.setVisibility(GONE);
                    }
                    holder.mItemPrice.setText("₪ " + model.getPrice());
                    holder.menuItemPrice2.setText("₪ " + model.getSpecification());
                    holder.itemView.setOnClickListener(v -> {
                    });

                    String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

                    holder.mItemAddBtn.setOnClickListener(view -> {
                        String selectedItemName = holder.mItemName.getText().toString();
                        addItemToCart(selectedItemName, model, uid);
                        holder.mItemAddBtn.setVisibility(GONE);
                        holder.mRemoveItemBtn.setVisibility(View.VISIBLE);
                    });

                    holder.mRemoveItemBtn.setOnClickListener(view -> {
                        String selectedItemName = holder.mItemName.getText().toString();
                        removeItemFromCart(selectedItemName, uid);
                        holder.mRemoveItemBtn.setVisibility(GONE);
                        holder.mItemAddBtn.setVisibility(View.VISIBLE);
                    });

                    DocumentReference docRef = db.collection("UserList").document(uid).collection("CartItems").document(holder.mItemName.getText().toString());
                    docRef.get().addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (Objects.requireNonNull(documentSnapshot).exists()) {

                                holder.mItemAddBtn.setVisibility(GONE);
                                holder.mRemoveItemBtn.setVisibility(View.VISIBLE);

                            }
                        }

                    });
                }
            }

            @NotNull
            @Override
            public MenuItemHolder onCreateViewHolder(@NotNull ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.restaurant_menuitems_view, group, false);
                return new MenuItemHolder(view);
            }

            @Override
            public void onError(@NotNull FirebaseFirestoreException e) {
                Log.e("error", Objects.requireNonNull(e.getMessage()));
            }
        };
        adapter.startListening();
        adapter.notifyDataSetChanged();
        mMenuItemRecyclerView.setAdapter(adapter);

    }

    private void addItemToCart(String selectedItemName, RestaurantMenuItems model, String uid) {
        Map<String, String> cartItemMap = new HashMap<>();
        cartItemMap.put("select_name", selectedItemName);
        cartItemMap.put("select_price", model.getPrice());
        cartItemMap.put("select_specification", model.getSpecification());
        cartItemMap.put("item_count", "1");
        db.collection("UserList").document(uid).collection("CartItems")
                .document(selectedItemName).
                set(cartItemMap)
                .addOnSuccessListener(aVoid -> {

                    db.collection("UserList").document(uid).collection("CartItems").get().addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {
                            int count = 0;
                            for (DocumentSnapshot ignored : Objects.requireNonNull(task.getResult())) {
                                count++;
                            }
                            Snackbar snackbar = Snackbar
                                    .make(mRootView, "Added " + count + " items in  your cart", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Cart", view -> {
                                        Snackbar snackbar1 = Snackbar.make(mRootView, "Message is restored!", Snackbar.LENGTH_SHORT);
                                        Intent intent = new Intent(this, CartItemActivity.class);
                                        startActivity(intent);
                                        snackbar1.dismiss();
                                    });
                            snackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                            snackbar.show();
                        }
                    });
                }).addOnFailureListener(e -> {
                });

        HashMap<String, Object> resNameMap = new HashMap<>();
        resNameMap.put("restaurant_cart_name", mResName);
        resNameMap.put("restaurant_cart_uid", mRestaurantUid);
        resNameMap.put("restaurant_delivery_time", mResDeliveryTime);
        resNameMap.put("restaurant_cart_spotimage", mResImage);

        db.collection("UserList").document(uid).update(resNameMap).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Added Item Successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Adding Item Failed", Toast.LENGTH_SHORT).show();
        });

    }

    private void removeItemFromCart(String selectedItemName, String uid) {
        db.collection("UserList").document(uid).collection("CartItems").document(selectedItemName).delete().addOnCompleteListener(task ->
                Toast.makeText(getApplicationContext(), "Item Removed From Cart", Toast.LENGTH_SHORT).show());
    }

    public static class MenuItemHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.menuItemName)
        TextView mItemName;
        @BindView(R.id.foodMark)
        ImageView foodSpecification;
        @BindView(R.id.menuItemPrice)
        TextView mItemPrice;
        @BindView(R.id.menuItemCategory)
        TextView mItemCategory;
        @BindView(R.id.addMenuItemBtn)
        Button mItemAddBtn;
        @BindView(R.id.removeMenuItemBtn)
        Button mRemoveItemBtn;
        @BindView(R.id.notAvailableText)
        TextView mNotAvailableText;
        @BindView(R.id.menuItemd)
        TextView menuItemd;
        @BindView(R.id.menuItemPrice2)
        TextView menuItemPrice2;

        public MenuItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private void checkFavRes() {
        db.collection("UserList")
                .document(uid)
                .collection("FavoriteRestaurants")
                .document(mRestaurantUid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot docRef = task.getResult();
                        if (Objects.requireNonNull(docRef).exists()) {
                            mFavoriteAnim.setProgress(0.1f);
                            mFavoriteAnim.resumeAnimation();
                        } else {
                            mFavoriteAnim.setProgress(0.0f);
                        }
                    }
                });
    }

    private void favRes() {
        Map<String, Object> favResMap = new HashMap<>();
        favResMap.put("restaurant_uid", mRestaurantUid);
        favResMap.put("restaurant_name", mResName);
        favResMap.put("restaurant_image", mResImage);
        favResMap.put("restaurant_price", mResPrice);

        db.collection("UserList")
                .document(uid)
                .collection("FavoriteRestaurants")
                .document(mRestaurantUid).set(favResMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getApplicationContext(), mResName + " has marked as favorite", Toast.LENGTH_SHORT).show();
                });
    }

    private void delFavRes() {
        db.collection("UserList")
                .document(uid)
                .collection("FavoriteRestaurants")
                .document(mRestaurantUid).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getApplicationContext(), "Removed as favorite", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {


        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(v);
            return viewHolder;

        }


        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            viewHolder.itemTitle.setText(list.get(i));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder /*implements Inter*/ {
            TextView itemTitle;

            ViewHolder(View itemView) {
                super(itemView);
                itemTitle = itemView.findViewById(R.id.tv_row);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        Snackbar.make(v, "click on position " + list.get(position), Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                        gender = list.get(position);
                        getMenuItems();

                    }

                });
            }
        }


    }
}
