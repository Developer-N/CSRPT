package ir.namoo.commons.model;

import android.database.Cursor;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class AthanDAO_Impl implements AthanDAO {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Athan> __insertionAdapterOfAthan;

  private final EntityDeletionOrUpdateAdapter<Athan> __deletionAdapterOfAthan;

  private final EntityDeletionOrUpdateAdapter<Athan> __updateAdapterOfAthan;

  private final SharedSQLiteStatement __preparedStmtOfClearDB;

  public AthanDAO_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAthan = new EntityInsertionAdapter<Athan>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `athans` (`name`,`link`,`type`,`title`,`id`) VALUES (?,?,?,?,nullif(?, 0))";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Athan value) {
        if (value.getName() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getName());
        }
        if (value.getLink() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getLink());
        }
        stmt.bindLong(3, value.getType());
        if (value.getFileName() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getFileName());
        }
        stmt.bindLong(5, value.getId());
      }
    };
    this.__deletionAdapterOfAthan = new EntityDeletionOrUpdateAdapter<Athan>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `athans` WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Athan value) {
        stmt.bindLong(1, value.getId());
      }
    };
    this.__updateAdapterOfAthan = new EntityDeletionOrUpdateAdapter<Athan>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE OR ABORT `athans` SET `name` = ?,`link` = ?,`type` = ?,`title` = ?,`id` = ? WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Athan value) {
        if (value.getName() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getName());
        }
        if (value.getLink() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getLink());
        }
        stmt.bindLong(3, value.getType());
        if (value.getFileName() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getFileName());
        }
        stmt.bindLong(5, value.getId());
        stmt.bindLong(6, value.getId());
      }
    };
    this.__preparedStmtOfClearDB = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "delete from athans";
        return _query;
      }
    };
  }

  @Override
  public void insert(final Athan athan) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfAthan.insert(athan);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final Athan athan) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfAthan.handle(athan);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final Athan athan) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfAthan.handle(athan);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void clearDB() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfClearDB.acquire();
    __db.beginTransaction();
    try {
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
      __preparedStmtOfClearDB.release(_stmt);
    }
  }

  @Override
  public List<Athan> getAllAthans() {
    final String _sql = "select * from athans";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfLink = CursorUtil.getColumnIndexOrThrow(_cursor, "link");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final List<Athan> _result = new ArrayList<Athan>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final Athan _item;
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        final String _tmpLink;
        if (_cursor.isNull(_cursorIndexOfLink)) {
          _tmpLink = null;
        } else {
          _tmpLink = _cursor.getString(_cursorIndexOfLink);
        }
        final int _tmpType;
        _tmpType = _cursor.getInt(_cursorIndexOfType);
        final String _tmpFileName;
        if (_cursor.isNull(_cursorIndexOfFileName)) {
          _tmpFileName = null;
        } else {
          _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
        }
        _item = new Athan(_tmpName,_tmpLink,_tmpType,_tmpFileName);
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public Athan getAthan(final int id) {
    final String _sql = "select * from athans where id=?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfLink = CursorUtil.getColumnIndexOrThrow(_cursor, "link");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final Athan _result;
      if(_cursor.moveToFirst()) {
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        final String _tmpLink;
        if (_cursor.isNull(_cursorIndexOfLink)) {
          _tmpLink = null;
        } else {
          _tmpLink = _cursor.getString(_cursorIndexOfLink);
        }
        final int _tmpType;
        _tmpType = _cursor.getInt(_cursorIndexOfType);
        final String _tmpFileName;
        if (_cursor.isNull(_cursorIndexOfFileName)) {
          _tmpFileName = null;
        } else {
          _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
        }
        _result = new Athan(_tmpName,_tmpLink,_tmpType,_tmpFileName);
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _result.setId(_tmpId);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public Athan getAthan(final String name) {
    final String _sql = "select * from athans where name=?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (name == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, name);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfLink = CursorUtil.getColumnIndexOrThrow(_cursor, "link");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final Athan _result;
      if(_cursor.moveToFirst()) {
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        final String _tmpLink;
        if (_cursor.isNull(_cursorIndexOfLink)) {
          _tmpLink = null;
        } else {
          _tmpLink = _cursor.getString(_cursorIndexOfLink);
        }
        final int _tmpType;
        _tmpType = _cursor.getInt(_cursorIndexOfType);
        final String _tmpFileName;
        if (_cursor.isNull(_cursorIndexOfFileName)) {
          _tmpFileName = null;
        } else {
          _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
        }
        _result = new Athan(_tmpName,_tmpLink,_tmpType,_tmpFileName);
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _result.setId(_tmpId);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
