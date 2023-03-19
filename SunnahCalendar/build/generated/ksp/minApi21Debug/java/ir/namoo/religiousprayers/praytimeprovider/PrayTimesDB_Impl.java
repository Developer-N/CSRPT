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
public final class PrayTimesDB_Impl extends PrayTimesDB {
  private volatile PrayTimesDAO _prayTimesDAO;

  @Override
  protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration configuration) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(configuration, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("CREATE TABLE IF NOT EXISTS `CurrentPrayTimes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `dayNumber` INTEGER NOT NULL, `fajr` TEXT NOT NULL, `sunrise` TEXT NOT NULL, `dhuhr` TEXT NOT NULL, `asr` TEXT NOT NULL, `maghrib` TEXT NOT NULL, `isha` TEXT NOT NULL)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `EditedPrayTimes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `dayNumber` INTEGER NOT NULL, `fajr` TEXT NOT NULL, `sunrise` TEXT NOT NULL, `dhuhr` TEXT NOT NULL, `asr` TEXT NOT NULL, `maghrib` TEXT NOT NULL, `isha` TEXT NOT NULL)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'e47476590eb6669b2e7f2d7acd674355')");
      }

      @Override
      public void dropAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("DROP TABLE IF EXISTS `CurrentPrayTimes`");
        _db.execSQL("DROP TABLE IF EXISTS `EditedPrayTimes`");
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
        final HashMap<String, TableInfo.Column> _columnsCurrentPrayTimes = new HashMap<String, TableInfo.Column>(8);
        _columnsCurrentPrayTimes.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCurrentPrayTimes.put("dayNumber", new TableInfo.Column("dayNumber", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCurrentPrayTimes.put("fajr", new TableInfo.Column("fajr", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCurrentPrayTimes.put("sunrise", new TableInfo.Column("sunrise", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCurrentPrayTimes.put("dhuhr", new TableInfo.Column("dhuhr", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCurrentPrayTimes.put("asr", new TableInfo.Column("asr", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCurrentPrayTimes.put("maghrib", new TableInfo.Column("maghrib", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCurrentPrayTimes.put("isha", new TableInfo.Column("isha", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCurrentPrayTimes = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCurrentPrayTimes = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCurrentPrayTimes = new TableInfo("CurrentPrayTimes", _columnsCurrentPrayTimes, _foreignKeysCurrentPrayTimes, _indicesCurrentPrayTimes);
        final TableInfo _existingCurrentPrayTimes = TableInfo.read(_db, "CurrentPrayTimes");
        if (! _infoCurrentPrayTimes.equals(_existingCurrentPrayTimes)) {
          return new RoomOpenHelper.ValidationResult(false, "CurrentPrayTimes(ir.namoo.religiousprayers.praytimeprovider.CurrentPrayTimesEntity).\n"
                  + " Expected:\n" + _infoCurrentPrayTimes + "\n"
                  + " Found:\n" + _existingCurrentPrayTimes);
        }
        final HashMap<String, TableInfo.Column> _columnsEditedPrayTimes = new HashMap<String, TableInfo.Column>(8);
        _columnsEditedPrayTimes.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEditedPrayTimes.put("dayNumber", new TableInfo.Column("dayNumber", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEditedPrayTimes.put("fajr", new TableInfo.Column("fajr", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEditedPrayTimes.put("sunrise", new TableInfo.Column("sunrise", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEditedPrayTimes.put("dhuhr", new TableInfo.Column("dhuhr", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEditedPrayTimes.put("asr", new TableInfo.Column("asr", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEditedPrayTimes.put("maghrib", new TableInfo.Column("maghrib", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEditedPrayTimes.put("isha", new TableInfo.Column("isha", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysEditedPrayTimes = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesEditedPrayTimes = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoEditedPrayTimes = new TableInfo("EditedPrayTimes", _columnsEditedPrayTimes, _foreignKeysEditedPrayTimes, _indicesEditedPrayTimes);
        final TableInfo _existingEditedPrayTimes = TableInfo.read(_db, "EditedPrayTimes");
        if (! _infoEditedPrayTimes.equals(_existingEditedPrayTimes)) {
          return new RoomOpenHelper.ValidationResult(false, "EditedPrayTimes(ir.namoo.religiousprayers.praytimeprovider.EditedPrayTimesEntity).\n"
                  + " Expected:\n" + _infoEditedPrayTimes + "\n"
                  + " Found:\n" + _existingEditedPrayTimes);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "e47476590eb6669b2e7f2d7acd674355", "b6fd9164a50b3e9648e30f0fe417b6da");
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
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "CurrentPrayTimes","EditedPrayTimes");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `CurrentPrayTimes`");
      _db.execSQL("DELETE FROM `EditedPrayTimes`");
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
    _typeConvertersMap.put(PrayTimesDAO.class, PrayTimesDAO_Impl.getRequiredConverters());
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
  public PrayTimesDAO prayTimes() {
    if (_prayTimesDAO != null) {
      return _prayTimesDAO;
    } else {
      synchronized(this) {
        if(_prayTimesDAO == null) {
          _prayTimesDAO = new PrayTimesDAO_Impl(this);
        }
        return _prayTimesDAO;
      }
    }
  }
}
