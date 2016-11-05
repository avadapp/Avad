package com.android.avad.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.android.avad.R;
import com.android.avad.models.AutoCompleteModel;

import java.util.ArrayList;

/**
 * Created by sagar_000 on 10/27/2016.
 */
public class AutoCompleteAdapter extends ArrayAdapter<AutoCompleteModel> {

    private static final String LOG_TAG = AutoCompleteAdapter.class.getSimpleName();

    ArrayList<AutoCompleteModel> customers, tempCustomer, suggestions;

    public AutoCompleteAdapter(Context context, ArrayList<AutoCompleteModel> objects) {
        super(context, android.R.layout.simple_list_item_1, objects);
        this.customers = objects;
        this.tempCustomer = new ArrayList<>(objects);
        this.suggestions = new ArrayList<>(objects);

    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AutoCompleteModel customer = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.auto_complete_items, parent, false);
        }
        TextView txtCustomer = (TextView) convertView.findViewById(R.id.auto_text);
        TextView ivCustomerImage = (TextView) convertView.findViewById(R.id.auto_text_counts);
        if (txtCustomer != null)
            txtCustomer.setText(customer.getInterests());
        if (ivCustomerImage != null)
            ivCustomerImage.setText(customer.getPeopleCount());
        // Now assign alternate color for rows
        if (position % 2 == 0)
            convertView.setBackgroundColor(getContext().getColor(R.color.colorAccent));
        else
            convertView.setBackgroundColor(getContext().getColor(R.color.colorPrimary));

        return convertView;
    }


    @Override
    public Filter getFilter() {
        return myFilter;
    }

    Filter myFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            AutoCompleteModel customer = (AutoCompleteModel) resultValue;
            return customer.getInterests();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                suggestions.clear();
                for (AutoCompleteModel people : tempCustomer) {
                    if (people.getInterests().toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                        suggestions.add(people);
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<AutoCompleteModel> c = (ArrayList<AutoCompleteModel>) results.values;
            if (results != null && results.count > 0) {
                clear();
                for (AutoCompleteModel cust : c) {
                    add(cust);
                    notifyDataSetChanged();
                }
            }
        }
    };
}
