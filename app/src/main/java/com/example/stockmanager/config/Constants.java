package com.example.stockmanager.config;

public class Constants {

    /// FTP Server Configuration
//    public static final String FTP_HOST= "10.0.2.2"; // For Android simulator
    public static final String FTP_HOST= "192.168.137.1"; // FTP Address
    public static final Integer FTP_PORT = 21;     // Access Port
    public static final String FTP_USER = "user";  // Username
    public static final String FTP_PASS  ="user";  // Password


    /// This value specifies the rate of QRCode gets frames and parse it
    public static final int QRCODE_SEARCH_INTERVAL_MS = 500;

}
