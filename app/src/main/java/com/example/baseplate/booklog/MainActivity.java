package com.example.baseplate.booklog;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.strictmode.SqliteObjectLeakedViolation;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.baseplate.booklog.data.DbHelper;

import com.example.baseplate.booklog.data.ContractHelper;
import com.example.baseplate.booklog.data.ContractHelper.BookEntry;

public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    private DbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        mDbHelper = new DbHelper(this);
    }

    private void displayDatabaseInfo() {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_NAME,
                BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_BOOK_QUANTITY,
                BookEntry.COLUMN_BOOK_SUPPLIER_NAME,
                BookEntry.COLUMN_BOOK_SUPPLIER_CONT_NO
        };

        Cursor cursor = database.query(
                BookEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        TextView textView = (TextView) findViewById(R.id.text_view_book);

        try {
            textView.setText("Database contains " + cursor.getCount() + "books \n");
            textView.append(BookEntry._ID + "\t" +
                    BookEntry.COLUMN_BOOK_NAME + "\t" +
                    BookEntry.COLUMN_BOOK_PRICE + "\t" +
                    BookEntry.COLUMN_BOOK_QUANTITY + "\t" +
                    BookEntry.COLUMN_BOOK_SUPPLIER_NAME + "\t" +
                    BookEntry.COLUMN_BOOK_SUPPLIER_CONT_NO + "\n");

            int idColumnIndex = cursor.getColumnIndex(BookEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
            int supplerColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_NAME);
            int supplierContactColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_CONT_NO);

            while (cursor.moveToNext()){
                int currentID = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                int currentPrice = cursor.getInt(priceColumnIndex);
                int currentQuantity = cursor.getInt(quantityColumnIndex);
                String currentSupplier = cursor.getString(supplerColumnIndex);
                int currentSuppierPhone = cursor.getInt(supplierContactColumnIndex);
                textView.append("\n" + currentID + "\t" +
                        currentName + "\t" +
                        currentPrice + "\t" +
                        currentQuantity + "\t" +
                        currentSupplier + "\t" +
                        currentSuppierPhone + "\n");
            }
        } finally {
            cursor.close();
        }
    }

    private void insertData(){
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(BookEntry.COLUMN_BOOK_NAME, "Sample Name");
        contentValues.put(BookEntry.COLUMN_BOOK_PRICE, 100);
        contentValues.put(BookEntry.COLUMN_BOOK_QUANTITY, 5);
        contentValues.put(BookEntry.COLUMN_BOOK_SUPPLIER_NAME, "Sample Supplier");
        contentValues.put(BookEntry.COLUMN_BOOK_SUPPLIER_CONT_NO, 123456789);

        long rowID = database.insert(BookEntry.TABLE_NAME, null, contentValues);
        if (rowID == 1) {
            Log.e(LOG_TAG, "Test Data inserted. Row ID =" + rowID);
        } else {
            Log.e(LOG_TAG, "Data insertion error. Row id = " + rowID);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_test_data) {
            insertData();
            displayDatabaseInfo();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
