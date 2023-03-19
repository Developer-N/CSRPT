package ir.namoo.commons.model;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
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
public final class CityDAO_Impl implements CityDAO {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CityModel> __insertionAdapterOfCityModel;

  public CityDAO_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCityModel = new EntityInsertionAdapter<CityModel>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `city` (`id`,`name`,`latitude`,`longitude`,`is_hanafi`,`province_id`,`created_at`,`updated_at`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, CityModel value) {
        stmt.bindLong(1, value.getId());
        if (value.getName() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getName());
        }
        stmt.bindDouble(3, value.getLatitude());
        stmt.bindDouble(4, value.getLongitude());
        stmt.bindLong(5, value.is_hanafi());
        stmt.bindLong(6, value.getProvince_id());
        if (value.getCreated_at() == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindString(7, value.getCreated_at());
        }
        if (value.getUpdated_at() == null) {
          stmt.bindNull(8);
        } else {
          stmt.bindString(8, value.getUpdated_at());
        }
      }
    };
  }

  @Override
  public Object insert(final List<CityModel> cities,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCityModel.insert(cities);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public List<CityModel> getAllCity() {
    final String _sql = "select * from city";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
      final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
      final int _cursorIndexOfIsHanafi = CursorUtil.getColumnIndexOrThrow(_cursor, "is_hanafi");
      final int _cursorIndexOfProvinceId = CursorUtil.getColumnIndexOrThrow(_cursor, "province_id");
      final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
      final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
      final List<CityModel> _result = new ArrayList<CityModel>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final CityModel _item;
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        final double _tmpLatitude;
        _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
        final double _tmpLongitude;
        _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
        final int _tmpIs_hanafi;
        _tmpIs_hanafi = _cursor.getInt(_cursorIndexOfIsHanafi);
        final int _tmpProvince_id;
        _tmpProvince_id = _cursor.getInt(_cursorIndexOfProvinceId);
        final String _tmpCreated_at;
        if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
          _tmpCreated_at = null;
        } else {
          _tmpCreated_at = _cursor.getString(_cursorIndexOfCreatedAt);
        }
        final String _tmpUpdated_at;
        if (_cursor.isNull(_cursorIndexOfUpdatedAt)) {
          _tmpUpdated_at = null;
        } else {
          _tmpUpdated_at = _cursor.getString(_cursorIndexOfUpdatedAt);
        }
        _item = new CityModel(_tmpId,_tmpName,_tmpLatitude,_tmpLongitude,_tmpIs_hanafi,_tmpProvince_id,_tmpCreated_at,_tmpUpdated_at);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public Object getCity(final int id, final Continuation<? super CityModel> continuation) {
    final String _sql = "select * from city where id=?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<CityModel>() {
      @Override
      public CityModel call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfIsHanafi = CursorUtil.getColumnIndexOrThrow(_cursor, "is_hanafi");
          final int _cursorIndexOfProvinceId = CursorUtil.getColumnIndexOrThrow(_cursor, "province_id");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final CityModel _result;
          if(_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final int _tmpIs_hanafi;
            _tmpIs_hanafi = _cursor.getInt(_cursorIndexOfIsHanafi);
            final int _tmpProvince_id;
            _tmpProvince_id = _cursor.getInt(_cursorIndexOfProvinceId);
            final String _tmpCreated_at;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreated_at = null;
            } else {
              _tmpCreated_at = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            final String _tmpUpdated_at;
            if (_cursor.isNull(_cursorIndexOfUpdatedAt)) {
              _tmpUpdated_at = null;
            } else {
              _tmpUpdated_at = _cursor.getString(_cursorIndexOfUpdatedAt);
            }
            _result = new CityModel(_tmpId,_tmpName,_tmpLatitude,_tmpLongitude,_tmpIs_hanafi,_tmpProvince_id,_tmpCreated_at,_tmpUpdated_at);
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
  public Object getCity(final String name, final Continuation<? super CityModel> continuation) {
    final String _sql = "select * from city where name=?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (name == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, name);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<CityModel>() {
      @Override
      public CityModel call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfIsHanafi = CursorUtil.getColumnIndexOrThrow(_cursor, "is_hanafi");
          final int _cursorIndexOfProvinceId = CursorUtil.getColumnIndexOrThrow(_cursor, "province_id");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final CityModel _result;
          if(_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final int _tmpIs_hanafi;
            _tmpIs_hanafi = _cursor.getInt(_cursorIndexOfIsHanafi);
            final int _tmpProvince_id;
            _tmpProvince_id = _cursor.getInt(_cursorIndexOfProvinceId);
            final String _tmpCreated_at;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreated_at = null;
            } else {
              _tmpCreated_at = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            final String _tmpUpdated_at;
            if (_cursor.isNull(_cursorIndexOfUpdatedAt)) {
              _tmpUpdated_at = null;
            } else {
              _tmpUpdated_at = _cursor.getString(_cursorIndexOfUpdatedAt);
            }
            _result = new CityModel(_tmpId,_tmpName,_tmpLatitude,_tmpLongitude,_tmpIs_hanafi,_tmpProvince_id,_tmpCreated_at,_tmpUpdated_at);
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
