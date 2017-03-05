package com.example.magena.glasspicturetofirebase;

/**
 * Created by Magena on 3/5/2017.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.example.magena.glasspicturetofirebase.card.EmbeddedCardLayoutActivity;
import com.example.magena.glasspicturetofirebase.opengl.OpenGlService;
import com.example.magena.glasspicturetofirebase.slider.SliderActivity;
import com.example.magena.glasspicturetofirebase.theming.TextAppearanceActivity;
import com.example.magena.glasspicturetofirebase.touchpad.SelectGestureDemoActivity;
import com.example.magena.glasspicturetofirebase.voicemenu.VoiceMenuActivity;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import com.example.magena.glasspicturetofirebase.card.CardAdapter;
import com.example.magena.glasspicturetofirebase.card.CardBuilderActivity;
import com.example.magena.glasspicturetofirebase.card.CardScrollViewActivity;

import java.util.ArrayList;
import java.util.List;


public class TakePictureActivity extends Activity{

    private CardBuilder cardBuilder;
    private CardScrollView mCardScroller;

    static final int TAKE_PICTURE = 0;
    static final int SEND_TO_FIREBASE = 1;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        cardBuilder = new CardBuilder(getApplicationContext(),CardBuilder.Layout.TEXT);
        cardBuilder.setText("Taking a Picture");
        View cardView = cardBuilder.getView();
        setContentView(cardView);

    }

    private List<CardBuilder> createCards(Context context) {
        ArrayList<CardBuilder> cards = new ArrayList<CardBuilder>();
        cards.add(TAKE_PICTURE, new CardBuilder(context, CardBuilder.Layout.TEXT)
                .setText(R.string.text_identify_person));
        cards.add(SEND_TO_FIREBASE, new CardBuilder(context, CardBuilder.Layout.TEXT)
                .setText(R.string.text_card_builder));
        return cards;
    }

    private void setCardScrollerListener() {
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                int soundEffect = Sounds.TAP;
                switch (position) {
                    case TAKE_PICTURE:
                        CardBuilder card = new CardBuilder(getApplicationContext(), CardBuilder.Layout.TEXT);
                        card.setText("Opening Camera");
                        View cardView = card.getView();
                        setContentView(cardView);
                        startActivity(new Intent(TakePictureActivity.this, ZoomActivity.class));
                        break;

                    case SEND_TO_FIREBASE:
                        startActivity(new Intent(TakePictureActivity.this, ZoomActivity.class));
                        break;

                    default:
                        soundEffect = Sounds.ERROR;
                }

                // Play sound.
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.playSoundEffect(soundEffect);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        CardBuilder card = new CardBuilder(this, CardBuilder.Layout.TEXT);
        card.setText("Resuming Taking a Picture");
        View cardView = card.getView();
        setContentView(cardView);
    }

    @Override
    protected void onPause() {
        CardBuilder card = new CardBuilder(this, CardBuilder.Layout.TEXT);
        card.setText("Pausing Picture-Taking Activity");
        View cardView = card.getView();
        setContentView(cardView);
        super.onPause();
    }
}
