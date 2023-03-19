package ir.namoo.commons.model;

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
public final class AthanSettingsDB_Impl extends AthanSettingsDB {
  private volatile AthanSettingsDAO _athanSettingsDAO;

  @Override
  protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration configuration) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(configuration, new RoomOpenHelper.Delegate(7) {
      @Override
      public void createAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("CREATE TABLE IF NOT EXISTS `athans_settings` (`athan_key` TEXT NOT NULL, `state` INTEGER NOT NULL, `play_doa` INTEGER NOT NULL, `play_type` INTEGER NOT NULL, `is_before_enabled` INTEGER NOT NULL, `before_minute` INTEGER NOT NULL, `is_after_enabled` INTEGER NOT NULL, `after_minute` INTEGER NOT NULL, `is_silent_enabled` INTEGER NOT NULL, `silent_minute` INTEGER NOT NULL, `is_ascending` INTEGER NOT NULL, `athan_volume` INTEGER NOT NULL, `athan_uri` TEXT NOT NULL, `alert_uri` TEXT NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '06cb4706b7760d9624f395115df315f2')");
      }

      @Override
      public void dropAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("DROP TABLE IF EXISTS `athans_settings`");
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
        final HashMap<String, TableInfo.Column> _columnsAthansSettings = new HashMap<String, TableInfo.Column>(15);
        _columnsAthansSettings.put("athan_key", new TableInfo.Column("athan_key", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAthansSettings.put("state", new TableInfo.Column("state", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAthansSettings.put("play_doa", new TableInfo.Column("play_doa", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAthansSettings.put("play_type", new TableInfo.Column("play_type", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAthansSettings.put("is_before_enabled", new TableInfo.Column("is_before_enabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAthansSettings.put("before_minute", new TableInfo.Column("before_minute", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAthansSettings.put("is_after_enabled", new TableInfo.Column("is_after_enabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAthansSettings.put("after_minute", new TableInfo.Column("after_minute", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAthansSettings.put("is_silent_enabled", new TableInfo.Column("is_silent_enabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAthansSettings.put("silent_minute", new TableInfo.Column("silent_minute", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAthansSettings.put("is_ascending", new TableInfo.Column("is_ascending", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAthansSettings.put("athan_volume", new TableInfo.Column("athan_volume", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAthansSettings.put("athan_uri", new TableInfo.Column("athan_uri", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAthansSettings.put("alert_uri", new TableInfo.Column("alert_uri", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAthansSettings.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAthansSettings = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAthansSettings = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAthansSettings = new TableInfo("athans_settings", _columnsAthansSettings, _foreignKeysAthansSettings, _indicesAthansSettings);
        final TableInfo _existingAthansSettings = TableInfo.read(_db, "athans_settings");
        if (! _infoAthansSettings.equals(_existingAthansSettings)) {
          return new RoomOpenHelper.ValidationResult(false, "athans_settings(ir.namoo.commons.model.AthanSetting).\n"
                  + " Expected:\n" + _infoAthansSettings + "\n"
                  + " Found:\n" + _existingAthansSettings);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "06cb4706b7760d9624f395115df315f2", "a0c001b14a8ea09877b7bdbe29519a3d");
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
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "athans_settings");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `athans_settings`");
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
    _typeConvertersMap.put(AthanSettingsDAO.class, AthanSettingsDAO_Impl.getRequiredConverters());
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
  public AthanSettingsDAO athanSettingsDAO() {
    if (_athanSettingsDAO != null) {
      return _athanSettingsDAO;
    } else {
      synchronized(this) {
        if(_athanSettingsDAO == null) {
          _athanSettingsDAO = new AthanSettingsDAO_Impl(this);
        }
        return _athanSettingsDAO;
      }
    }
  }
}
