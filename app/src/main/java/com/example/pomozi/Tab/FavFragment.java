package com.example.pomozi.Tab;

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
import com.example.pomozi.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class FavFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProfileMyAdapter adapter;
    private LinearLayoutManager layoutManager;
    private Fav fav1=new Fav();
    private ArrayList<String> favo=new ArrayList<>();
    private ZivUpload ziv= new ZivUpload();
    private List<ZivUpload> itemList =new ArrayList<>();
    private String uid;
    //
    private HashMap<DatabaseReference, ValueEventListener> mListenerMap;
    //FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mRef;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fav, container, false);
    }
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        SharedPreferences prefss = requireContext().getSharedPreferences("shared_pref_name", Context.MODE_PRIVATE);
        uid=prefss.getString("uid","");
        recyclerView=view.findViewById(R.id.recycler_test);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        mRef=FirebaseDatabase.getInstance().getReference("Ziv");

        MySwipeHelper swipeHelper = new MySwipeHelper(getContext(),recyclerView,200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MySwipeHelper.MyButton> buffer) {
                buffer.add(new MyButton(requireContext(),"Delete",30,R.drawable.ic_delete_black, Color.parseColor("#FFFFFF"),
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
        Log.d("FavF:",ref.toString());
        mListenerMap=new HashMap<>();
//dohvati popis svi ziv sto pratis
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                itemList.clear();
                fav1=dataSnapshot.getValue(Fav.class);
                if(fav1!=null) {
                    Log.d("FavF:vr",fav1.toString());
                    //postavi_listener();
                    for(Map.Entry<String, String> entry :fav1.getFav().entrySet()){
                        favo.add(entry.getValue());
                        DatabaseReference reff= FirebaseDatabase.getInstance().getReference("Ziv").child(entry.getValue());
                        reff.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                ziv = dataSnapshot.getValue(ZivUpload.class);
                                if (ziv != null) {
                                    ziv.setKey(dataSnapshot.getKey());
                                    itemList.add(ziv);
                                    if (itemList.size() == fav1.getFav().size()) {
                                        adapter = new ProfileMyAdapter(getActivity(), itemList);
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