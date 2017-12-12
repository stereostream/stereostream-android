package io.complicated.stereostream.api.room;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import io.complicated.stereostream.R;
import io.complicated.stereostream.utils.PrimitiveArrayAdapter;

public final class RoomAdapter extends PrimitiveArrayAdapter<Room> {
    public RoomAdapter(final Context context, final Room[] rooms) {
        super(context, rooms);
    }

    @Override
    @NonNull
    public final View getView(final int position, View convertView,
                              final @NonNull ViewGroup parent) {
        // Get the data item for this position
        final Room room = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.room_item, parent, false);

        convertView.getLayoutParams().height = 70;

        // Lookup view for data population
        final TextView name = (TextView) convertView.findViewById(R.id.room_item_name);
        final TextView owner = (TextView) convertView.findViewById(R.id.room_item_owner);
        // Populate the data into the template view using the data object
        name.setText(room.getName());
        owner.setText(room.getOwner());
        // Return the completed view to render on screen
        return convertView;
    }
}
