package com.example.s2t_empty;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;

public class SavingPopup extends Activity {

    String text;
    String fileName;

    EditText nameEdit;
    SharedPreferences sp;

    //TODO: show whole layout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saving_popup);

        this.nameEdit = findViewById(R.id.editTextTextPersonName);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        this.text = getIntent().getStringExtra("text");
        this.fileName = getIntent().getStringExtra("fileName");

        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width*.8), (int) (height*.5));

//        WindowManager.LayoutParams params = getWindow().getAttributes();
//        params.gravity = Gravity.CENTER;
//        params.x = 0;
//        params.y = -20;

//        getWindow().setAttributes(params);
    }

    public void closePopup(View myView){
        finish();
    }

    public void saveText(View myView){
        sp = getSharedPreferences("MySavedTexts", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        String key = generateKey();
        //TODO: check and handle case that key already exists in sp
        if(key.isEmpty()){
            key = "savedText1";
        }
        editor.putString(key, this.text);
        Intent intent = new Intent();
        intent.putExtra("name", nameEdit.getText().toString());
        setResult(1, intent);
        editor.commit(); //TODO: apply instead of commit?
        finish();
    }

    //TODO: include date in a better way?
    private String generateKey(){
        String nameToSave = this.nameEdit.getText().toString();
        return fileName + "_" + nameToSave;
    }
}