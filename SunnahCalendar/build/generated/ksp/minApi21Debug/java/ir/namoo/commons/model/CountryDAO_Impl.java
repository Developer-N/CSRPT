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
public final class CountryDAO_Impl implements CountryDAO {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CountryModel> __insertionAdapterOfCountryModel;

  public CountryDAO_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCountryModel = new EntityInsertionAdapter<CountryModel>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `country` (`id`,`name`,`latitude`,`longitude`,`created_at`,`updated_at`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, CountryModel value) {
        stmt.bindLong(1, value.getId());
        if (value.getName() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getName());
        }
        stmt.bindDouble(3, value.getLatitude());
        stmt.bindDouble(4, value.getLongitude());
        if (value.getCreated_at() == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, value.getCreated_at());
        }
        if (value.getUpdated_at() == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, value.getUpdated_at());
        }
      }
    };
  }

  @Override
  public Object insert(final List<CountryModel> cities,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCountryModel.insert(cities);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object getAllCountries(final Continuation<? super List<CountryModel>> continuation) {
    final String _sql = "select * from country";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CountryModel>>() {
      @Override
      public List<CountryModel> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final List<CountryModel> _result = new ArrayList<CountryModel>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final CountryModel _item;
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
            _item = new CountryModel(_tmpId,_tmpName,_tmpLatitude,_tmpLongitude,_tmpCreated_at,_tmpUpdated_at);
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
