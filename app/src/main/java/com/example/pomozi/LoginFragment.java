package com.example.pomozi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pomozi.Model.User;
import com.example.pomozi.R;
import com.example.pomozi.RegisterFragment;
import com.example.pomozi.ProfileFragment;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.Objects;

public class LoginFragment extends Fragment implements View.OnClickListener{

    private SignInButton googleSignInButton;
    private static final int RC_SIGN_IN = 1;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private String profilePicUrl;
    private TextView reg;
    private TextView log,ime_nav,email_nav;
    private View v;
    private EditText email,lozinka;
    private DatabaseReference myRef ;
    private FirebaseDatabase database;
    private Button login;
    private User user_dohvati;
    private ImageView profile_photo;



    public View onCreateView(@NonNull LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login,container,false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        v=view;
        mAuth = FirebaseAuth.getInstance();
        email=view.findViewById(R.id.email_log);
        lozinka=view.findViewById(R.id.lozinka_log);
        login=view.findViewById(R.id.log);
        login.setOnClickListener(this);
        myRef = database.getInstance().getReference("Kor");
        //za google
        googleSignInButton=view.findViewById(R.id.sign_in_button);
        googleSignInButton.setOnClickListener(this);
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(Objects.requireNonNull(getActivity()), gso);


        reg=view.findViewById(R.id.reg);
        reg.setOnClickListener(this);
        //facebook
        callbackManager = CallbackManager.Factory.create();
        loginButton = view.findViewById(R.id.login_button);
        loginButton.setPermissions("email", "public_profile");
        loginButton.setFragment(this);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("Tag", "facebook:onSuccess:" + loginResult.toString());
                handleFacebookAccessToken(loginResult.getAccessToken());
            }
            @Override
            public void onCancel() {
                Log.d("Tag", "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("Tag", "facebook:onError", error);
            }
        });
    }
    //google
    private void signIn() {
        //Log.d("Probam :", String.valueOf(4));
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onStart() {
        super.onStart();
        //Log.d("Probam :", String.valueOf(1));
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            //updateUI(currentUser);
            FragmentTransaction ft = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.nav_host_fragment, new ProfileFragment());
            ft.commit();
        }
    }
    private void updateUI(FirebaseUser user) {
        if(user!=null) {
            //Log.d("Probam3 :", String.valueOf(user));
            User user1;
            String username = null;
            if (user.getDisplayName() == null) {
                //username=naziv;
            } else {
                username = user.getDisplayName();
            }
            String url = null;
            //profilePicUrl="https://graph.facebook.com/"+token.getUserId()+"/picture?type=large";
            if (profilePicUrl == null) {
                if (user.getPhotoUrl() != null) {
                    url = user.getPhotoUrl().toString();
                }
            } else {
                url = profilePicUrl;
            }
            String email = user.getEmail();
            String uid = user.getUid();
            user1 = new User(uid, username, url, "", "", "", email, "");
            Task<Void> mDatabaseRef;
            Map<String, Object> postValues2 = user1.toMap();
            if (FirebaseDatabase.getInstance().getReference("Kor").child(uid) == null) {
                mDatabaseRef = FirebaseDatabase.getInstance().getReference("Kor").child(uid).updateChildren(postValues2);
                String finalUsername = username;
                String finalUrl = url;
                mDatabaseRef.addOnSuccessListener(aVoid -> {
                    SharedPreferences prefs = Objects.requireNonNull(getContext()).getSharedPreferences("shared_pref_name", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("email", email);
                    editor.putString("username", finalUsername);
                    editor.putString("uid", uid);
                    editor.putBoolean("hasLogin", true);
                    editor.putString("url", user1.getUrl());
                    editor.apply();
                    NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
                    View headerView = navigationView.getHeaderView(0);
                    ime_nav = headerView.findViewById(R.id.ime_navigation);
                    email_nav = headerView.findViewById(R.id.email_navigation);
                    ime_nav.setText(finalUsername);
                    email_nav.setText(email);
                    profile_photo=headerView.findViewById(R.id.imageViewprofile);
                    log=headerView.findViewById(R.id.log_in);
                    log.setText(R.string.log_out);
                    if(finalUrl !=null){
                        Glide.with(getActivity()).load(finalUrl).apply(RequestOptions.circleCropTransform()).into(profile_photo);
                    }
                    ProfileFragment fragment=new ProfileFragment();
                    //Bundle args = new Bundle();
                    //args.putString("login", "log");
                    //fragment.setArguments(args);
                    FragmentTransaction ft = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.nav_host_fragment, fragment);
                    //ft.addToBackStack("tag_back2");
                    ft.commit();
                }).addOnFailureListener(e -> Log.d("Neuspjel ", "upload"));
            } else if (FirebaseDatabase.getInstance().getReference("Kor").child(uid) != null || user_dohvati != null) {
                SharedPreferences prefs = Objects.requireNonNull(getContext()).getSharedPreferences("shared_pref_name", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                String finalUrl = url;
                String uid_pri = user_dohvati.getUid();
                editor.putString("email", user_dohvati.getEmail());
                editor.putString("username", user_dohvati.getIme());
                editor.putString("uid", uid_pri);
                editor.putBoolean("hasLogin", true);
                editor.putString("url", user_dohvati.getUrl());
                editor.apply();
                SharedPreferences prefss = Objects.requireNonNull(getContext()).getSharedPreferences("shared_pref_name", Context.MODE_PRIVATE);
                NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
                View headerView = navigationView.getHeaderView(0);
                ime_nav = headerView.findViewById(R.id.ime_navigation);
                email_nav = headerView.findViewById(R.id.email_navigation);
                ime_nav.setText(user_dohvati.getIme());
                email_nav.setText(user_dohvati.getEmail());
                profile_photo=headerView.findViewById(R.id.imageViewprofile);
                log=headerView.findViewById(R.id.log_in);
                log.setText(R.string.log_out);
                //Log.d("LoginF:",finalUrl);
                //Log.d("LoginF1:", String.valueOf(profile_photo));
                if(finalUrl!=null){
                    Glide.with(getActivity()).load(finalUrl).apply(RequestOptions.circleCropTransform()).into(profile_photo);
                }

                ProfileFragment fragment=new ProfileFragment();
                //Bundle args = new Bundle();
                //args.putString("login", "log");
                //fragment.setArguments(args);
                FragmentTransaction ft = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.nav_host_fragment, fragment);
                ft.commit();
            }
        }else{
            FragmentTransaction ft = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.nav_host_fragment, new LoginFragment());
            ft.commit();
        }
    }
    @Override
    public void onClick(View v) {
        if(v.equals(googleSignInButton)){
            signIn();
        }else if(v.equals(reg)){
            FragmentTransaction ft = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.nav_host_fragment, new RegisterFragment());
            ft.addToBackStack("log_frag");
            ft.commit();
            //finish();
        }else if(v.equals(login)){
            if(!(email.getText().toString().trim().equals("")) && !(lozinka.getText().toString().trim().equals(""))){
                log();
            }else{
                Toast.makeText(getContext(), "email ili lozinka prazni.",
                        Toast.LENGTH_SHORT).show();
            }
        }

    }
    //facebook
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("TAG", "handleFacebookAccessToken:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(Objects.requireNonNull(getActivity()), task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Tag", "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        user_dohvati=new User();
                        user_dohvati.setEmail(user.getEmail());
                        user_dohvati.setUid(user.getUid());
                        user_dohvati.setIme(user.getDisplayName());
                        assert user != null;
                        Log.d("Taguser", user.toString());
                        try {
                            profilePicUrl="https://graph.facebook.com/"+token.getUserId()+"/picture?type=large";
                            user_dohvati.setUrl(profilePicUrl);
                            Log.d("Facebook:slika",profilePicUrl);
                        }catch (Exception e)
                        {
                            user_dohvati.setUrl(user.getPhotoUrl().toString());
                            Log.d("Facebook:error:catch", Objects.requireNonNull(e.getMessage()));
                        }
                        updateUI(user);

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Tag", "signInWithCredential:failure", task.getException());
                        Toast.makeText(getContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
        }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("Tag", "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(Objects.requireNonNull(getActivity()), task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Tag", "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        user_dohvati=new User();
                        assert user != null;
                        user_dohvati.setUid(user.getUid());
                        user_dohvati.setEmail(user.getEmail());
                        user_dohvati.setIme(user.getDisplayName());
                        user_dohvati.setUrl(user.getPhotoUrl().toString());
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Tag", "signInWithCredential:failure", task.getException());
                        Toast.makeText(getContext(),"Autentikacija neuspjela",Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }
    public void log() {

        mAuth.signInWithEmailAndPassword(email.getText().toString(), lozinka.getText().toString())
                .addOnCompleteListener(Objects.requireNonNull(getActivity()), task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("log s emailom i pass", "signInWithEmail:success");
                        final FirebaseUser user = mAuth.getCurrentUser();
                        assert user != null;
                        myRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                              @Override
                              public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                  //dodala mozda maknem kasnije
                                  user_dohvati= dataSnapshot.getValue(User.class);
                                  Log.d("log():",user_dohvati.toString());
                                  //Log.d("evoooo",dataSnapshot.toString());
                                  assert user_dohvati != null;
                                  //naziv = skl.getNaziv();
                                  updateUI(user);
                                  //Log.d("naziv",naziv);
                              }

                              @Override
                              public void onCancelled(@NonNull DatabaseError databaseError) {
                                  Toast.makeText(getContext(), "Otkazan log in error: " + databaseError, Toast.LENGTH_SHORT).show();
                              }
                          }
                        );
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("log s emailom i pas", "signInWithEmail:failure", task.getException());
                        Toast.makeText(getActivity(), "Authentication failed. error: " + task.getException(), Toast.LENGTH_SHORT).show();

                    }
                });

    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        //za google log in
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                //firebaseAuthWithGoogle(account.getIdToken());
                firebaseAuthWithGoogle(account);
                //Toast.makeText(getBaseContext(),"Uspjesan log in",Toast.LENGTH_SHORT).show;
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("tag", "Google sign in failed", e);
                Toast.makeText(getContext(),"Nespjesan log in error: "+e,Toast.LENGTH_SHORT).show();
            }
        }else{
            // Pass the activity result back to the Facebook SDK
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
}
