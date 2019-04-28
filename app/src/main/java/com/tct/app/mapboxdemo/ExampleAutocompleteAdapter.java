package com.tct.app.mapboxdemo;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mapbox.android.search.autocomplete.AutocompleteAdapter;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;

public class ExampleAutocompleteAdapter extends AutocompleteAdapter {
  private Context mContext;

  public ExampleAutocompleteAdapter(Context context) {
    super(context);
    mContext = context;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View view = inflateView(convertView, parent);
    CarmenFeature feature = getItem(position);
    return updateViewData(view, feature);
  }

  private View inflateView(View convertView, ViewGroup parent) {
    if (convertView == null) {
      LayoutInflater inflater = LayoutInflater.from(mContext);
      return inflater.inflate(R.layout.example_autocomplete_list_item,
              parent, false);
    } else {
      return convertView;
    }
  }

  private View updateViewData(View view, CarmenFeature feature) {
    TextView text = view.findViewById(R.id.listItemText);
    text.setText(feature.text());

    TextView address = view.findViewById(R.id.listItemAddress);
    if (TextUtils.isEmpty(feature.address())) {
      address.setVisibility(View.GONE);
    } else {
      address.setText(feature.address());
    }
    return view;
  }
}