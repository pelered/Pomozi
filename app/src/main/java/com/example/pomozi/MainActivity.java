package com.example.pomozi;

import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pomozi.Tab.ObjaveFragment;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private TextView log;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth=FirebaseAuth.getInstance();



        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        String menuFragment = getIntent().getStringExtra("notifikacija");
        String oznaka=getIntent().getStringExtra("oznakan");

        FragmentTransaction fttt = getSupportFragmentManager().beginTransaction();
        // If menuFragment is defined, then this activity was launched with a fragment selection
        if (menuFragment!=null) {
            // Log.d("Main:",menuFragment);
            //Log.d("Main2",oznaka);
            // Here we can decide what do to -- perhaps load other parameters from the intent extras such as IDs, etc
            if (menuFragment.equals("not")) {
                Log.d("Main3", String.valueOf(menuFragment.equals("not")));
                PrikazZivFragment prikazZivFragment = new PrikazZivFragment();
                Bundle args = new Bundle();
                //Log.d("PrikazZivvlas:",vlasnik.getText().toString());
                args.putString("oznaka", oznaka);
                prikazZivFragment.setArguments(args);
                fttt.replace(R.id.nav_host_fragment, prikazZivFragment);
                fttt.addToBackStack("tag_back_prikazZiv");
                fttt.commit();
            }
        } else {
            // Activity was not launched with a menuFragment selected -- continue as if this activity was opened from a launcher (for example)
            HomeFragment standardFragment = new HomeFragment();
            fttt.replace(R.id.nav_host_fragment, standardFragment);
            fttt.commit();
        }

        /*FragmentTransaction ftt = getSupportFragmentManager().beginTransaction();
        ftt.replace(R.id.nav_host_fragment, new HomeFragment());
        ftt.commit();*/
        View view=navigationView.getHeaderView(0);
        log=view.findViewById(R.id.log_in);
        log.setOnClickListener(v -> {
            if(log.getText().equals("Log in")){
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.nav_host_fragment, new LoginFragment());
            ft.addToBackStack("tag_log");
            ft.commit();
            DrawerLayout drawerr = findViewById(R.id.drawer_layout);
            drawerr.closeDrawer(GravityCompat.START);
            }else{
                //potrebno da se moze odlogirat i s google,da mozes kasnije i druge accounte birati
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                // Build a GoogleSignInClient with the options specified by gso.
                mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
                mAuth.signOut();
                mGoogleSignInClient.signOut().addOnCompleteListener(
                        task -> {

                            FragmentTransaction ft =getSupportFragmentManager().beginTransaction();
                            ft.replace(R.id.nav_host_fragment, new HomeFragment());
                            //ft.addToBackStack("tag_back1_profile");
                            ft.commit();
                        });
                LoginManager.getInstance().logOut();
                SharedPreferences prefs = getSharedPreferences("shared_pref_name", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                View headerView = navigationView.getHeaderView(0);
                TextView ime_nav,email_nav;
                ImageView photo_nav;
                ime_nav=headerView.findViewById(R.id.ime_navigation);
                email_nav=headerView.findViewById(R.id.email_navigation);
                log=headerView.findViewById(R.id.log_in);
                photo_nav=headerView.findViewById(R.id.imageViewprofile);
                ime_nav.setText(R.string.nav_header_title);
                email_nav.setText(R.string.nav_header_subtitle);
                Glide.with(this).load(R.mipmap.ic_launcher_round).apply(RequestOptions.circleCropTransform()).into(photo_nav);
                log.setText(R.string.log_in);
                Menu menu=navigationView.getMenu();
                MenuItem item =menu.findItem(R.id.dodaj_objavu);
                item.setVisible(false);
                item =menu.findItem(R.id.ispis_objava);
                item.setVisible(false);
                item =menu.findItem(R.id.moj_profil);
                item.setVisible(false);
                //Log.d("dal izbrise","usao sam");
                editor.apply();
            }
        });
        SharedPreferences prefs = Objects.requireNonNull(this).getSharedPreferences("shared_pref_name", Context.MODE_PRIVATE);
        if(prefs.getString("username",null)!=null){
            TextView ime_nav,email_nav;
            ImageView profile;
            profile=view.findViewById(R.id.imageViewprofile);
            ime_nav = view.findViewById(R.id.ime_navigation);
            email_nav = view.findViewById(R.id.email_navigation);
            ime_nav.setText(prefs.getString("username",null));
            email_nav.setText(prefs.getString("email",null));
            String url=prefs.getString("url",null);
            log.setText(R.string.log_out);
            if (url!=null) {
                Glide.with(this).load(url).apply(RequestOptions.circleCropTransform()).into(profile);
            }else if(prefs.getString("uid",null)==null){
                Glide.with(this).load(R.mipmap.ic_launcher_round).apply(RequestOptions.circleCropTransform()).into(profile);
            }
            Menu menu=navigationView.getMenu();
            MenuItem item =menu.findItem(R.id.dodaj_objavu);
            item.setVisible(true);
            item =menu.findItem(R.id.ispis_objava);
            item.setVisible(true);
            item =menu.findItem(R.id.moj_profil);
            item.setVisible(true);
        }


    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
            } else {
                super.onBackPressed();
            }
            //super.onBackPressed();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.nav_host_fragment, new SearchFragment());
            ft.addToBackStack("tag_back_search");
            ft.commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.nav_host_fragment, new HomeFragment());
            ft.addToBackStack("tag_back1");
            ft.commit();
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);}
        else if (id == R.id.dodaj_objavu) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.nav_host_fragment, new EditZiv());
            ft.addToBackStack("tag_back3");
            ft.commit();
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);

        } else if (id == R.id.ispis_objava) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.nav_host_fragment,new ObjaveFragment());
            ft.addToBackStack("tag_back4");
            ft.commit();
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);

        }
        else if (id == R.id.moj_profil) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.nav_host_fragment,new ProfileFragment());
            ft.addToBackStack("tag_back4");
            ft.commit();
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);

        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
