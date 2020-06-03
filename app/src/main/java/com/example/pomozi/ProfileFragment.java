package com.example.pomozi;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pomozi.R;
import com.example.pomozi.ui.home.HomeFragment;
import com.facebook.login.LoginFragment;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//Glide.with(context).load(uri).apply(RequestOptions().circleCrop()).into(imageView)
public class ProfileFragment extends Fragment implements View.OnClickListener {
    private String username,email,photo;
    private String url;
    private TextView u,e;
    private Button logout,vrati;
    private ImageView profile_photo;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private RecyclerView recyclerView;
    //private ProfileMyAdapter adapter;
    private LinearLayoutManager layoutManager;
    private String uid;
    //private Fav fav1;
    private ArrayList<String> favo=new ArrayList<>();
    //private ZivUpload ziv= new ZivUpload();
    //private List<Item> itemList =new ArrayList<>();

    private TextView log,ime_nav,email_nav;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile,container,false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        SharedPreferences prefs = Objects.requireNonNull(getContext()).getSharedPreferences("shared_pref_name", Context.MODE_PRIVATE);
        //prefs.getString("uid",null);
        //Log.d("OnCreate:",prefs.toString());
        //Log.d("OnCreate:1",prefs.getString("url",null));
        username=prefs.getString("username",null);
        email=prefs.getString("email",null);
        url=prefs.getString("url",null);
        uid=prefs.getString("uid",null);

        u=view.findViewById(R.id.username);
        e=view.findViewById(R.id.email);
        logout=view.findViewById(R.id.logout);
        vrati=view.findViewById(R.id.vrati);
        profile_photo=view.findViewById(R.id.photo);
        mAuth=FirebaseAuth.getInstance();

        logout.setOnClickListener(this);
        vrati.setOnClickListener(this);
        u.setText(username);
        e.setText(email);

        if(url!=null){
            Glide.with(this).load(url).apply(RequestOptions.circleCropTransform()).into(profile_photo);
        }

        //potrebno da se moze odlogirat i s google,da mozes kasnije i druge accounte birati
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(Objects.requireNonNull(getActivity()), gso);

    }
    @Override
    public void onClick(View view) {
        if(view.equals(logout)){
            mAuth.signOut();
            mGoogleSignInClient.signOut().addOnCompleteListener(Objects.requireNonNull(getActivity()),
                    task -> {

                        FragmentTransaction ft =(getActivity()).getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.nav_host_fragment, new HomeFragment());
                        //ft.addToBackStack("tag_back1_profile");
                        ft.commit();
                    });
            LoginManager.getInstance().logOut();
            SharedPreferences prefs = Objects.requireNonNull(getContext()).getSharedPreferences("shared_pref_name", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            NavigationView navigationView =getActivity().findViewById(R.id.nav_view);
            View headerView = navigationView.getHeaderView(0);
            ime_nav=headerView.findViewById(R.id.ime_navigation);
            email_nav=headerView.findViewById(R.id.email_navigation);
            ime_nav.setText(R.string.nav_header_title);
            email_nav.setText(R.string.nav_header_subtitle);
            //Log.d("dal izbrise","usao sam");
            editor.commit();
        }else if(view.equals(vrati)){
            //todo
            FragmentTransaction ft =(getActivity()).getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.nav_host_fragment, new HomeFragment());
            //ft.addToBackStack("tag_back1_profile");
            ft.commit();

        }
    }
    public void onStart() {
        super.onStart();
        FirebaseUser user=mAuth.getCurrentUser();
        if(user == null){
            FragmentTransaction ft =(Objects.requireNonNull(getActivity())).getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.nav_host_fragment, new LoginFragment());
            //ft.addToBackStack("tag_back1_profile");
            ft.commit();

        }
    }
}
