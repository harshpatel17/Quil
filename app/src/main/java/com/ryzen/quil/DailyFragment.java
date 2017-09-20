package com.ryzen.quil;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

import static android.widget.Toast.makeText;


public class DailyFragment extends DialogFragment implements RecognitionListener{

    private SpeechRecognizer recognizer;
    private String KW_DIGITS = "digits";

    private Button weightBtn;
    private Button caloriesBtn;
    private Button okBtn;

    private EditText userWeight;
    private EditText userCalories;
    private TextView statusText;

    private DailyCommunicator mDailyCommunicator;

    private boolean weightSelect = true;
    private boolean calSelect = false;

    public void onAttach(Activity activity){
        super.onAttach(activity);
        mDailyCommunicator = (DailyCommunicator) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        View v = getActivity().getLayoutInflater().inflate(R.layout.activity_daily_dialog_frag, null);
        runRecognizerSetup();

        statusText = (TextView) v.findViewById(R.id.statusText);
        userWeight = (EditText) v.findViewById(R.id.userWeight);
        userCalories = (EditText) v.findViewById(R.id.userCalories);

        weightBtn = (Button) v.findViewById(R.id.weightBtn);
        weightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recognizer.stop();
                weightSelect = true;
                calSelect = false;
                userWeight.setText("");
                statusText.setText("Recording in Weight");
                recognizer.startListening(KW_DIGITS);
            }
        });
        
        caloriesBtn= (Button) v.findViewById(R.id.caloriesBtn);
        caloriesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recognizer.stop();
                calSelect = true;
                weightSelect = false;
                userCalories.setText("");
                statusText.setText("Recording in Calories");
                recognizer.startListening(KW_DIGITS);
            }
        });
        okBtn = (Button) v.findViewById(R.id.okBtn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double weight = 0;
                double calories = 0;
                if (!userWeight.getText().toString().equals("")) {
                    weight = Double.parseDouble(userWeight.getText().toString());
                }
                if (!userCalories.getText().toString().equals("")) {
                    calories = Double.parseDouble(userCalories.getText().toString());
                }
                mDailyCommunicator.WeightCaloriesData(weight, calories);
                dismiss();
            }
        });

        return new AlertDialog.Builder(getActivity()).setView(v).setTitle("Daily Log").create();

    }

    // Run IO to for recognizer
    private void runRecognizerSetup(){
        // Async task to run recognizer
        new AsyncTask<Void, Void, Exception>(){
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(getActivity());
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                }catch(IOException ioe){
                    return ioe;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result){
                if (result != null){
                }
            }
        }.execute();

    }

        private void setupRecognizer(File assetsDir) throws IOException {
        recognizer = SpeechRecognizerSetup.defaultSetup().setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File (assetsDir, "cmudict-en-us.dict"))
                .setRawLogDir(assetsDir)
                .getRecognizer();
        recognizer.addListener(this);

        // digits workout grammar search
        File digitsGrammar = new File(assetsDir, "digits.gram");
        recognizer.addGrammarSearch(KW_DIGITS, digitsGrammar);

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (recognizer != null){
            recognizer.cancel();
            recognizer.shutdown();
        }
    }


    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null){
            return;
        }
//        String text = hypothesis.getHypstr();

    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
        if(hypothesis != null){
            String text = hypothesis.getHypstr();
            String finalNumber = "";
            if (weightSelect){
                finalNumber += numberTranslate(text);
                userWeight.setText(finalNumber);
                statusText.setText("Weight Recorded");
            }else if (calSelect){
                finalNumber += numberTranslate(text);
                userCalories.setText(finalNumber);
                statusText.setText("Calories Recorded");
            }
            makeText(this.getActivity(), text, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onEndOfSpeech() {
        recognizer.stop();
    }

    @Override
    public void onError(Exception e) {
    }

    @Override
    public void onTimeout() {
        recognizer.stop();
    }


    public double numberTranslate(String numString){
        Scanner s = new Scanner(numString);
        String currentWord = "";
        int oneDigitArray [] = new int[]{0};
        int twoDigitArray [] = new int[]{0,0};
        int threeDigitArray [] = new int[]{0,0,0};
        int fourDigitArray [] = new int [] {0, 0, 0, 0};
        int digitTens = 0;
        int digitOnes = 0;
        int decimalNum = 0;
        boolean isThousand = false;
        boolean isHundred = false;
        boolean hasTens = false;
        boolean isTeen = false;
        boolean isDecimal = false;

        while(s.hasNext()) {
            currentWord = s.next();
            if(currentWord.equals("hundred")){
                threeDigitArray[0] = threeDigitArray[2];
                threeDigitArray[2] = 0;
                fourDigitArray[1] = fourDigitArray[3];
                fourDigitArray[3] = 0;
                isHundred = true;
                continue;
            } else if(currentWord.equals("thousand") && (calSelect)) {
                fourDigitArray[0] = fourDigitArray[3];
                fourDigitArray[3] = 0;
                isThousand = true;
                continue;
            } else if(currentWord.equals("point") && (weightSelect)){
                isDecimal = true;
                continue;
            }

            switch (currentWord) {
                case "zero":
                    break;
                case "one":
                    digitOnes = 1;
                    break;
                case "two":
                    digitOnes = 2;
                    break;
                case "three":
                    digitOnes = 3;
                    break;
                case "four":
                    digitOnes = 4;
                    break;
                case "five":
                    digitOnes = 5;
                    break;
                case "six":
                    digitOnes = 6;
                    break;
                case "seven":
                    digitOnes = 7;
                    break;
                case "eight":
                    digitOnes = 8;
                    break;
                case "nine":
                    digitOnes = 9;
                    break;
            }
            if (isDecimal){
                decimalNum = digitOnes;
                break;
            }
            if (calSelect) {
                if (!isThousand) {
                    oneDigitArray[0] = digitOnes;
                    twoDigitArray[1] = digitOnes;
                    threeDigitArray[2] = digitOnes;
                    fourDigitArray[3] = digitOnes;
                } else if (isThousand) {
                    fourDigitArray[3] = digitOnes;
                }
            }
            if (!isHundred) {
                oneDigitArray[0] = digitOnes;
                twoDigitArray[1] = digitOnes;
                threeDigitArray[2] = digitOnes;
                fourDigitArray[3] = digitOnes;
            }else if (isHundred) {
                threeDigitArray[2] = digitOnes;
                fourDigitArray[3] = digitOnes;
            }

            if (hasTens){
                break;
            }

            digitOnes = 0;
            switch (currentWord) {
                case "ten":
                    digitOnes = 0;
                    isTeen = true;
                    break;
                case "eleven":
                    digitOnes = 1;
                    isTeen = true;
                    break;
                case "twelve":
                    digitOnes = 2;
                    isTeen = true;
                    break;
                case "thirteen":
                    digitOnes = 3;
                    isTeen = true;
                    break;
                case "fourteen":
                    digitOnes = 4;
                    isTeen = true;
                    break;
                case "fifteen":
                    digitOnes = 5;
                    hasTens = true;
                    break;
                case "sixteen":
                    digitOnes = 6;
                    isTeen = true;
                    break;
                case "seventeen":
                    digitOnes = 7;
                    isTeen = true;
                    break;
                case "eighteen":
                    digitOnes = 8;
                    isTeen = true;
                    break;
                case "nineteen":
                    digitOnes = 9;
                    isTeen = true;
                    break;
            }
            if (isTeen) {
                digitTens = 1;
                twoDigitArray[0] = digitTens;
                twoDigitArray[1] = digitOnes;
                threeDigitArray[1] = digitTens;
                threeDigitArray[2] = digitOnes;
                fourDigitArray[2] = digitTens;
                fourDigitArray[3] = digitOnes;
            }
            digitTens = 0;
            switch (currentWord) {
                case "twenty":
                    digitTens = 2;
                    hasTens = true;
                    break;
                case "thirty":
                    digitTens = 3;
                    hasTens = true;
                    break;
                case "forty":
                    digitTens = 4;
                    hasTens = true;
                    break;
                case "fifty":
                    digitTens = 5;
                    hasTens = true;
                    break;
                case "sixty":
                    digitTens = 6;
                    hasTens = true;
                    break;
                case "seventy":
                    digitTens = 7;
                    hasTens = true;
                    break;
                case "eighty":
                    digitTens = 8;
                    hasTens = true;
                    break;
                case "ninety":
                    digitTens = 9;
                    hasTens = true;
                    break;
            }
            if (hasTens) {
                twoDigitArray[0] = digitTens;
                threeDigitArray[1] = digitTens;
                fourDigitArray[2] = digitTens;
            }
        }

        String finalNumString = "";
        double finalNum = 0;
        if (isThousand){
            finalNumString += fourDigitArray[0] + "" + fourDigitArray[1] + "" + fourDigitArray[2] + "" + fourDigitArray[3];
        }
        else if (isHundred){
            finalNumString += threeDigitArray[0] + "" + threeDigitArray[1] + "" + threeDigitArray[2];
        }else if (hasTens || isTeen){
            finalNumString += twoDigitArray[0] + "" + twoDigitArray[1];
        }else{
            finalNumString += oneDigitArray[0];
        }
        finalNum = Double.parseDouble(finalNumString);
        if (isDecimal){
            finalNumString = "0." + decimalNum;
            finalNum += Double.parseDouble(finalNumString);
        }
        return finalNum;
    }

    // Interface to get Calories and Weight data
    interface DailyCommunicator{
        public void WeightCaloriesData(double weight, double calories);
    }

}


