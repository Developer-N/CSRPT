package ir.namoo.religiousprayers.praytimeprovider;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
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
public final class DownloadedPrayTimesDAO_Impl implements DownloadedPrayTimesDAO {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DownloadedPrayTimesEntity> __insertionAdapterOfDownloadedPrayTimesEntity;

  private final EntityDeletionOrUpdateAdapter<DownloadedPrayTimesEntity> __updateAdapterOfDownloadedPrayTimesEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearDownloadFor;

  public DownloadedPrayTimesDAO_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDownloadedPrayTimesEntity = new EntityInsertionAdapter<DownloadedPrayTimesEntity>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `DownloadedPrayTimes` (`id`,`day`,`fajr`,`sunrise`,`dhuhr`,`asr`,`asr_hanafi`,`maghrib`,`isha`,`city_id`,`created_at`,`updated_at`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, DownloadedPrayTimesEntity value) {
        stmt.bindLong(1, value.getId());
        stmt.bindLong(2, value.getDay());
        if (value.getFajr() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getFajr());
        }
        if (value.getSunrise() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getSunrise());
        }
        if (value.getDhuhr() == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, value.getDhuhr());
        }
        if (value.getAsr() == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, value.getAsr());
        }
        if (value.getAsrHanafi() == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindString(7, value.getAsrHanafi());
        }
        if (value.getMaghrib() == null) {
          stmt.bindNull(8);
        } else {
          stmt.bindString(8, value.getMaghrib());
        }
        if (value.getIsha() == null) {
          stmt.bindNull(9);
        } else {
          stmt.bindString(9, value.getIsha());
        }
        stmt.bindLong(10, value.getCityID());
        if (value.getCreatedAt() == null) {
          stmt.bindNull(11);
        } else {
          stmt.bindString(11, value.getCreatedAt());
        }
        if (value.getUpdatedAt() == null) {
          stmt.bindNull(12);
        } else {
          stmt.bindString(12, value.getUpdatedAt());
        }
      }
    };
    this.__updateAdapterOfDownloadedPrayTimesEntity = new EntityDeletionOrUpdateAdapter<DownloadedPrayTimesEntity>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE OR ABORT `DownloadedPrayTimes` SET `id` = ?,`day` = ?,`fajr` = ?,`sunrise` = ?,`dhuhr` = ?,`asr` = ?,`asr_hanafi` = ?,`maghrib` = ?,`isha` = ?,`city_id` = ?,`created_at` = ?,`updated_at` = ? WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, DownloadedPrayTimesEntity value) {
        stmt.bindLong(1, value.getId());
        stmt.bindLong(2, value.getDay());
        if (value.getFajr() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getFajr());
        }
        if (value.getSunrise() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getSunrise());
        }
        if (value.getDhuhr() == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, value.getDhuhr());
        }
        if (value.getAsr() == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, value.getAsr());
        }
        if (value.getAsrHanafi() == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindString(7, value.getAsrHanafi());
        }
        if (value.getMaghrib() == null) {
          stmt.bindNull(8);
        } else {
          stmt.bindString(8, value.getMaghrib());
        }
        if (value.getIsha() == null) {
          stmt.bindNull(9);
        } else {
          stmt.bindString(9, value.getIsha());
        }
        stmt.bindLong(10, value.getCityID());
        if (value.getCreatedAt() == null) {
          stmt.bindNull(11);
        } else {
          stmt.bindString(11, value.getCreatedAt());
        }
        if (value.getUpdatedAt() == null) {
          stmt.bindNull(12);
        } else {
          stmt.bindString(12, value.getUpdatedAt());
        }
        stmt.bindLong(13, value.getId());
      }
    };
    this.__preparedStmtOfClearDownloadFor = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "delete from DownloadedPrayTimes where city_id=?";
        return _query;
      }
    };
  }

  @Override
  public Object insertToDownload(final List<DownloadedPrayTimesEntity> prayTimes,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDownloadedPrayTimesEntity.insert(prayTimes);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object insertToDownload(final DownloadedPrayTimesEntity prayTime,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDownloadedPrayTimesEntity.insert(prayTime);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object updateDownload(final DownloadedPrayTimesEntity prayTime,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfDownloadedPrayTimesEntity.handle(prayTime);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object updateDownload(final List<DownloadedPrayTimesEntity> prayTimes,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfDownloadedPrayTimesEntity.handleMultiple(prayTimes);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object clearDownloadFor(final int cityID, final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearDownloadFor.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, cityID);
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
          __preparedStmtOfClearDownloadFor.release(_stmt);
        }
      }
    }, continuation);
  }

  @Override
  public Object getAllDownloaded(
      final Continuation<? super List<DownloadedPrayTimesEntity>> continuation) {
    final String _sql = "select * from DownloadedPrayTimes";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DownloadedPrayTimesEntity>>() {
      @Override
      public List<DownloadedPrayTimesEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "day");
          final int _cursorIndexOfFajr = CursorUtil.getColumnIndexOrThrow(_cursor, "fajr");
          final int _cursorIndexOfSunrise = CursorUtil.getColumnIndexOrThrow(_cursor, "sunrise");
          final int _cursorIndexOfDhuhr = CursorUtil.getColumnIndexOrThrow(_cursor, "dhuhr");
          final int _cursorIndexOfAsr = CursorUtil.getColumnIndexOrThrow(_cursor, "asr");
          final int _cursorIndexOfAsrHanafi = CursorUtil.getColumnIndexOrThrow(_cursor, "asr_hanafi");
          final int _cursorIndexOfMaghrib = CursorUtil.getColumnIndexOrThrow(_cursor, "maghrib");
          final int _cursorIndexOfIsha = CursorUtil.getColumnIndexOrThrow(_cursor, "isha");
          final int _cursorIndexOfCityID = CursorUtil.getColumnIndexOrThrow(_cursor, "city_id");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final List<DownloadedPrayTimesEntity> _result = new ArrayList<DownloadedPrayTimesEntity>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final DownloadedPrayTimesEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpDay;
            _tmpDay = _cursor.getInt(_cursorIndexOfDay);
            final String _tmpFajr;
            if (_cursor.isNull(_cursorIndexOfFajr)) {
              _tmpFajr = null;
            } else {
              _tmpFajr = _cursor.getString(_cursorIndexOfFajr);
            }
            final String _tmpSunrise;
            if (_cursor.isNull(_cursorIndexOfSunrise)) {
              _tmpSunrise = null;
            } else {
              _tmpSunrise = _cursor.getString(_cursorIndexOfSunrise);
            }
            final String _tmpDhuhr;
            if (_cursor.isNull(_cursorIndexOfDhuhr)) {
              _tmpDhuhr = null;
            } else {
              _tmpDhuhr = _cursor.getString(_cursorIndexOfDhuhr);
            }
            final String _tmpAsr;
            if (_cursor.isNull(_cursorIndexOfAsr)) {
              _tmpAsr = null;
            } else {
              _tmpAsr = _cursor.getString(_cursorIndexOfAsr);
            }
            final String _tmpAsrHanafi;
            if (_cursor.isNull(_cursorIndexOfAsrHanafi)) {
              _tmpAsrHanafi = null;
            } else {
              _tmpAsrHanafi = _cursor.getString(_cursorIndexOfAsrHanafi);
            }
            final String _tmpMaghrib;
            if (_cursor.isNull(_cursorIndexOfMaghrib)) {
              _tmpMaghrib = null;
            } else {
              _tmpMaghrib = _cursor.getString(_cursorIndexOfMaghrib);
            }
            final String _tmpIsha;
            if (_cursor.isNull(_cursorIndexOfIsha)) {
              _tmpIsha = null;
            } else {
              _tmpIsha = _cursor.getString(_cursorIndexOfIsha);
            }
            final int _tmpCityID;
            _tmpCityID = _cursor.getInt(_cursorIndexOfCityID);
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            final String _tmpUpdatedAt;
            if (_cursor.isNull(_cursorIndexOfUpdatedAt)) {
              _tmpUpdatedAt = null;
            } else {
              _tmpUpdatedAt = _cursor.getString(_cursorIndexOfUpdatedAt);
            }
            _item = new DownloadedPrayTimesEntity(_tmpId,_tmpDay,_tmpFajr,_tmpSunrise,_tmpDhuhr,_tmpAsr,_tmpAsrHanafi,_tmpMaghrib,_tmpIsha,_tmpCityID,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getDownloadFor(final int cityID, final int day,
      final Continuation<? super DownloadedPrayTimesEntity> continuation) {
    final String _sql = "select * from DownloadedPrayTimes where city_id=? and day=?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, cityID);
    _argIndex = 2;
    _statement.bindLong(_argIndex, day);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<DownloadedPrayTimesEntity>() {
      @Override
      public DownloadedPrayTimesEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "day");
          final int _cursorIndexOfFajr = CursorUtil.getColumnIndexOrThrow(_cursor, "fajr");
          final int _cursorIndexOfSunrise = CursorUtil.getColumnIndexOrThrow(_cursor, "sunrise");
          final int _cursorIndexOfDhuhr = CursorUtil.getColumnIndexOrThrow(_cursor, "dhuhr");
          final int _cursorIndexOfAsr = CursorUtil.getColumnIndexOrThrow(_cursor, "asr");
          final int _cursorIndexOfAsrHanafi = CursorUtil.getColumnIndexOrThrow(_cursor, "asr_hanafi");
          final int _cursorIndexOfMaghrib = CursorUtil.getColumnIndexOrThrow(_cursor, "maghrib");
          final int _cursorIndexOfIsha = CursorUtil.getColumnIndexOrThrow(_cursor, "isha");
          final int _cursorIndexOfCityID = CursorUtil.getColumnIndexOrThrow(_cursor, "city_id");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final DownloadedPrayTimesEntity _result;
          if(_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpDay;
            _tmpDay = _cursor.getInt(_cursorIndexOfDay);
            final String _tmpFajr;
            if (_cursor.isNull(_cursorIndexOfFajr)) {
              _tmpFajr = null;
            } else {
              _tmpFajr = _cursor.getString(_cursorIndexOfFajr);
            }
            final String _tmpSunrise;
            if (_cursor.isNull(_cursorIndexOfSunrise)) {
              _tmpSunrise = null;
            } else {
              _tmpSunrise = _cursor.getString(_cursorIndexOfSunrise);
            }
            final String _tmpDhuhr;
            if (_cursor.isNull(_cursorIndexOfDhuhr)) {
              _tmpDhuhr = null;
            } else {
              _tmpDhuhr = _cursor.getString(_cursorIndexOfDhuhr);
            }
            final String _tmpAsr;
            if (_cursor.isNull(_cursorIndexOfAsr)) {
              _tmpAsr = null;
            } else {
              _tmpAsr = _cursor.getString(_cursorIndexOfAsr);
            }
            final String _tmpAsrHanafi;
            if (_cursor.isNull(_cursorIndexOfAsrHanafi)) {
              _tmpAsrHanafi = null;
            } else {
              _tmpAsrHanafi = _cursor.getString(_cursorIndexOfAsrHanafi);
            }
            final String _tmpMaghrib;
            if (_cursor.isNull(_cursorIndexOfMaghrib)) {
              _tmpMaghrib = null;
            } else {
              _tmpMaghrib = _cursor.getString(_cursorIndexOfMaghrib);
            }
            final String _tmpIsha;
            if (_cursor.isNull(_cursorIndexOfIsha)) {
              _tmpIsha = null;
            } else {
              _tmpIsha = _cursor.getString(_cursorIndexOfIsha);
            }
            final int _tmpCityID;
            _tmpCityID = _cursor.getInt(_cursorIndexOfCityID);
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            final String _tmpUpdatedAt;
            if (_cursor.isNull(_cursorIndexOfUpdatedAt)) {
              _tmpUpdatedAt = null;
            } else {
              _tmpUpdatedAt = _cursor.getString(_cursorIndexOfUpdatedAt);
            }
            _result = new DownloadedPrayTimesEntity(_tmpId,_tmpDay,_tmpFajr,_tmpSunrise,_tmpDhuhr,_tmpAsr,_tmpAsrHanafi,_tmpMaghrib,_tmpIsha,_tmpCityID,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getDownloadFor(final int cityID,
      final Continuation<? super List<DownloadedPrayTimesEntity>> continuation) {
    final String _sql = "select * from DownloadedPrayTimes where city_id=?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, cityID);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DownloadedPrayTimesEntity>>() {
      @Override
      public List<DownloadedPrayTimesEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "day");
          final int _cursorIndexOfFajr = CursorUtil.getColumnIndexOrThrow(_cursor, "fajr");
          final int _cursorIndexOfSunrise = CursorUtil.getColumnIndexOrThrow(_cursor, "sunrise");
          final int _cursorIndexOfDhuhr = CursorUtil.getColumnIndexOrThrow(_cursor, "dhuhr");
          final int _cursorIndexOfAsr = CursorUtil.getColumnIndexOrThrow(_cursor, "asr");
          final int _cursorIndexOfAsrHanafi = CursorUtil.getColumnIndexOrThrow(_cursor, "asr_hanafi");
          final int _cursorIndexOfMaghrib = CursorUtil.getColumnIndexOrThrow(_cursor, "maghrib");
          final int _cursorIndexOfIsha = CursorUtil.getColumnIndexOrThrow(_cursor, "isha");
          final int _cursorIndexOfCityID = CursorUtil.getColumnIndexOrThrow(_cursor, "city_id");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final List<DownloadedPrayTimesEntity> _result = new ArrayList<DownloadedPrayTimesEntity>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final DownloadedPrayTimesEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpDay;
            _tmpDay = _cursor.getInt(_cursorIndexOfDay);
            final String _tmpFajr;
            if (_cursor.isNull(_cursorIndexOfFajr)) {
              _tmpFajr = null;
            } else {
              _tmpFajr = _cursor.getString(_cursorIndexOfFajr);
            }
            final String _tmpSunrise;
            if (_cursor.isNull(_cursorIndexOfSunrise)) {
              _tmpSunrise = null;
            } else {
              _tmpSunrise = _cursor.getString(_cursorIndexOfSunrise);
            }
            final String _tmpDhuhr;
            if (_cursor.isNull(_cursorIndexOfDhuhr)) {
              _tmpDhuhr = null;
            } else {
              _tmpDhuhr = _cursor.getString(_cursorIndexOfDhuhr);
            }
            final String _tmpAsr;
            if (_cursor.isNull(_cursorIndexOfAsr)) {
              _tmpAsr = null;
            } else {
              _tmpAsr = _cursor.getString(_cursorIndexOfAsr);
            }
            final String _tmpAsrHanafi;
            if (_cursor.isNull(_cursorIndexOfAsrHanafi)) {
              _tmpAsrHanafi = null;
            } else {
              _tmpAsrHanafi = _cursor.getString(_cursorIndexOfAsrHanafi);
            }
            final String _tmpMaghrib;
            if (_cursor.isNull(_cursorIndexOfMaghrib)) {
              _tmpMaghrib = null;
            } else {
              _tmpMaghrib = _cursor.getString(_cursorIndexOfMaghrib);
            }
            final String _tmpIsha;
            if (_cursor.isNull(_cursorIndexOfIsha)) {
              _tmpIsha = null;
            } else {
              _tmpIsha = _cursor.getString(_cursorIndexOfIsha);
            }
            final int _tmpCityID;
            _tmpCityID = _cursor.getInt(_cursorIndexOfCityID);
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            final String _tmpUpdatedAt;
            if (_cursor.isNull(_cursorIndexOfUpdatedAt)) {
              _tmpUpdatedAt = null;
            } else {
              _tmpUpdatedAt = _cursor.getString(_cursorIndexOfUpdatedAt);
            }
            _item = new DownloadedPrayTimesEntity(_tmpId,_tmpDay,_tmpFajr,_tmpSunrise,_tmpDhuhr,_tmpAsr,_tmpAsrHanafi,_tmpMaghrib,_tmpIsha,_tmpCityID,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getCities(final Continuation<? super List<Integer>> continuation) {
    final String _sql = "select distinct city_id from DownloadedPrayTimes";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Integer>>() {
      @Override
      public List<Integer> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<Integer> _result = new ArrayList<Integer>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final Integer _item;
            _item = _cursor.getInt(0);
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
