package com.example.pomozi.Model;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Fav {
    private HashMap<String,String> fav =new HashMap<>();

    public Fav() {
    }

    public Fav(HashMap<String, String> fav) {
        this.fav = fav;
    }

    public HashMap<String, String> getFav() {
        return fav;
    }

    public void setFav(HashMap<String, String> fav) {
        this.fav = fav;
    }

    @NonNull
    @Override
    public String toString() {
        return "Fav{" +
                "fav=" + fav +
                '}';
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("fav", fav);
        return result;
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Fav)) return false;
        Fav fav1 = (Fav) o;
        return Objects.equals(fav, fav1.fav);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return Objects.hash(fav);
    }
}
