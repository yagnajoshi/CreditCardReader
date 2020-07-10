package com.yagna.cardreader.view.camera;

public interface OnCardCaptureListner {
     void onCardRead(String cardNumver, String cardhHolderName, String validTillMonth, String validTillYear);

    void onResetCardReading();

}
