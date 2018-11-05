package com.example.baseplate.booklog;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.baseplate.booklog.data.ContractHelper.BookEntry;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private Uri mCurrentBookUri;
    private static final int BOOK_LOADER = 0;
    private TextView mDetailsBookName;
    private TextView mDetailsBookPrice;
    private TextView mDetailsBookQuantity;
    private TextView mDetailsBookSupplier;
    private TextView mDetailsBookSupplierContact;
    private Button mDetailsIncrementButton;
    private Button mDetailsDecrementButton;
    private Button mDetailsCallButton;

    private int changeBookValueBy = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        mDetailsBookName = (TextView) findViewById(R.id.details_name_field);
        mDetailsBookPrice = (TextView) findViewById(R.id.details_price_field);
        mDetailsBookQuantity = (TextView) findViewById(R.id.details_quantity_field);
        mDetailsBookSupplier = (TextView) findViewById(R.id.details_supplier_name_field);
        mDetailsBookSupplierContact = (TextView) findViewById(R.id.details_supplier_phone);
        mDetailsIncrementButton = (Button) findViewById(R.id.increment_button);
        mDetailsDecrementButton = (Button) findViewById(R.id.decrement_button);
        mDetailsCallButton = (Button) findViewById(R.id.details_call_button);

        getSupportLoaderManager().initLoader(BOOK_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.edit_book:
                Intent intent = new Intent(DetailsActivity.this, EditorActivity.class);
                intent.setData(mCurrentBookUri);
                startActivity(intent);
                finish();
                return true;

            case R.id.delete_book:
                showDeleteConfirmationDialog();
                return true;

            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteCurrentBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
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

    private void deleteCurrentBook(){
        if(mCurrentBookUri != null){
            int deletedRows = getContentResolver().delete(mCurrentBookUri, null, null);
            if(deletedRows == 0){
                Toast.makeText(this, getString(R.string.delete_book_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.delete_book_successful), Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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
        if(cursor == null || cursor.getCount()< 1){
            return;
        }
        if(cursor.moveToFirst()){
            int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_NAME);
            int supplierContactColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_CONT_NO);

            final String name = cursor.getString(nameColumnIndex);
            Float price = cursor.getFloat(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplierName = cursor.getString(supplierColumnIndex);
            final String supplierContact = cursor.getString(supplierContactColumnIndex);

            mDetailsBookName.setText(name);
            mDetailsBookPrice.setText(String.valueOf(price));
            mDetailsBookQuantity.setText(String.valueOf(quantity));
            mDetailsBookSupplier.setText(supplierName);
            mDetailsBookSupplierContact.setText(supplierContact);

            mDetailsIncrementButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String oldBookQuantity = mDetailsBookQuantity.getText().toString();
                    int newBookQuantity = Integer.valueOf(oldBookQuantity) + changeBookValueBy;
                    if(newBookQuantity>=0){
                        ContentValues values = new ContentValues();
                        values.put(BookEntry.COLUMN_BOOK_QUANTITY, newBookQuantity);
                        getContentResolver().update(mCurrentBookUri, values,null, null);
                        mDetailsBookQuantity.setText(String.valueOf(newBookQuantity));
                    }
                }
            });

            mDetailsDecrementButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String oldBookQuantity = mDetailsBookQuantity.getText().toString();
                    int newBookQuantity = Integer.valueOf(oldBookQuantity) - changeBookValueBy;
                    if(newBookQuantity>=0){
                        ContentValues values = new ContentValues();
                        values.put(BookEntry.COLUMN_BOOK_QUANTITY, newBookQuantity);
                        getContentResolver().update(mCurrentBookUri, values, null, null);
                        mDetailsBookQuantity.setText(String.valueOf(newBookQuantity));
                    }
                }
            });

            mDetailsCallButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String phoneUri = "tel: " + supplierContact;
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse(phoneUri));
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mDetailsBookName.setText("");
        mDetailsBookPrice.setText("");
        mDetailsBookQuantity.setText("");
        mDetailsBookSupplier.setText("");
        mDetailsBookSupplierContact.setText("");
    }
}
