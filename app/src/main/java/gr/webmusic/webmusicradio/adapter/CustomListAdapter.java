package gr.webmusic.webmusicradio.adapter;

/**
 * Created by Bill Karvelas on 3/3/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

import gr.webmusic.webmusicradio.R;
import gr.webmusic.webmusicradio.app.AppController;
import gr.webmusic.webmusicradio.pojo.Item;

public class CustomListAdapter extends BaseAdapter {
        private Activity activity;
        private LayoutInflater inflater;
        private List<Item> postItems;
        ImageLoader imageLoader = AppController.getInstance().getImageLoader();

        public CustomListAdapter(Activity activity, List<Item> postItems) {
            this.activity = activity;
            this.postItems = postItems;
        }

        @Override
        public int getCount() {
            return postItems.size();
        }

        @Override
        public Object getItem(int location) {
            return postItems.get(location);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (inflater == null)
                inflater = (LayoutInflater) activity
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null)
                convertView = inflater.inflate(R.layout.list_row, null);

            if (imageLoader == null)
                imageLoader = AppController.getInstance().getImageLoader();
            NetworkImageView thumbNail = (NetworkImageView) convertView
                    .findViewById(R.id.thumbnail);
            TextView title = (TextView) convertView.findViewById(R.id.title);

            // getting post data for the row
            Item m = postItems.get(position);

            // thumbnail image
            thumbNail.setImageUrl(m.getThumbnailUrl(), imageLoader);

            // title
            title.setText(m.getTitle());


            return convertView;
        }

    }
