package com.example.pomozi;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.pomozi.Adapter.ProfileMyAdapter;
import com.example.pomozi.Helper.MySwipeHelper;
import com.example.pomozi.Model.Fav;
import com.example.pomozi.Model.ZivUpload;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class FavFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProfileMyAdapter adapter;
    private LinearLayoutManager layoutManager;
    private Fav fav1;
    private ArrayList<String> favo=new ArrayList<>();
    private ZivUpload ziv= new ZivUpload();
    private List<ZivUpload> itemList =new ArrayList<>();
    private String uid;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fav, container, false);
    }
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        SharedPreferences prefs = Objects.requireNonNull(getContext()).getSharedPreferences("shared_pref_name", Context.MODE_PRIVATE);
        uid=prefs.getString("uid",null);
        recyclerView=view.findViewById(R.id.recycler_test);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        MySwipeHelper swipeHelper = new MySwipeHelper(getContext(),recyclerView,200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MySwipeHelper.MyButton> buffer) {
                buffer.add(new MyButton(Objects.requireNonNull(getContext()),"Delete",30,R.drawable.ic_delete_black, Color.parseColor("#FFFFFF"),
                        pos -> {
                            String key= null;
                            //Log.d("delete:",fav1.toString());
                            for(Map.Entry<String, String> entry :fav1.getFav().entrySet()){
                                //Log.d("delete1:",entry.getValue());
                                if(adapter.getItem(pos).getKey().equals(entry.getValue())){
                                    //Log.d("delete2:",entry.getKey());
                                    key = entry.getKey();
                                    break; //breaking because its one to one map
                                }
                            }
                            Toast.makeText(getContext(),"Delete",Toast.LENGTH_SHORT).show();
                            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Fav");
                            //Log.d("delete3:",ref.toString());
                            String finalKey = key;
                            ref.child(uid).child("fav").child(key).removeValue((databaseError, databaseReference) -> {
                                adapter.removeItem(pos);
                                fav1.getFav().remove(finalKey);
                            });
                            //Log.d("KliK: ", adapter.getItem(pos).getOznaka().toString());
                        }));
            }
        };
        generateItem();

    }
    private void generateItem() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Fav").child(uid);
        fav1=new Fav();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fav1=dataSnapshot.getValue(Fav.class);
                if(fav1!=null) {
                    for(Map.Entry<String, String> entry :fav1.getFav().entrySet()){
                        favo.add(entry.getValue());
                        DatabaseReference reff= FirebaseDatabase.getInstance().getReference("Ziv").child(entry.getValue());
                        reff.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                ziv = dataSnapshot.getValue(ZivUpload.class);

                                if (ziv != null) {
                                    ziv.setKey(dataSnapshot.getKey());
                                    Log.d("generateItem", ziv.toString());
                                    itemList.add(ziv);

                                    //Log.d("generateItem2", String.valueOf(itemList.size()));
                                    //Log.d("generateItem3", String.valueOf(fav1.getFav().size()));
                                    if (itemList.size() == fav1.getFav().size()) {
                                        Log.d("generateItem1", itemList.toString());
                                        adapter = new ProfileMyAdapter(getContext(), itemList);
                                        recyclerView.setAdapter(adapter);
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.d("onCancelled:ziv dohvati",databaseError.getMessage());
                            }
                        });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("onCancelled:fav: ",databaseError.getMessage());
            }
        });

    }
}