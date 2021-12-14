package com.example.pomozi.Model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class ZivUpload {


    private String vrsta;
    private String stanje;
    private String status;
    private String adresa,grad,zupanija;
    private String opis;
    private String id_vlasnika;
    private String date;
    private String last_date;
    public Map<String,String> url =new HashMap<>();
    @Exclude
    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ZivUpload() {
    }

    public String getVrsta() {
        return vrsta;
    }

    public void setVrsta(String vrsta) {
        this.vrsta = vrsta;
    }

    public String getStanje() {
        return stanje;
    }

    public void setStanje(String stanje) {
        this.stanje = stanje;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdresa() {
        return adresa;
    }

    public void setAdresa(String adresa) {
        this.adresa = adresa;
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

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public String getId_vlasnika() {
        return id_vlasnika;
    }

    public void setId_vlasnika(String id_vlasnika) {
        this.id_vlasnika = id_vlasnika;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLast_date() {
        return last_date;
    }

    public void setLast_date(String last_date) {
        this.last_date = last_date;
    }

    public Map<String, String> getUrl() {
        return url;
    }

    public void setUrl(Map<String, String> url) {
        this.url = url;
    }

    public ZivUpload(String vrsta, String stanje, String status, String adresa, String grad, String zupanija, String opis, String id_vlasnika, String date, String last_date, Map<String, String> url) {
        this.vrsta = vrsta;
        this.stanje = stanje;
        this.status = status;
        this.adresa = adresa;
        this.grad = grad;
        this.zupanija = zupanija;
        this.opis = opis;
        this.id_vlasnika = id_vlasnika;
        this.date = date;
        this.last_date = last_date;
        this.url = url;
    }

    @Override
    public String toString() {
        return "ZivUpload{" +
                "vrsta='" + vrsta + '\'' +
                ", stanje='" + stanje + '\'' +
                ", status='" + status + '\'' +
                ", adresa='" + adresa + '\'' +
                ", grad='" + grad + '\'' +
                ", zupanija='" + zupanija + '\'' +
                ", opis='" + opis + '\'' +
                ", id_vlasnika='" + id_vlasnika + '\'' +
                ", date='" + date + '\'' +
                ", last_date='" + last_date + '\'' +
                ", url=" + url +
                '}';
    }
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("status",status);
        result.put("stanje",stanje);
        result.put("vrsta",vrsta);
        result.put("opis",opis);
        result.put("grad",grad);
        result.put("adresa",adresa);
        result.put("zupanija",zupanija);
        result.put("id_vlasnika",id_vlasnika);
        result.put("date",date);
        result.put("last_date",last_date);
        result.put("url", url);
        return result;
    }
}
