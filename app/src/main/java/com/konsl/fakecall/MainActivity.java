package com.konsl.fakecall;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.picker.widget.SeslNumberPicker;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.konsl.fakecall.call.utils.Interval;
import com.konsl.fakecall.call.utils.StartCallReceiver;
import com.konsl.fakecall.databinding.ActivityMainBinding;
import com.konsl.fakecall.history.AppDatabase;
import com.konsl.fakecall.history.HistoryEntry;
import com.konsl.fakecall.settings.SettingsActivity;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_READ_CONTACTS_REQUEST_CODE = 1000001;

    ActivityMainBinding binding;
    Menu optionsMenu;
    IndexAdapter adapter;

    LiveData<List<HistoryEntry>> entriesLifeData;

    ActivityResultLauncher<Intent> settingsActivityCallback;
    ActivityResultLauncher<Intent> contactsPickerCallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        settingsActivityCallback = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> initLiveData());

        contactsPickerCallback = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent resultIntent = result.getData();
                    if (resultIntent == null)
                        return;

                    Uri contactUri = resultIntent.getData();

                    Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
                    cursor.moveToFirst();

                    int columnNumber = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    if (columnNumber < 0)
                        return;

                    showTimerDialog(cursor.getString(columnNumber));

                    cursor.close();
                });

        adapter = new IndexAdapter();

        binding.historyList.setLayoutManager(new LinearLayoutManager(this));
        binding.historyList.setAdapter(adapter);
        binding.historyList.addItemDecoration(new ItemDecoration(this));
        binding.historyList.setItemAnimator(null);
        binding.historyList.seslSetFillBottomEnabled(true);
        binding.historyList.seslSetLastRoundedCorner(true);
        binding.historyList.seslSetFastScrollerEnabled(true);
        binding.historyList.seslSetGoToTopEnabled(true);

        initLiveData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
            menu.findItem(R.id.menu_main_enable_pictures).setVisible(false);

        optionsMenu = menu;
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.menu_main_enable_pictures) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSION_READ_CONTACTS_REQUEST_CODE);
        } else if (itemId == R.id.menu_main_clear_history) {
            AppDatabase.getDatabase(this).historyDao().clear();
        } else if (itemId == R.id.menu_main_help) {
            showHelp();
        } else if (itemId == R.id.menu_main_settings) {
            settingsActivityCallback.launch(new Intent(this, SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_READ_CONTACTS_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            optionsMenu.findItem(R.id.menu_main_enable_pictures).setVisible(false);
            adapter.notifyItemRangeChanged(0, adapter.getItemCount());
        }
        if (requestCode == PERMISSION_READ_CONTACTS_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.message_read_contacts_failed);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.settings, (btn, which) -> {
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.show();
        }
    }

    private void showTimerDialog(String phoneNumber) {
        SeslNumberPicker timerPicker = new SeslNumberPicker(this);
        timerPicker.setMinValue(0);
        timerPicker.setMaxValue(12);
        timerPicker.setWrapSelectorWheel(false);
        timerPicker.setDisplayedValues(getResources().getStringArray(R.array.timer_lengths));
        timerPicker.getEditText().setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE)
                timerPicker.setEditTextMode(false);
            return false;
        });

        new AlertDialog.Builder(this)
                .setTitle(R.string.timer)
                .setView(timerPicker)
                .setPositiveButton(android.R.string.ok, (btn, which) ->
                        startCall(phoneNumber, Interval.getInterval(timerPicker.getValue())))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void initLiveData() {
        if (entriesLifeData != null)
            entriesLifeData.removeObservers(this);

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("history_show_duplicates", false))
            entriesLifeData = AppDatabase.getDatabase(this)
                    .historyDao().getAllLive();
        else
            entriesLifeData = AppDatabase.getDatabase(this)
                    .historyDao().getAllLiveWithoutDuplicates();

        entriesLifeData.observe(this, this::submitCallHistory);
    }

    private void submitCallHistory(List<HistoryEntry> history) {
        ArrayList<HistoryEntry> viewList = new ArrayList<>(history);

        HistoryEntry entryCallContact = new HistoryEntry();
        entryCallContact.id = -1;
        entryCallContact.phoneNumber = "call_contact";

        HistoryEntry entryCallNumber = new HistoryEntry();
        entryCallNumber.id = -1;
        entryCallNumber.phoneNumber = "call_number";

        viewList.add(0, entryCallContact);
        viewList.add(1, entryCallNumber);

        adapter.submitList(viewList);
    }

    private void startCall(String phoneNumber, Duration delay) {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("is_first_run", true)) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.putBoolean("is_first_run", false);
            editor.apply();

            showHelp();
        }

        AlarmManager alarmManager = getSystemService(AlarmManager.class);

        Intent i = new Intent(this, StartCallReceiver.class);
        i.putExtra(StartCallReceiver.INPUT_PHONE_NUMBER, phoneNumber);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_IMMUTABLE);

        long endTimeMillis = Calendar.getInstance().getTimeInMillis() + delay.toMillis();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms())
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endTimeMillis, pendingIntent);
        else
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endTimeMillis, pendingIntent);
    }

    public Uri getPhotoUri(long contactId) {
        try {
            Cursor cur = getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    null,
                    ContactsContract.Data.CONTACT_ID + "=" + contactId + " AND "
                            + ContactsContract.Data.MIMETYPE + "='"
                            + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'", null,
                    null);
            if (cur != null) {
                if (!cur.moveToFirst()) {
                    cur.close();
                    return null;
                }
                cur.close();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        return Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
    }

    private void showHelp() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.title_phone_account_disabled)
                .setMessage(R.string.message_phone_account_disabled)
                .show();
    }

    public class IndexAdapter extends ListAdapter<HistoryEntry, IndexAdapter.ViewHolder> {

        IndexAdapter() {
            super(new DiffUtil.ItemCallback<HistoryEntry>() {
                @Override
                public boolean areItemsTheSame(@NonNull HistoryEntry oldItem, @NonNull HistoryEntry newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull HistoryEntry oldItem, @NonNull HistoryEntry newItem) {
                    return oldItem.equals(newItem);
                }
            });
        }

        @NonNull
        @Override
        public IndexAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            View view = inflater.inflate(R.layout.history_entry, parent, false);

            return new IndexAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull IndexAdapter.ViewHolder holder, final int position) {
            HistoryEntry historyEntry = getItem(position);

            if (historyEntry.id == -1) {
                if (historyEntry.phoneNumber.equals("call_contact")) {
                    holder.imageView.setImageResource(dev.oneuiproject.oneui.R.drawable.ic_oui_phone_call_someone);
                    holder.imageView.setScaleType(ImageView.ScaleType.CENTER);
                    holder.textViewTop.setText(R.string.pick_contact);
                    holder.textViewBottom.setVisibility(View.GONE);

                    holder.mainView.setOnClickListener(v -> contactsPickerCallback.launch(
                            new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)));

                    return;
                }

                if (historyEntry.phoneNumber.equals("call_number")) {
                    holder.imageView.setImageResource(dev.oneuiproject.oneui.R.drawable.ic_oui_dialing_keyboard);
                    holder.imageView.setScaleType(ImageView.ScaleType.CENTER);
                    holder.textViewTop.setText(R.string.input_phone_number);
                    holder.textViewBottom.setVisibility(View.GONE);

                    holder.mainView.setOnClickListener(v -> {
                        EditText editText = new EditText(MainActivity.this);

                        editText.setSingleLine();
                        editText.setTextSize(35f);

                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);

                        layoutParams.topMargin = getResources().getDimensionPixelSize(R.dimen.margin_dialog_edittext);
                        layoutParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.margin_dialog_edittext);

                        editText.setLayoutParams(layoutParams);

                        editText.setBackgroundResource(android.R.color.transparent);
                        editText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                        editText.setInputType(InputType.TYPE_CLASS_PHONE);
                        editText.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

                        FrameLayout container = new FrameLayout(MainActivity.this);
                        container.addView(editText);

                        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                                .setTitle(R.string.input_phone_number)
                                .setView(container)
                                .setPositiveButton(android.R.string.ok, (btn, which) ->
                                        showTimerDialog(editText.getText().toString()))
                                .setNegativeButton(android.R.string.cancel, null)
                                .create();

                        editText.requestFocus();
                        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

                        dialog.show();
                    });

                    return;
                }
            }

            holder.imageView.setScaleType(ImageView.ScaleType.FIT_XY);

            if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED) {
                holder.imageView.setImageResource(R.drawable.samsung_contact);
                holder.textViewTop.setText(historyEntry.phoneNumber);
                holder.textViewBottom.setVisibility(View.GONE);

                return;
            }

            Uri filterUri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, Uri.encode(historyEntry.phoneNumber));
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
            Cursor cur = getContentResolver().query(filterUri, projection, null, null, null);

            if (!cur.moveToFirst()) {
                holder.imageView.setImageResource(R.drawable.samsung_contact);
                holder.textViewTop.setText(historyEntry.phoneNumber);
                holder.textViewBottom.setVisibility(View.GONE);
            } else {
                int columnId = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
                int columnName = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

                Uri photoUri = getPhotoUri(cur.getInt(columnId));

                if (photoUri != null)
                    holder.imageView.setImageURI(photoUri);
                else
                    holder.imageView.setImageResource(R.drawable.samsung_contact);

                holder.textViewTop.setText(cur.getString(columnName));
                holder.textViewBottom.setVisibility(View.VISIBLE);
                holder.textViewBottom.setText(historyEntry.phoneNumber);
            }

            cur.close();

            holder.mainView.setOnClickListener(v -> showTimerDialog(historyEntry.phoneNumber));
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView textViewTop;
            TextView textViewBottom;

            View mainView;

            ViewHolder(View itemView) {
                super(itemView);
                mainView = itemView;

                imageView = itemView.findViewById(R.id.history_contact_icon);
                textViewTop = itemView.findViewById(R.id.history_text_top);
                textViewBottom = itemView.findViewById(R.id.history_text_bottom);
            }
        }
    }

    private static class ItemDecoration extends RecyclerView.ItemDecoration {
        private final Drawable mDivider;

        public ItemDecoration(@NonNull Context context) {
            TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(androidx.appcompat.R.attr.isLightTheme, outValue, true);

            mDivider = context.getDrawable(outValue.data == 0
                    ? androidx.appcompat.R.drawable.sesl_list_divider_dark
                    : androidx.appcompat.R.drawable.sesl_list_divider_light);
        }

        public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent,
                           @NonNull RecyclerView.State state) {
            super.onDraw(c, parent, state);

            for (int i = 0; i < parent.getChildCount(); i++) {
                View child = parent.getChildAt(i);
                final int top = child.getBottom()
                        + ((ViewGroup.MarginLayoutParams) child.getLayoutParams()).bottomMargin;
                final int bottom = mDivider.getIntrinsicHeight() + top;

                mDivider.setBounds(parent.getLeft(), top, parent.getRight(), bottom);
                mDivider.draw(c);
            }
        }
    }
}
