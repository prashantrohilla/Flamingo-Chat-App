package com.chatapp.example.flamingoapp.phase3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.chatapp.example.flamingoapp.adapters.MyPhotoAdapter;
import com.chatapp.example.flamingoapp.models.Post;
import com.chatapp.example.flamingoapp.models.Users;
import com.chatapp.example.flamingoapp.phase2.ChatDetailActivity;
import com.chatapp.example.flamingoapp.phase2.ChatListActivity;
import com.chatapp.example.flamingoapp.phase2.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.phone.DoctorAppointment.R;
import com.phone.DoctorAppointment.databinding.ActivityUserProfileBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    ActivityUserProfileBinding binding;
    String profilePic;
    String userName ;
    FirebaseUser auth;
    MyPhotoAdapter myPhotoAdapter;
    List<Post> postList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        String userId = getIntent().getStringExtra("userId");
        auth=FirebaseAuth.getInstance().getCurrentUser();


        binding.myPosts.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        binding.myPosts.setLayoutManager(mLayoutManager);

        postList = new ArrayList<>();
        myPhotoAdapter = new MyPhotoAdapter(getApplicationContext(), postList);
        binding.myPosts.setAdapter(myPhotoAdapter);

        myPhotos();

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference()
                            .child("Users").child(userId);

                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            Users user= snapshot.getValue(Users.class);
                            assert user != null;
                            Picasso.get().load(user.getProfilepic()).placeholder(R.drawable.user2).into(binding.userPic);
                            binding.userName.setText(user.getUserName());
                            binding.userFullName.setText(user.getFullName());
                            binding.userBio.setText(user.getUserBio());
                            binding.userLink.setText(user.getUserLink());

                            profilePic=user.getProfilepic();
                            userName=user.getUserName();
                            isFollowing(userId, binding.followButton);// passing id and button to method
                            getFollowers();

                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });


        binding.chatSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(UserProfileActivity.this, ChatListActivity.class);
                startActivity(i);
            }
        });

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(UserProfileActivity.this, HomeActivity.class);
                startActivity(i);
            }
        });


        binding.message.setOnClickListener(new View.OnClickListener() {  // sending user data to chat detail activity
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserProfileActivity.this, ChatDetailActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("profilePic", profilePic);
                intent.putExtra("userName", userName);
                startActivity(intent);
            }
        });


        binding.followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(binding.followButton.getText().toString().equals("follow"))
                {
                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(auth.getUid()).child("following").child(userId)
                            .setValue(true);

                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(userId).child("followers").child(auth.getUid())
                            .setValue(true);
                }
                else
                {
                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(auth.getUid()).child("following").child(userId)
                            .removeValue();

                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(userId).child("followers").child(auth.getUid())
                            .removeValue();
                }

            }
        });


    }

    private void getFollowers() {
        String userId = getIntent().getStringExtra("userId");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(userId).child("followers");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                binding.followersText.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

            }
        });

        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(userId).child("following");

        reference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                binding.followingText.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

            }
        });

        DatabaseReference reference3=FirebaseDatabase.getInstance().getReference()
                .child("Posts");

        reference3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                int i=0;
                for(DataSnapshot snapshot1:snapshot.getChildren())
                {
                    Post post=snapshot1.getValue(Post.class);
                    if(post.getPublisher().equals(userId))
                    {
                        Log.d("refernce debug"," "+post.getPublisher());
                        i++;
                    }
                }
                binding.postText.setText(""+i);


            }

            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

            }
        });
    }

    private void isFollowing(final String userId, final TextView button)
    { DatabaseReference reference= FirebaseDatabase.getInstance()
            .getReference().child("Follow").child(auth.getUid())
            .child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                if(snapshot.child(userId).exists())
                {
                    button.setText("following");
                }
                else
                {
                    button.setText("follow");
                }
            }

            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

            }
        });
    }

    private  void myPhotos()
    {
        String userId = getIntent().getStringExtra("userId");
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("Posts");

        reference.addValueEventListener(new ValueEventListener() {  // getting list of users from database
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();                                                                   // taking data snapshot from firebase
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // to avoid slow loading
                    Post post= dataSnapshot.getValue(Post.class);
                    if(post.getPublisher().equals(userId))
                    {
                        postList.add(post);
                    }
                    //  Collections.reverse(postList);
                    myPhotoAdapter.notifyDataSetChanged();
                    Log.d("refernce debug"," "+postList.size());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}