package com.example.s2t_empty;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.model.ListEntryObject;
import com.example.services.OurUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SavedText extends Fragment {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //TODO: maybe add title to page

        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_saved_text, container, false);

        // fill listview with values from shared preferences
        ListView listView = view.findViewById(R.id.listView);
        SharedPreferences sp = getActivity().getSharedPreferences(String.valueOf(R.string.sp_name), Context.MODE_PRIVATE);
        CustomAdapter customAdapter = new CustomAdapter(getActivity(), 0, makeShowableList((HashMap<String, String>) sp.getAll())); //TODO fix warning
        listView.setAdapter(customAdapter);
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private List<ListEntryObject> makeShowableList(HashMap<String, String> sharedPref){
        OurUtils utils = new OurUtils();
        List<ListEntryObject> showableList = new ArrayList<>();
        sharedPref.entrySet().forEach(input -> showableList.add(new ListEntryObject(input.getKey(), utils.makeListEntryTitle(input.getKey()), input.getValue())));
        return showableList;
    }

    private class CustomAdapter extends ArrayAdapter {
        List<ListEntryObject> entries;

        public CustomAdapter(@NonNull Context context, int resource, @NonNull List<ListEntryObject> entries) {
            super(context, resource, entries);
            this.entries = entries;
        }

        @Override
        public int getCount() {
            return entries.size();
        }

        @Override
        public Object getItem(int i) {
            return entries.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            final View result;

            if (view == null) {
                result = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_entry, viewGroup, false);
            } else {
                result = view;
            }

            TextView title = result.findViewById(R.id.title);
            TextView subtitle = result.findViewById(R.id.subtitle);
            ImageView deleteIcon = result.findViewById(R.id.delete_icon);

            deleteIcon.setVisibility(View.INVISIBLE);
            ListEntryObject entry = entries.get(i);
            title.setText(entry.getTitle());
            subtitle.setText(defineEntryText(entry));

            result.setOnClickListener(v -> {
                entry.setOpened(!entry.isOpenend());
                subtitle.setText(defineEntryText(entry));
            });

            result.setOnLongClickListener(v -> {
                switch (deleteIcon.getVisibility()){
                    case View.INVISIBLE:
                        deleteIcon.setVisibility(View.VISIBLE);
                        break;
                    case View.VISIBLE:
                        deleteIcon.setVisibility(View.INVISIBLE);
                        break;
                }
                return true;
            });

            deleteIcon.setOnClickListener(v -> {
                SharedPreferences sp = getActivity().getSharedPreferences(String.valueOf(R.string.sp_name), Context.MODE_PRIVATE);
                if(sp.contains(entries.get(i).getKey())){
                    SharedPreferences.Editor editor = sp.edit();
                    editor.remove(entries.get(i).getKey());
                    editor.apply();
                    remove(entries.get(i));
                    this.notifyDataSetChanged();
                }
            });
            //TODO: add possibility to delete items?
            return result;
        }

        private String defineEntryText(ListEntryObject entry){
            if(entry.isOpenend()) {
                return entry.getText();
            } else {
                return entry.getText().substring(0,50) .concat("...");
            }
        }


    }
}