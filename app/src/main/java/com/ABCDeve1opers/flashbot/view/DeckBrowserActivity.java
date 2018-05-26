package com.ABCDeve1opers.flashbot.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.ABCDeve1opers.flashbot.model.Card;
import com.ABCDeve1opers.flashbot.model.Deck;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The deck browser is a tool to search for specific cards and to modify/delete them easily. All
 * cards are presented in a list view. It is also possible to shuffle the deck and to reset the
 * levels of the cards.
 */
public class DeckBrowserActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private static final int MAX_RESULTS = 200;
    private static final String TAG = "DeckBrowserActivity";

    private String deckName;
    private Deck deck;
    private TextToSpeech tts;
    private ArrayAdapter<Card> cardAdapter;
    private List<Card> cards = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck_browser);
        Log.v(TAG,"oncreate deckbrowser activity");

        final ListView cardList = (ListView) findViewById(R.id.card_list);
        cardAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cards);
        cardList.setAdapter(cardAdapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.deck_list_toolbar);
        setSupportActionBar(toolbar);

        // normal click: edit
        cardList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final Card card = cards.get(position);
                final Dialog dialog = new Dialog(DeckBrowserActivity.this);
                dialog.setContentView(R.layout.card_dialog);
                dialog.setTitle(getString(R.string.edit_card));
                final EditText frontEdit = (EditText) dialog.findViewById(R.id.edit_front);
                frontEdit.setText(card.getFront());
                final EditText backEdit = (EditText) dialog.findViewById(R.id.edit_back);
                backEdit.setText(card.getBack());
                Button cancelButton = (Button) dialog.findViewById(R.id.button_cancel);
                Button okButton = (Button) dialog.findViewById(R.id.button_ok);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String front = frontEdit.getText().toString().trim();
                        String back = backEdit.getText().toString().trim();
                        if (front.length() == 0)
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.front_is_empty), Toast.LENGTH_SHORT).show();
                        else if(back.length() == 0)
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.back_is_empty), Toast.LENGTH_SHORT).show();
                        else {
                            card.edit(front, back);
                            deck.saveDeck();
                            cardAdapter.notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    }
                });
                dialog.show();
            }
        });

        // long click: delete
        cardList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final Card card = cardAdapter.getItem(position);
                AlertDialog.Builder dialog = new AlertDialog.Builder(DeckBrowserActivity.this);
                dialog.setTitle(getString(R.string.delete_card));
                dialog.setMessage(getString(R.string.really_delete_card));
                dialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(deck.deleteCard(card)) {
                            Log.v(TAG,"delete card called");
                            cards.remove(position);
                            cardAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.cannot_delete_last_card),
                                    Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                });
                dialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.create().show();
                return true;
            }
        });

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        deckName = getIntent().getStringExtra("deck name");
        setTitle(deckName);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(DeckBrowserActivity.this);
                dialog.setContentView(R.layout.card_dialog);
                dialog.setTitle(getString(R.string.add_new_card));
                final EditText frontEdit = (EditText) dialog.findViewById(R.id.edit_front);
                final EditText backEdit = (EditText) dialog.findViewById(R.id.edit_back);
                Button cancelButton = (Button) dialog.findViewById(R.id.button_cancel);
                Button okButton = (Button) dialog.findViewById(R.id.button_ok);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String front = frontEdit.getText().toString().trim();
                        String back = backEdit.getText().toString().trim();
                        if (front.length() == 0)
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.front_is_empty), Toast.LENGTH_SHORT).show();
                        else if(back.length() == 0)
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.back_is_empty), Toast.LENGTH_SHORT).show();
                        else {
                            Card newCard = new Card(front,back);
                            deck.addNewCard(newCard);
                            cards.add(newCard);
                            deck.saveDeck();
                            cardAdapter.notifyDataSetChanged();
                            dialog.dismiss();


                        }
                    }
                });
                dialog.show();

            }
        });

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.addDrawerListener(toggle);
//        toggle.syncState();
//
//        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        try {
            deck = Deck.loadDeck(deckName);
        } catch(IOException e){
            Toast.makeText(getApplicationContext(), getString(R.string.deck_could_not_be_loaded),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        displayCardList("");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.edit_deck_name){
            Toast.makeText(getApplicationContext(),
                    "edit deck name clicked", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_deck_actions,menu);
        return true;

    }

    private void displayCardList(String searchTerm){
        cards.clear();
        cards.addAll(deck.searchCards(searchTerm, MAX_RESULTS));
        cardAdapter.notifyDataSetChanged();
    }
    private void askForTTSActivation(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.activate_tts_new_deck));
        builder.setMessage(getString(R.string.want_activate_tts));
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                deck.activateTTS();
                initTTS();
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void initTTS(){
        final Locale locale = getLocaleForTTS();
        if(locale != null){
            tts = new TextToSpeech(this, new TextToSpeech.OnInitListener(){
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        tts.setLanguage(locale);
                    }
                }
            });
        }
    }
    private Locale getLocaleForTTS(){
        String lang = deck.getLanguage();
        if(lang == null || lang.equals(""))
            return null;
        String country = deck.getAccent();
        if(country == null || country.equals(""))
            return new Locale(lang);
        return new Locale(lang, country);
    }
    private void reloadDeck(){
        setTitle(deckName);
        try {
            deck = Deck.loadDeck(deckName);
            if(!deck.isUsingTTS() && !deck.getLanguage().equals("") && deck.isNew())
                askForTTSActivation();
            if(deck.isUsingTTS())
                initTTS();
//            showNextCard();
        } catch(IOException e){
            Toast.makeText(getApplicationContext(), getString(R.string.deck_could_not_be_loaded),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
