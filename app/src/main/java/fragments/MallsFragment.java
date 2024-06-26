package fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import adapters.SliderAdapter;
import ui.cart.CartItemActivity;
import ui.cart.EmptyCartActivity;
import ui.location.ChangeLocationActivity;
import ui.main.MainRestaurantPageActivity;

import com.mall.anamall.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.util.Objects;

import models.RestaurantDetail;
//import ui.location.ChangeLocationActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import ru.nikartm.support.ImageBadgeView;

public class MallsFragment extends Fragment {

    private FirebaseUser mCurrentUser;
    private FirebaseFirestore db;
    private String address;
    SliderView sliderView;
    int[] images = {R.drawable.upi_logo,
            R.drawable.empty_cart_image,
            R.drawable.cash_icon};
    LinearLayoutManager linearLayoutManager;
    private RecyclerView mRestaurantRecyclerView;
    LinearLayoutManager linearLayoutManager2;
    private RecyclerView mRestaurantRecyclerView2;
    private ImageBadgeView mImageBadgeView;
    private ImageBadgeView mImageBadgeView2;

    public MallsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant, container, false);
        init(view);

        DrawerLayout btn = getActivity().findViewById(R.id.drawer_layout2);
        btn.closeDrawer(GravityCompat.START);

        fetchLocation(view);
        getItemsInCartNo();
        Activity activity = getActivity();
        if (activity != null && isAdded()) {
            getRestaurantDetails(requireContext());
            getRestaurantDetails2(requireContext());
        }
        sliderView = view.findViewById(R.id.imageSlider2);

        SliderAdapter sliderAdapter = new SliderAdapter(images);

        sliderView.setSliderAdapter(sliderAdapter);
        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM);
        sliderView.setScrollTimeInSec(4); //set scroll delay in seconds :
        sliderView.setSliderTransformAnimation(SliderAnimations.DEPTHTRANSFORMATION);
        sliderView.startAutoCycle();
        return view;
    }
    private void init(View view) {
        Activity activity = getActivity();
        if (activity != null && isAdded()) {


//Or any other depends on activity context to be live like dailog


            LinearLayout mAddressContainer = view.findViewById(R.id.addressContainer);
            mAddressContainer.setOnClickListener(view1 -> {

            Intent intent = new Intent(getActivity(), ChangeLocationActivity.class);
            intent.putExtra("INT", "ONE");
            startActivity(intent);
            });
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mCurrentUser = mAuth.getCurrentUser();
            db = FirebaseFirestore.getInstance();
            Toolbar mToolbar = view.findViewById(R.id.customToolBar);
            ((AppCompatActivity) requireActivity()).setSupportActionBar(mToolbar);
            linearLayoutManager = new GridLayoutManager(getContext(), 2);
            mRestaurantRecyclerView = view.findViewById(R.id.restaurant_recyclerView);
            mRestaurantRecyclerView.setLayoutManager(linearLayoutManager);
            linearLayoutManager2 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,false);
            mRestaurantRecyclerView2 = view.findViewById(R.id.cuisineGridView);
            mRestaurantRecyclerView2.setLayoutManager(linearLayoutManager2);
            mImageBadgeView = view.findViewById(R.id.imageBadgeView);
            mImageBadgeView2 = view.findViewById(R.id.imageBadgeView2);
            mImageBadgeView2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DrawerLayout btn = getActivity().findViewById(R.id.drawer_layout2);
                    btn.openDrawer(GravityCompat.START);
                }
            });
        }

    }

    private void fetchLocation(View view) {
        if (mCurrentUser != null) {
            DocumentReference docRef = db.collection("UserList").document(mCurrentUser.getUid());
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    assert documentSnapshot != null;
                    address = String.valueOf(documentSnapshot.get("address"));
                    TextView textView = view.findViewById(R.id.userLocation);
                    textView.setText(address);
                }
            });
        }
    }

    private void getItemsInCartNo() {
        db.collection("UserList").document(mCurrentUser.getUid()).collection("CartItems").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int count = 0;
                for (DocumentSnapshot ignored : Objects.requireNonNull(task.getResult())) {
                    count++;
                }
                mImageBadgeView.setBadgeValue(count);
                mImageBadgeView.setOnClickListener(view -> {
                    if (mImageBadgeView.getBadgeValue() != 0) {
                        sendUserToCheckOut();
                    } else {
                        sendUserToEmptyCart();
                    }
                });

            }
        });
    }


    private void getRestaurantDetails(Context context) {
        Query query = db.collection("[Mall]");
        FirestoreRecyclerOptions<RestaurantDetail> menuItemModel = new FirestoreRecyclerOptions.Builder<RestaurantDetail>()
                .setQuery(query, RestaurantDetail.class)
                .build();
        FirestoreRecyclerAdapter<RestaurantDetail, RestaurantItemViewHolder> restaurantAdapter = new FirestoreRecyclerAdapter<RestaurantDetail, RestaurantItemViewHolder>(menuItemModel) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull RestaurantItemViewHolder holder, int position, @NonNull RestaurantDetail model) {
                if (mCurrentUser != null) {
                    DocumentReference docRef = db.collection("UserList").document(mCurrentUser.getUid());
                    docRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            assert documentSnapshot != null;
                            Double mUserLat = (Double) documentSnapshot.get("latitude");
                            Double mUserLong = (Double) documentSnapshot.get("longitude");
                            Double mResLat = model.getLatitude();
                            Double mResLong = model.getLongitude();
                            int prepTime = Integer.parseInt(Objects.requireNonNull(model.getRestaurant_prep_time()).replace(" Mins", ""));
                            Location userLocation = new Location("");
                            userLocation.setLatitude(mUserLat);
                            userLocation.setLongitude(mUserLong);
                            Location restaurantLocation = new Location("");
                            restaurantLocation.setLatitude(mResLat);
                            restaurantLocation.setLongitude(mResLong);

                            int distanceInMeters = (int) (userLocation.distanceTo(restaurantLocation));
                            int AvgDrivingSpeedPerKm = 666;
                            int estimatedDriveTimeInMinutes = distanceInMeters / AvgDrivingSpeedPerKm;
                            String deliveryTime = String.valueOf(estimatedDriveTimeInMinutes + prepTime);
                            holder.mRestaurantName.setText(model.getRestaurant_name());
                            String RUID = model.getRestaurant_uid();
                            holder.mAverageDeliveryTime.setText(deliveryTime + " mins");

                            Glide.with(context)
                                    .load(model.getRestaurant_spotimage())
                                    .into(holder.mRestaurantSpotImage);
                            holder.mAveragePrice.setText("\u20B9" + model.getAverage_price() + " Per Person | ");
                            holder.itemView.setOnClickListener(view -> {

                                Intent intent = new Intent(context, MainRestaurantPageActivity.class);
                                intent.putExtra("RUID", RUID);
                                intent.putExtra("NAME", model.getRestaurant_name());
                                intent.putExtra("DISTANCE", String.valueOf(distanceInMeters / 1000));
                                intent.putExtra("TIME", deliveryTime);
                                intent.putExtra("PRICE", model.getAverage_price());
                                intent.putExtra("RES_IMAGE", model.getRestaurant_spotimage());
                                intent.putExtra("RES_NUM", model.getRestaurant_phonenumber());
                                startActivity(intent);
                                requireActivity().overridePendingTransition(0, 0);

                            });
                        }
                    });
                }

            }

            @NonNull
            @Override
            public RestaurantItemViewHolder onCreateViewHolder(@NonNull ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.restaurant_main_detail, group, false);
                return new RestaurantItemViewHolder(view);
            }

            @Override
            public void onError(@NonNull @NotNull FirebaseFirestoreException e) {

            }
        };
        restaurantAdapter.startListening();
        restaurantAdapter.notifyDataSetChanged();
        mRestaurantRecyclerView.setAdapter(restaurantAdapter);
    }
    private void getRestaurantDetails2(Context context) {
        Query query = db.collection("[Mall]").whereEqualTo("top","Yes");
        FirestoreRecyclerOptions<RestaurantDetail> menuItemModel = new FirestoreRecyclerOptions.Builder<RestaurantDetail>()
                .setQuery(query, RestaurantDetail.class)
                .build();
        FirestoreRecyclerAdapter<RestaurantDetail, RestaurantItemViewHolder2> restaurantAdapter = new FirestoreRecyclerAdapter<RestaurantDetail, RestaurantItemViewHolder2>(menuItemModel) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull RestaurantItemViewHolder2 holder, int position, @NonNull RestaurantDetail model) {
                if (mCurrentUser != null) {
                    DocumentReference docRef = db.collection("UserList").document(mCurrentUser.getUid());
                    docRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            assert documentSnapshot != null;
                            Glide.with(context).load(model.getRestaurant_spotimage()).into(holder.cuisineImage);
                            String RUID = model.getRestaurant_uid();
                            Double mUserLat = (Double) documentSnapshot.get("latitude");
                            Double mUserLong = (Double) documentSnapshot.get("longitude");
                            Double mResLat = model.getLatitude();
                            Double mResLong = model.getLongitude();
                            int prepTime = Integer.parseInt(Objects.requireNonNull(model.getRestaurant_prep_time()).replace(" Mins", ""));
                            Location userLocation = new Location("");
                            userLocation.setLatitude(mUserLat);
                            userLocation.setLongitude(mUserLong);
                            Location restaurantLocation = new Location("");
                            restaurantLocation.setLatitude(mResLat);
                            restaurantLocation.setLongitude(mResLong);

                            int distanceInMeters = (int) (userLocation.distanceTo(restaurantLocation));
                            int AvgDrivingSpeedPerKm = 666;
                            int estimatedDriveTimeInMinutes = distanceInMeters / AvgDrivingSpeedPerKm;
                            String deliveryTime = String.valueOf(estimatedDriveTimeInMinutes + prepTime);

                            holder.cuisineName.setText(model.getRestaurant_name());
                            holder.itemView.setOnClickListener(view -> {

                                Intent intent = new Intent(context, MainRestaurantPageActivity.class);
                                intent.putExtra("RUID", RUID);
                                intent.putExtra("NAME", model.getRestaurant_name());
                                intent.putExtra("DISTANCE", String.valueOf(distanceInMeters / 1000));
                                intent.putExtra("TIME", deliveryTime);
                                intent.putExtra("PRICE", model.getAverage_price());
                                intent.putExtra("RES_IMAGE", model.getRestaurant_spotimage());
                                intent.putExtra("RES_NUM", model.getRestaurant_phonenumber());
                                startActivity(intent);
                                requireActivity().overridePendingTransition(0, 0);

                            });
                        }
                    });
                }

            }

            @NonNull
            @Override
            public RestaurantItemViewHolder2 onCreateViewHolder(@NonNull ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.custom_cuisine_layout, group, false);
                return new RestaurantItemViewHolder2(view);
            }

            @Override
            public void onError(@NonNull @NotNull FirebaseFirestoreException e) {

            }
        };
        restaurantAdapter.startListening();
        restaurantAdapter.notifyDataSetChanged();
        mRestaurantRecyclerView2.setAdapter(restaurantAdapter);
    }

    public static class RestaurantItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.resName)
        TextView mRestaurantName;
        @BindView(R.id.resImage)
        ImageView mRestaurantSpotImage;
        @BindView(R.id.average_price)
        TextView mAveragePrice;
        @BindView(R.id.average_time)
        TextView mAverageDeliveryTime;

        public RestaurantItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
    public static class RestaurantItemViewHolder2 extends RecyclerView.ViewHolder {
        @BindView(R.id.cuisineName)
        TextView cuisineName;
        @BindView(R.id.cuisineImage)
        ImageView cuisineImage;

        public RestaurantItemViewHolder2(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private void sendUserToCheckOut() {
        Intent intent = new Intent(getActivity(), CartItemActivity.class);
        startActivity(intent);
        requireActivity().overridePendingTransition(0, 0);
    }

    private void sendUserToEmptyCart() {
        Intent intent = new Intent(getActivity(), EmptyCartActivity.class);
        startActivity(intent);
        requireActivity().overridePendingTransition(0, 0);
    }
}