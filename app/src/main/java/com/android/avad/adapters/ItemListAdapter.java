package com.android.avad.adapters;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.android.avad.models.Search;
import com.android.avad.R;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sagar_000 on 8/10/2016.
 */
public class ItemListAdapter extends FirebaseListAdapter<Search> {

    public ItemListAdapter(Query ref , int layout, Activity activity) {
    super(ref,Search.class,layout,activity);
    }

    /**
     * Bind an instance of the ExampleObject class to our view. This method is called by <code>FirebaseListAdapter</code>
     * when there is a data change, and we are given an instance of a View that corresponds to the layout that we passed
     * to the constructor, as well as a single ExampleObject instance that represents the current data to bind.
     *
     * @param v    A view instance corresponding to the layout we passed to the constructor.
     * @param model An instance representing the current state of a message
     */

    @Override
    protected void populateView(View v, Search model) {
        TextView body = (TextView)v.findViewById(R.id.auto_text);
        // populate the list element
        //body.setText(model.getBody());
    }

    @Override
    protected List<Search> filters(List<Search> models, CharSequence constraint) {
        List<Search> filterList = new ArrayList<>();
        for (int i = 0; i < models.size(); i++) {
            /* implement your own filtering logic
             * and then call  filterList.add(models.get(i));
             */
            filterList.add(models.get(i));
        }
        return filterList;
    }

    @Override
    protected Map<String, Search> filterKeys(List<Search> mModels) {
        return null;
    }
}
