package com.sms1516.porcelli.daniele.wichat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.transition.ChangeBounds;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * An activity representing a single News detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link com.sms1516.porcelli.daniele.wichat.ConversationListActivity}.
 */
public class ConversationDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_detail);
        TextView contactName = (TextView) findViewById(R.id.contact_name);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Intent intent = getIntent();
        contactName.setText(intent.getCharSequenceExtra(CostantKeys.ACTION_START_CONVERSATION_ACTIVITY_EXTRA_NAME));

        //Imposta la transizione animata di entrata dell'activity se l'app
        //viene eseguita su un dispositivo con Android 5.0 o superiore.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setSharedElementEnterTransition(new ChangeBounds());

            //Rimuove l'ombra dell'elevazione dalla action bar.
            getSupportActionBar().setElevation(0);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(CostantKeys.ACTION_START_CONVERSATION_ACTIVITY_EXTRA_MAC,
                    getIntent().getStringExtra(CostantKeys.ACTION_START_CONVERSATION_ACTIVITY_EXTRA_MAC));
            ConversationDetailFragment fragment = new ConversationDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.frameProva, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            Message.deleteAllMessages();
            navigateUpTo(new Intent(this, ConversationListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
