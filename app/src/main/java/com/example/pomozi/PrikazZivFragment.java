package com.example.pomozi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pomozi.Adapter.SliderAdapterExample;
import com.example.pomozi.Model.Fav;
import com.example.pomozi.Model.User;
import com.example.pomozi.Model.ZivUpload;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class PrikazZivFragment extends Fragment {
    private String oznaka_ziv;
    private FirebaseDatabase database;
    private DatabaseReference mDatabaseRef;
    private SliderView sliderView1;
    private ZivUpload odabrana_ziv;
    private TextView stanje,opis,vlasnik, datumi, adresa,status,vrsta, grad, zupanija;
    private ArrayList<String> slike= new ArrayList<>();
    private ImageView favorite,profil;
    private boolean oznacen_fav=false;
    private String uid;
    private ArrayList<String> favo;
    private Fav fav1;
    private User user;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_prikaz_ziv, container, false);
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        SharedPreferences prefs = Objects.requireNonNull(getContext()).getSharedPreferences("shared_pref_name", Context.MODE_PRIVATE);

        if (getArguments()==null){
            Toast.makeText(getContext(),"Nisi smio ovo uspjet,javi mi kako",Toast.LENGTH_SHORT).show();
        }else{
            //Toast.makeText(getContext(),"Oznaka: "+getArguments().getString("oznaka"),Toast.LENGTH_SHORT).show();
            oznaka_ziv= getArguments().getString("oznaka");
        }
        database=FirebaseDatabase.getInstance();
        mDatabaseRef=database.getReference("Ziv");
        stanje=view.findViewById(R.id.stanje);
        opis=view.findViewById(R.id.opis);
        vlasnik=view.findViewById(R.id.korisnik);
        datumi =view.findViewById(R.id.datum);
        adresa =view.findViewById(R.id.address);
        status=view.findViewById(R.id.status);
        vrsta=view.findViewById(R.id.vrsta);
        grad =view.findViewById(R.id.grad_pri);
        zupanija =view.findViewById(R.id.zupanija_pri);
        sliderView1=view.findViewById(R.id.imageSlider);
        favorite=view.findViewById(R.id.favorite);
        favorite.setVisibility(View.INVISIBLE);
        profil=view.findViewById(R.id.profile_prikaz);
        vlasnik.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileFragment fragment=new ProfileFragment();
                Bundle args = new Bundle();
                //Log.d("PrikazZivvlas:",vlasnik.getText().toString());
                args.putString("id_vlasnik", user.getUid());
                fragment.setArguments(args);
                FragmentTransaction ft =getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.nav_host_fragment, fragment);
                ft.addToBackStack("tag_prikaz_ziv");
                ft.commit();
            }
        });
        favo=new ArrayList<>();
        if(prefs.getString("uid",null)!=null) {
            uid=prefs.getString("uid",null);
            favorite.setVisibility(View.VISIBLE);
            favorite.setOnClickListener(v -> {
                dodaj_favorita();
            });
        }
        ucitaj_podatke();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void dodaj_favorita() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Fav").child(uid);
        if(!oznacen_fav) {
            //oznacimo da nam se svida, oznaka je na true;
            if(fav1==null){
                favo=new ArrayList<>();
                favo.add(odabrana_ziv.getKey());
                fav1=new Fav();
            }else {
                favo.add(odabrana_ziv.getKey());
                fav1.getFav().clear();
            }
            //Log.d("dodaj_fav0:",favo.toString());
            if(fav1!=null) {
                for (int i = 0; i < favo.size(); i++) {
                    fav1.getFav().put(i + "_k", favo.get(i));
                }
            }else{
                HashMap<String,String> pr=new HashMap<>();
                pr.put((0 + "_k"),favo.get(0));
                fav1.setFav(pr);
            }
            //Log.d("dodaj_fav:",fav1.toString());
            Fav fav_up = new Fav(fav1.getFav());
            //Log.d("dodaj_fav:1",fav_up.toString());
            Map<String, Object> postValues2=fav_up.toMap();
            //Log.d("dodaj_fav:2",postValues2.toString());
            ref.updateChildren(postValues2).addOnSuccessListener(aVoid -> {
                favorite.setBackgroundResource(R.drawable.ic_favorite_black_24dp);
                oznacen_fav=true;
            }).addOnFailureListener(e -> {
                fav1.getFav().remove(fav1.getFav().size()+"_k",odabrana_ziv.getKey());
                favo.remove(odabrana_ziv.getKey());
                Toast.makeText(getActivity(),"Neuspjelo dodavanje",Toast.LENGTH_SHORT);
            });

        }else{
            //oznacimo da nam se ne sviđa,oznaka false
            String key= null;
            //String value="somename";
            for(Map.Entry<String, String> entry :fav1.getFav().entrySet()){
                if(odabrana_ziv.getKey().equals(entry.getValue())){
                    key = entry.getKey();
                    break; //breaking because its one to one map
                }
            }
            //Log.d("dodaj_fav5.5", String.valueOf(ref.child("fav").child(key)));
            ref.child("fav").child(key).removeValue((databaseError, databaseReference) -> {
                favo.remove(odabrana_ziv.getKey());
                for (int i=0;i<favo.size();i++){
                    fav1.getFav().put(i+"_k",favo.get(i));
                }
                // Log.d("dodaj_fav:6",fav1.toString());
                favorite.setBackgroundResource(R.drawable.ic_favorite_border_yellow);
                oznacen_fav=false;
            });

        }
    }

    private void ucitaj_podatke() {
        mDatabaseRef.child(oznaka_ziv).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                slike.clear();
                odabrana_ziv = dataSnapshot.getValue(ZivUpload.class);
                //Log.d("dadaj:0", dataSnapshot.getValue().toString());
                if (dataSnapshot.hasChild("url")) {
                    for (Map.Entry<String, String> entry : odabrana_ziv.getUrl().entrySet()) {
                        slike.add(entry.getValue());
                    }
                }

                odabrana_ziv.setKey(dataSnapshot.getKey());
                DatabaseReference refe = FirebaseDatabase.getInstance().getReference("Kor").child(odabrana_ziv.getId_vlasnika());
                Log.d("PrikazZiv:user",refe.toString());
                refe.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        user=dataSnapshot.getValue(User.class);
                        Log.d("PrikazZiv:user",user.toString());
                        postavi_podatke();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("onCancelled:kor: ", databaseError.getMessage());
                    }
                });
                if (uid != null){
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Fav").child(uid);
                //todo mozda queryy dodati da konkretno nađe po key zivotinje
                ref.addListenerForSingleValueEvent(new ValueEventListener(){
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        fav1 = dataSnapshot.getValue(Fav.class);
                        if (fav1 != null) {
                            for (Map.Entry<String, String> entry : fav1.getFav().entrySet()) {
                                favo.add(entry.getValue());
                            }
                            if (fav1.getFav().containsValue(odabrana_ziv.getKey())) {
                                //Log.d("dadaj:2", String.valueOf(fav1));
                                favorite.setBackgroundResource(R.drawable.ic_favorite_black_24dp);
                                oznacen_fav = true;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("onCancelled:fav: ", databaseError.getMessage());
                    }
                });

            }


            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("onCancelled:ziv: ",databaseError.getMessage());
                Toast.makeText(getActivity(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void postavi_podatke() {

        stanje.setText("Stanje: "+odabrana_ziv.getStanje());
        vlasnik.setText(user.getIme());
        if(!user.getUrl().equals("")){
            Glide.with(this).load(user.getUrl()).apply(RequestOptions.circleCropTransform()).into(profil);
        }
        grad.setText("Grad: "+odabrana_ziv.getGrad());
        datumi.setText("Stvoren: "+odabrana_ziv.getDate()+" Ažurirano: "+odabrana_ziv.getLast_date());
        status.setText("Status: "+odabrana_ziv.getStatus());
        zupanija.setText("Županija: "+ odabrana_ziv.getZupanija());
        vrsta.setText("Vrsta: "+odabrana_ziv.getVrsta());
        opis.setText(odabrana_ziv.getOpis());
        adresa.setText("Adresa: "+ odabrana_ziv.getAdresa());
        inicijalizirajSlider();

    }

    private void inicijalizirajSlider() {
        final SliderAdapterExample adapter= new SliderAdapterExample(getActivity());
        adapter.setCount(slike.size());
        adapter.slike2(slike);
        sliderView1.setSliderAdapter(adapter);
        sliderView1.setIndicatorAnimation(IndicatorAnimations.SLIDE);
        sliderView1.setSliderTransformAnimation(SliderAnimations.CUBEINROTATIONTRANSFORMATION);
        sliderView1.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH);
        sliderView1.setIndicatorSelectedColor(Color.WHITE);
        sliderView1.setIndicatorUnselectedColor(Color.GRAY);
        sliderView1.setScrollTimeInSec(15);
        sliderView1.setOnIndicatorClickListener(position -> sliderView1.setCurrentPagePosition(position));
    }
}
