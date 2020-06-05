package com.example.pomozi;

import android.app.AlertDialog;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class IspisFragment extends Fragment {
    private String id_skl;
    private RecyclerView mRecyclerView;
    private IspisAdapterZiv mAdapter;
    private ProgressBar mProgressCircle;
    private List<ZivUpload> mUploads;
    private DatabaseReference mDatabaseRef;

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
            Toast.makeText(getContext(),"Nisi smio ovo uspjet,javi mi kako",Toast.LENGTH_SHORT).show();
        }else {
            //todo srediti id da se opcija brisi javlja samo kada je vlasnik u pitanju
            id_skl=getArguments().getString("id_skl");
        }
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Ziv");

        ucitaj_podatke();

        /*MySwipeHelper swipeHelper =*/ new MySwipeHelper(getContext(),mRecyclerView,200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MySwipeHelper.MyButton> buffer) {
                buffer.add(new MyButton(Objects.requireNonNull(getContext()),"Izbriši",30,R.drawable.ic_delete_black, Color.parseColor("#FFFFFF"),
                        pos -> {
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
                        ZivUpload upload = postSnapshot.getValue(ZivUpload.class);
                    assert upload != null;
                    upload.setKey(postSnapshot.getKey());
                    mUploads.add(upload);
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
