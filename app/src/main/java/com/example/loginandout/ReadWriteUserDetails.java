package com.example.loginandout;

public class ReadWriteUserDetails {
    public  String doB, gender, mobile;

    //Constructor needed to obtain snapshot
    public ReadWriteUserDetails(){};
    public ReadWriteUserDetails( String textDoB, String textGender, String textMobile){
        this.doB = textDoB;
        this.gender = textGender;
        this.mobile = textMobile;
    }
}
