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
public final class LocationsDB_Impl extends LocationsDB {
  private volatile CityDAO _cityDAO;

  private volatile CountryDAO _countryDAO;

  private volatile ProvinceDAO _provinceDAO;

  @Override
  protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration configuration) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(configuration, new RoomOpenHelper.Delegate(5) {
      @Override
      public void createAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("CREATE TABLE IF NOT EXISTS `city` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `is_hanafi` INTEGER NOT NULL, `province_id` INTEGER NOT NULL, `created_at` TEXT NOT NULL, `updated_at` TEXT NOT NULL, PRIMARY KEY(`id`))");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `province` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `country_id` INTEGER NOT NULL, `created_at` TEXT NOT NULL, `updated_at` TEXT NOT NULL, PRIMARY KEY(`id`))");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `country` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `created_at` TEXT NOT NULL, `updated_at` TEXT NOT NULL, PRIMARY KEY(`id`))");
        _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '5e315670cf4d4d32a9859e1f97482417')");
      }

      @Override
      public void dropAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("DROP TABLE IF EXISTS `city`");
        _db.execSQL("DROP TABLE IF EXISTS `province`");
        _db.execSQL("DROP TABLE IF EXISTS `country`");
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
        final HashMap<String, TableInfo.Column> _columnsCity = new HashMap<String, TableInfo.Column>(8);
        _columnsCity.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCity.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCity.put("latitude", new TableInfo.Column("latitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCity.put("longitude", new TableInfo.Column("longitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCity.put("is_hanafi", new TableInfo.Column("is_hanafi", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCity.put("province_id", new TableInfo.Column("province_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCity.put("created_at", new TableInfo.Column("created_at", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCity.put("updated_at", new TableInfo.Column("updated_at", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCity = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCity = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCity = new TableInfo("city", _columnsCity, _foreignKeysCity, _indicesCity);
        final TableInfo _existingCity = TableInfo.read(_db, "city");
        if (! _infoCity.equals(_existingCity)) {
          return new RoomOpenHelper.ValidationResult(false, "city(ir.namoo.commons.model.CityModel).\n"
                  + " Expected:\n" + _infoCity + "\n"
                  + " Found:\n" + _existingCity);
        }
        final HashMap<String, TableInfo.Column> _columnsProvince = new HashMap<String, TableInfo.Column>(7);
        _columnsProvince.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProvince.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProvince.put("latitude", new TableInfo.Column("latitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProvince.put("longitude", new TableInfo.Column("longitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProvince.put("country_id", new TableInfo.Column("country_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProvince.put("created_at", new TableInfo.Column("created_at", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProvince.put("updated_at", new TableInfo.Column("updated_at", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysProvince = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesProvince = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoProvince = new TableInfo("province", _columnsProvince, _foreignKeysProvince, _indicesProvince);
        final TableInfo _existingProvince = TableInfo.read(_db, "province");
        if (! _infoProvince.equals(_existingProvince)) {
          return new RoomOpenHelper.ValidationResult(false, "province(ir.namoo.commons.model.ProvinceModel).\n"
                  + " Expected:\n" + _infoProvince + "\n"
                  + " Found:\n" + _existingProvince);
        }
        final HashMap<String, TableInfo.Column> _columnsCountry = new HashMap<String, TableInfo.Column>(6);
        _columnsCountry.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCountry.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCountry.put("latitude", new TableInfo.Column("latitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCountry.put("longitude", new TableInfo.Column("longitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCountry.put("created_at", new TableInfo.Column("created_at", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCountry.put("updated_at", new TableInfo.Column("updated_at", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCountry = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCountry = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCountry = new TableInfo("country", _columnsCountry, _foreignKeysCountry, _indicesCountry);
        final TableInfo _existingCountry = TableInfo.read(_db, "country");
        if (! _infoCountry.equals(_existingCountry)) {
          return new RoomOpenHelper.ValidationResult(false, "country(ir.namoo.commons.model.CountryModel).\n"
                  + " Expected:\n" + _infoCountry + "\n"
                  + " Found:\n" + _existingCountry);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "5e315670cf4d4d32a9859e1f97482417", "72225ab3326fe22dde07eaaf2733043a");
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
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "city","province","country");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `city`");
      _db.execSQL("DELETE FROM `province`");
      _db.execSQL("DELETE FROM `country`");
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
    _typeConvertersMap.put(CityDAO.class, CityDAO_Impl.getRequiredConverters());
    _typeConvertersMap.put(CountryDAO.class, CountryDAO_Impl.getRequiredConverters());
    _typeConvertersMap.put(ProvinceDAO.class, ProvinceDAO_Impl.getRequiredConverters());
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
  public CityDAO cityDAO() {
    if (_cityDAO != null) {
      return _cityDAO;
    } else {
      synchronized(this) {
        if(_cityDAO == null) {
          _cityDAO = new CityDAO_Impl(this);
        }
        return _cityDAO;
      }
    }
  }

  @Override
  public CountryDAO countryDAO() {
    if (_countryDAO != null) {
      return _countryDAO;
    } else {
      synchronized(this) {
        if(_countryDAO == null) {
          _countryDAO = new CountryDAO_Impl(this);
        }
        return _countryDAO;
      }
    }
  }

  @Override
  public ProvinceDAO provinceDAO() {
    if (_provinceDAO != null) {
      return _provinceDAO;
    } else {
      synchronized(this) {
        if(_provinceDAO == null) {
          _provinceDAO = new ProvinceDAO_Impl(this);
        }
        return _provinceDAO;
      }
    }
  }
}
