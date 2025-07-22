package com.example.drunk;

public class UserHelperClass {
    public String fullName, policeId, email, phone, rank, station, password;

    public UserHelperClass() {} // Required for Firestore

    public UserHelperClass(String fullName, String policeId, String email, String phone, String rank, String station, String password) {
        this.fullName = fullName;
        this.policeId = policeId;
        this.email = email;
        this.phone = phone;
        this.rank = rank;
        this.station = station;
        this.password = password;
    }
}
