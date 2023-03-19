package ir.namoo.religiousprayers.ui.azkar;

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
public final class AzkarDB_Impl extends AzkarDB {
  private volatile AzkarDAO _azkarDAO;

  @Override
  protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration configuration) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(configuration, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("CREATE TABLE IF NOT EXISTS `azkar_categories` (`id` INTEGER NOT NULL, `ckb` TEXT, `ar` TEXT, `fa` TEXT, `en` TEXT, PRIMARY KEY(`id`))");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `azkar_chapters` (`id` INTEGER NOT NULL, `category_id` INTEGER NOT NULL, `ckb` TEXT, `ar` TEXT, `fa` TEXT, `en` TEXT, `fav` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `azkar_items` (`id` INTEGER NOT NULL, `chapter_id` INTEGER NOT NULL, `ckb` TEXT, `ar` TEXT, `fa` TEXT, `en` TEXT, `sound` TEXT, PRIMARY KEY(`id`))");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `azkar_references` (`id` INTEGER NOT NULL, `chapter_id` INTEGER NOT NULL, `ckb` TEXT, `ar` TEXT, `fa` TEXT, `en` TEXT, PRIMARY KEY(`id`))");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `tasbih` (`id` INTEGER NOT NULL, `zikr` TEXT NOT NULL, `count` INTEGER NOT NULL, `time` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '7fcd77f8d51f052d9f8dc150b9744c95')");
      }

      @Override
      public void dropAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("DROP TABLE IF EXISTS `azkar_categories`");
        _db.execSQL("DROP TABLE IF EXISTS `azkar_chapters`");
        _db.execSQL("DROP TABLE IF EXISTS `azkar_items`");
        _db.execSQL("DROP TABLE IF EXISTS `azkar_references`");
        _db.execSQL("DROP TABLE IF EXISTS `tasbih`");
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
        final HashMap<String, TableInfo.Column> _columnsAzkarCategories = new HashMap<String, TableInfo.Column>(5);
        _columnsAzkarCategories.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAzkarCategories.put("ckb", new TableInfo.Column("ckb", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAzkarCategories.put("ar", new TableInfo.Column("ar", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAzkarCategories.put("fa", new TableInfo.Column("fa", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAzkarCategories.put("en", new TableInfo.Column("en", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAzkarCategories = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAzkarCategories = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAzkarCategories = new TableInfo("azkar_categories", _columnsAzkarCategories, _foreignKeysAzkarCategories, _indicesAzkarCategories);
        final TableInfo _existingAzkarCategories = TableInfo.read(_db, "azkar_categories");
        if (! _infoAzkarCategories.equals(_existingAzkarCategories)) {
          return new RoomOpenHelper.ValidationResult(false, "azkar_categories(ir.namoo.religiousprayers.ui.azkar.AzkarCategory).\n"
                  + " Expected:\n" + _infoAzkarCategories + "\n"
                  + " Found:\n" + _existingAzkarCategories);
        }
        final HashMap<String, TableInfo.Column> _columnsAzkarChapters = new HashMap<String, TableInfo.Column>(7);
        _columnsAzkarChapters.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAzkarChapters.put("category_id", new TableInfo.Column("category_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAzkarChapters.put("ckb", new TableInfo.Column("ckb", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAzkarChapters.put("ar", new TableInfo.Column("ar", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAzkarChapters.put("fa", new TableInfo.Column("fa", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAzkarChapters.put("en", new TableInfo.Column("en", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAzkarChapters.put("fav", new TableInfo.Column("fav", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAzkarChapters = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAzkarChapters = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAzkarChapters = new TableInfo("azkar_chapters", _columnsAzkarChapters, _foreignKeysAzkarChapters, _indicesAzkarChapters);
        final TableInfo _existingAzkarChapters = TableInfo.read(_db, "azkar_chapters");
        if (! _infoAzkarChapters.equals(_existingAzkarChapters)) {
          return new RoomOpenHelper.ValidationResult(false, "azkar_chapters(ir.namoo.religiousprayers.ui.azkar.AzkarChapter).\n"
                  + " Expected:\n" + _infoAzkarChapters + "\n"
                  + " Found:\n" + _existingAzkarChapters);
        }
        final HashMap<String, TableInfo.Column> _columnsAzkarItems = new HashMap<String, TableInfo.Column>(7);
        _columnsAzkarItems.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAzkarItems.put("chapter_id", new TableInfo.Column("chapter_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAzkarItems.put("ckb", new TableInfo.Column("ckb", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAzkarItems.put("ar", new TableInfo.Column("ar", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAzkarItems.put("fa", new TableInfo.Column("fa", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAzkarItems.put("en", new TableInfo.Column("en", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAzkarItems.put("sound", new TableInfo.Column("sound", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAzkarItems = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAzkarItems = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAzkarItems = new TableInfo("azkar_items", _columnsAzkarItems, _foreignKeysAzkarItems, _indicesAzkarItems);
        final TableInfo _existingAzkarItems = TableInfo.read(_db, "azkar_items");
        if (! _infoAzkarItems.equals(_existingAzkarItems)) {
          return new RoomOpenHelper.ValidationResult(false, "azkar_items(ir.namoo.religiousprayers.ui.azkar.AzkarItem).\n"
                  + " Expected:\n" + _infoAzkarItems + "\n"
                  + " Found:\n" + _existingAzkarItems);
        }
        final HashMap<String, TableInfo.Column> _columnsAzkarReferences = new HashMap<String, TableInfo.Column>(6);
        _columnsAzkarReferences.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAzkarReferences.put("chapter_id", new TableInfo.Column("chapter_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAzkarReferences.put("ckb", new TableInfo.Column("ckb", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAzkarReferences.put("ar", new TableInfo.Column("ar", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAzkarReferences.put("fa", new TableInfo.Column("fa", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAzkarReferences.put("en", new TableInfo.Column("en", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAzkarReferences = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAzkarReferences = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAzkarReferences = new TableInfo("azkar_references", _columnsAzkarReferences, _foreignKeysAzkarReferences, _indicesAzkarReferences);
        final TableInfo _existingAzkarReferences = TableInfo.read(_db, "azkar_references");
        if (! _infoAzkarReferences.equals(_existingAzkarReferences)) {
          return new RoomOpenHelper.ValidationResult(false, "azkar_references(ir.namoo.religiousprayers.ui.azkar.AzkarReference).\n"
                  + " Expected:\n" + _infoAzkarReferences + "\n"
                  + " Found:\n" + _existingAzkarReferences);
        }
        final HashMap<String, TableInfo.Column> _columnsTasbih = new HashMap<String, TableInfo.Column>(4);
        _columnsTasbih.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTasbih.put("zikr", new TableInfo.Column("zikr", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTasbih.put("count", new TableInfo.Column("count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTasbih.put("time", new TableInfo.Column("time", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTasbih = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTasbih = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTasbih = new TableInfo("tasbih", _columnsTasbih, _foreignKeysTasbih, _indicesTasbih);
        final TableInfo _existingTasbih = TableInfo.read(_db, "tasbih");
        if (! _infoTasbih.equals(_existingTasbih)) {
          return new RoomOpenHelper.ValidationResult(false, "tasbih(ir.namoo.religiousprayers.ui.azkar.Tasbih).\n"
                  + " Expected:\n" + _infoTasbih + "\n"
                  + " Found:\n" + _existingTasbih);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "7fcd77f8d51f052d9f8dc150b9744c95", "232ab17402371dff0980e3403f949848");
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
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "azkar_categories","azkar_chapters","azkar_items","azkar_references","tasbih");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `azkar_categories`");
      _db.execSQL("DELETE FROM `azkar_chapters`");
      _db.execSQL("DELETE FROM `azkar_items`");
      _db.execSQL("DELETE FROM `azkar_references`");
      _db.execSQL("DELETE FROM `tasbih`");
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
    _typeConvertersMap.put(AzkarDAO.class, AzkarDAO_Impl.getRequiredConverters());
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
  public AzkarDAO azkarDao() {
    if (_azkarDAO != null) {
      return _azkarDAO;
    } else {
      synchronized(this) {
        if(_azkarDAO == null) {
          _azkarDAO = new AzkarDAO_Impl(this);
        }
        return _azkarDAO;
      }
    }
  }
}
