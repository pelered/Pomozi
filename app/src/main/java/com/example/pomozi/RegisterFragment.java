package com.example.pomozi;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pomozi.Adapter.PlaceAutoSuggestAdapter;
import com.example.pomozi.Model.User;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Objects;

public class RegisterFragment extends Fragment implements View.OnClickListener{
    private String TAG = "Tag";

    private Button reg;

    private EditText ime,email,lozinka,potvrda,tel_broj;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private AutoCompleteTextView autoCompleteTextView,grad,zupanija;
    private View v;
    private TextView log,ime_nav,email_nav;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        v=view;
        email=view.findViewById(R.id.email);
        lozinka=view.findViewById(R.id.lozinka);
        potvrda=view.findViewById(R.id.potvrdi);
        tel_broj=view.findViewById(R.id.broj);

        ime=view.findViewById(R.id.naziv);
        reg=view.findViewById(R.id.button2);
        reg.setOnClickListener(this);



        grad=view.findViewById(R.id.grad);
        grad.setAdapter(new PlaceAutoSuggestAdapter(getActivity(),android.R.layout.simple_list_item_1));
        grad.setOnItemClickListener((parent, vieww, position, id) -> {
            if(autoCompleteTextView!=null) {
                Log.d("Address : ", autoCompleteTextView.getText().toString());

                LatLng latLng = getLatLngFromAddress(autoCompleteTextView.getText().toString());
                if (latLng != null) {
                    //Log.d("Lat Lng : ", " " + latLng.latitude + " " + latLng.longitude);
                    Address address = getAddressFromLatLng(latLng);
                    if (address != null) {
                        zupanija.setText(address.getAdminArea());
                    } else {
                        Log.d("Adddress", "Address Not Found");
                    }
                } else {
                    Log.d("Lat Lng", "Lat Lng Not Found");
                }
            }

        });
        zupanija=view.findViewById(R.id.zupanija);
        zupanija.setAdapter(new PlaceAutoSuggestAdapter(getActivity(),android.R.layout.simple_list_item_1));
        zupanija.setOnItemClickListener((parent, vieww, position, id) -> {
            if(autoCompleteTextView!=null) {
                Log.d("Address : ", autoCompleteTextView.getText().toString());

                LatLng latLng = getLatLngFromAddress(autoCompleteTextView.getText().toString());
                if (latLng != null) {
                    //Log.d("Lat Lng : ", " " + latLng.latitude + " " + latLng.longitude);
                    Address address = getAddressFromLatLng(latLng);
                    if (address != null) {
                    } else {
                        Log.d("Adddress", "Address Not Found");
                    }
                } else {
                    Log.d("Lat Lng", "Lat Lng Not Found");
                }
            }

        });
        autoCompleteTextView=view.findViewById(R.id.autocomplete);
        autoCompleteTextView.setAdapter(new PlaceAutoSuggestAdapter(getActivity(),android.R.layout.simple_list_item_1));
        autoCompleteTextView.setOnItemClickListener((parent, vieww, position, id) -> {
            if(autoCompleteTextView!=null) {
                //Log.d("Address : ", autoCompleteTextView.getText().toString());
                LatLng latLng = getLatLngFromAddress(autoCompleteTextView.getText().toString());
                if (latLng != null) {
                    Log.d("Lat Lng : ", " " + latLng.latitude + " " + latLng.longitude);
                    Address address = getAddressFromLatLng(latLng);
                    if (address != null) {
                        zupanija.setText(address.getAdminArea());
                        grad.setText(address.getLocality());
                    } else {
                        Log.d("Adddress", "Address Not Found");
                    }
                } else {
                    Log.d("Lat Lng", "Lat Lng Not Found");
                }
            }

        });
        mAuth = FirebaseAuth.getInstance();
    }
    @Override
    public void onClick(View view) {
        if(provjeri()){
            //Log.d("Naziv",skriven.getText().toString());
            try{
                mAuth.createUserWithEmailAndPassword(email.getText().toString(), lozinka.getText().toString()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        assert user != null;
                        updateUI(user);
                        //String uid, String ime, String url, String add,String zupanija,  String grad, String email, String tel_broj


                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(getActivity(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show();

                    }
                });
            }catch (Exception e){
                Log.w(TAG, "Neuspjeh: ", e);
            }
        }
    }
    private void updateUI(FirebaseUser user) {
        User user_up=new User(user.getUid(),ime.getText().toString(),"",autoCompleteTextView.getText().toString(),zupanija.getText().toString(),grad.getText().toString(),
                email.getText().toString(),tel_broj.getText().toString());
        Log.d("onClick::",user_up.toString());
        myRef=database.getInstance().getReference("Kor");
        myRef.child(user.getUid()).setValue(user_up);
        String username;
        if(user.getDisplayName()==null ){
            username=ime.getText().toString();
        }else{
            username=user.getDisplayName();
        }
        String email=user.getEmail();
        String uid=user.getUid();
        SharedPreferences prefs = requireContext().getSharedPreferences("shared_pref_name", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("email",email);
        editor.putString("username", username);
        editor.putString("uid",uid);
        editor.putString("grad",user_up.getGrad());
        editor.putString("zupanija",user_up.getZupanija());
        editor.putString("tel_broj",user_up.getTel_broj());
        editor.putString("url","");
        editor.putString("add",user_up.getAdd());
        editor.putBoolean("hasLogin",true);
        Log.d("onClick::",editor.toString());
        editor.apply();
        NavigationView navigationView =getActivity().findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        ime_nav=headerView.findViewById(R.id.ime_navigation);
        email_nav=headerView.findViewById(R.id.email_navigation);
        log=headerView.findViewById(R.id.log_in);
        log.setText(R.string.log_out);
        ime_nav.setText(username);
        email_nav.setText(email);
        Menu menu=navigationView.getMenu();
        MenuItem item =menu.findItem(R.id.dodaj_objavu);
        item.setVisible(true);
        item =menu.findItem(R.id.ispis_objava);
        item.setVisible(true);
        item =menu.findItem(R.id.moj_profil);
        item.setVisible(true);
        //image with glide
        FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.nav_host_fragment, new ProfileFragment());
        ft.commit();

    }

    public boolean provjeri(){
        //Log.d("Register:", String.valueOf(lozinka.getText().toString().trim().length()));
        if(!(lozinka.getText().toString().trim().length() < 6)) {
            if (!(lozinka.getText().toString().equals(potvrda.getText().toString()))) {
                Toast.makeText(getActivity(), "Lozinke nisu iste", Toast.LENGTH_LONG).show();
                return false;
            } else {
                return true;
            }
        }else{
            Toast.makeText(getActivity(), "Lozinka mora imati minimun 6 znakova", Toast.LENGTH_LONG).show();
            return false;
        }
    }
    private LatLng getLatLngFromAddress(String address){

        Geocoder geocoder=new Geocoder(getActivity());
        List<Address> addressList;

        try {
            addressList = geocoder.getFromLocationName(address, 1);
            if(addressList!=null){
                Address singleaddress=addressList.get(0);
                return new LatLng(singleaddress.getLatitude(),singleaddress.getLongitude());
            }
            else{
                return null;
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }

    private Address getAddressFromLatLng(LatLng latLng){
        Geocoder geocoder=new Geocoder(getActivity());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 5);
            if(addresses!=null){
                return addresses.get(0);
            }
            else{
                return null;
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }
}
