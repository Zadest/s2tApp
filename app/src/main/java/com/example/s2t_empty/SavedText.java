package com.example.s2t_empty;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.model.ListEntryObject;
import com.example.services.OurUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SavedText extends Fragment {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_saved_text, container, false);

        // action bar
        setHasOptionsMenu(true);

        // fill listview with values from shared preferences
        ListView listView = view.findViewById(R.id.listView);
        SharedPreferences sp = getActivity().getSharedPreferences(String.valueOf(R.string.sp_name), Context.MODE_PRIVATE);
        CustomAdapter customAdapter = new CustomAdapter(getActivity(), 0, makeShowableList((HashMap<String, String>) sp.getAll())); //TODO fix warning
        listView.setAdapter(customAdapter);

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id== R.id.button_info_start_screen){
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Die Speech2Text App");
            builder.setMessage("Hilfe");
            //builder.setIcon(R.drawable.testpic); TODO: eventuell app-icon einfuegen?
            builder.setView(R.layout.help_save_text);
            builder.setPositiveButton("ok", null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    // inflate actionbar
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.menu_actionbar, menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private List<ListEntryObject> makeShowableList(HashMap<String, String> sharedPref){
        List<ListEntryObject> showableList = new ArrayList<>();
        sharedPref.entrySet().forEach(input -> showableList.add(new ListEntryObject(input.getKey(), makeListEntryTitle(input.getKey()), getSpannableText(input.getValue()))));
        Collections.sort(showableList, Collections.reverseOrder());
        return showableList;
    }

    //formats title: <Name> am <Date>
    @RequiresApi(api = Build.VERSION_CODES.O)
    private String makeListEntryTitle(String fileName){
        OurUtils utils = new OurUtils();
        String infoString = "Unbekannte Herkunft";
        //file from whatsApp?
        if (fileName.startsWith("PTT-")) {
            String splittedDate = utils.getSplittedDate(fileName);
            String nameOfSpeaker ="";
            String[] parts = fileName.split("_");
            if (parts.length > 1){
                nameOfSpeaker = parts[parts.length-1].concat(" ");
            }
            infoString = "".concat(nameOfSpeaker).concat("am ").concat(splittedDate);
        }
        return  infoString;
    }

    //extracts and applies info on spans at end of string
    @RequiresApi(api = Build.VERSION_CODES.N)
    private SpannableString getSpannableText(String htmlValue){
        CharSequence charSequence = Html.fromHtml(htmlValue, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
        SpannableStringBuilder spStrB = new SpannableStringBuilder(charSequence);
        //remove trailing newline chars
        if(spStrB.charAt(spStrB.length()-2) == '\n' && spStrB.charAt(spStrB.length()-1) == '\n'){
            spStrB.replace(spStrB.length() -2, spStrB.length(), "");
        }
        return SpannableString.valueOf(spStrB);
    }

    private class CustomAdapter extends ArrayAdapter<ListEntryObject> {
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
        public ListEntryObject getItem(int i) {
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
            return result;
        }

        //switches between open and closed mode
        private SpannableString defineEntryText(ListEntryObject entry){
            if(entry.isOpenend() || entry.getText().length() <= 51) {
                return entry.getText();
            } else {
                SpannableStringBuilder builder = new SpannableStringBuilder();
                builder.append(entry.getText().subSequence(0, 50)).append("...");
                return SpannableString.valueOf(builder);
            }
        }


    }
}