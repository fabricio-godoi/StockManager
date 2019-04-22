package com.example.stockmanager.eventbus;

/**
 * Created by AlissonGodoi on 24/08/2017.
 */

public enum MessageCode {

    /**
     * When the QRCodeReader finds a QRCode or is updated
     * arg0 = 'RESTART' or 'RESULT'
     * arg1 = if arg0 is 'RESULT' then arg1 will have the QRCode decoded in text
     */
    QRCODE_CALLBACK,

    /**
     * Take picture result
     * arg0 = File object of the picture taken
     */
    TAKEPICTURE_CALLBACK,

    /**
     * Send command to SyncDataService to start ASP the data synchronization
     */
    FORCE_DATA_SYNCHRONIZATION,

}
