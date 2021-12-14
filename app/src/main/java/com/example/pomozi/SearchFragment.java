package com.example.pomozi;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;


public class SearchFragment extends Fragment {
    private EditText grad,zupanija;
    private RadioGroup stanje,status,vrsta;
    private RadioButton id_vrsta,id_stanje,id_status;
    private Button search,refresh;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vrsta=view.findViewById(R.id.vrsta_s);
        stanje=view.findViewById(R.id.stanje_s);
        status=view.findViewById(R.id.status_s);
        search=view.findViewById(R.id.search_s);
        grad=view.findViewById(R.id.grad_search);
        zupanija=view.findViewById(R.id.zup_search);
        refresh=view.findViewById(R.id.refresh_s);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                grad.getText().clear();
                zupanija.getText().clear();
                stanje.clearCheck();
                status.clearCheck();
                vrsta.clearCheck();
            }
        });
        search.setOnClickListener(v -> {
            IspisFragment fragment=new IspisFragment();
            Bundle args = new Bundle();
            if(vrsta.getCheckedRadioButtonId()!=-1){
                id_vrsta =view.findViewById(vrsta.getCheckedRadioButtonId());
                args.putString("vrsta", id_vrsta.getText().toString());
            }
            if(status.getCheckedRadioButtonId()!=-1){
                id_status=view.findViewById(status.getCheckedRadioButtonId());
                args.putString("status",  id_status.getText().toString());
            }
            if(stanje.getCheckedRadioButtonId()!=-1){
                id_stanje=view.findViewById(stanje.getCheckedRadioButtonId());
                args.putString("stanje", id_stanje.getText().toString());
            }
            if (!TextUtils.isEmpty(grad.getText().toString())){
                args.putString("grad",grad.getText().toString());
            }
            if (!TextUtils.isEmpty(zupanija.getText().toString())){
                args.putString("zup",zupanija.getText().toString() );
            }
            args.putBoolean("search",true );
            fragment.setArguments(args);
            Log.d("Search",args.toString());
            FragmentTransaction ft =getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.nav_host_fragment, fragment);
            ft.addToBackStack("tag_ispis");
            ft.commit();
        });

    }
}