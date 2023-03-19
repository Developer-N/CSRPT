package ir.namoo.quran.db;

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
public final class QuranDB_Impl extends QuranDB {
  private volatile ChaptersDao _chaptersDao;

  private volatile QuranDao _quranDao;

  private volatile PJHDao _pJHDao;

  @Override
  protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration configuration) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(configuration, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("CREATE TABLE IF NOT EXISTS `quran_all` (`index` INTEGER NOT NULL, `sura` INTEGER, `aya` INTEGER, `simple` TEXT, `simple_clean` TEXT, `uthmani` TEXT, `en_transilation` TEXT, `en_pickthall` TEXT, `fa_khorramdel` TEXT, `ku_asan` TEXT, `note` TEXT, `fav` INTEGER, PRIMARY KEY(`index`))");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `hezb` (`aya` INTEGER, `sura` INTEGER, `Page` INTEGER, `hizb` INTEGER NOT NULL, `JozA` INTEGER, PRIMARY KEY(`hizb`))");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `juz` (`id` INTEGER NOT NULL, `sura` INTEGER, `aya` INTEGER, PRIMARY KEY(`id`))");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `safhe` (`MetaDataID` INTEGER NOT NULL, `sura` INTEGER, `page` INTEGER, `aya` INTEGER, PRIMARY KEY(`MetaDataID`))");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `chapters` (`sura` INTEGER NOT NULL, `ayas_count` INTEGER, `first_aya_id` INTEGER, `name_arabic` TEXT, `name_transliteration` TEXT, `type` TEXT, `revelation_order` INTEGER, `rukus` INTEGER, `bismillah` INTEGER, `fav` INTEGER, PRIMARY KEY(`sura`))");
        _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '636a60460fb67d65cdbfeaf145186db1')");
      }

      @Override
      public void dropAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("DROP TABLE IF EXISTS `quran_all`");
        _db.execSQL("DROP TABLE IF EXISTS `hezb`");
        _db.execSQL("DROP TABLE IF EXISTS `juz`");
        _db.execSQL("DROP TABLE IF EXISTS `safhe`");
        _db.execSQL("DROP TABLE IF EXISTS `chapters`");
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
        final HashMap<String, TableInfo.Column> _columnsQuranAll = new HashMap<String, TableInfo.Column>(12);
        _columnsQuranAll.put("index", new TableInfo.Column("index", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuranAll.put("sura", new TableInfo.Column("sura", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuranAll.put("aya", new TableInfo.Column("aya", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuranAll.put("simple", new TableInfo.Column("simple", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuranAll.put("simple_clean", new TableInfo.Column("simple_clean", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuranAll.put("uthmani", new TableInfo.Column("uthmani", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuranAll.put("en_transilation", new TableInfo.Column("en_transilation", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuranAll.put("en_pickthall", new TableInfo.Column("en_pickthall", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuranAll.put("fa_khorramdel", new TableInfo.Column("fa_khorramdel", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuranAll.put("ku_asan", new TableInfo.Column("ku_asan", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuranAll.put("note", new TableInfo.Column("note", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuranAll.put("fav", new TableInfo.Column("fav", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysQuranAll = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesQuranAll = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoQuranAll = new TableInfo("quran_all", _columnsQuranAll, _foreignKeysQuranAll, _indicesQuranAll);
        final TableInfo _existingQuranAll = TableInfo.read(_db, "quran_all");
        if (! _infoQuranAll.equals(_existingQuranAll)) {
          return new RoomOpenHelper.ValidationResult(false, "quran_all(ir.namoo.quran.db.QuranEntity).\n"
                  + " Expected:\n" + _infoQuranAll + "\n"
                  + " Found:\n" + _existingQuranAll);
        }
        final HashMap<String, TableInfo.Column> _columnsHezb = new HashMap<String, TableInfo.Column>(5);
        _columnsHezb.put("aya", new TableInfo.Column("aya", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHezb.put("sura", new TableInfo.Column("sura", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHezb.put("Page", new TableInfo.Column("Page", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHezb.put("hizb", new TableInfo.Column("hizb", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHezb.put("JozA", new TableInfo.Column("JozA", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysHezb = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesHezb = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoHezb = new TableInfo("hezb", _columnsHezb, _foreignKeysHezb, _indicesHezb);
        final TableInfo _existingHezb = TableInfo.read(_db, "hezb");
        if (! _infoHezb.equals(_existingHezb)) {
          return new RoomOpenHelper.ValidationResult(false, "hezb(ir.namoo.quran.db.HizbEntity).\n"
                  + " Expected:\n" + _infoHezb + "\n"
                  + " Found:\n" + _existingHezb);
        }
        final HashMap<String, TableInfo.Column> _columnsJuz = new HashMap<String, TableInfo.Column>(3);
        _columnsJuz.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsJuz.put("sura", new TableInfo.Column("sura", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsJuz.put("aya", new TableInfo.Column("aya", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysJuz = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesJuz = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoJuz = new TableInfo("juz", _columnsJuz, _foreignKeysJuz, _indicesJuz);
        final TableInfo _existingJuz = TableInfo.read(_db, "juz");
        if (! _infoJuz.equals(_existingJuz)) {
          return new RoomOpenHelper.ValidationResult(false, "juz(ir.namoo.quran.db.JuzEntity).\n"
                  + " Expected:\n" + _infoJuz + "\n"
                  + " Found:\n" + _existingJuz);
        }
        final HashMap<String, TableInfo.Column> _columnsSafhe = new HashMap<String, TableInfo.Column>(4);
        _columnsSafhe.put("MetaDataID", new TableInfo.Column("MetaDataID", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSafhe.put("sura", new TableInfo.Column("sura", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSafhe.put("page", new TableInfo.Column("page", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSafhe.put("aya", new TableInfo.Column("aya", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSafhe = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSafhe = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSafhe = new TableInfo("safhe", _columnsSafhe, _foreignKeysSafhe, _indicesSafhe);
        final TableInfo _existingSafhe = TableInfo.read(_db, "safhe");
        if (! _infoSafhe.equals(_existingSafhe)) {
          return new RoomOpenHelper.ValidationResult(false, "safhe(ir.namoo.quran.db.PageEntity).\n"
                  + " Expected:\n" + _infoSafhe + "\n"
                  + " Found:\n" + _existingSafhe);
        }
        final HashMap<String, TableInfo.Column> _columnsChapters = new HashMap<String, TableInfo.Column>(10);
        _columnsChapters.put("sura", new TableInfo.Column("sura", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChapters.put("ayas_count", new TableInfo.Column("ayas_count", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChapters.put("first_aya_id", new TableInfo.Column("first_aya_id", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChapters.put("name_arabic", new TableInfo.Column("name_arabic", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChapters.put("name_transliteration", new TableInfo.Column("name_transliteration", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChapters.put("type", new TableInfo.Column("type", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChapters.put("revelation_order", new TableInfo.Column("revelation_order", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChapters.put("rukus", new TableInfo.Column("rukus", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChapters.put("bismillah", new TableInfo.Column("bismillah", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChapters.put("fav", new TableInfo.Column("fav", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysChapters = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesChapters = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoChapters = new TableInfo("chapters", _columnsChapters, _foreignKeysChapters, _indicesChapters);
        final TableInfo _existingChapters = TableInfo.read(_db, "chapters");
        if (! _infoChapters.equals(_existingChapters)) {
          return new RoomOpenHelper.ValidationResult(false, "chapters(ir.namoo.quran.db.ChapterEntity).\n"
                  + " Expected:\n" + _infoChapters + "\n"
                  + " Found:\n" + _existingChapters);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "636a60460fb67d65cdbfeaf145186db1", "252f5c9577a23a37dbedff00542c45a5");
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
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "quran_all","hezb","juz","safhe","chapters");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `quran_all`");
      _db.execSQL("DELETE FROM `hezb`");
      _db.execSQL("DELETE FROM `juz`");
      _db.execSQL("DELETE FROM `safhe`");
      _db.execSQL("DELETE FROM `chapters`");
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
    _typeConvertersMap.put(ChaptersDao.class, ChaptersDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(QuranDao.class, QuranDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PJHDao.class, PJHDao_Impl.getRequiredConverters());
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
  public ChaptersDao chaptersDao() {
    if (_chaptersDao != null) {
      return _chaptersDao;
    } else {
      synchronized(this) {
        if(_chaptersDao == null) {
          _chaptersDao = new ChaptersDao_Impl(this);
        }
        return _chaptersDao;
      }
    }
  }

  @Override
  public QuranDao quranDao() {
    if (_quranDao != null) {
      return _quranDao;
    } else {
      synchronized(this) {
        if(_quranDao == null) {
          _quranDao = new QuranDao_Impl(this);
        }
        return _quranDao;
      }
    }
  }

  @Override
  public PJHDao pjhDao() {
    if (_pJHDao != null) {
      return _pJHDao;
    } else {
      synchronized(this) {
        if(_pJHDao == null) {
          _pJHDao = new PJHDao_Impl(this);
        }
        return _pJHDao;
      }
    }
  }
}
