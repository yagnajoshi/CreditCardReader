package com.yagna.cardreader.view.camera;

import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.text.TextBlock;

import java.util.ArrayList;

/**
 * Created by Welcome on 23-05-2017.
 */


public class TextOperations  {

    private OcrDetectorProcessor ocrDetectorProcessor;
    public  ArrayList<ArrayList<String[]>> ransom=new ArrayList<>();

     ArrayList<String[]> child=new ArrayList();
    public  OnCardCaptureListner activity;
    private int digiount;

    String cardNoValue ="",nameValue="", valtillValue ="",binValue="", banknameValue="",bankphoneValue="",cardcategoryValue="",cardtypeValue="",countryValue="",bankSiteValue="",validValue="",cardBrandValue="";

    public TextOperations(OnCardCaptureListner activity, OcrDetectorProcessor ocrDetectorProcessor) {
        this.activity=activity;
        this.ocrDetectorProcessor = ocrDetectorProcessor;
    }


    public void grabText(SparseArray<TextBlock> items) {
        child=new ArrayList();

        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);
            if (item != null && item.getValue() != null) {
                int textType=getTypeOfText( items,i);
                Log.e("Operating", "Text detected! " + item.getValue());
                Log.e("Operating", "Text detected! " +textType );
               child.add(new String[]{""+textType,item.getValue(),""+digiount});
            }
        }
        if(child.size()>5&&ransom.size()<11)
            ransom.add( child);
        if(ransom.size()==5){
            ocrDetectorProcessor.readingState = ocrDetectorProcessor.OFF;
            activity.onResetCardReading();
            parsingCardData();
        }
    }


    public void parsingCardData(){



        ArrayList<ArrayList<String[]>> dummyData = ransom;
        int cardCount = 0, nameCount = 4, valCount = 0, binCount = 0;

        for (int i = 0; i < ransom.size(); i++) {
            for (int j = 0; j < ransom.get(i).size(); j++) {
                if (dummyData.get(i).get(j)[0].equalsIgnoreCase("1")) {
                    if (Integer.parseInt(dummyData.get(i).get(j)[2]) > cardCount && dummyData.get(i).get(j)[1].replace(" ","").length()>=cardNoValue.length()) {
                        cardCount = Integer.parseInt(dummyData.get(i).get(j)[2]);
                        cardNoValue = dummyData.get(i).get(j)[1].replace(" ","");
                    }
                }
                if (dummyData.get(i).get(j)[0].equalsIgnoreCase("2")) {

                    String validity = dummyData.get(i).get(j)[1].replace(" ","");
                    if (!validity.equalsIgnoreCase("")) {
                        if(validity.contains("/"))
                        {
                            try {
                                validity = validity.substring(validity.indexOf("/") - 2, validity.indexOf("/") + 3);

                            } catch (Exception e) {
                                validity =dummyData.get(i).get(j)[1].replace(" ","");
                            }
                        }
                        else if(validity.contains("-"))
                        {
                            try {
                                validity = validity.substring(validity.indexOf("-") - 2, validity.indexOf("-") + 3);

                            } catch (Exception e) {
                                validity =dummyData.get(i).get(j)[1].replace(" ","");
                            }
                        }

                    }
                    if(valtillValue.length()>2&&(validity.endsWith(valtillValue.substring(valtillValue.indexOf("/")+1))||validity.endsWith(valtillValue.substring(valtillValue.indexOf("-")+1)))){

                        if(Integer.parseInt(validity.substring(validity.length()-2))>Integer.parseInt(valtillValue.substring(valtillValue.length()-2))){
                            valtillValue = validity;

                        }

                    }
                    else {
                        valtillValue = validity;
                    }


                }
                if (dummyData.get(i).get(j)[0].equalsIgnoreCase("3")) {
                    if (!dummyData.get(i).get(j)[1].equalsIgnoreCase("")) {
                        if (dummyData.get(i).get(j)[1].toLowerCase().trim().contains("visa") ||
                                dummyData.get(i).get(j)[1].toLowerCase().trim().contains("master") ||
                                dummyData.get(i).get(j)[1].toLowerCase().trim().contains("card") ||
                                dummyData.get(i).get(j)[1].toLowerCase().trim().contains("rupay")) {
                            cardBrandValue = dummyData.get(i).get(j)[1];
                        }
                    }
                }

                if (dummyData.get(i).get(j)[0].equalsIgnoreCase("6")) {
                    if (Integer.parseInt(dummyData.get(i).get(j)[2]) < nameCount) {
                        nameCount = Integer.parseInt(dummyData.get(i).get(j)[2]);
                        nameValue = dummyData.get(i).get(j)[1];
                    }
                }
            }

        }
        /////////////////// Fitting Card Details
        String[] monthYear = new String[]{};
        if(valtillValue.contains("/")){
            monthYear = valtillValue.split("/");
        }
        else if(valtillValue.contains("-"))
        {
            monthYear = valtillValue.split("-");
        }
        else if(valtillValue.contains(" "))
        {
            monthYear = valtillValue.split(" ");
        }

        String mmBox="",yyBox="";
        if(monthYear.length>1) {
            mmBox = (monthYear[0]);
            yyBox = (monthYear[1]);
        }
        else{
            mmBox = (valtillValue);
        }
        getCorrectedCardNoValue();
        activity.onCardRead(cardNoValue, nameValue, mmBox,yyBox);
        ransom.clear();
    }


    private int getTypeOfText(SparseArray<TextBlock> items, int position) {

        TextBlock item = items.valueAt(position);
        String value = item.getValue();
        digiount=0;
        if(value.replace(" ","").length()==16){

            String value_1=value.replace(" ","");
           // value_1= getCardValue( value_1);

            for(int i=0;i<value_1.length();i++){
                if (android.text.TextUtils.isDigitsOnly(value_1.charAt(i)+"")){
                    digiount++;
                }
            }

            if(digiount>10){
             return 1;//Card No
            }

         }
        if(value.contains("/")||value.contains("-")){
            String value_1=value.replace(" ","");
                String[] str=new String[]{};
                if(value_1.contains("/"))
                   str=value_1.split("/");
                if(value_1.contains("-"))
                    str=value_1.split("-");
                if(str.length==2){
                    str[0]=""+str[0].charAt(str[0].length()-2)+str[0].charAt(str[0].length()-1);
                    str[1]=""+str[1].charAt(0)+str[1].charAt(1);
                    if(android.text.TextUtils.isDigitsOnly(str[0])&&android.text.TextUtils.isDigitsOnly(str[1])){
                        return 2;//Validity
                    }
                }

        }
        if(value.toLowerCase().contains("visa")||value.toLowerCase().contains("mastercard")||value.toLowerCase().contains("master")||value.toLowerCase().contains("rupay")||value.toLowerCase().contains("card")){
            return 3;//Card Type
        }


        if(value.toLowerCase().contains("bank")){
            return 4;
        }


        if(value.toLowerCase().length()<=6){
            String value_1=value.replace(" ","");
             digiount=0;
            for(int i=0;i<value_1.length();i++){
                if (android.text.TextUtils.isDigitsOnly(value_1.charAt(i)+"")){
                    digiount++;
                }
            }

            if(digiount>3){
                return 5;//Bin no
            }

        }


        if(value.toLowerCase().trim().split(" ").length>1)
        {
            try {
            if (position >= (items.size() / 2)
                    && !value.toLowerCase().contains("month/")
                    && !value.toLowerCase().contains("/year")
                    &&  !value.toLowerCase().contains("bank")
                    &&  !value.toLowerCase().contains("month/year")
                    &&  !value.toLowerCase().contains("valid thru")
            ) {
            String[] str = value.toLowerCase().split(" ");
            String str1=value.toLowerCase().replace(" ","");

                    if (str.length > 1) {
                        digiount = 0;
                        for (int i = 0; i < str1.length(); i++) {
                            if (android.text.TextUtils.isDigitsOnly(str1.charAt(i) + "")) {
                                digiount++;
                            }
                        }

                        if (digiount < 4) {
                            return 6;//Name
                        }
                    }
                }
            }catch (Exception e){}

        }



        return 0;//Extra
    }

    public String getCorrectedCardNoValue() {

        cardNoValue=cardNoValue.replace(" ","")
                .replace("a", "4")
                .replace("u","0")
                .replace("U","0")
                .replace("s","5")
                .replace("S","5")
                .replace("A","4")
                .replace("e","2")
                .replace("d","2")
                .replace("B","8")
                .replace("b","6")
                .replace("T","7")
                .replace("/","7")
                .replace("L","1")
                .replace("l","1")
                .replace("o","0")
                .replace("E","8")
                .replace("z","2")
                .replace("0","0")
                .replace("t","4");

        return cardNoValue;
    }


}
