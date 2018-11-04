package com.example.baseplate.booklog.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.baseplate.booklog.data.ContractHelper;
import com.example.baseplate.booklog.data.ContractHelper.BookEntry;

public class BookProvider extends ContentProvider {
    public static final String LOG_TAG = BookProvider.class.getSimpleName();

    public static final int BOOKS = 100;
    public static final int BOOK_ID = 101;

    public static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(ContractHelper.CONTENT_AUTHORITY, ContractHelper.PATH_BOOKS, BOOKS);
        sUriMatcher.addURI(ContractHelper.CONTENT_AUTHORITY, ContractHelper.PATH_BOOKS + "/#", BOOK_ID);
    }

    private DbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new DbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match){
            case BOOKS:
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs,null, null, sortOrder);
                break;

            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Cannot query URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch(match){
            case BOOKS:
                return BookEntry.CONTENT_LIST_TYPE;

            case BOOK_ID:
                return BookEntry.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case BOOKS:
                return insertBook(uri, contentValues);

            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertBook(Uri uri, ContentValues contentValues){
        String name = contentValues.getAsString(BookEntry.COLUMN_BOOK_NAME);
        if (name == null){
            throw new IllegalArgumentException("The book must be named before entry");
        }

        Integer price = contentValues.getAsInteger(BookEntry.COLUMN_BOOK_PRICE);
        if (price == null || price < 0) {
            throw new IllegalArgumentException("The book needs to be priced");
        }

        Integer quantity = contentValues.getAsInteger(BookEntry.COLUMN_BOOK_QUANTITY);
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("There must be at least one book.");
        }

        String supplierName = contentValues.getAsString(BookEntry.COLUMN_BOOK_SUPPLIER_NAME);
        if (supplierName == null) {
            throw new IllegalArgumentException("Requires a supplier namee");
        }

        Integer supplierPhone = contentValues.getAsInteger(BookEntry.COLUMN_BOOK_SUPPLIER_CONT_NO);
        if (supplierPhone == null) {
            throw new IllegalArgumentException("Requires supplier phone number");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(BookEntry.TABLE_NAME, null, contentValues);
        if(id == -1){
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case BOOKS:
                return updateBookDb(uri, contentValues, selection, selectionArgs);
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                return updateBookDb(uri, contentValues, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Update unsupported at: " + uri);
        }
    }

    private int updateBookDb(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs){
        if(contentValues.containsKey(BookEntry.COLUMN_BOOK_NAME)){
            String name = contentValues.getAsString(BookEntry.COLUMN_BOOK_NAME);
            if(name == null){
                throw new IllegalArgumentException("Book name required.");
            }
        }

        if(contentValues.containsKey(BookEntry.COLUMN_BOOK_PRICE)){
            Float price = contentValues.getAsFloat(BookEntry.COLUMN_BOOK_PRICE);
            if (price == null || price < 0) {
                throw new IllegalArgumentException("The book needs to be priced");
            }
        }

        if(contentValues.containsKey(BookEntry.COLUMN_BOOK_QUANTITY)){
            Integer quantity = contentValues.getAsInteger(BookEntry.COLUMN_BOOK_QUANTITY);
            if (quantity == null || quantity < 0) {
                throw new IllegalArgumentException("There must be at least one book.");
            }
        }

        if(contentValues.containsKey(BookEntry.COLUMN_BOOK_SUPPLIER_NAME)){
            String supplierName = contentValues.getAsString(BookEntry.COLUMN_BOOK_SUPPLIER_NAME);
            if (supplierName == null) {
                throw new IllegalArgumentException("Requires a supplier namee");
            }
        }

        if(contentValues.containsKey(BookEntry.COLUMN_BOOK_SUPPLIER_CONT_NO)){
            Integer supplierPhone = contentValues.getAsInteger(BookEntry.COLUMN_BOOK_SUPPLIER_CONT_NO);
            if (supplierPhone == null) {
                throw new IllegalArgumentException("Requires supplier phone number");
            }
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsUpdated = database.update(BookEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        if(rowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);

        switch (match){
            case BOOKS:
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Deletion not supported for " + uri);
        }
        if(rowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }


}
