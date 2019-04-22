# StockManager
Android Application for Demonstration Purposes

This application has the following features:

1- A demonstration of a dashboard in the main screen, using the [Android MP Chart](https://github.com/PhilJay/MPAndroidChart) library;
2- QRCode reader feature that uses the main camera to check for any QRCode;
3- A image capture feature that sends the pictures taken to a FTP Server configured in the [Constants](https://github.com/fabricio-godoi/StockManager/blob/master/app/src/main/java/com/example/stockmanager/config/Constants.java) file;


# Dashboard
![Bar Chart](https://github.com/fabricio-godoi/StockManager/blob/master/screenshots/barChart.png | width=250)
![Pie Chart](https://github.com/fabricio-godoi/StockManager/blob/master/screenshots/pieChart.png | width=250)
![Line Chart](https://github.com/fabricio-godoi/StockManager/blob/master/screenshots/lineChart.png)


# QRCode reader

The camera view will keep checking for any QRCode that appear in the screen.
When a QRCode is found, the information the saved in the internal APK configuration file.

Every QRCode found can be accessed in the left pane in the in the "QR Code" option.
A list with the QRCodes will popup, and any selected will open the browser with the select code.
This is useful to reopen any code that is a URL for a site.

Also any file select in the "Gallery" option will be check for a QRCode. If any QRCode is found, then
the QRCode Reader screen will popup with the image and the QRCode above it.

![QRCode](https://github.com/fabricio-godoi/StockManager/blob/master/screenshots/qrCodeReader.png | width=250)


![Left Pane](https://github.com/fabricio-godoi/StockManager/blob/master/screenshots/leftMenu.png | width=250)

![Code List](https://github.com/fabricio-godoi/StockManager/blob/master/screenshots/qrCodeSelect.png | width=250)
