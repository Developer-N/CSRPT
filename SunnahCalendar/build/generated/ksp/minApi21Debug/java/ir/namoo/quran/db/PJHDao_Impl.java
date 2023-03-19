package ir.namoo.quran.db;

import android.database.Cursor;
import androidx.lifecycle.LiveData;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

@SuppressWarnings({"unchecked", "deprecation"})
public final class PJHDao_Impl implements PJHDao {
  private final RoomDatabase __db;

  public PJHDao_Impl(RoomDatabase __db) {
    this.__db = __db;
  }

  @Override
  public LiveData<List<PageEntity>> getAllPage(final int page) {
    final String _sql = "SELECT * FROM safhe where page=?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, page);
    return __db.getInvalidationTracker().createLiveData(new String[]{"safhe"}, false, new Callable<List<PageEntity>>() {
      @Override
      public List<PageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMetaDataID = CursorUtil.getColumnIndexOrThrow(_cursor, "MetaDataID");
          final int _cursorIndexOfSura = CursorUtil.getColumnIndexOrThrow(_cursor, "sura");
          final int _cursorIndexOfPage = CursorUtil.getColumnIndexOrThrow(_cursor, "page");
          final int _cursorIndexOfAya = CursorUtil.getColumnIndexOrThrow(_cursor, "aya");
          final List<PageEntity> _result = new ArrayList<PageEntity>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final PageEntity _item;
            final int _tmpMetaDataID;
            _tmpMetaDataID = _cursor.getInt(_cursorIndexOfMetaDataID);
            final Integer _tmpSura;
            if (_cursor.isNull(_cursorIndexOfSura)) {
              _tmpSura = null;
            } else {
              _tmpSura = _cursor.getInt(_cursorIndexOfSura);
            }
            final Integer _tmpPage;
            if (_cursor.isNull(_cursorIndexOfPage)) {
              _tmpPage = null;
            } else {
              _tmpPage = _cursor.getInt(_cursorIndexOfPage);
            }
            final Integer _tmpAya;
            if (_cursor.isNull(_cursorIndexOfAya)) {
              _tmpAya = null;
            } else {
              _tmpAya = _cursor.getInt(_cursorIndexOfAya);
            }
            _item = new PageEntity(_tmpMetaDataID,_tmpSura,_tmpPage,_tmpAya);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<JuzEntity>> getAllJuz() {
    final String _sql = "SELECT * FROM juz";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"juz"}, false, new Callable<List<JuzEntity>>() {
      @Override
      public List<JuzEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSura = CursorUtil.getColumnIndexOrThrow(_cursor, "sura");
          final int _cursorIndexOfAya = CursorUtil.getColumnIndexOrThrow(_cursor, "aya");
          final List<JuzEntity> _result = new ArrayList<JuzEntity>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final JuzEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final Integer _tmpSura;
            if (_cursor.isNull(_cursorIndexOfSura)) {
              _tmpSura = null;
            } else {
              _tmpSura = _cursor.getInt(_cursorIndexOfSura);
            }
            final Integer _tmpAya;
            if (_cursor.isNull(_cursorIndexOfAya)) {
              _tmpAya = null;
            } else {
              _tmpAya = _cursor.getInt(_cursorIndexOfAya);
            }
            _item = new JuzEntity(_tmpId,_tmpSura,_tmpAya);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<HizbEntity>> getAllHezb() {
    final String _sql = "SELECT * FROM hezb";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"hezb"}, false, new Callable<List<HizbEntity>>() {
      @Override
      public List<HizbEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfAya = CursorUtil.getColumnIndexOrThrow(_cursor, "aya");
          final int _cursorIndexOfSura = CursorUtil.getColumnIndexOrThrow(_cursor, "sura");
          final int _cursorIndexOfPage = CursorUtil.getColumnIndexOrThrow(_cursor, "Page");
          final int _cursorIndexOfHizb = CursorUtil.getColumnIndexOrThrow(_cursor, "hizb");
          final int _cursorIndexOfJozA = CursorUtil.getColumnIndexOrThrow(_cursor, "JozA");
          final List<HizbEntity> _result = new ArrayList<HizbEntity>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final HizbEntity _item;
            final Integer _tmpAya;
            if (_cursor.isNull(_cursorIndexOfAya)) {
              _tmpAya = null;
            } else {
              _tmpAya = _cursor.getInt(_cursorIndexOfAya);
            }
            final Integer _tmpSura;
            if (_cursor.isNull(_cursorIndexOfSura)) {
              _tmpSura = null;
            } else {
              _tmpSura = _cursor.getInt(_cursorIndexOfSura);
            }
            final Integer _tmpPage;
            if (_cursor.isNull(_cursorIndexOfPage)) {
              _tmpPage = null;
            } else {
              _tmpPage = _cursor.getInt(_cursorIndexOfPage);
            }
            final int _tmpHizb;
            _tmpHizb = _cursor.getInt(_cursorIndexOfHizb);
            final Integer _tmpJozA;
            if (_cursor.isNull(_cursorIndexOfJozA)) {
              _tmpJozA = null;
            } else {
              _tmpJozA = _cursor.getInt(_cursorIndexOfJozA);
            }
            _item = new HizbEntity(_tmpAya,_tmpSura,_tmpPage,_tmpHizb,_tmpJozA);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
