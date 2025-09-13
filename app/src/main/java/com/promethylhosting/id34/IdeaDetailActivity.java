package com.promethylhosting.id34;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class IdeaDetailActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idea_detail);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putString(IdeaDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(IdeaDetailFragment.ARG_ITEM_ID));
            IdeaDetailFragment fragment = new IdeaDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.idea_detail_container, fragment)
                    .commit();
        }
    }
    
    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
        	startActivity(new Intent(getApplicationContext(), IdeaAddActivity.class));
            //NavUtils.navigateUpTo(this, new Intent(this, IdeaListActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
