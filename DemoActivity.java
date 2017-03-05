package com.example.magena.glasspicturetofirebase;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import java.util.ArrayList;
import java.util.List;


import com.google.android.glass.media.Sounds;
import com.example.magena.glasspicturetofirebase.card.CardAdapter;
import com.example.magena.glasspicturetofirebase.card.CardBuilderActivity;
import com.example.magena.glasspicturetofirebase.card.CardScrollViewActivity;
import com.example.magena.glasspicturetofirebase.card.EmbeddedCardLayoutActivity;
import com.example.magena.glasspicturetofirebase.opengl.OpenGlService;
import com.example.magena.glasspicturetofirebase.slider.SliderActivity;
import com.example.magena.glasspicturetofirebase.theming.TextAppearanceActivity;
import com.example.magena.glasspicturetofirebase.touchpad.SelectGestureDemoActivity;
import com.example.magena.glasspicturetofirebase.voicemenu.VoiceMenuActivity;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a card scroll view with examples of different GDK APIs.
 *
 * <ol>
 * <li> CardBuilder API
 * <li> CardScrollView API
 * <li> GestureDetector
 * <li> textAppearance[Large|Medium|Small]
 * <li> OpenGL LiveCard
 * <li> VoiceMenu
 * </ol>
 */
public class DemoActivity extends Activity {

    private static final String TAG = DemoActivity.class.getSimpleName();

    // Index of api demo cards.
    // Visible for testing.
    static final int IDENTIFY_PERSON = 0;
    static final int CARD_BUILDER = 1;
    static final int CARD_BUILDER_EMBEDDED_LAYOUT = 2;
    static final int CARD_SCROLL_VIEW = 3;
    static final int GESTURE_DETECTOR = 4;
    static final int TEXT_APPEARANCE = 5;
    static final int OPENGL = 6;
    static final int VOICE_MENU = 7;
    static final int SLIDER = 8;

    private CardScrollAdapter mAdapter;
    private CardScrollView mCardScroller;

    // Visible for testing.
    CardScrollView getScroller() {
        return mCardScroller;
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        mAdapter = new CardAdapter(createCards(this));
        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(mAdapter);
        setContentView(mCardScroller);
        setCardScrollerListener();
    }

    /**
     * Create list of API demo cards.
     */
    private List<CardBuilder> createCards(Context context) {
        ArrayList<CardBuilder> cards = new ArrayList<CardBuilder>();
        cards.add(IDENTIFY_PERSON, new CardBuilder(context, CardBuilder.Layout.TEXT)
                .setText(R.string.text_identify_person));
        cards.add(CARD_BUILDER, new CardBuilder(context, CardBuilder.Layout.TEXT)
                .setText(R.string.text_card_builder));
        cards.add(CARD_BUILDER_EMBEDDED_LAYOUT, new CardBuilder(context, CardBuilder.Layout.TEXT)
                .setText(R.string.text_card_builder_embedded_layout));
        cards.add(CARD_SCROLL_VIEW, new CardBuilder(context, CardBuilder.Layout.TEXT)
                .setText(R.string.text_card_scroll_view));
        cards.add(GESTURE_DETECTOR, new CardBuilder(context, CardBuilder.Layout.TEXT)
                .setText(R.string.text_gesture_detector));
        cards.add(TEXT_APPEARANCE, new CardBuilder(context, CardBuilder.Layout.TEXT)
                .setText(R.string.text_text_appearance));
        cards.add(OPENGL, new CardBuilder(context, CardBuilder.Layout.TEXT)
                .setText(R.string.text_opengl));
        cards.add(VOICE_MENU, new CardBuilder(context, CardBuilder.Layout.TEXT)
                .setText(R.string.text_voice_menu));
        cards.add(SLIDER, new CardBuilder(context, CardBuilder.Layout.TEXT)
                .setText(R.string.text_slider));
        return cards;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCardScroller.activate();
    }

    @Override
    protected void onPause() {
        mCardScroller.deactivate();
        super.onPause();
    }

    /**
     * Different type of activities can be shown, when tapped on a card.
     */
    private void setCardScrollerListener() {
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Clicked view at position " + position + ", row-id " + id);
                int soundEffect = Sounds.TAP;
                switch (position) {
                    case IDENTIFY_PERSON:
                        CardBuilder card = new CardBuilder(getApplicationContext(),CardBuilder.Layout.TEXT);
                        card.setText("Opening Camera");
                        View cardView = card.getView();
                        setContentView(cardView);
                        startActivity(new Intent(DemoActivity.this, TakePictureActivity.class));
                        break;

                    case CARD_BUILDER:
                        startActivity(new Intent(DemoActivity.this, CardBuilderActivity.class));
                        break;

                    case CARD_BUILDER_EMBEDDED_LAYOUT:
                        startActivity(new Intent(
                                DemoActivity.this, EmbeddedCardLayoutActivity.class));
                        break;

                    case CARD_SCROLL_VIEW:
                        startActivity(new Intent(DemoActivity.this,
                                CardScrollViewActivity.class));
                        break;

                    case GESTURE_DETECTOR:
                        startActivity(new Intent(DemoActivity.this,
                                SelectGestureDemoActivity.class));
                        break;

                    case TEXT_APPEARANCE:
                        startActivity(new Intent(DemoActivity.this,
                                TextAppearanceActivity.class));
                        break;

                    case OPENGL:
                        startService(new Intent(DemoActivity.this, OpenGlService.class));
                        break;

                    case VOICE_MENU:
                        startActivity(new Intent(DemoActivity.this, VoiceMenuActivity.class));
                        break;

                    case SLIDER:
                        startActivity(new Intent(DemoActivity.this, SliderActivity.class));
                        break;

                    default:
                        soundEffect = Sounds.ERROR;
                        Log.d(TAG, "Don't show anything");
                }

                // Play sound.
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.playSoundEffect(soundEffect);
            }
        });
    }
}
