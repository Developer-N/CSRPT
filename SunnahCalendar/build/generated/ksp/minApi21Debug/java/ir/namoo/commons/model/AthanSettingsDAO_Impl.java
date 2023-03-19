package ir.namoo.commons.model;

import android.database.Cursor;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
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
public final class AthanSettingsDAO_Impl implements AthanSettingsDAO {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AthanSetting> __insertionAdapterOfAthanSetting;

  private final EntityDeletionOrUpdateAdapter<AthanSetting> __deletionAdapterOfAthanSetting;

  private final EntityDeletionOrUpdateAdapter<AthanSetting> __updateAdapterOfAthanSetting;

  public AthanSettingsDAO_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAthanSetting = new EntityInsertionAdapter<AthanSetting>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `athans_settings` (`athan_key`,`state`,`play_doa`,`play_type`,`is_before_enabled`,`before_minute`,`is_after_enabled`,`after_minute`,`is_silent_enabled`,`silent_minute`,`is_ascending`,`athan_volume`,`athan_uri`,`alert_uri`,`id`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,nullif(?, 0))";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, AthanSetting value) {
        if (value.getAthanKey() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getAthanKey());
        }
        final int _tmp = value.getState() ? 1 : 0;
        stmt.bindLong(2, _tmp);
        final int _tmp_1 = value.getPlayDoa() ? 1 : 0;
        stmt.bindLong(3, _tmp_1);
        stmt.bindLong(4, value.getPlayType());
        final int _tmp_2 = value.isBeforeEnabled() ? 1 : 0;
        stmt.bindLong(5, _tmp_2);
        stmt.bindLong(6, value.getBeforeAlertMinute());
        final int _tmp_3 = value.isAfterEnabled() ? 1 : 0;
        stmt.bindLong(7, _tmp_3);
        stmt.bindLong(8, value.getAfterAlertMinute());
        final int _tmp_4 = value.isSilentEnabled() ? 1 : 0;
        stmt.bindLong(9, _tmp_4);
        stmt.bindLong(10, value.getSilentMinute());
        final int _tmp_5 = value.isAscending() ? 1 : 0;
        stmt.bindLong(11, _tmp_5);
        stmt.bindLong(12, value.getAthanVolume());
        if (value.getAthanURI() == null) {
          stmt.bindNull(13);
        } else {
          stmt.bindString(13, value.getAthanURI());
        }
        if (value.getAlertURI() == null) {
          stmt.bindNull(14);
        } else {
          stmt.bindString(14, value.getAlertURI());
        }
        stmt.bindLong(15, value.getId());
      }
    };
    this.__deletionAdapterOfAthanSetting = new EntityDeletionOrUpdateAdapter<AthanSetting>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `athans_settings` WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, AthanSetting value) {
        stmt.bindLong(1, value.getId());
      }
    };
    this.__updateAdapterOfAthanSetting = new EntityDeletionOrUpdateAdapter<AthanSetting>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE OR ABORT `athans_settings` SET `athan_key` = ?,`state` = ?,`play_doa` = ?,`play_type` = ?,`is_before_enabled` = ?,`before_minute` = ?,`is_after_enabled` = ?,`after_minute` = ?,`is_silent_enabled` = ?,`silent_minute` = ?,`is_ascending` = ?,`athan_volume` = ?,`athan_uri` = ?,`alert_uri` = ?,`id` = ? WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, AthanSetting value) {
        if (value.getAthanKey() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getAthanKey());
        }
        final int _tmp = value.getState() ? 1 : 0;
        stmt.bindLong(2, _tmp);
        final int _tmp_1 = value.getPlayDoa() ? 1 : 0;
        stmt.bindLong(3, _tmp_1);
        stmt.bindLong(4, value.getPlayType());
        final int _tmp_2 = value.isBeforeEnabled() ? 1 : 0;
        stmt.bindLong(5, _tmp_2);
        stmt.bindLong(6, value.getBeforeAlertMinute());
        final int _tmp_3 = value.isAfterEnabled() ? 1 : 0;
        stmt.bindLong(7, _tmp_3);
        stmt.bindLong(8, value.getAfterAlertMinute());
        final int _tmp_4 = value.isSilentEnabled() ? 1 : 0;
        stmt.bindLong(9, _tmp_4);
        stmt.bindLong(10, value.getSilentMinute());
        final int _tmp_5 = value.isAscending() ? 1 : 0;
        stmt.bindLong(11, _tmp_5);
        stmt.bindLong(12, value.getAthanVolume());
        if (value.getAthanURI() == null) {
          stmt.bindNull(13);
        } else {
          stmt.bindString(13, value.getAthanURI());
        }
        if (value.getAlertURI() == null) {
          stmt.bindNull(14);
        } else {
          stmt.bindString(14, value.getAlertURI());
        }
        stmt.bindLong(15, value.getId());
        stmt.bindLong(16, value.getId());
      }
    };
  }

  @Override
  public void insert(final AthanSetting... athanSetting) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfAthanSetting.insert(athanSetting);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final AthanSetting athanSetting) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfAthanSetting.handle(athanSetting);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final AthanSetting athanSetting) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfAthanSetting.handle(athanSetting);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<AthanSetting> getAllAthanSettings() {
    final String _sql = "select * from athans_settings";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfAthanKey = CursorUtil.getColumnIndexOrThrow(_cursor, "athan_key");
      final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
      final int _cursorIndexOfPlayDoa = CursorUtil.getColumnIndexOrThrow(_cursor, "play_doa");
      final int _cursorIndexOfPlayType = CursorUtil.getColumnIndexOrThrow(_cursor, "play_type");
      final int _cursorIndexOfIsBeforeEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "is_before_enabled");
      final int _cursorIndexOfBeforeAlertMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "before_minute");
      final int _cursorIndexOfIsAfterEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "is_after_enabled");
      final int _cursorIndexOfAfterAlertMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "after_minute");
      final int _cursorIndexOfIsSilentEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "is_silent_enabled");
      final int _cursorIndexOfSilentMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "silent_minute");
      final int _cursorIndexOfIsAscending = CursorUtil.getColumnIndexOrThrow(_cursor, "is_ascending");
      final int _cursorIndexOfAthanVolume = CursorUtil.getColumnIndexOrThrow(_cursor, "athan_volume");
      final int _cursorIndexOfAthanURI = CursorUtil.getColumnIndexOrThrow(_cursor, "athan_uri");
      final int _cursorIndexOfAlertURI = CursorUtil.getColumnIndexOrThrow(_cursor, "alert_uri");
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final List<AthanSetting> _result = new ArrayList<AthanSetting>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final AthanSetting _item;
        final String _tmpAthanKey;
        if (_cursor.isNull(_cursorIndexOfAthanKey)) {
          _tmpAthanKey = null;
        } else {
          _tmpAthanKey = _cursor.getString(_cursorIndexOfAthanKey);
        }
        final boolean _tmpState;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfState);
        _tmpState = _tmp != 0;
        final boolean _tmpPlayDoa;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfPlayDoa);
        _tmpPlayDoa = _tmp_1 != 0;
        final int _tmpPlayType;
        _tmpPlayType = _cursor.getInt(_cursorIndexOfPlayType);
        final boolean _tmpIsBeforeEnabled;
        final int _tmp_2;
        _tmp_2 = _cursor.getInt(_cursorIndexOfIsBeforeEnabled);
        _tmpIsBeforeEnabled = _tmp_2 != 0;
        final int _tmpBeforeAlertMinute;
        _tmpBeforeAlertMinute = _cursor.getInt(_cursorIndexOfBeforeAlertMinute);
        final boolean _tmpIsAfterEnabled;
        final int _tmp_3;
        _tmp_3 = _cursor.getInt(_cursorIndexOfIsAfterEnabled);
        _tmpIsAfterEnabled = _tmp_3 != 0;
        final int _tmpAfterAlertMinute;
        _tmpAfterAlertMinute = _cursor.getInt(_cursorIndexOfAfterAlertMinute);
        final boolean _tmpIsSilentEnabled;
        final int _tmp_4;
        _tmp_4 = _cursor.getInt(_cursorIndexOfIsSilentEnabled);
        _tmpIsSilentEnabled = _tmp_4 != 0;
        final int _tmpSilentMinute;
        _tmpSilentMinute = _cursor.getInt(_cursorIndexOfSilentMinute);
        final boolean _tmpIsAscending;
        final int _tmp_5;
        _tmp_5 = _cursor.getInt(_cursorIndexOfIsAscending);
        _tmpIsAscending = _tmp_5 != 0;
        final int _tmpAthanVolume;
        _tmpAthanVolume = _cursor.getInt(_cursorIndexOfAthanVolume);
        final String _tmpAthanURI;
        if (_cursor.isNull(_cursorIndexOfAthanURI)) {
          _tmpAthanURI = null;
        } else {
          _tmpAthanURI = _cursor.getString(_cursorIndexOfAthanURI);
        }
        final String _tmpAlertURI;
        if (_cursor.isNull(_cursorIndexOfAlertURI)) {
          _tmpAlertURI = null;
        } else {
          _tmpAlertURI = _cursor.getString(_cursorIndexOfAlertURI);
        }
        _item = new AthanSetting(_tmpAthanKey,_tmpState,_tmpPlayDoa,_tmpPlayType,_tmpIsBeforeEnabled,_tmpBeforeAlertMinute,_tmpIsAfterEnabled,_tmpAfterAlertMinute,_tmpIsSilentEnabled,_tmpSilentMinute,_tmpIsAscending,_tmpAthanVolume,_tmpAthanURI,_tmpAlertURI);
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
  public AthanSetting getSetting(final int id) {
    final String _sql = "select * from athans_settings where id=?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfAthanKey = CursorUtil.getColumnIndexOrThrow(_cursor, "athan_key");
      final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
      final int _cursorIndexOfPlayDoa = CursorUtil.getColumnIndexOrThrow(_cursor, "play_doa");
      final int _cursorIndexOfPlayType = CursorUtil.getColumnIndexOrThrow(_cursor, "play_type");
      final int _cursorIndexOfIsBeforeEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "is_before_enabled");
      final int _cursorIndexOfBeforeAlertMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "before_minute");
      final int _cursorIndexOfIsAfterEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "is_after_enabled");
      final int _cursorIndexOfAfterAlertMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "after_minute");
      final int _cursorIndexOfIsSilentEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "is_silent_enabled");
      final int _cursorIndexOfSilentMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "silent_minute");
      final int _cursorIndexOfIsAscending = CursorUtil.getColumnIndexOrThrow(_cursor, "is_ascending");
      final int _cursorIndexOfAthanVolume = CursorUtil.getColumnIndexOrThrow(_cursor, "athan_volume");
      final int _cursorIndexOfAthanURI = CursorUtil.getColumnIndexOrThrow(_cursor, "athan_uri");
      final int _cursorIndexOfAlertURI = CursorUtil.getColumnIndexOrThrow(_cursor, "alert_uri");
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final AthanSetting _result;
      if(_cursor.moveToFirst()) {
        final String _tmpAthanKey;
        if (_cursor.isNull(_cursorIndexOfAthanKey)) {
          _tmpAthanKey = null;
        } else {
          _tmpAthanKey = _cursor.getString(_cursorIndexOfAthanKey);
        }
        final boolean _tmpState;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfState);
        _tmpState = _tmp != 0;
        final boolean _tmpPlayDoa;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfPlayDoa);
        _tmpPlayDoa = _tmp_1 != 0;
        final int _tmpPlayType;
        _tmpPlayType = _cursor.getInt(_cursorIndexOfPlayType);
        final boolean _tmpIsBeforeEnabled;
        final int _tmp_2;
        _tmp_2 = _cursor.getInt(_cursorIndexOfIsBeforeEnabled);
        _tmpIsBeforeEnabled = _tmp_2 != 0;
        final int _tmpBeforeAlertMinute;
        _tmpBeforeAlertMinute = _cursor.getInt(_cursorIndexOfBeforeAlertMinute);
        final boolean _tmpIsAfterEnabled;
        final int _tmp_3;
        _tmp_3 = _cursor.getInt(_cursorIndexOfIsAfterEnabled);
        _tmpIsAfterEnabled = _tmp_3 != 0;
        final int _tmpAfterAlertMinute;
        _tmpAfterAlertMinute = _cursor.getInt(_cursorIndexOfAfterAlertMinute);
        final boolean _tmpIsSilentEnabled;
        final int _tmp_4;
        _tmp_4 = _cursor.getInt(_cursorIndexOfIsSilentEnabled);
        _tmpIsSilentEnabled = _tmp_4 != 0;
        final int _tmpSilentMinute;
        _tmpSilentMinute = _cursor.getInt(_cursorIndexOfSilentMinute);
        final boolean _tmpIsAscending;
        final int _tmp_5;
        _tmp_5 = _cursor.getInt(_cursorIndexOfIsAscending);
        _tmpIsAscending = _tmp_5 != 0;
        final int _tmpAthanVolume;
        _tmpAthanVolume = _cursor.getInt(_cursorIndexOfAthanVolume);
        final String _tmpAthanURI;
        if (_cursor.isNull(_cursorIndexOfAthanURI)) {
          _tmpAthanURI = null;
        } else {
          _tmpAthanURI = _cursor.getString(_cursorIndexOfAthanURI);
        }
        final String _tmpAlertURI;
        if (_cursor.isNull(_cursorIndexOfAlertURI)) {
          _tmpAlertURI = null;
        } else {
          _tmpAlertURI = _cursor.getString(_cursorIndexOfAlertURI);
        }
        _result = new AthanSetting(_tmpAthanKey,_tmpState,_tmpPlayDoa,_tmpPlayType,_tmpIsBeforeEnabled,_tmpBeforeAlertMinute,_tmpIsAfterEnabled,_tmpAfterAlertMinute,_tmpIsSilentEnabled,_tmpSilentMinute,_tmpIsAscending,_tmpAthanVolume,_tmpAthanURI,_tmpAlertURI);
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
