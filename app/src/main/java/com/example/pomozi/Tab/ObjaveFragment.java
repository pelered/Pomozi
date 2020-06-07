package com.example.pomozi.Tab;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.pomozi.Adapter.ProfileMyAdapter;
import com.example.pomozi.EditZiv;
import com.example.pomozi.Helper.MySwipeHelper;
import com.example.pomozi.Model.ZivUpload;
import com.example.pomozi.R;
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


public class ObjaveFragment extends Fragment {

    private RecyclerView recyclerViewObjave;
    private ProfileMyAdapter myAdapter;
    private LinearLayoutManager layoutManager;

    private ZivUpload ziv= new ZivUpload();
    private List<ZivUpload> itemList =new ArrayList<>();
    private String uid;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_objave, container, false);
    }
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        SharedPreferences prefs = Objects.requireNonNull(getContext()).getSharedPreferences("shared_pref_name", Context.MODE_PRIVATE);
        uid=prefs.getString("uid",null);
        recyclerViewObjave =view.findViewById(R.id.recycler_objave);
        recyclerViewObjave.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(getContext());
        recyclerViewObjave.setLayoutManager(layoutManager);
        MySwipeHelper swipeHelper = new MySwipeHelper(getContext(), recyclerViewObjave,200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buffer) {
                buffer.add(new MyButton(Objects.requireNonNull(getContext()),"Delete",30,R.drawable.ic_delete_black, Color.parseColor("#FFFFFF"),
                        pos -> {
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
                            args.putString("oznaka", itemList.get(pos).getKey());
                            fragment.setArguments(args);
                            FragmentTransaction ft =((FragmentActivity) getContext()).getSupportFragmentManager().beginTransaction();
                            ft.replace(R.id.nav_host_fragment, fragment);
                            ft.addToBackStack("tag_objave_");
                            ft.commit();

                        }));
            }
        };
        generateItem();
    }
    private void obrisi_ziv(int pos) {
        for(int i =0; i<itemList.get(pos).getUrl().size();i++){
            int finalI = i;
            FirebaseStorage.getInstance().getReferenceFromUrl(Objects.requireNonNull(itemList.get(pos).getUrl().get(i + "_key"))).delete().addOnSuccessListener(aVoid -> {
                if (finalI+1==itemList.get(pos).getUrl().size()){
                    FirebaseDatabase.getInstance().getReference("Ziv").child(itemList.get(pos).getKey()).removeValue().addOnSuccessListener(aVoidd -> {
                        Toast.makeText(getContext(),"Uspješno izbrisano",Toast.LENGTH_SHORT).show();
                        itemList.remove(pos);
                        myAdapter.notifyItemRemoved(pos);
                    }).addOnFailureListener(e -> Toast.makeText(getContext(),"NeUspješno izbrisano",Toast.LENGTH_SHORT).show());
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(),"NeUspješno izbrisana slika",Toast.LENGTH_SHORT).show();
            });
        }


    }
    private void generateItem() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Ziv");
        Query query = ref.orderByChild("id_vlasnika").equalTo(uid);
        //Log.d("Ispisujem0:",query.toString());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Log.d("Ispisujem",postSnapshot.getValue().toString());
                    ziv = postSnapshot.getValue(ZivUpload.class);
                    if (ziv != null) {
                        ziv.setKey(postSnapshot.getKey());
                        //Log.d("generateItem", ziv.toString());
                        itemList.add(ziv);
                    }
                }
                myAdapter = new ProfileMyAdapter(getContext(), itemList);
                recyclerViewObjave.setAdapter(myAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("onCancelled:fav: ",databaseError.getMessage());
            }
        });

    }
}