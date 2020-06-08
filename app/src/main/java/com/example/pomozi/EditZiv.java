package com.example.pomozi;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.ReturnMode;
import com.esafirm.imagepicker.model.Image;
import com.example.pomozi.Adapter.SliderAdapterExample;
import com.example.pomozi.Model.ZivUpload;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.sangcomz.fishbun.FishBun;
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter;
import com.sangcomz.fishbun.define.Define;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;


public class EditZiv extends Fragment {

    private SliderView sliderView;
    private Button upload,odaberi_sliku,izbrisi_sliku;
    private AutoCompleteTextView adres,grad,zupanija;
    private EditText opis;
    private SliderAdapterExample adapter;
    private ArrayList<String> slike_ucitavanje=new ArrayList<>();
    private HashMap<String,Uri> ImageList = new HashMap<>();
    private HashMap<String,String> slike_map=new HashMap<>();

    private int pozicija;
    private ArrayList<String> brisi_slike;
    private ProgressBar progressBar;
    private StorageTask<UploadTask.TaskSnapshot> mUploadTask;
    private StorageReference mStorageRef;
    private FirebaseDatabase database;
    private DatabaseReference mDatabaseRef;
    private int count=0;
    private ZivUpload dohvaceno=null;
    private RadioGroup stanje,status,vrsta;
    private RadioButton id_vrsta,id_stanje,id_status;
    private View vi;
    private SharedPreferences prefs;
    private String id_korisnika;
    private String oznaka_ziv;
    private String key;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_ziv, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //iz SharedPref se uzima id jer jedino korisnik logirano može imat ovdje pristup
        prefs = Objects.requireNonNull(getContext()).getSharedPreferences("shared_pref_name", Context.MODE_PRIVATE);
        id_korisnika = prefs.getString("uid",null);
        Log.d("onViewCreated",id_korisnika);
        if (id_korisnika==null){
            Toast.makeText(getContext(),"Nisi smio ovo uspjet,javi mi kako",Toast.LENGTH_SHORT).show();
        }

        if(getArguments()!=null){
           if(!TextUtils.isEmpty(getArguments().getString("oznaka"))) {
                database = FirebaseDatabase.getInstance();
                mDatabaseRef = database.getReference("Ziv");
                oznaka_ziv = getArguments().getString("oznaka");
                ucitajPodatke();
            }
        }else {
            //ako je null onda ne postoji ziv
            oznaka_ziv="";
        }
        progressBar=view.findViewById(R.id.progress_bar);
        adres=view.findViewById(R.id.adresa_ziv);
        grad=view.findViewById(R.id.grad_ziv);
        zupanija=view.findViewById(R.id.zupanija_ziv);
        opis=view.findViewById(R.id.opis_ziv);

        upload=view.findViewById(R.id.uplodaj);
        odaberi_sliku=view.findViewById(R.id.button_choose_image);
        izbrisi_sliku=view.findViewById(R.id.obrisi_sliku);
        sliderView =view.findViewById(R.id.imageSlider);
        progressBar.setVisibility(View.INVISIBLE);
        mStorageRef = FirebaseStorage.getInstance().getReference("Ziv");
        vi=view;

        /*id_vrsta = view.findViewById(vrsta.getCheckedRadioButtonId());
        id_stanje=view.findViewById(stanje.getCheckedRadioButtonId());
        id_status=view.findViewById(status.getCheckedRadioButtonId());*/
        vrsta=view.findViewById(R.id.vrsta);
        stanje=view.findViewById(R.id.stanje);
        status=view.findViewById(R.id.status);
        brisi_slike=new ArrayList<>();
        odaberi_sliku.setOnClickListener(v -> openFileChooser());
        izbrisi_sliku.setOnClickListener(v -> {
            if (mUploadTask != null && mUploadTask.isInProgress()) {
                Toast.makeText(getActivity(), "Upload in progress", Toast.LENGTH_SHORT).show();
            } else {
                obrisi();
            }
        });
        upload.setOnClickListener(v -> {

            if (mUploadTask != null && mUploadTask.isInProgress()) {
                Toast.makeText(getActivity(), "Upload in progress", Toast.LENGTH_SHORT).show();
            } else {
                //prvo provjeri dal upisana oznaka vec postoji,samo ucitaj podatke i spremi u true ili false
                if(TextUtils.isEmpty(opis.getText().toString()) || status.getCheckedRadioButtonId()==-1 || TextUtils.isEmpty(grad.getText().toString()) || TextUtils.isEmpty(zupanija.getText().toString()) || stanje.getCheckedRadioButtonId()==-1 || vrsta.getCheckedRadioButtonId()==-1){
                    Toast.makeText(getActivity(), "Jedino adresa može ostata prazna", Toast.LENGTH_LONG).show();
                }else {
                    progressBar.setVisibility(View.VISIBLE);
                    if(TextUtils.isEmpty(adres.getText().toString())){
                        adres.setText("");
                    }
                    uploadFile();
                }
            }
        });
    }

    private void ucitajPodatke(){
        mDatabaseRef.child(oznaka_ziv).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //dodajemo sve vrste pasmina
                slike_ucitavanje.clear();
                dohvaceno = dataSnapshot.getValue(ZivUpload.class);
                postavi_vrijednosti();
                if(dataSnapshot.hasChild("url")){
                    //spremamo hash mapu
                    //ovo radimo da mozemo prikazati slike lijepo,potreban samo url
                    for(Map.Entry<String, String> entry :dohvaceno.getUrl().entrySet())
                    {
                        slike_ucitavanje.add(entry.getValue());
                    }
                }else {
                    //stvara se prazna lista,da ne dode do greske
                    slike_ucitavanje.clear();
                }

                //popis svih pasmina koji se stavljaju u adapter za autofill
                progressBar.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Skidanje nije uspjelo", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
            }

        });
        new Handler().postDelayed(() -> {
            if(!slike_ucitavanje.isEmpty()){
                inicijalizirajSlider(slike_ucitavanje);
            }
        }, 1000);
    }
    //postavlja vrijednosti varijabli pri ucitvanju iz baze
    @SuppressLint("SetTextI18n")
    private void postavi_vrijednosti() {
        adres.setText(dohvaceno.getAdresa());
        grad.setText(dohvaceno.getGrad());
        opis.setText(dohvaceno.getOpis());
        zupanija.setText(dohvaceno.getZupanija());
        if(dohvaceno.getStanje()!=null) {
            if (dohvaceno.getStanje().equals("Rješeno")) {
                RadioButton id = vi.findViewById(R.id.rjeseno);
                id.setChecked(true);
            } else if (dohvaceno.getStanje().equals("Nerješeno")) {
                RadioButton id = vi.findViewById(R.id.nerjeseno);
                id.setChecked(true);
            }
        }
        if(dohvaceno.getStatus()!=null) {
            if (dohvaceno.getStatus().equals("Izgubljen")) {
                RadioButton id = vi.findViewById(R.id.izgubljeno);
                id.setChecked(true);
            } else if (dohvaceno.getStatus().equals("Nađeno")) {
                RadioButton id = vi.findViewById(R.id.nadeno);
                id.setChecked(true);
            }
        }
        if(dohvaceno.getVrsta()!=null) {
            if (dohvaceno.getVrsta().equals("Pas")) {
                RadioButton id = vi.findViewById(R.id.pas);
                id.setChecked(true);
            } else if (dohvaceno.getVrsta().equals("Macka")) {
                RadioButton id = vi.findViewById(R.id.macka);
                id.setChecked(true);
            }else if (dohvaceno.getVrsta().equals("Zec")) {
                RadioButton id = vi.findViewById(R.id.zec);
                id.setChecked(true);
            }else if (dohvaceno.getVrsta().equals("Ptica")) {
                RadioButton id = vi.findViewById(R.id.ptica);
                id.setChecked(true);
            }else if (dohvaceno.getVrsta().equals("Drugo")) {
                RadioButton id = vi.findViewById(R.id.drugo);
                id.setChecked(true);
            }
        }
    }
    //uplodamo slike i dohvacamorl njihov
    private void uploadFile() {
        Log.d("upload","Tusam");
        //dohvaca se trenutno prikazan popis slika
        slike_ucitavanje=adapter.getList();
        //da spremimo u hasmapu s hashem vec ucitane slike
        HashMap<String,String> vec_ucitane=new HashMap<>();
        ImageList=new HashMap<>();
        HashMap<String,String> slike_iz_baze=new HashMap<>();
        List ImageList_key= new ArrayList();
        List vec_ucitane_key= new ArrayList();
        if (!slike_ucitavanje.isEmpty()) {
            for (int i = 0; i < slike_ucitavanje.size(); i++) {
                if (slike_ucitavanje.get(i).contains("http")) {
                    //ovdje spremamo slike koje su već u bazi
                    vec_ucitane.put((i + "_key"), slike_ucitavanje.get(i));
                    vec_ucitane_key.add(Integer.toString(i));
                } else {
                    //tu spremamo slike koje smo ucitali iz galerije
                    ImageList.put(Integer.toString(i), Uri.parse(slike_ucitavanje.get(i)));
                    ImageList_key.add(Integer.toString(i));
                }
            }
        }
        //TODO prvo provjeri dal upisana oznaka vec postoji
        //provjeravamo dal postoje odabrane slike iz galerije
        if (!ImageList.isEmpty()) {
            count=0;
            for (int uploads = 0; uploads < ImageList.size(); uploads++) {
                final Uri Image = ImageList.get(ImageList_key.get(uploads));
                final StorageReference fileReference;
                Boolean video;
                if(Image.toString().contains("mp4")){
                    video=true;
                    fileReference= mStorageRef.child(System.currentTimeMillis()
                            + "."+getFileExtension(new File(String.valueOf(Image))));
                }else{
                    video=false;
                    fileReference = mStorageRef.child(System.currentTimeMillis()
                            + "."+getFileExtension(Image));
                }

                if(video){
                    mUploadTask = fileReference.putFile(Uri.fromFile(new File(String.valueOf(Image))));
                }else{
                    mUploadTask = fileReference.putFile(Image);
                }

                //Log.d("upload_slika3",mUploadTask.toString());
                // Register observers to listen for when the download is done or if it fails
                mUploadTask.addOnFailureListener(exception -> Toast.makeText(getActivity(), "Upload nije uspio " + exception.toString(), Toast.LENGTH_LONG).show())
                        .addOnSuccessListener(taskSnapshot -> {
                     mUploadTask.continueWithTask(task -> {
                        //
                        if (!task.isSuccessful()) {
                            throw Objects.requireNonNull(task.getException());
                        }
                        // Continue with the task to get the download URL
                        return fileReference.getDownloadUrl();
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                Log.d("Upload()uploads ", String.valueOf(count));
                                //spremam u hash mapu
                                assert downloadUri != null;
                                //sprema se link novo spremljenih slika u bazi
                                slike_iz_baze.put(ImageList_key.get(count).toString(),downloadUri.toString());
                                count++;
                                Toast.makeText(getActivity(), "Upload.Dohvacen url: "+count, Toast.LENGTH_LONG).show();

                            } else {
                                Toast.makeText(getActivity(), "Upload nije uspio", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }).addOnProgressListener(taskSnapshot -> {
                });

            }
        } else{
            //ako ne postoje odabrane slike iz galerije samo ponovo zapisujemo već skinute,tj uplodane slike
            Toast.makeText(getActivity(), "No file selected", Toast.LENGTH_SHORT).show();
            //zkj slike_map
            slike_map=new HashMap<>(vec_ucitane);
        }
        //napravti da odbrojava dok count ne bude jenda velicini image list

        new Handler().postDelayed(() -> dodaj_u_bazu_podataka(vec_ucitane,slike_iz_baze), 10000);
    }
    private String getFileExtension(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        Log.d("upload",extension);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }
    private static String getFileExtension(File file) {
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }
    //dohvacamo koja vrsta je slika
    private String getFileExtension(Uri uri) {
        Log.d("upload_get",uri.toString());
        ContentResolver cR = Objects.requireNonNull(getActivity()).getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));


    }
    //slika se dodajeu bazu podataka , kao i podaci o zivotinji
    private void dodaj_u_bazu_podataka(HashMap<String, String> vec_ucitane, HashMap<String, String> imageList){
        ImageList.clear();
        slike_map=new HashMap<>();
        for(int i=0; i<(vec_ucitane.size()+imageList.size());i++){
            if(vec_ucitane.containsKey((i+"_key"))){
                slike_map.put((i+"_key"),vec_ucitane.get(i+"_key"));

            }else if(imageList.containsKey(Integer.toString(i))){
                slike_map.put((i+"_key"),(imageList.get(Integer.toString(i))));
            }
        }
        id_vrsta =vi.findViewById(vrsta.getCheckedRadioButtonId());
        id_status=vi.findViewById(status.getCheckedRadioButtonId());
        id_stanje=vi.findViewById(stanje.getCheckedRadioButtonId());
        String created_at;
        String last_updated;
        //datum stvaranja, i zadnji update datum
        if(dohvaceno==null){
            created_at = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());
        }else{
            created_at=dohvaceno.getDate();
        }
        last_updated=new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());
//String vrsta, String stanje, String status, String adresa, String grad, String zupanija, String opis, String id_vlasnika, String date, String last_date, Map<String, String> url
        //pripremamo za upload
        ZivUpload upload2 = new ZivUpload(id_vrsta.getText().toString(),id_stanje.getText().toString(),
                id_status.getText().toString(),adres.getText().toString(),grad.getText().toString(),zupanija.getText().toString(),
                opis.getText().toString(), id_korisnika,created_at,last_updated,slike_map);
        //Log.d("azuriraj:",upload2.toString());
        Map<String, Object> postValues2=upload2.toMap();

        database= FirebaseDatabase.getInstance();
        mDatabaseRef = database.getReference("Ziv");

        if(TextUtils.isEmpty(oznaka_ziv)){
            key= mDatabaseRef.push().getKey();
        }else{
            key=oznaka_ziv;
        }
        if(!brisi_slike.isEmpty()) {
            for(int i=0;i<brisi_slike.size();i++) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(brisi_slike.get(i));
                int finalI = i;
                storageReference.delete().addOnSuccessListener(aVoid -> {
                    //Toast.makeText(getContext(), "Slika izbrisana", Toast.LENGTH_SHORT).show();
                    if((finalI+1)==brisi_slike.size()) {
                        //Log.d("dodajSliku_count:", String.valueOf(finalI));

                        mDatabaseRef.child(key).updateChildren(postValues2).addOnSuccessListener(aVoidd -> {
                            Toast.makeText(getContext(), "Uplodano/Ažurirano", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }).addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Neuspjel pokušaj uplodanja/ažuriranja", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        });
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.INVISIBLE);
                });
            }
        }else{
            mDatabaseRef.child(key).updateChildren(postValues2).addOnSuccessListener(aVoidd -> {
                Toast.makeText(getContext(), "Uplodano/Ažurirano", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Neuspjel pokušaj uplodanja/ažuriranja", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
            });
        }
        brisi_slike.clear();
        //resetira se jer smo uplodali slike

    }


    private void obrisi() {
        pozicija=sliderView.getCurrentPagePosition();
        //samo nas zanimaju slike koje su vec u bazi podataka/storagu, i ove iz galerije se maknu iz slidera samo se ne dodaju u ovu listu
        if(adapter.getImage(pozicija).contains("http")) {
            brisi_slike.add(adapter.getImage(pozicija));
        }
        adapter.deleteItem(pozicija);
        adapter.notifyDataSetChanged();
    }
    private void openFileChooser() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        View mView = getLayoutInflater().inflate(R.layout.custom_dialog,null);
        Button btn_video = (Button)mView.findViewById(R.id.video);
        Button btn_slike = (Button)mView.findViewById(R.id.slike);
        alert.setView(mView);
        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);
        btn_video.setOnClickListener(v -> {
            ImagePicker.create(EditZiv.this)
                    .returnMode(ReturnMode.GALLERY_ONLY) // set whether pick and / or camera action should return immediate result or not.
                    .folderMode(true) // folder mode (false by default)
                    .toolbarFolderTitle("Folder") // folder selection title
                    .toolbarImageTitle("Tap to select") // image selection title
                    .toolbarArrowColor(Color.BLACK) // Toolbar 'up' arrow color
                    .includeVideo(true) // Show video on image picker
                    .onlyVideo(true) // include video (false by default)
                    .single() // single mode
                    .limit(10) // max images can be selected (99 by default)
                    .showCamera(false) // show camera or not (true by default)
                    .enableLog(false) // disabling log
                    .start(); // start image picker activity with request code
            alertDialog.dismiss();
        });
        btn_slike.setOnClickListener(v -> {
            FishBun.with(EditZiv.this)
                    .setImageAdapter(new GlideAdapter())
                    .setMaxCount(10)
                    .setMinCount(1)
                    .setPickerSpanCount(5)
                    .setActionBarColor(Color.parseColor("#466deb"), Color.parseColor("#466deb"), false)
                    .setActionBarTitleColor(Color.parseColor("#ffffff"))
                    .setAlbumSpanCount(2, 3)
                    .setButtonInAlbumActivity(false)
                    .setCamera(true)
                    .setReachLimitAutomaticClose(true)
                    .setHomeAsUpIndicatorDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_custom_back_white))
                    .setDoneButtonDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_check))
                    .setAllDoneButtonDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_check))
                    .setAllViewTitle("All")
                    .setMenuAllDoneText("All Done")
                    .setActionBarTitle("Odaberi slike")
                    .textOnNothingSelected("Please select 1 or more!")
                    .textOnImagesSelectionLimitReached("Limit Reached!")
                    .setSelectCircleStrokeColor(Color.BLACK)
                    .startAlbum();
            alertDialog.dismiss();
        });
        alertDialog.show();

    }
    //inicijalizacija slidera nakon sto se slike skinu,ako ih ima
    private void inicijalizirajSlider( ArrayList<String> slike_slider){
        //inicjalizira se tu kada prvi put stvaramo adapter
        adapter= new SliderAdapterExample(getActivity());
        adapter.setCount(slike_slider.size());
        adapter.slike2(slike_slider);
        sliderView.setSliderAdapter(adapter);
        sliderView.setIndicatorAnimation(IndicatorAnimations.SLIDE); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
        sliderView.setSliderTransformAnimation(SliderAnimations.CUBEINROTATIONTRANSFORMATION);
        sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH);
        sliderView.setIndicatorSelectedColor(Color.WHITE);
        sliderView.setIndicatorUnselectedColor(Color.GRAY);
        sliderView.setScrollTimeInSec(15);
        sliderView.setOnIndicatorClickListener(position -> sliderView.setCurrentPagePosition(position));
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageData) {
        super.onActivityResult(requestCode, resultCode, imageData);
        Log.d("onAcitivityResult:", String.valueOf(requestCode)+resultCode+imageData);
        Log.d("onAcitivityResult:1", String.valueOf(Define.ALBUM_REQUEST_CODE));
        switch (requestCode) {
            case Define.ALBUM_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    if (imageData != null) {
                        //svaki put spremi odabrane slike s mobitela, nakon svakog poziva galeriji, uvijek se resetira
                        ArrayList<Uri> slike = new ArrayList<>(Objects.requireNonNull(imageData.getParcelableArrayListExtra(Define.INTENT_PATH)));
                        //ovdje spremamo sve trenutne slike koje imamo prikazane,nakon svakog odabira u galeriji se dodaju najnovije slike na vec postojece
                        for (int i = 0; i < slike.size(); i++) {
                            slike_ucitavanje.add(slike.get(i).toString());
                        }
                        ArrayList<String> targetList = new ArrayList<>();
                        //radimo privremenu listu u koju spremamo slike kooje smo dobili iz galerije
                        slike.forEach(uri -> targetList.add(uri.toString()));
                        //dodajemo novo odabrane slike u adapter na mjesto na kojem je sliderview bio
                        //kada se odabralo Odaberi slike
                        if (sliderView.getSliderAdapter()!=null){
                            Log.d("result():", String.valueOf(sliderView.getCurrentPagePosition()));
                            adapter.addItem(targetList, sliderView.getCurrentPagePosition());
                            adapter.notifyDataSetChanged();
                            targetList.clear();
                        }else{
                            inicijalizirajSlider(targetList);

                        }

                    }
                }
                break;
            case 553:
                if(imageData!=null) {
                    // Get a list of picked images
                    List<Image> images = ImagePicker.getImages(imageData);
                    // or get a single image only
                    Image image = ImagePicker.getFirstImageOrNull(imageData);
                    Log.d("onAcitivityResult:Video", image.getPath());
                /*for (int i = 0; i < slike.size(); i++) {
                    slike_ucitavanje.add(slike.get(i).toString());
                }*/
                    slike_ucitavanje.add(image.toString());
                    ArrayList<String> targetList = new ArrayList<>();
                    targetList.add(image.getPath());
                    //radimo privremenu listu u koju spremamo slike kooje smo dobili iz galerije
                    //slike.forEach(uri -> targetList.add(uri.toString()));
                    //dodajemo novo odabrane slike u adapter na mjesto na kojem je sliderview bio
                    //kada se odabralo Odaberi slike
                    if (sliderView.getSliderAdapter() != null) {
                        // Log.d("result():", String.valueOf(sliderView.getCurrentPagePosition()));
                        adapter.addItem(targetList, sliderView.getCurrentPagePosition());
                        adapter.notifyDataSetChanged();
                        targetList.clear();
                    } else {
                        inicijalizirajSlider(targetList);
                    }
                }
                break;
        }


    }
}
