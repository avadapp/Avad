package com.android.avad.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.android.avad.R;
import com.android.avad.models.AutoCompleteModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sagar_000 on 10/27/2016.
 */
public class AutoCompleteAdapter extends ArrayAdapter<AutoCompleteModel> {
    private LayoutInflater layoutInflater;
    List<AutoCompleteModel> mModel;

    private Filter mFilter = new Filter() {
        @Override
        public String convertResultToString(Object resultValue) {
            return ((AutoCompleteModel)resultValue).getInterests();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null) {
                ArrayList<AutoCompleteModel> suggestions = new ArrayList<>();
                for (AutoCompleteModel model : mModel) {
                    // Note: change the "contains" to "startsWith" if you only want starting matches
                    if (model.getInterests().toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                        suggestions.add(model);
                    }
                }

                results.values = suggestions;
                results.values = suggestions.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            if (results != null && results.count > 0) {
                // we have filtered results
                addAll((ArrayList<AutoCompleteModel>) results.values);
            } else {
                // no filter, add entire original list back in
                addAll(mModel);
            }
            notifyDataSetChanged();
        }
    };

    public AutoCompleteAdapter(Context context, int textViewResourceId, List<AutoCompleteModel> models) {
        super(context, textViewResourceId, models);
        // copy all the customers into a master list
        mModel = new ArrayList<>(models.size());
        mModel.addAll(models);
        layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = layoutInflater.inflate(R.layout.auto_complete_items, null);
        }

        AutoCompleteModel model = getItem(position);

        TextView name = (TextView) view.findViewById(R.id.auto_text);
        name.setText(model.getInterests());
        TextView peopleCount = (TextView) view.findViewById(R.id.auto_text_counts);
        peopleCount.setText(model.getPeopleCount());

        return view;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }
}
