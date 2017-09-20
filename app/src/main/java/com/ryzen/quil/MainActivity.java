package com.ryzen.quil;

import android.Manifest;
import android.app.FragmentManager;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

import static android.widget.Toast.makeText;

public class MainActivity extends AppCompatActivity implements RecognitionListener, DatePickerFragment.DateCommunicator,
    DailyFragment.DailyCommunicator{

    Button recordBtn, stopBtn, saveBtn, addBtn;

    TextView dateText, dateTitle;

    TextView weightText, weightTitle;

    TextView caloriesText, caloriesTitle;

    TextView outputText, statusText, resultText;

    EditText userWeight, userCalories;

    boolean weightSelect = true;
    boolean calSelect = false;

    private boolean workoutBool = true;

    String bodyType, movementType = "";
    int setNum, weightsNum, repsNum = -1;
    int month, day, year = 0;

    private static final String TAG = "MainActivity";

    private final String KW_SEARCH = "search";
    private final String KW_CHEST = "chest";
    private final String KW_BACK = "back";
    private final String KW_SHOULDERS = "shoulders";
    private final String KW_LEGS = "legs";
    private final String KW_ARMS = "arms";
    private final String KW_CORE = "core";
    private final String KW_REST = "rest";
    private final String KW_SETS = "sets";
    private final String KW_REPS = "reps";
    private final String KW_WEIGHTS = "weights";
    private final String KW_DIGITS = "digits";

    ArrayList<ChestData> ChestRecord = new ArrayList<ChestData>();
    ArrayList<BackData> BackRecord = new ArrayList<BackData>();
    ArrayList<ShouldersData> ShouldersRecord = new ArrayList<ShouldersData>();
    ArrayList<LegData> LegRecord = new ArrayList<LegData>();
    ArrayList<ArmsData> ArmsRecord = new ArrayList<ArmsData>();
    ArrayList<CoreData> CoreRecord = new ArrayList<CoreData>();
    ArrayList<RestData> restRecord = new ArrayList<RestData>();

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    SpeechRecognizer recognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recordBtn = (Button) findViewById(R.id.recordBtn);
        stopBtn = (Button) findViewById(R.id.stopBtn);
        saveBtn = (Button) findViewById(R.id.saveBtn);
        addBtn = (Button) findViewById(R.id.addBtn);

        outputText = (TextView) findViewById(R.id.outputText);
        statusText = (TextView) findViewById(R.id.statusText);
        resultText = (TextView) findViewById(R.id.resultText);

        userWeight = (EditText) findViewById(R.id.userWeight);
        userCalories = (EditText) findViewById(R.id.userCalories);

        // Checks permission for microphone recording on device
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }

        runRecognizerSetup();

        // Button to start recording workout
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isMovement = false;
                isSet = false;
                isWeight = false;
                isRep = false;
                workoutBool = true;
                statusText.setText("Choose Body Part: ");
                recognizer.startListening(KW_SEARCH);
            }
        });

        // Button that stops the recording
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                statusText.setText("Ready");
                recognizer.stop();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createData();
            }
        });

        // Fragment to have user input weight and calories
        weightText = (TextView) findViewById(R.id.weightText);
        weightTitle = (TextView) findViewById(R.id.weightTitle);
        weightTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recognizer.stop();
                weightSelect = true;
                calSelect = false;
                userWeight.setText("");
                statusText.setText("Recording in Weight");
                recognizer.startListening(KW_DIGITS);
//                DailyFragment dailyFrag = new DailyFragment();
//                dailyFrag.show(getSupportFragmentManager(), "Daily_Fragment");
            }
        });

        // Calories, calls same fragment as weight
        caloriesText = (TextView) findViewById(R.id.caloriesText);
        caloriesTitle = (TextView) findViewById(R.id.caloriesTitle);
        caloriesTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recognizer.stop();
                calSelect = true;
                weightSelect = false;
                userCalories.setText("");
                statusText.setText("Recording in Calories");
                recognizer.startListening(KW_DIGITS);
//                DailyFragment dailyFrag = new DailyFragment();
//                dailyFrag.show(getSupportFragmentManager(), "Daily_Fragment");
            }
        });


        // Date picking dialog fragment
        dateText = (TextView) findViewById(R.id.dateText);
        dateTitle = (TextView) findViewById(R.id.dateTitle);
//        dateText.setText(month + "/" + day + "/"+ year);
        dateTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerFragment datePickerFragment = new DatePickerFragment();
                datePickerFragment.show(getSupportFragmentManager(), "DatePicker");
            }
        });
    }


    // Run IO to for recognizer
    private void runRecognizerSetup(){
        // Async task to run recognizer
        new AsyncTask<Void, Void, Exception>(){
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(MainActivity.this);
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
                else{
                    statusText.setText("Ready");
                }
            }
        }.execute();

    }

    // Asks and checks for permissions for record audio input
    @Override
    public void onRequestPermissionsResult(int requestCode, String[]permissions, int[]grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO){
            if(grantResults.length>0 && grantResults[0] ==PackageManager.PERMISSION_GRANTED) {
                runRecognizerSetup();
            }else{
                finish();
            }
        }
    }


    private void setupRecognizer(File assetsDir) throws IOException{
        recognizer = SpeechRecognizerSetup.defaultSetup().setAcousticModel(new File (assetsDir, "en-us-ptm"))
                .setDictionary(new File (assetsDir, "cmudict-en-us.dict"))
                .setRawLogDir(assetsDir)
                .getRecognizer();
        recognizer.addListener(this);

        // Body part workout grammar search
        File searchGrammar = new File(assetsDir, "body.gram");
        recognizer.addGrammarSearch(KW_SEARCH, searchGrammar);

        // Chest grammar search
        File chestGrammar = new File(assetsDir, "chest.gram");
        recognizer.addGrammarSearch(KW_CHEST, chestGrammar);

        // Back grammar search
        File backGrammar = new File(assetsDir, "back.gram");
        recognizer.addGrammarSearch(KW_BACK, backGrammar);

        // Shoulders grammar search
        File shouldersGrammar = new File(assetsDir, "shoulders.gram");
        recognizer.addGrammarSearch(KW_SHOULDERS, shouldersGrammar);

        // Legs grammar search
        File legsGrammar = new File(assetsDir, "legs.gram");
        recognizer.addGrammarSearch(KW_LEGS, legsGrammar);

        // Arms grammar search
        File armsGrammar = new File(assetsDir, "arms.gram");
        recognizer.addGrammarSearch(KW_ARMS, armsGrammar);

        // Core grammar search
        File coreGrammar = new File(assetsDir, "core.gram");
        recognizer.addGrammarSearch(KW_CORE, coreGrammar);

        // Set grammar search
        File setsGrammar = new File(assetsDir, "sets.gram");
        recognizer.addGrammarSearch(KW_SETS, setsGrammar);

        // Reps grammar search
        File repsGrammar = new File(assetsDir, "reps.gram");
        recognizer.addGrammarSearch(KW_REPS, repsGrammar);

        // Weights grammar search
        File weightsGrammar = new File(assetsDir, "weights.gram");
        recognizer.addGrammarSearch(KW_WEIGHTS, weightsGrammar);

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

    boolean isMovement, isSet, isWeight, isRep = false;

    // Method to determine if one of the given body part is chosen
    public void BodyPart(String searchWord){
        recognizer.stop();

        // If words are key words
        if (searchWord.equals(KW_REST)){
            bodyType = searchWord;
            statusText.setText("Resting");
            outputText.setText("Resting");
        }else if (workoutBool) {
            statusText.setText("Recording.. .  ." + searchWord);
            bodyType = searchWord;
            recognizer.startListening(searchWord, 10000);
        }
    }
    public void listenForData() {
        recognizer.stop();

        if (isSet){
            recognizer.startListening(KW_SETS);
        }else if(isWeight){
            recognizer.startListening(KW_WEIGHTS);
        }else if(isRep){
            recognizer.startListening(KW_REPS);
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
        String text = hypothesis.getHypstr();
        if(workoutBool){
            BodyPart(text);
        }

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
                finalNumber += weightCaloriesTranslate(text);
                userWeight.setText(finalNumber);
                statusText.setText("Weight Recorded");
            }else if (calSelect){
                finalNumber += weightCaloriesTranslate(text);
                userCalories.setText(finalNumber);
                statusText.setText("Calories Recorded");
            }

            if (workoutBool){
                outputText.setText("workout is false");
                isMovement = true;
                workoutBool = false;
            }else if (isMovement) {
                outputText.setText("Movementing");
                movementType = text;
            }else if (isSet) {
                outputText.setText("Setting");
                movementType = text;
                isSet = false;
                isWeight = true;
            }else if (isWeight){
                setNum = numberTranslate(text);
                isWeight = false;
                isRep = true;
            }else if(isRep){
                weightsNum = numberTranslate(text);
                isRep = false;
            }else if (!isRep){
                statusText.setText("Ready");
                repsNum = numberTranslate(text);
            }
            String finalResult = "[" + bodyType + "]" + " [" + movementType + "]\nSet: " + setNum + ", Weight: " + weightsNum +
                    ", Reps: " + repsNum;
            resultText.setText(finalResult);

            makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onEndOfSpeech() {
        if (!workoutBool && isMovement){
            isMovement = false;
            isSet = true;
            listenForData();
        } else if(isSet){
            outputText.setText("Setting_2");
            listenForData();
        } else if(isWeight){
            outputText.setText("weighting_2");
            listenForData();
        } else if(isRep){
            outputText.setText("repping_2");
            listenForData();
        } else outputText.setText("");
    }

    public void createData(){
        if (bodyType.equals(KW_CHEST)){
            outputText.setText(bodyType + " entry has been saved.");
        }else if(bodyType.equals(KW_BACK)){
            outputText.setText(bodyType + " entry has been saved.");
        }else if(bodyType.equals(KW_LEGS)){
            outputText.setText(bodyType + " entry has been saved.");
        }else if(bodyType.equals(KW_ARMS)){
            outputText.setText(bodyType + " entry has been saved.");
        }else if(bodyType.equals(KW_SHOULDERS)) {
            outputText.setText(bodyType + " entry has been saved.");
        }else if(bodyType.equals(KW_CORE)){
            outputText.setText(bodyType + " entry has been saved.");
        }else if(bodyType.equals(KW_REST)){

        }
        else{
            outputText.setText("Please enter valid information");
        }
    }

    @Override
    public void onError(Exception e) {
        statusText.setText("Error error");
    }

    @Override
    public void onTimeout() {
        recognizer.stop();
    }

    // Method to convert text numbers into numbers
    public int numberTranslate(String numString) {
        Scanner s = new Scanner(numString);
        String currentWord = "";
        int oneDigitArray[] = new int[]{0};
        int twoDigitArray[] = new int[]{0, 0};
        int threeDigitArray[] = new int[]{0, 0, 0};
        int digitTens = 0;
        int digitOnes = 0;
        boolean isHundred = false;
        boolean hasTens = false;
        boolean isTeen = false;

        while (s.hasNext()) {
            currentWord = s.next();
            if (currentWord.equals("hundred")) {
                threeDigitArray[0] = threeDigitArray[2];
                threeDigitArray[2] = 0;
                isHundred = true;
                continue;
            }
            if (currentWord.equals("pounds")){
                break;
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

            if (!isHundred) {
                oneDigitArray[0] = digitOnes;
                twoDigitArray[1] = digitOnes;
                threeDigitArray[2] = digitOnes;
            } else if (isHundred) {
                threeDigitArray[2] = digitOnes;
            }

            if (hasTens) {
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
            }
        }

        String finalNumString = "";
        int finalNum = 0;
        if (isHundred) {
            finalNumString += threeDigitArray[0] + "" + threeDigitArray[1] + "" + threeDigitArray[2];
        } else if (hasTens || isTeen) {
            finalNumString += twoDigitArray[0] + "" + twoDigitArray[1];
        } else {
            finalNumString += oneDigitArray[0];
        }
        finalNum = Integer.parseInt(finalNumString);
        return finalNum;
    }

    public double weightCaloriesTranslate(String numString){
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

    // Interface of DatePickerDialog Fragment
    @Override
    public void dateMessage(int month, int day, int year) {
        dateText.setText(month + "/" + day + "/"+ year);
    }

    // Interface to get calories and weight dialog fragment data
    @Override
    public void WeightCaloriesData(double weight, double calories) {
        weightText.setText(Double.toString(weight) + " lbs");
        caloriesText.setText(Double.toString(calories) + " calories");
    }

    public void saveWorkout(){
//        String bodyType, movementType = "";
//        int setNum, weightsNum, repsNum = 0;
        if (bodyType.equals(KW_REST)){
        }
        else if(setNum == -1 || weightsNum == -1 || repsNum == -1) {
            outputText.setText("Fill in all of the necessary information");
            resultText.setText("[TYPE] [MOVEMENT]\\n SET: 0, WEIGHT: 0, REPS: 0");
        }else {
            if (bodyType.equals("KW_CHEST")) {
//                ChestData chestData = new ChestData();
            } else if (bodyType.equals("KW_BACK")) {
//                BackData backData = new BackData();

            } else if (bodyType.equals("KW_SHOULDERS")) {
//                ShouldersData backData = new ShouldersData();

            } else if (bodyType.equals("KW_ARMS")) {
//                ArmsData armsData = new ArmsData();

            } else if (bodyType.equals("KW_LEGS")) {
//                LegData legData = new LegData();

            } else if (bodyType.equals("KW_CORE")) {
//                CoreData coreData = new CoreData();

            }
            outputText.setText("Data has been saved to " + bodyType);
            resultText.setText("[TYPE] [MOVEMENT]\\n SET: 0, WEIGHT: 0, REPS: 0");
            bodyType = "";
            movementType = "";
            setNum = -1;
            weightsNum = -1;
            repsNum = -1;
        }

    }
}














