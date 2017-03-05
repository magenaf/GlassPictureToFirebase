package com.example.magena.glasspicturetofirebase;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.content.Context;
import android.view.View;

import com.example.magena.glasspicturetofirebase.card.CardBuilderActivity;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.example.magena.glasspicturetofirebase.card.CardAdapter;



import com.google.android.glass.media.Sounds;
import com.example.magena.glasspicturetofirebase.card.CardScrollViewActivity;
import com.example.magena.glasspicturetofirebase.card.EmbeddedCardLayoutActivity;
import com.example.magena.glasspicturetofirebase.opengl.OpenGlService;
import com.example.magena.glasspicturetofirebase.slider.SliderActivity;
import com.example.magena.glasspicturetofirebase.theming.TextAppearanceActivity;
import com.example.magena.glasspicturetofirebase.touchpad.SelectGestureDemoActivity;
import com.example.magena.glasspicturetofirebase.voicemenu.VoiceMenuActivity;
import com.google.android.glass.widget.CardScrollView;



public class MenuActivity extends Activity {
    private static final String TAG = "MenuActivity";
    private CardBuilder cardBuilder;
    private CardScrollAdapter mAdapter;
    private CardScrollView mCardScroller;

    CardScrollView getScroller() {
        return mCardScroller;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cardBuilder = new CardBuilder(getApplicationContext(),CardBuilder.Layout.TEXT);
        cardBuilder.setText("Identify A Person");
        View cardView = cardBuilder.getView();
        setContentView(cardView);
    }

    @Override
    public void onResume() {
        super.onResume();
        openOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.stop:
                stopService(new Intent(this, AppService.class));
                return true;

            case R.id.preview:
                Intent intent = new Intent(this, PreviewActivity.class);
                startActivity(intent);
                return true;

            case R.id.zoom:
                Intent intent2 = new Intent(this, ZoomActivity.class);
                startActivity(intent2);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        finish();

    }

}
