package ir.namoo.religiousprayers.praytimeprovider;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomOpenHelper;
import androidx.room.RoomOpenHelper.Delegate;
import androidx.room.RoomOpenHelper.ValidationResult;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.room.util.TableInfo.Column;
import androidx.room.util.TableInfo.ForeignKey;
import androidx.room.util.TableInfo.Index;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Callback;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unchecked", "deprecation"})
public final class DownloadedPrayTimesDB_Impl extends DownloadedPrayTimesDB {
  private volatile DownloadedPrayTimesDAO _downloadedPrayTimesDAO;

  @Override
  protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration configuration) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(configuration, new RoomOpenHelper.Delegate(3) {
      @Override
      public void createAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("CREATE TABLE IF NOT EXISTS `DownloadedPrayTimes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `day` INTEGER NOT NULL, `fajr` TEXT NOT NULL, `sunrise` TEXT NOT NULL, `dhuhr` TEXT NOT NULL, `asr` TEXT NOT NULL, `asr_hanafi` TEXT NOT NULL, `maghrib` TEXT NOT NULL, `isha` TEXT NOT NULL, `city_id` INTEGER NOT NULL, `created_at` TEXT NOT NULL, `updated_at` TEXT NOT NULL)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'fe8ee1d61f43146889615daeafcb8cf0')");
      }

      @Override
      public void dropAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("DROP TABLE IF EXISTS `DownloadedPrayTimes`");
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onDestructiveMigration(_db);
          }
        }
      }

      @Override
      public void onCreate(SupportSQLiteDatabase _db) {
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onCreate(_db);
          }
        }
      }

      @Override
      public void onOpen(SupportSQLiteDatabase _db) {
        mDatabase = _db;
        internalInitInvalidationTracker(_db);
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onOpen(_db);
          }
        }
      }

      @Override
      public void onPreMigrate(SupportSQLiteDatabase _db) {
        DBUtil.dropFtsSyncTriggers(_db);
      }

      @Override
      public void onPostMigrate(SupportSQLiteDatabase _db) {
      }

      @Override
      public RoomOpenHelper.ValidationResult onValidateSchema(SupportSQLiteDatabase _db) {
        final HashMap<String, TableInfo.Column> _columnsDownloadedPrayTimes = new HashMap<String, TableInfo.Column>(12);
        _columnsDownloadedPrayTimes.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedPrayTimes.put("day", new TableInfo.Column("day", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedPrayTimes.put("fajr", new TableInfo.Column("fajr", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedPrayTimes.put("sunrise", new TableInfo.Column("sunrise", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedPrayTimes.put("dhuhr", new TableInfo.Column("dhuhr", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedPrayTimes.put("asr", new TableInfo.Column("asr", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedPrayTimes.put("asr_hanafi", new TableInfo.Column("asr_hanafi", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedPrayTimes.put("maghrib", new TableInfo.Column("maghrib", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedPrayTimes.put("isha", new TableInfo.Column("isha", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedPrayTimes.put("city_id", new TableInfo.Column("city_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedPrayTimes.put("created_at", new TableInfo.Column("created_at", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedPrayTimes.put("updated_at", new TableInfo.Column("updated_at", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDownloadedPrayTimes = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDownloadedPrayTimes = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDownloadedPrayTimes = new TableInfo("DownloadedPrayTimes", _columnsDownloadedPrayTimes, _foreignKeysDownloadedPrayTimes, _indicesDownloadedPrayTimes);
        final TableInfo _existingDownloadedPrayTimes = TableInfo.read(_db, "DownloadedPrayTimes");
        if (! _infoDownloadedPrayTimes.equals(_existingDownloadedPrayTimes)) {
          return new RoomOpenHelper.ValidationResult(false, "DownloadedPrayTimes(ir.namoo.religiousprayers.praytimeprovider.DownloadedPrayTimesEntity).\n"
                  + " Expected:\n" + _infoDownloadedPrayTimes + "\n"
                  + " Found:\n" + _existingDownloadedPrayTimes);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "fe8ee1d61f43146889615daeafcb8cf0", "bb20f3ceea8586c7f3611a34a59555de");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
        .name(configuration.name)
        .callback(_openCallback)
        .build();
    final SupportSQLiteOpenHelper _helper = configuration.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "DownloadedPrayTimes");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `DownloadedPrayTimes`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(DownloadedPrayTimesDAO.class, DownloadedPrayTimesDAO_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  public List<Migration> getAutoMigrations(
      @NonNull Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecsMap) {
    return Arrays.asList();
  }

  @Override
  public DownloadedPrayTimesDAO downloadedPrayTimes() {
    if (_downloadedPrayTimesDAO != null) {
      return _downloadedPrayTimesDAO;
    } else {
      synchronized(this) {
        if(_downloadedPrayTimesDAO == null) {
          _downloadedPrayTimesDAO = new DownloadedPrayTimesDAO_Impl(this);
        }
        return _downloadedPrayTimesDAO;
      }
    }
  }
}
