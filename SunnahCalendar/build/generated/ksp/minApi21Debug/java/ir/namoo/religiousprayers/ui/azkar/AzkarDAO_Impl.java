package ir.namoo.religiousprayers.ui.azkar;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@SuppressWarnings({"unchecked", "deprecation"})
public final class AzkarDAO_Impl implements AzkarDAO {
  private final RoomDatabase __db;

  private final EntityDeletionOrUpdateAdapter<AzkarChapter> __updateAdapterOfAzkarChapter;

  public AzkarDAO_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__updateAdapterOfAzkarChapter = new EntityDeletionOrUpdateAdapter<AzkarChapter>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE OR ABORT `azkar_chapters` SET `id` = ?,`category_id` = ?,`ckb` = ?,`ar` = ?,`fa` = ?,`en` = ?,`fav` = ? WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, AzkarChapter value) {
        stmt.bindLong(1, value.getId());
        stmt.bindLong(2, value.getCategoryID());
        if (value.getKurdish() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getKurdish());
        }
        if (value.getArabic() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getArabic());
        }
        if (value.getPersian() == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, value.getPersian());
        }
        if (value.getEnglish() == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, value.getEnglish());
        }
        stmt.bindLong(7, value.getFav());
        stmt.bindLong(8, value.getId());
      }
    };
  }

  @Override
  public Object updateAzkarChapter(final AzkarChapter azkarChapter,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfAzkarChapter.handle(azkarChapter);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object getAzkarCategories(final Continuation<? super List<AzkarCategory>> continuation) {
    final String _sql = "select * from azkar_categories";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AzkarCategory>>() {
      @Override
      public List<AzkarCategory> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfKurdish = CursorUtil.getColumnIndexOrThrow(_cursor, "ckb");
          final int _cursorIndexOfArabic = CursorUtil.getColumnIndexOrThrow(_cursor, "ar");
          final int _cursorIndexOfPersian = CursorUtil.getColumnIndexOrThrow(_cursor, "fa");
          final int _cursorIndexOfEnglish = CursorUtil.getColumnIndexOrThrow(_cursor, "en");
          final List<AzkarCategory> _result = new ArrayList<AzkarCategory>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final AzkarCategory _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpKurdish;
            if (_cursor.isNull(_cursorIndexOfKurdish)) {
              _tmpKurdish = null;
            } else {
              _tmpKurdish = _cursor.getString(_cursorIndexOfKurdish);
            }
            final String _tmpArabic;
            if (_cursor.isNull(_cursorIndexOfArabic)) {
              _tmpArabic = null;
            } else {
              _tmpArabic = _cursor.getString(_cursorIndexOfArabic);
            }
            final String _tmpPersian;
            if (_cursor.isNull(_cursorIndexOfPersian)) {
              _tmpPersian = null;
            } else {
              _tmpPersian = _cursor.getString(_cursorIndexOfPersian);
            }
            final String _tmpEnglish;
            if (_cursor.isNull(_cursorIndexOfEnglish)) {
              _tmpEnglish = null;
            } else {
              _tmpEnglish = _cursor.getString(_cursorIndexOfEnglish);
            }
            _item = new AzkarCategory(_tmpId,_tmpKurdish,_tmpArabic,_tmpPersian,_tmpEnglish);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, continuation);
  }

  @Override
  public Object getAzkarChapters(final Continuation<? super List<AzkarChapter>> continuation) {
    final String _sql = "select * from azkar_chapters";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AzkarChapter>>() {
      @Override
      public List<AzkarChapter> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCategoryID = CursorUtil.getColumnIndexOrThrow(_cursor, "category_id");
          final int _cursorIndexOfKurdish = CursorUtil.getColumnIndexOrThrow(_cursor, "ckb");
          final int _cursorIndexOfArabic = CursorUtil.getColumnIndexOrThrow(_cursor, "ar");
          final int _cursorIndexOfPersian = CursorUtil.getColumnIndexOrThrow(_cursor, "fa");
          final int _cursorIndexOfEnglish = CursorUtil.getColumnIndexOrThrow(_cursor, "en");
          final int _cursorIndexOfFav = CursorUtil.getColumnIndexOrThrow(_cursor, "fav");
          final List<AzkarChapter> _result = new ArrayList<AzkarChapter>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final AzkarChapter _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpCategoryID;
            _tmpCategoryID = _cursor.getInt(_cursorIndexOfCategoryID);
            final String _tmpKurdish;
            if (_cursor.isNull(_cursorIndexOfKurdish)) {
              _tmpKurdish = null;
            } else {
              _tmpKurdish = _cursor.getString(_cursorIndexOfKurdish);
            }
            final String _tmpArabic;
            if (_cursor.isNull(_cursorIndexOfArabic)) {
              _tmpArabic = null;
            } else {
              _tmpArabic = _cursor.getString(_cursorIndexOfArabic);
            }
            final String _tmpPersian;
            if (_cursor.isNull(_cursorIndexOfPersian)) {
              _tmpPersian = null;
            } else {
              _tmpPersian = _cursor.getString(_cursorIndexOfPersian);
            }
            final String _tmpEnglish;
            if (_cursor.isNull(_cursorIndexOfEnglish)) {
              _tmpEnglish = null;
            } else {
              _tmpEnglish = _cursor.getString(_cursorIndexOfEnglish);
            }
            final int _tmpFav;
            _tmpFav = _cursor.getInt(_cursorIndexOfFav);
            _item = new AzkarChapter(_tmpId,_tmpCategoryID,_tmpKurdish,_tmpArabic,_tmpPersian,_tmpEnglish,_tmpFav);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, continuation);
  }

  @Override
  public Object getAzkarChapter(final int chapterID,
      final Continuation<? super AzkarChapter> continuation) {
    final String _sql = "select * from azkar_chapters where id=?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, chapterID);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<AzkarChapter>() {
      @Override
      public AzkarChapter call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCategoryID = CursorUtil.getColumnIndexOrThrow(_cursor, "category_id");
          final int _cursorIndexOfKurdish = CursorUtil.getColumnIndexOrThrow(_cursor, "ckb");
          final int _cursorIndexOfArabic = CursorUtil.getColumnIndexOrThrow(_cursor, "ar");
          final int _cursorIndexOfPersian = CursorUtil.getColumnIndexOrThrow(_cursor, "fa");
          final int _cursorIndexOfEnglish = CursorUtil.getColumnIndexOrThrow(_cursor, "en");
          final int _cursorIndexOfFav = CursorUtil.getColumnIndexOrThrow(_cursor, "fav");
          final AzkarChapter _result;
          if(_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpCategoryID;
            _tmpCategoryID = _cursor.getInt(_cursorIndexOfCategoryID);
            final String _tmpKurdish;
            if (_cursor.isNull(_cursorIndexOfKurdish)) {
              _tmpKurdish = null;
            } else {
              _tmpKurdish = _cursor.getString(_cursorIndexOfKurdish);
            }
            final String _tmpArabic;
            if (_cursor.isNull(_cursorIndexOfArabic)) {
              _tmpArabic = null;
            } else {
              _tmpArabic = _cursor.getString(_cursorIndexOfArabic);
            }
            final String _tmpPersian;
            if (_cursor.isNull(_cursorIndexOfPersian)) {
              _tmpPersian = null;
            } else {
              _tmpPersian = _cursor.getString(_cursorIndexOfPersian);
            }
            final String _tmpEnglish;
            if (_cursor.isNull(_cursorIndexOfEnglish)) {
              _tmpEnglish = null;
            } else {
              _tmpEnglish = _cursor.getString(_cursorIndexOfEnglish);
            }
            final int _tmpFav;
            _tmpFav = _cursor.getInt(_cursorIndexOfFav);
            _result = new AzkarChapter(_tmpId,_tmpCategoryID,_tmpKurdish,_tmpArabic,_tmpPersian,_tmpEnglish,_tmpFav);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, continuation);
  }

  @Override
  public Object getAzkarItems(final int chapterID,
      final Continuation<? super List<AzkarItem>> continuation) {
    final String _sql = "select * from azkar_items where chapter_id=?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, chapterID);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AzkarItem>>() {
      @Override
      public List<AzkarItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfChapterID = CursorUtil.getColumnIndexOrThrow(_cursor, "chapter_id");
          final int _cursorIndexOfKurdish = CursorUtil.getColumnIndexOrThrow(_cursor, "ckb");
          final int _cursorIndexOfArabic = CursorUtil.getColumnIndexOrThrow(_cursor, "ar");
          final int _cursorIndexOfPersian = CursorUtil.getColumnIndexOrThrow(_cursor, "fa");
          final int _cursorIndexOfEnglish = CursorUtil.getColumnIndexOrThrow(_cursor, "en");
          final int _cursorIndexOfSound = CursorUtil.getColumnIndexOrThrow(_cursor, "sound");
          final List<AzkarItem> _result = new ArrayList<AzkarItem>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final AzkarItem _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpChapterID;
            _tmpChapterID = _cursor.getInt(_cursorIndexOfChapterID);
            final String _tmpKurdish;
            if (_cursor.isNull(_cursorIndexOfKurdish)) {
              _tmpKurdish = null;
            } else {
              _tmpKurdish = _cursor.getString(_cursorIndexOfKurdish);
            }
            final String _tmpArabic;
            if (_cursor.isNull(_cursorIndexOfArabic)) {
              _tmpArabic = null;
            } else {
              _tmpArabic = _cursor.getString(_cursorIndexOfArabic);
            }
            final String _tmpPersian;
            if (_cursor.isNull(_cursorIndexOfPersian)) {
              _tmpPersian = null;
            } else {
              _tmpPersian = _cursor.getString(_cursorIndexOfPersian);
            }
            final String _tmpEnglish;
            if (_cursor.isNull(_cursorIndexOfEnglish)) {
              _tmpEnglish = null;
            } else {
              _tmpEnglish = _cursor.getString(_cursorIndexOfEnglish);
            }
            final String _tmpSound;
            if (_cursor.isNull(_cursorIndexOfSound)) {
              _tmpSound = null;
            } else {
              _tmpSound = _cursor.getString(_cursorIndexOfSound);
            }
            _item = new AzkarItem(_tmpId,_tmpChapterID,_tmpKurdish,_tmpArabic,_tmpPersian,_tmpEnglish,_tmpSound);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, continuation);
  }

  @Override
  public Object getAzkarReferences(final int chapterID,
      final Continuation<? super List<AzkarReference>> continuation) {
    final String _sql = "select * from azkar_references where chapter_id=?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, chapterID);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AzkarReference>>() {
      @Override
      public List<AzkarReference> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfChapterID = CursorUtil.getColumnIndexOrThrow(_cursor, "chapter_id");
          final int _cursorIndexOfKurdish = CursorUtil.getColumnIndexOrThrow(_cursor, "ckb");
          final int _cursorIndexOfArabic = CursorUtil.getColumnIndexOrThrow(_cursor, "ar");
          final int _cursorIndexOfPersian = CursorUtil.getColumnIndexOrThrow(_cursor, "fa");
          final int _cursorIndexOfEnglish = CursorUtil.getColumnIndexOrThrow(_cursor, "en");
          final List<AzkarReference> _result = new ArrayList<AzkarReference>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final AzkarReference _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpChapterID;
            _tmpChapterID = _cursor.getInt(_cursorIndexOfChapterID);
            final String _tmpKurdish;
            if (_cursor.isNull(_cursorIndexOfKurdish)) {
              _tmpKurdish = null;
            } else {
              _tmpKurdish = _cursor.getString(_cursorIndexOfKurdish);
            }
            final String _tmpArabic;
            if (_cursor.isNull(_cursorIndexOfArabic)) {
              _tmpArabic = null;
            } else {
              _tmpArabic = _cursor.getString(_cursorIndexOfArabic);
            }
            final String _tmpPersian;
            if (_cursor.isNull(_cursorIndexOfPersian)) {
              _tmpPersian = null;
            } else {
              _tmpPersian = _cursor.getString(_cursorIndexOfPersian);
            }
            final String _tmpEnglish;
            if (_cursor.isNull(_cursorIndexOfEnglish)) {
              _tmpEnglish = null;
            } else {
              _tmpEnglish = _cursor.getString(_cursorIndexOfEnglish);
            }
            _item = new AzkarReference(_tmpId,_tmpChapterID,_tmpKurdish,_tmpArabic,_tmpPersian,_tmpEnglish);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, continuation);
  }

  @Override
  public Object getTasbihList(final Continuation<? super List<Tasbih>> continuation) {
    final String _sql = "select * from tasbih";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Tasbih>>() {
      @Override
      public List<Tasbih> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfZikr = CursorUtil.getColumnIndexOrThrow(_cursor, "zikr");
          final int _cursorIndexOfCount = CursorUtil.getColumnIndexOrThrow(_cursor, "count");
          final int _cursorIndexOfTotalCount = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
          final List<Tasbih> _result = new ArrayList<Tasbih>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final Tasbih _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpZikr;
            if (_cursor.isNull(_cursorIndexOfZikr)) {
              _tmpZikr = null;
            } else {
              _tmpZikr = _cursor.getString(_cursorIndexOfZikr);
            }
            final int _tmpCount;
            _tmpCount = _cursor.getInt(_cursorIndexOfCount);
            final int _tmpTotalCount;
            _tmpTotalCount = _cursor.getInt(_cursorIndexOfTotalCount);
            _item = new Tasbih(_tmpId,_tmpZikr,_tmpCount,_tmpTotalCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, continuation);
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
