package com.example.pomozi.Model;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class User
{
    private String uid;
    private String ime;
    private String url;
    private String grad;
    private String zupanija;
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User(String uid, String ime, String url, String email, String grad, String zupanija) {
        this.uid = uid;
        this.ime = ime;
        this.url = url;
        this.email= email;
        this.grad = grad;
        this.zupanija = zupanija;
    }
    public User(String uid,String ime, String url, String email) {
        this.uid = uid;
        this.ime = ime;
        this.url = url;
        this.email= email;
    }

    public User() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getGrad() {
        return grad;
    }

    public void setGrad(String grad) {
        this.grad = grad;
    }

    public String getZupanija() {
        return zupanija;
    }

    public void setZupanija(String zupanija) {
        this.zupanija = zupanija;
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid",uid);
        result.put("ime",ime);
        result.put("url",url);
        result.put("email",email);
        result.put("grad",grad);
        result.put("zupanija",zupanija);
        return result;
    }
    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", ime='" + ime + '\'' +
                ", url='" + url + '\'' +
                ", grad='" + grad + '\'' +
                ", zupanija='" + zupanija + '\'' +
                '}';
    }
}
