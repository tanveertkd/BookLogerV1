package com.example.baseplate.booklog;

import android.app.AlertDialog;
//import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
//import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
//import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.renderscript.Script;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.example.baseplate.booklog.data.ContractHelper;
import com.example.baseplate.booklog.data.ContractHelper.BookEntry;

import java.net.URI;
import java.util.Currency;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int EXISTING_BOOK_LOADER = 0;

    private Uri mCurrentBookUri;
    private EditText mNameEditField;
    private EditText mPriceEditField;
    private EditText mQuantityEditField;
    private EditText mSupplierEditFleld;
    private EditText mSupplierContactEditField;

    private boolean mBookChanged = false;

    private View.OnTouchListener mTouchListner = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        if(mCurrentBookUri == null){
            setTitle(getString(R.string.add_book));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.edit_book));
            getSupportLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }

        mNameEditField = (EditText) findViewById(R.id.name_field);
        mPriceEditField = (EditText) findViewById(R.id.price_field);
        mQuantityEditField = (EditText) findViewById(R.id.quantity_field);
        mSupplierEditFleld = (EditText) findViewById(R.id.supplier_name_field);
        mSupplierContactEditField = (EditText) findViewById(R.id.supplier_phone);

        mNameEditField.setOnTouchListener(mTouchListner);
        mPriceEditField.setOnTouchListener(mTouchListner);
        mQuantityEditField.setOnTouchListener(mTouchListner);
        mSupplierEditFleld.setOnTouchListener(mTouchListner);
        mSupplierContactEditField.setOnTouchListener(mTouchListner);
    }

    private void saveBookData(){
        String bookNameString = mNameEditField.getText().toString().trim();
        String bookPriceString = mPriceEditField.getText().toString().trim();
        String bookQuantityString = mQuantityEditField.getText().toString().trim();
        String bookSupplierString = mSupplierEditFleld.getText().toString().trim();
        String bookSupplierContactString = mSupplierContactEditField.getText().toString().trim();

        if(mCurrentBookUri == null
                && TextUtils.isEmpty(bookNameString)
                && TextUtils.isEmpty(bookPriceString)
                && TextUtils.isEmpty(bookQuantityString)
                && TextUtils.isEmpty(bookSupplierString)
                && TextUtils.isEmpty(bookSupplierContactString)){
            Toast.makeText(this, getString(R.string.fill_all), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if(TextUtils.isEmpty(bookNameString)
                || TextUtils.isEmpty(bookPriceString)
                || TextUtils.isEmpty(bookQuantityString)
                || TextUtils.isEmpty(bookSupplierString)
                || TextUtils.isEmpty(bookSupplierContactString)){
            Toast.makeText(this, getString(R.string.fill_all), Toast.LENGTH_SHORT).show();
        } else {
            ContentValues values = new ContentValues();
            values.put(BookEntry.COLUMN_BOOK_NAME, bookNameString);
            values.put(BookEntry.COLUMN_BOOK_PRICE, bookPriceString);
            values.put(BookEntry.COLUMN_BOOK_QUANTITY, bookQuantityString);
            values.put(BookEntry.COLUMN_BOOK_SUPPLIER_NAME, bookSupplierString);
            values.put(BookEntry.COLUMN_BOOK_SUPPLIER_CONT_NO, bookSupplierContactString);

            if(mCurrentBookUri == null) {
                Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);
                if(newUri == null){
                    Toast.makeText(this, getString(R.string.insert_book_failed), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, getString(R.string.insert_book_successful), Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);
                if(rowsAffected == 0){
                    Toast.makeText(this, getString(R.string.update_failed), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, getString(R.string.update_successful), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(mCurrentBookUri == null){
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_save:
                saveBookData();
                finish();
                return true;

            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;

            case android.R.id.home:
                if(!mBookChanged){
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonOnClickListner = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                showUnsavedChangesDialog(discardButtonOnClickListner);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(!mBookChanged){
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(dialogInterface != null){
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(dialogInterface!=null){
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteBook(){
        if(mCurrentBookUri!=null){
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);
            if(rowsDeleted == 0){
                Toast.makeText(this, getString(R.string.delete_book_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.delete_book_successful), Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_NAME,
                BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_BOOK_QUANTITY,
                BookEntry.COLUMN_BOOK_SUPPLIER_NAME,
                BookEntry.COLUMN_BOOK_SUPPLIER_CONT_NO
        };
        return new CursorLoader(this,
                mCurrentBookUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if(cursor.moveToFirst()){
            int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_NAME);
            int supplierContactColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_CONT_NO);

            String name = cursor.getString(nameColumnIndex);
            Float price = cursor.getFloat(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplierName = cursor.getString(supplierColumnIndex);
            String supplierContact = cursor.getString(supplierContactColumnIndex);

            mNameEditField.setText(name);
            mPriceEditField.setText(String.valueOf(price));
            mQuantityEditField.setText(String.valueOf(quantity));
            mSupplierEditFleld.setText(supplierName);
            mSupplierContactEditField.setText(supplierContact);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditField.setText("");
        mPriceEditField.setText("");
        mQuantityEditField.setText("");
        mSupplierEditFleld.setText("");
        mSupplierContactEditField.setText("");
    }
}
