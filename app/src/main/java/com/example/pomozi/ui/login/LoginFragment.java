package com.example.pomozi.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.pomozi.Model.User;
import com.example.pomozi.R;
import com.example.pomozi.ui.ProfileFragment;
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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

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

    public View onCreateView(@NonNull LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login,container,false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
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
            updateUI(currentUser);
        }
    }
    private void updateUI(FirebaseUser user) {
        Log.d("Probam3 :", String.valueOf(user));
        User user1;
        String username = null;
        if(user.getDisplayName()==null){
            //username=naziv;
        }else{
            username=user.getDisplayName();
        }
        String url = null;
        //profilePicUrl="https://graph.facebook.com/"+token.getUserId()+"/picture?type=large";
        if(profilePicUrl==null){
            if(user.getPhotoUrl()!=null) {
                url = user.getPhotoUrl().toString();
            }
        }else{
            url=profilePicUrl;
        }
        String email=user.getEmail();
        String uid=user.getUid();

        user1=new User(uid,username,url,email);
        Task<Void> mDatabaseRef;
        Map<String, Object> postValues2=user1.toMap();
        mDatabaseRef= FirebaseDatabase.getInstance().getReference("Kor").child(uid).updateChildren(postValues2);
        String finalUsername = username;
        mDatabaseRef.addOnSuccessListener(aVoid -> {
            Log.d("Uspjel ", "upload");
            SharedPreferences prefs = Objects.requireNonNull(getContext()).getSharedPreferences("shared_pref_name", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("email",email);
            editor.putString("username",finalUsername);
            editor.putString("uid",uid);
            editor.putBoolean("hasLogin",true);
            editor.putString("url",user1.getUrl());
            Log.d("updateUser()1",user1.toString());
            editor.apply();
            //image with glide
            FragmentTransaction ft = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.nav_host_fragment, new ProfileFragment());
            //ft.addToBackStack("tag_back2");
            ft.commit();
        }).addOnFailureListener(e -> Log.d("Neuspjel ", "upload"));
    }
    @Override
    public void onClick(View v) {
        if(v.equals(googleSignInButton)){
            signIn();
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
                        //Log.d("Tagfacebook" +                                    "1", token.getUserId().toString());;
                        //Log.d("Tagfacebook" +                                    "2", String.valueOf(task.getResult().getUser().getPhotoUrl()));
                        //Log.d("Tagfacebook" +                                    "3", task.getResult().getAdditionalUserInfo().getProfile().toString());;
                        FirebaseUser user = mAuth.getCurrentUser();
                        assert user != null;
                        Log.d("Taguser", user.toString());
                        try {
                            profilePicUrl="https://graph.facebook.com/"+token.getUserId()+"/picture?type=large";
                            Log.d("Facebook:slika",profilePicUrl);
                        }catch (Exception e)
                        {
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
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Tag", "signInWithCredential:failure", task.getException());
                        Toast.makeText(getContext(),"Autentikacija neuspjela",Toast.LENGTH_SHORT).show();
                        updateUI(null);
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
