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
public final class PrayTimesDAO_Impl implements PrayTimesDAO {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CurrentPrayTimesEntity> __insertionAdapterOfCurrentPrayTimesEntity;

  private final EntityInsertionAdapter<EditedPrayTimesEntity> __insertionAdapterOfEditedPrayTimesEntity;

  private final EntityDeletionOrUpdateAdapter<EditedPrayTimesEntity> __updateAdapterOfEditedPrayTimesEntity;

  private final SharedSQLiteStatement __preparedStmtOfCleanCurrentPrayTimes;

  private final SharedSQLiteStatement __preparedStmtOfCleanEditedPrayTimes;

  public PrayTimesDAO_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCurrentPrayTimesEntity = new EntityInsertionAdapter<CurrentPrayTimesEntity>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `CurrentPrayTimes` (`id`,`dayNumber`,`fajr`,`sunrise`,`dhuhr`,`asr`,`maghrib`,`isha`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, CurrentPrayTimesEntity value) {
        stmt.bindLong(1, value.getId());
        stmt.bindLong(2, value.getDayNumber());
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
        if (value.getMaghrib() == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindString(7, value.getMaghrib());
        }
        if (value.getIsha() == null) {
          stmt.bindNull(8);
        } else {
          stmt.bindString(8, value.getIsha());
        }
      }
    };
    this.__insertionAdapterOfEditedPrayTimesEntity = new EntityInsertionAdapter<EditedPrayTimesEntity>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `EditedPrayTimes` (`id`,`dayNumber`,`fajr`,`sunrise`,`dhuhr`,`asr`,`maghrib`,`isha`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, EditedPrayTimesEntity value) {
        stmt.bindLong(1, value.getId());
        stmt.bindLong(2, value.getDayNumber());
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
        if (value.getMaghrib() == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindString(7, value.getMaghrib());
        }
        if (value.getIsha() == null) {
          stmt.bindNull(8);
        } else {
          stmt.bindString(8, value.getIsha());
        }
      }
    };
    this.__updateAdapterOfEditedPrayTimesEntity = new EntityDeletionOrUpdateAdapter<EditedPrayTimesEntity>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE OR ABORT `EditedPrayTimes` SET `id` = ?,`dayNumber` = ?,`fajr` = ?,`sunrise` = ?,`dhuhr` = ?,`asr` = ?,`maghrib` = ?,`isha` = ? WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, EditedPrayTimesEntity value) {
        stmt.bindLong(1, value.getId());
        stmt.bindLong(2, value.getDayNumber());
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
        if (value.getMaghrib() == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindString(7, value.getMaghrib());
        }
        if (value.getIsha() == null) {
          stmt.bindNull(8);
        } else {
          stmt.bindString(8, value.getIsha());
        }
        stmt.bindLong(9, value.getId());
      }
    };
    this.__preparedStmtOfCleanCurrentPrayTimes = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "delete from CurrentPrayTimes";
        return _query;
      }
    };
    this.__preparedStmtOfCleanEditedPrayTimes = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "delete from EditedPrayTimes";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final List<CurrentPrayTimesEntity> prayTimes,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCurrentPrayTimesEntity.insert(prayTimes);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object insertEdited(final List<EditedPrayTimesEntity> prayTimes,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfEditedPrayTimesEntity.insert(prayTimes);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object updateEdited(final List<EditedPrayTimesEntity> prayTimes,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfEditedPrayTimesEntity.handleMultiple(prayTimes);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object cleanCurrentPrayTimes(final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfCleanCurrentPrayTimes.acquire();
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
          __preparedStmtOfCleanCurrentPrayTimes.release(_stmt);
        }
      }
    }, continuation);
  }

  @Override
  public Object cleanEditedPrayTimes(final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfCleanEditedPrayTimes.acquire();
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
          __preparedStmtOfCleanEditedPrayTimes.release(_stmt);
        }
      }
    }, continuation);
  }

  @Override
  public Object getAll(final Continuation<? super List<CurrentPrayTimesEntity>> continuation) {
    final String _sql = "select * from CurrentPrayTimes";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CurrentPrayTimesEntity>>() {
      @Override
      public List<CurrentPrayTimesEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDayNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "dayNumber");
          final int _cursorIndexOfFajr = CursorUtil.getColumnIndexOrThrow(_cursor, "fajr");
          final int _cursorIndexOfSunrise = CursorUtil.getColumnIndexOrThrow(_cursor, "sunrise");
          final int _cursorIndexOfDhuhr = CursorUtil.getColumnIndexOrThrow(_cursor, "dhuhr");
          final int _cursorIndexOfAsr = CursorUtil.getColumnIndexOrThrow(_cursor, "asr");
          final int _cursorIndexOfMaghrib = CursorUtil.getColumnIndexOrThrow(_cursor, "maghrib");
          final int _cursorIndexOfIsha = CursorUtil.getColumnIndexOrThrow(_cursor, "isha");
          final List<CurrentPrayTimesEntity> _result = new ArrayList<CurrentPrayTimesEntity>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final CurrentPrayTimesEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpDayNumber;
            _tmpDayNumber = _cursor.getInt(_cursorIndexOfDayNumber);
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
            _item = new CurrentPrayTimesEntity(_tmpId,_tmpDayNumber,_tmpFajr,_tmpSunrise,_tmpDhuhr,_tmpAsr,_tmpMaghrib,_tmpIsha);
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
  public Object get(final int dayNumber,
      final Continuation<? super CurrentPrayTimesEntity> continuation) {
    final String _sql = "select * from CurrentPrayTimes where dayNumber=?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, dayNumber);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<CurrentPrayTimesEntity>() {
      @Override
      public CurrentPrayTimesEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDayNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "dayNumber");
          final int _cursorIndexOfFajr = CursorUtil.getColumnIndexOrThrow(_cursor, "fajr");
          final int _cursorIndexOfSunrise = CursorUtil.getColumnIndexOrThrow(_cursor, "sunrise");
          final int _cursorIndexOfDhuhr = CursorUtil.getColumnIndexOrThrow(_cursor, "dhuhr");
          final int _cursorIndexOfAsr = CursorUtil.getColumnIndexOrThrow(_cursor, "asr");
          final int _cursorIndexOfMaghrib = CursorUtil.getColumnIndexOrThrow(_cursor, "maghrib");
          final int _cursorIndexOfIsha = CursorUtil.getColumnIndexOrThrow(_cursor, "isha");
          final CurrentPrayTimesEntity _result;
          if(_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpDayNumber;
            _tmpDayNumber = _cursor.getInt(_cursorIndexOfDayNumber);
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
            _result = new CurrentPrayTimesEntity(_tmpId,_tmpDayNumber,_tmpFajr,_tmpSunrise,_tmpDhuhr,_tmpAsr,_tmpMaghrib,_tmpIsha);
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
  public Object getAllEdited(final Continuation<? super List<EditedPrayTimesEntity>> continuation) {
    final String _sql = "select * from EditedPrayTimes";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<EditedPrayTimesEntity>>() {
      @Override
      public List<EditedPrayTimesEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDayNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "dayNumber");
          final int _cursorIndexOfFajr = CursorUtil.getColumnIndexOrThrow(_cursor, "fajr");
          final int _cursorIndexOfSunrise = CursorUtil.getColumnIndexOrThrow(_cursor, "sunrise");
          final int _cursorIndexOfDhuhr = CursorUtil.getColumnIndexOrThrow(_cursor, "dhuhr");
          final int _cursorIndexOfAsr = CursorUtil.getColumnIndexOrThrow(_cursor, "asr");
          final int _cursorIndexOfMaghrib = CursorUtil.getColumnIndexOrThrow(_cursor, "maghrib");
          final int _cursorIndexOfIsha = CursorUtil.getColumnIndexOrThrow(_cursor, "isha");
          final List<EditedPrayTimesEntity> _result = new ArrayList<EditedPrayTimesEntity>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final EditedPrayTimesEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpDayNumber;
            _tmpDayNumber = _cursor.getInt(_cursorIndexOfDayNumber);
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
            _item = new EditedPrayTimesEntity(_tmpId,_tmpDayNumber,_tmpFajr,_tmpSunrise,_tmpDhuhr,_tmpAsr,_tmpMaghrib,_tmpIsha);
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
  public Object getEdited(final int dayNumber,
      final Continuation<? super EditedPrayTimesEntity> continuation) {
    final String _sql = "select * from EditedPrayTimes where dayNumber=?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, dayNumber);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<EditedPrayTimesEntity>() {
      @Override
      public EditedPrayTimesEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDayNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "dayNumber");
          final int _cursorIndexOfFajr = CursorUtil.getColumnIndexOrThrow(_cursor, "fajr");
          final int _cursorIndexOfSunrise = CursorUtil.getColumnIndexOrThrow(_cursor, "sunrise");
          final int _cursorIndexOfDhuhr = CursorUtil.getColumnIndexOrThrow(_cursor, "dhuhr");
          final int _cursorIndexOfAsr = CursorUtil.getColumnIndexOrThrow(_cursor, "asr");
          final int _cursorIndexOfMaghrib = CursorUtil.getColumnIndexOrThrow(_cursor, "maghrib");
          final int _cursorIndexOfIsha = CursorUtil.getColumnIndexOrThrow(_cursor, "isha");
          final EditedPrayTimesEntity _result;
          if(_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpDayNumber;
            _tmpDayNumber = _cursor.getInt(_cursorIndexOfDayNumber);
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
            _result = new EditedPrayTimesEntity(_tmpId,_tmpDayNumber,_tmpFajr,_tmpSunrise,_tmpDhuhr,_tmpAsr,_tmpMaghrib,_tmpIsha);
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

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
