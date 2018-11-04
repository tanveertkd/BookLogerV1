package com.example.baseplate.booklog;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.baseplate.booklog.data.ContractHelper;
import com.example.baseplate.booklog.data.ContractHelper.BookEntry;

public class BookCursorAdapter extends CursorAdapter {
    private Context mContext;
    private static final String LOG_TAG = BookCursorAdapter.class.getSimpleName();
    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView nameView = (TextView) view.findViewById(R.id.book_name);
        TextView priceView = (TextView) view.findViewById(R.id.price);
        TextView quantityView = (TextView) view.findViewById(R.id.quantity);
        Button buttonView = (Button) view.findViewById(R.id.button);

        int idColumnIndex = cursor.getColumnIndex(BookEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
        int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);

        final int bookID = cursor.getInt(idColumnIndex);
        String bookName = cursor.getString(nameColumnIndex);
        int bookPrice = cursor.getInt(priceColumnIndex);
        final int bookQuantity = cursor.getInt(quantityColumnIndex);
        final int newQuantity = bookQuantity - 1;
        Log.e( LOG_TAG, "Current: " + bookQuantity + "new: " + newQuantity);

        nameView.setText(bookName);
        priceView.setText(String.valueOf(bookPrice));
        quantityView.setText(String.valueOf(bookQuantity));

        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newQuantity >= 0)
                {
                    final ContentValues values = new ContentValues();
                    values.put(BookEntry.COLUMN_BOOK_QUANTITY, newQuantity);
                    final Uri currentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, bookID);
                    int rowAffected = context.getContentResolver().update(currentBookUri, values, null, null);

                    if (rowAffected == 0)
                    {
                        Snackbar.make(view, R.string.not_sold, Snackbar.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Snackbar snackbar = Snackbar.make(view, R.string.sold, Snackbar.LENGTH_LONG).setAction(R.string.undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(BookEntry.COLUMN_BOOK_QUANTITY, bookQuantity);
                                Uri currentBook = ContentUris.withAppendedId(BookEntry.CONTENT_URI, bookID);
                                context.getContentResolver().update(currentBook, contentValues, null, null);
                            }
                        });
                        snackbar.show();
                    }
                }
                else
                {
                    Snackbar.make(view, R.string.out_of_stock, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }
}
