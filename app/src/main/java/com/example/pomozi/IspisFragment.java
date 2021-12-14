package com.example.pomozi;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.pomozi.Adapter.IspisAdapterZiv;
import com.example.pomozi.Helper.MySwipeHelper;
import com.example.pomozi.Model.ZivUpload;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class IspisFragment extends Fragment {
    private String grad,zup,vrsta,stanje,status;
    private RecyclerView mRecyclerView;
    private IspisAdapterZiv mAdapter;
    private ProgressBar mProgressCircle;
    private List<ZivUpload> mUploads;
    private DatabaseReference mDatabaseRef;
    private String uid;
    private ZivUpload dohvati;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ispis, container, false);
    }
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mProgressCircle=view.findViewById(R.id.progress_circle);
        mUploads = new ArrayList<>();
        mProgressCircle.setVisibility(View.VISIBLE);
        if(getArguments()==null){
            mDatabaseRef = FirebaseDatabase.getInstance().getReference("Ziv");
            ucitaj_podatke();
            //Toast.makeText(getContext(),"Nisi smio ovo uspjet,javi mi kako",Toast.LENGTH_SHORT).show();
        }else if(getArguments().getBoolean("search")){
            grad=getArguments().getString("grad");
            zup=getArguments().getString("zup");
            stanje=getArguments().getString("stanje");
            status=getArguments().getString("status");
            vrsta=getArguments().getString("vrsta");
            odaberi_query();
        }
        SharedPreferences prefss = Objects.requireNonNull(getContext()).getSharedPreferences("shared_pref_name", Context.MODE_PRIVATE);
        uid=prefss.getString("uid","");
        /*if(!uid.equals("") ) {
            postavi_swiper();
        }*/
    }

    @SuppressLint("RestrictedApi")
    private void odaberi_query() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Ziv");
        Query query = null;
        if(grad!=null){
            query=ref.orderByChild("grad").startAt(grad).endAt(grad+'\uf8ff');
        }else if(zup!=null){
            query=ref.orderByChild("zupanija").startAt(zup).endAt(zup+'\uf8ff');
        }else if(status!=null){
            query=ref.orderByChild("status").startAt(status).endAt(status+'\uf8ff');
        }else if(vrsta!=null){
            query=ref.orderByChild("vrsta").startAt(vrsta).endAt(vrsta+'\uf8ff');
        }else if(stanje!=null){
            query=ref.orderByChild("stanje").startAt(stanje).endAt(stanje+'\uf8ff');
        }else{
            query=ref.orderByChild("last_date");
        }
        Log.d("Odaberi:",query.getPath().toString());
        dohvati_podatke(query);

    }

    private void dohvati_podatke(Query query) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("Dohvati_p0:",dataSnapshot.toString());
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Log.d("Dohvati_p1:",postSnapshot.toString());
                    if (grad != null) {
                        Log.d("Dohvati_p2:",postSnapshot.toString());
                        //trazi se odreden grad
                        if (status!=null) {
                            Log.d("Dohvati_p3:",postSnapshot.child("status").toString());
                            //trazi se odreden status
                            if(postSnapshot.child("status").getValue().equals(status)){
                                Log.d("Dohvati_p4:",postSnapshot.toString());
                                //naden odreden status ziv
                                if(vrsta!=null){
                                    Log.d("Dohvati_p5:",postSnapshot.toString());
                                    //trazi se odredena vrsta
                                    if(postSnapshot.child("vrsta").getValue().equals(vrsta)){
                                        Log.d("Dohvati_p6:",postSnapshot.toString());
                                        //nadena odredena vrsta ziv
                                        if(stanje!=null){
                                            Log.d("Dohvati_p7:",postSnapshot.toString());
                                            //razi se odredeno stanje
                                            if(postSnapshot.child("stanje").getValue().equals(stanje)){
                                                Log.d("Dohvati_p8:",postSnapshot.toString());
                                                //nadeno odredeno stanje
                                                dohvati = postSnapshot.getValue(ZivUpload.class);
                                                assert dohvati != null;
                                                dohvati.setKey(postSnapshot.getKey());
                                                mUploads.add(dohvati);
                                            }else{
                                                Log.d("Dohvati_p9:",postSnapshot.toString());
                                                //nije nadena ziv po zadanim zahtjevima
                                                //Toast.makeText(getActivity(),"Nije nadeno po parametrima:"+grad+status+vrsta+stanje,Toast.LENGTH_SHORT);
                                            }
                                        }else {
                                            //nije odredeno stanje
                                            dohvati = postSnapshot.getValue(ZivUpload.class);
                                            assert dohvati != null;
                                            dohvati.setKey(postSnapshot.getKey());
                                            mUploads.add(dohvati);
                                        }
                                    }else{
                                        Log.d("Dohvati_p9:",postSnapshot.toString());
                                        //Toast.makeText(getActivity(),"Nije nadeno po parametrima:"+grad+status+vrsta,Toast.LENGTH_S);
                                    }

                                }else{
                                    //nije odredena vrsta
                                    if(stanje!=null){
                                        //trazi se odredeno stanje
                                        if(postSnapshot.child("stanje").getValue().equals(stanje)){
                                            //nadeno odredeno stanje ziv
                                            dohvati = postSnapshot.getValue(ZivUpload.class);
                                            assert dohvati != null;
                                            dohvati.setKey(postSnapshot.getKey());
                                            mUploads.add(dohvati);

                                        }else{
                                            Log.d("Dohvati_p9:",postSnapshot.toString());
                                           // Toast.makeText(getActivity(),"Nije nadeno po parametrima:"+grad+status+stanje,Toast.LENGTH_LONG);
                                        }
                                    } else{
                                        //nije drendeno stanje ziv
                                        dohvati = postSnapshot.getValue(ZivUpload.class);
                                        assert dohvati != null;
                                        dohvati.setKey(postSnapshot.getKey());
                                        mUploads.add(dohvati);
                                    }
                                }
                                //ako status nije naden a postavljen je nema nijedne zivotinje i nista se ne prikaze
                            }else {
                                Log.d("Dohvati_p9:",postSnapshot.toString());
                                //Toast.makeText(getActivity(),"Nije nadeno po parametrima:"+grad+status,Toast.LENGTH_LONG);
                            }
                        //nije odreden status
                        } else if (vrsta!=null) {
                            //trazi se odredena vrsta
                            if(postSnapshot.child("vrsta").getValue().equals(vrsta)){
                                //nadena je odredena vrsta
                                if(stanje!=null){
                                    //trazi se odredeno stanje
                                    if(postSnapshot.child("stanje").getValue().equals(stanje)){
                                        //nadeno odredeno stanje
                                        dohvati = postSnapshot.getValue(ZivUpload.class);
                                        assert dohvati != null;
                                        dohvati.setKey(postSnapshot.getKey());
                                        mUploads.add(dohvati);
                                    }else{
                                        Log.d("Dohvati_p9:",postSnapshot.toString());
                                        //nije nadena ziv po zadanim zahtjevima
                                        //Toast.makeText(getActivity(),"Nije nadeno po parametrima:"+ grad +vrsta +stanje,Toast.LENGTH_LONG);
                                    }
                                }else{
                                    //sva stanja trazim ziv
                                    dohvati = postSnapshot.getValue(ZivUpload.class);
                                    assert dohvati != null;
                                    dohvati.setKey(postSnapshot.getKey());
                                    mUploads.add(dohvati);
                                }
                            }else{
                                //nije nadeda odredena vrsta
                                Log.d("Dohvati_p9:",postSnapshot.toString());
                                //Toast.makeText(getActivity(),"Nije nadeno po parametrima:"+grad+vrsta,Toast.LENGTH_LONG);
                            }
                        } else if (stanje!=null) {
                            if(postSnapshot.child("stanje").getValue().equals(stanje)){
                                //nadeno odredeno stanje
                                dohvati = postSnapshot.getValue(ZivUpload.class);
                                assert dohvati != null;
                                dohvati.setKey(postSnapshot.getKey());
                                mUploads.add(dohvati);
                            }else{
                                //nije nadena ziv po zadanim zahtjevima
                                Log.d("Dohvati_p9:",postSnapshot.toString());
                                //Toast.makeText(getActivity(),"Nije nadeno po parametrima:"+ grad +stanje,Toast.LENGTH_LONG);
                            }
                        }else{
                            Log.d("Dohvati_pgrad:",postSnapshot.toString());
                            dohvati = postSnapshot.getValue(ZivUpload.class);
                            assert dohvati != null;
                            dohvati.setKey(postSnapshot.getKey());
                            mUploads.add(dohvati);
                        }
                    } else if (zup != null) {
                        if(status!=null){
                            Log.d("Dohvati_p3:",postSnapshot.child("status").toString());
                            //trazi se odreden status
                            if(postSnapshot.child("status").getValue().equals(status)){
                                Log.d("Dohvati_p4:",postSnapshot.toString());
                                //naden odreden status ziv
                                if(vrsta!=null){
                                    Log.d("Dohvati_p5:",postSnapshot.toString());
                                    //trazi se odredena vrsta
                                    if(postSnapshot.child("vrsta").getValue().equals(vrsta)){
                                        Log.d("Dohvati_p6:",postSnapshot.toString());
                                        //nadena odredena vrsta ziv
                                        if(stanje!=null){
                                            Log.d("Dohvati_p7:",postSnapshot.toString());
                                            //razi se odredeno stanje
                                            if(postSnapshot.child("stanje").getValue().equals(stanje)){
                                                Log.d("Dohvati_p8:",postSnapshot.toString());
                                                //nadeno odredeno stanje
                                                dohvati = postSnapshot.getValue(ZivUpload.class);
                                                assert dohvati != null;
                                                dohvati.setKey(postSnapshot.getKey());
                                                mUploads.add(dohvati);
                                            }else{
                                                Log.d("Dohvati_p9:",postSnapshot.toString());
                                                //nije nadena ziv po zadanim zahtjevima
                                                //Toast.makeText(getActivity(),"Nije nadeno po parametrima:"+grad+status+vrsta+stanje,Toast.LENGTH_SHORT);
                                            }
                                        }else {
                                            //nije odredeno stanje
                                            dohvati = postSnapshot.getValue(ZivUpload.class);
                                            assert dohvati != null;
                                            dohvati.setKey(postSnapshot.getKey());
                                            mUploads.add(dohvati);
                                        }
                                    }else{
                                        Log.d("Dohvati_p9:",postSnapshot.toString());
                                        //Toast.makeText(getActivity(),"Nije nadeno po parametrima:"+grad+status+vrsta,Toast.LENGTH_S);
                                    }
                                }else{
                                    //nije odredena vrsta
                                    if(stanje!=null){
                                        //trazi se odredeno stanje
                                        if(postSnapshot.child("stanje").getValue().equals(stanje)){
                                            //nadeno odredeno stanje ziv
                                            dohvati = postSnapshot.getValue(ZivUpload.class);
                                            assert dohvati != null;
                                            dohvati.setKey(postSnapshot.getKey());
                                            mUploads.add(dohvati);
                                        }else{
                                            Log.d("Dohvati_p9:",postSnapshot.toString());
                                            // Toast.makeText(getActivity(),"Nije nadeno po parametrima:"+grad+status+stanje,Toast.LENGTH_LONG);
                                        }
                                    } else{
                                        //nije drendeno stanje ziv
                                        dohvati = postSnapshot.getValue(ZivUpload.class);
                                        assert dohvati != null;
                                        dohvati.setKey(postSnapshot.getKey());
                                        mUploads.add(dohvati);
                                    }
                                }
                            }else {
                                Log.d("Dohvati_p9_nema:",postSnapshot.toString());
                            }
                        }else if(vrsta!=null){
                            //trazi se odredena vrsta
                            if(postSnapshot.child("vrsta").getValue().equals(vrsta)){
                                //nadena je odredena vrsta
                                if(stanje!=null){
                                    //trazi se odredeno stanje
                                    if(postSnapshot.child("stanje").getValue().equals(stanje)){
                                        //nadeno odredeno stanje
                                        dohvati = postSnapshot.getValue(ZivUpload.class);
                                        assert dohvati != null;
                                        dohvati.setKey(postSnapshot.getKey());
                                        mUploads.add(dohvati);
                                    }else{
                                        Log.d("Dohvati_p9:",postSnapshot.toString());
                                        //nije nadena ziv po zadanim zahtjevima
                                        //Toast.makeText(getActivity(),"Nije nadeno po parametrima:"+ grad +vrsta +stanje,Toast.LENGTH_LONG);
                                    }
                                }else{
                                    //sva stanja trazim ziv
                                    dohvati = postSnapshot.getValue(ZivUpload.class);
                                    assert dohvati != null;
                                    dohvati.setKey(postSnapshot.getKey());
                                    mUploads.add(dohvati);
                                }
                            }else{
                                //nije nadeda odredena vrsta
                                Log.d("Dohvati_p9:",postSnapshot.toString());
                                //Toast.makeText(getActivity(),"Nije nadeno po parametrima:"+grad+vrsta,Toast.LENGTH_LONG);
                            }
                        }else  if(stanje!=null){
                            if(postSnapshot.child("stanje").getValue().equals(stanje)){
                                //nadeno odredeno stanje
                                dohvati = postSnapshot.getValue(ZivUpload.class);
                                assert dohvati != null;
                                dohvati.setKey(postSnapshot.getKey());
                                mUploads.add(dohvati);
                            }else{
                                //nije nadena ziv po zadanim zahtjevima
                                Log.d("Dohvati_p9:",postSnapshot.toString());
                                //Toast.makeText(getActivity(),"Nije nadeno po parametrima:"+ grad +stanje,Toast.LENGTH_LONG);
                            }
                        }else {
                            //ako je sve osim zup prazno
                            Log.d("Dohvati_pzup:",postSnapshot.toString());
                            dohvati = postSnapshot.getValue(ZivUpload.class);
                            assert dohvati != null;
                            dohvati.setKey(postSnapshot.getKey());
                            mUploads.add(dohvati);
                        }
                    } else if (status != null) {
                        Log.d("Dohvati_p3:",postSnapshot.child("status").toString());
                        //trazi se odreden status
                        if(postSnapshot.child("status").getValue().equals(status)){
                            Log.d("Dohvati_p4:",postSnapshot.toString());
                            //naden odreden status ziv
                            if(vrsta!=null){
                                Log.d("Dohvati_p5:",postSnapshot.toString());
                                //trazi se odredena vrsta
                                if(postSnapshot.child("vrsta").getValue().equals(vrsta)){
                                    Log.d("Dohvati_p6:",postSnapshot.toString());
                                    //nadena odredena vrsta ziv
                                    if(stanje!=null){
                                        Log.d("Dohvati_p7:",postSnapshot.toString());
                                        //razi se odredeno stanje
                                        if(postSnapshot.child("stanje").getValue().equals(stanje)){
                                            Log.d("Dohvati_p8:",postSnapshot.toString());
                                            //nadeno odredeno stanje
                                            dohvati = postSnapshot.getValue(ZivUpload.class);
                                            assert dohvati != null;
                                            dohvati.setKey(postSnapshot.getKey());
                                            mUploads.add(dohvati);
                                        }else{
                                            Log.d("Dohvati_p9:",postSnapshot.toString());
                                            //nije nadena ziv po zadanim zahtjevima
                                            //Toast.makeText(getActivity(),"Nije nadeno po parametrima:"+grad+status+vrsta+stanje,Toast.LENGTH_SHORT);
                                        }
                                    }else {
                                        //nije odredeno stanje
                                        dohvati = postSnapshot.getValue(ZivUpload.class);
                                        assert dohvati != null;
                                        dohvati.setKey(postSnapshot.getKey());
                                        mUploads.add(dohvati);
                                    }
                                }else{
                                    Log.d("Dohvati_p9:",postSnapshot.toString());
                                    //Toast.makeText(getActivity(),"Nije nadeno po parametrima:"+grad+status+vrsta,Toast.LENGTH_S);
                                }
                            }else{
                                //nije odredena vrsta
                                if(stanje!=null){
                                    //trazi se odredeno stanje
                                    if(postSnapshot.child("stanje").getValue().equals(stanje)){
                                        //nadeno odredeno stanje ziv
                                        dohvati = postSnapshot.getValue(ZivUpload.class);
                                        assert dohvati != null;
                                        dohvati.setKey(postSnapshot.getKey());
                                        mUploads.add(dohvati);
                                    }else{
                                        Log.d("Dohvati_p9:",postSnapshot.toString());
                                        // Toast.makeText(getActivity(),"Nije nadeno po parametrima:"+grad+status+stanje,Toast.LENGTH_LONG);
                                    }
                                } else{
                                    //nije drendeno stanje ziv
                                    dohvati = postSnapshot.getValue(ZivUpload.class);
                                    assert dohvati != null;
                                    dohvati.setKey(postSnapshot.getKey());
                                    mUploads.add(dohvati);
                                }
                            }
                        }else {
                            Log.d("Dohvati_p9_nema:",postSnapshot.toString());
                        }
                    } else if (vrsta != null) {
                        Log.d("Dohvati_p5:",postSnapshot.toString());
                        //trazi se odredena vrsta
                        if(postSnapshot.child("vrsta").getValue().equals(vrsta)){
                            Log.d("Dohvati_p6:",postSnapshot.toString());
                            //nadena odredena vrsta ziv
                            if(stanje!=null){
                                Log.d("Dohvati_p7:",postSnapshot.toString());
                                //razi se odredeno stanje
                                if(postSnapshot.child("stanje").getValue().equals(stanje)){
                                    Log.d("Dohvati_p8:",postSnapshot.toString());
                                    //nadeno odredeno stanje
                                    dohvati = postSnapshot.getValue(ZivUpload.class);
                                    assert dohvati != null;
                                    dohvati.setKey(postSnapshot.getKey());
                                    mUploads.add(dohvati);
                                }else{
                                    Log.d("Dohvati_p9:",postSnapshot.toString());
                                    //nije nadena ziv po zadanim zahtjevima
                                    //Toast.makeText(getActivity(),"Nije nadeno po parametrima:"+grad+status+vrsta+stanje,Toast.LENGTH_SHORT);
                                }
                            }else {
                                //nije odredeno stanje
                                dohvati = postSnapshot.getValue(ZivUpload.class);
                                assert dohvati != null;
                                dohvati.setKey(postSnapshot.getKey());
                                mUploads.add(dohvati);
                            }
                        }else{
                            Log.d("Dohvati_p9:",postSnapshot.toString());
                            //Toast.makeText(getActivity(),"Nije nadeno po parametrima:"+grad+status+vrsta,Toast.LENGTH_S);
                        }
                    } else if (stanje != null) {
                        Log.d("Dohvati_p7:",postSnapshot.toString());
                        //razi se odredeno stanje
                        if(postSnapshot.child("stanje").getValue().equals(stanje)){
                            Log.d("Dohvati_p8:",postSnapshot.toString());
                            //nadeno odredeno stanje
                            dohvati = postSnapshot.getValue(ZivUpload.class);
                            assert dohvati != null;
                            dohvati.setKey(postSnapshot.getKey());
                            mUploads.add(dohvati);
                        }else{
                            Log.d("Dohvati_p9:",postSnapshot.toString());
                            //nije nadena ziv po zadanim zahtjevima
                            //Toast.makeText(getActivity(),"Nije nadeno po parametrima:"+grad+status+vrsta+stanje,Toast.LENGTH_SHORT);
                        }
                    } else {
                        dohvati = postSnapshot.getValue(ZivUpload.class);
                        assert dohvati != null;
                        dohvati.setKey(postSnapshot.getKey());
                        mUploads.add(dohvati);
                    }
                }
                mAdapter=new IspisAdapterZiv(getActivity(),mUploads);
                mAdapter = new IspisAdapterZiv(getActivity(), mUploads);
                mRecyclerView.setAdapter(mAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Error:",databaseError.getMessage());
            }
        });
    }


    private void postavi_swiper() {
        MySwipeHelper swipeHelper = new MySwipeHelper(getContext(),mRecyclerView,200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MySwipeHelper.MyButton> buffer) {
                buffer.add(new MyButton(Objects.requireNonNull(getContext()),"Izbriši",30,R.drawable.ic_delete_black, Color.parseColor("#FFFFFF"),
                        pos -> {
                    //todo stavi da provjeri si vlasnik ove zivotinje
                            Toast.makeText(getActivity(),"Delete click",Toast.LENGTH_SHORT).show();
                            Log.d("KliK: ", String.valueOf(pos));
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Potvrda za brisanje");
                            builder.setMessage("Sigurno želite obrisati?");
                            builder.setCancelable(false);
                            builder.setPositiveButton("Yes", (dialog, which) -> {
                                Toast.makeText(getContext(), "You've choosen to delete all records", Toast.LENGTH_SHORT).show();
                                obrisi_ziv(pos);
                            });
                            builder.setNegativeButton("No", (dialog, which) -> Toast.makeText(getContext(), "Neće se obrisati", Toast.LENGTH_SHORT).show());
                            builder.show();

                        }));
                buffer.add(new MyButton(getContext(),"Ažuriraj",30, R.drawable.ic_mode_edit,Color.parseColor("#FFFFFF"),
                        pos -> {
                            // Log.d("KliK2: ",  mUploads.get(pos).getOznaka());
                            EditZiv fragment=new EditZiv();
                            Bundle args = new Bundle();
                            args.putString("oznaka", mUploads.get(pos).getKey());
                            fragment.setArguments(args);
                            FragmentTransaction ft =((FragmentActivity) getContext()).getSupportFragmentManager().beginTransaction();
                            ft.replace(R.id.nav_host_fragment, fragment);
                            ft.addToBackStack("tag_ispis_");
                            ft.commit();

                        }));
            }
        };

    }

    private void obrisi_ziv(int pos) {
        for(int i =0; i<mUploads.get(pos).getUrl().size();i++){
            int finalI = i;
            FirebaseStorage.getInstance().getReferenceFromUrl(Objects.requireNonNull(mUploads.get(pos).getUrl().get(i + "_key"))).delete().addOnSuccessListener(aVoid -> {
                if (finalI+1==mUploads.get(pos).getUrl().size()){
                    FirebaseDatabase.getInstance().getReference("Ziv").child(mUploads.get(pos).getKey()).removeValue().addOnSuccessListener(aVoidd -> {
                        Toast.makeText(getContext(),"Uspješno izbrisano",Toast.LENGTH_SHORT).show();
                        mUploads.remove(pos);
                        mAdapter.notifyItemRemoved(pos);

                    }).addOnFailureListener(e -> Toast.makeText(getContext(),"NeUspješno izbrisano",Toast.LENGTH_SHORT).show());
                }
            }).addOnFailureListener(e -> {

            });
        }
    }

    private void ucitaj_podatke() {
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    //Log.d("Ispisujem",postSnapshot.getValue().toString());
                    dohvati = postSnapshot.getValue(ZivUpload.class);
                    assert dohvati != null;
                    dohvati.setKey(postSnapshot.getKey());
                    mUploads.add(dohvati);
                }
                mAdapter=new IspisAdapterZiv(getActivity(),mUploads);
                mAdapter = new IspisAdapterZiv(getActivity(), mUploads);
                mRecyclerView.setAdapter(mAdapter);
                mProgressCircle.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Neuspjela autorizacija " , Toast.LENGTH_SHORT).show();
                mProgressCircle.setVisibility(View.INVISIBLE);
            }
        });
    }
}
