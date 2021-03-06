package com.ABCDeve1opers.flashbot.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RenameDeckActivity extends AppCompatActivity {
    private String deckName ;
    private EditText newDeckName;
    private TextView currentDeckName;
    private Button renameDeck;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rename_deck);
        deckName = getIntent().getStringExtra("deck name");
        renameDeck = (Button) findViewById(R.id.rename_deck_button);
        currentDeckName = (TextView) findViewById(R.id.current_deck_name);
        newDeckName = (EditText) findViewById(R.id.new_deck_name);
        currentDeckName.setText(getString(R.string.current_deck_name) + " " + deckName );

        Toolbar toolbar = (Toolbar) findViewById(R.id.rename_deck_toolbar);
        setSupportActionBar(toolbar);
//        toolbar.setTitleTextColor();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.rename_deck));
        newDeckName.setText(deckName);

        renameDeck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               updateDeckName();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(getApplicationContext(),RenameDeckActivity.class);
        intent.putExtra("oldName",deckName);
        setResult(RESULT_CANCELED,intent);
        finish();
        return true;
    }

    private void updateDeckName() {
        String newDeckNameString = newDeckName.getText().toString();
        if (newDeckNameString.isEmpty()){
            Toast.makeText(getApplicationContext(),getString(R.string.enter_deck_name),Toast.LENGTH_LONG);
        }else  if(newDeckNameString == deckName){
            Toast.makeText(getApplicationContext(),getString(R.string.new_deck_name_prompt),Toast.LENGTH_LONG);
        }else{
            Intent intent = new Intent(getApplicationContext(),RenameDeckActivity.class);
            intent.putExtra("newName",newDeckNameString);
            setResult(RESULT_OK,intent);
            finish();
        }
    }


}
