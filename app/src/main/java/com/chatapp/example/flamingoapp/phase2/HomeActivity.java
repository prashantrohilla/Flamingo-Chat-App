package com.chatapp.example.flamingoapp.phase2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.chatapp.example.flamingoapp.fragments.AddPostFragment;
import com.chatapp.example.flamingoapp.fragments.HomeFragment;
import com.chatapp.example.flamingoapp.fragments.NotificationFragment;
import com.chatapp.example.flamingoapp.fragments.ProfileFragment;
import com.chatapp.example.flamingoapp.fragments.SearchFragment;
import com.chatapp.example.flamingoapp.phase1.LoginActivity;
import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.google.firebase.auth.FirebaseAuth;
import com.phone.DoctorAppointment.R;
import com.phone.DoctorAppointment.databinding.ActivityHomeBinding;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class HomeActivity extends AppCompatActivity {

    ActivityHomeBinding binding;
    MeowBottomNavigation bottomNavigation;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();



        binding.chatSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(HomeActivity.this, ChatListActivity.class);
                startActivity(i);
            }
        });

        binding.logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();// user logout
                finishAffinity();
                Toast.makeText(HomeActivity.this, "You have been logged out.", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(HomeActivity.this,LoginActivity.class);
                startActivity(i);

            }
        });

        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.add(new MeowBottomNavigation.Model(1, R.drawable.home));
        bottomNavigation.add(new MeowBottomNavigation.Model(2, R.drawable.search));
        bottomNavigation.add(new MeowBottomNavigation.Model(3, R.drawable.add));
        bottomNavigation.add(new MeowBottomNavigation.Model(4, R.drawable.notification));
        bottomNavigation.add(new MeowBottomNavigation.Model(5, R.drawable.profile));

        bottomNavigation.show(1, true);
        { replace(new HomeFragment());
        getSupportActionBar().hide();}

        Bundle intent=getIntent().getExtras();
        if(intent!=null)
        {
                bottomNavigation.show(5, true);
                {
                    replace(new ProfileFragment());
                }


        }

        bottomNavigation.setOnClickMenuListener(new Function1<MeowBottomNavigation.Model, Unit>() {
            @Override
            public Unit invoke(MeowBottomNavigation.Model model) {

                switch (model.getId()) {
                    case 1:
                        replace(new HomeFragment());
                        getSupportActionBar().hide();
                        break;
                    case 2:
                        replace(new SearchFragment());
                        getSupportActionBar().hide();
                        break;
                    case 3:
                        replace(new AddPostFragment());
                        binding.layout1.setVisibility(View.INVISIBLE);
                        break;
                    case 4:
                        replace(new NotificationFragment());
                        getSupportActionBar().hide();
                        break;
                    case 5:
                        replace(new ProfileFragment());
                        getSupportActionBar().hide();
                        break;
                }
                return null;
            }
        });
    }

    private void replace(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLyout, fragment);
        transaction.commit();
    }
}