package ir.namoo.quran.db;

import android.database.Cursor;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Integer;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class QuranDao_Impl implements QuranDao {
  private final RoomDatabase __db;

  private final EntityDeletionOrUpdateAdapter<QuranEntity> __updateAdapterOfQuranEntity;

  public QuranDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__updateAdapterOfQuranEntity = new EntityDeletionOrUpdateAdapter<QuranEntity>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE OR ABORT `quran_all` SET `index` = ?,`sura` = ?,`aya` = ?,`simple` = ?,`simple_clean` = ?,`uthmani` = ?,`en_transilation` = ?,`en_pickthall` = ?,`fa_khorramdel` = ?,`ku_asan` = ?,`note` = ?,`fav` = ? WHERE `index` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, QuranEntity value) {
        stmt.bindLong(1, value.getIndex());
        if (value.getSura() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindLong(2, value.getSura());
        }
        if (value.getAya() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindLong(3, value.getAya());
        }
        if (value.getSimple() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getSimple());
        }
        if (value.getSimple_clean() == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, value.getSimple_clean());
        }
        if (value.getUthmani() == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, value.getUthmani());
        }
        if (value.getEn_transilation() == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindString(7, value.getEn_transilation());
        }
        if (value.getEn_pickthall() == null) {
          stmt.bindNull(8);
        } else {
          stmt.bindString(8, value.getEn_pickthall());
        }
        if (value.getFa_khorramdel() == null) {
          stmt.bindNull(9);
        } else {
          stmt.bindString(9, value.getFa_khorramdel());
        }
        if (value.getKu_asan() == null) {
          stmt.bindNull(10);
        } else {
          stmt.bindString(10, value.getKu_asan());
        }
        if (value.getNote() == null) {
          stmt.bindNull(11);
        } else {
          stmt.bindString(11, value.getNote());
        }
        if (value.getFav() == null) {
          stmt.bindNull(12);
        } else {
          stmt.bindLong(12, value.getFav());
        }
        stmt.bindLong(13, value.getIndex());
      }
    };
  }

  @Override
  public void update(final QuranEntity quranEntity) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfQuranEntity.handle(quranEntity);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<QuranEntity> getAllFor(final int sura) {
    final String _sql = "SELECT * FROM quran_all WHERE sura=?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sura);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "index");
      final int _cursorIndexOfSura = CursorUtil.getColumnIndexOrThrow(_cursor, "sura");
      final int _cursorIndexOfAya = CursorUtil.getColumnIndexOrThrow(_cursor, "aya");
      final int _cursorIndexOfSimple = CursorUtil.getColumnIndexOrThrow(_cursor, "simple");
      final int _cursorIndexOfSimpleClean = CursorUtil.getColumnIndexOrThrow(_cursor, "simple_clean");
      final int _cursorIndexOfUthmani = CursorUtil.getColumnIndexOrThrow(_cursor, "uthmani");
      final int _cursorIndexOfEnTransilation = CursorUtil.getColumnIndexOrThrow(_cursor, "en_transilation");
      final int _cursorIndexOfEnPickthall = CursorUtil.getColumnIndexOrThrow(_cursor, "en_pickthall");
      final int _cursorIndexOfFaKhorramdel = CursorUtil.getColumnIndexOrThrow(_cursor, "fa_khorramdel");
      final int _cursorIndexOfKuAsan = CursorUtil.getColumnIndexOrThrow(_cursor, "ku_asan");
      final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
      final int _cursorIndexOfFav = CursorUtil.getColumnIndexOrThrow(_cursor, "fav");
      final List<QuranEntity> _result = new ArrayList<QuranEntity>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final QuranEntity _item;
        final int _tmpIndex;
        _tmpIndex = _cursor.getInt(_cursorIndexOfIndex);
        final Integer _tmpSura;
        if (_cursor.isNull(_cursorIndexOfSura)) {
          _tmpSura = null;
        } else {
          _tmpSura = _cursor.getInt(_cursorIndexOfSura);
        }
        final Integer _tmpAya;
        if (_cursor.isNull(_cursorIndexOfAya)) {
          _tmpAya = null;
        } else {
          _tmpAya = _cursor.getInt(_cursorIndexOfAya);
        }
        final String _tmpSimple;
        if (_cursor.isNull(_cursorIndexOfSimple)) {
          _tmpSimple = null;
        } else {
          _tmpSimple = _cursor.getString(_cursorIndexOfSimple);
        }
        final String _tmpSimple_clean;
        if (_cursor.isNull(_cursorIndexOfSimpleClean)) {
          _tmpSimple_clean = null;
        } else {
          _tmpSimple_clean = _cursor.getString(_cursorIndexOfSimpleClean);
        }
        final String _tmpUthmani;
        if (_cursor.isNull(_cursorIndexOfUthmani)) {
          _tmpUthmani = null;
        } else {
          _tmpUthmani = _cursor.getString(_cursorIndexOfUthmani);
        }
        final String _tmpEn_transilation;
        if (_cursor.isNull(_cursorIndexOfEnTransilation)) {
          _tmpEn_transilation = null;
        } else {
          _tmpEn_transilation = _cursor.getString(_cursorIndexOfEnTransilation);
        }
        final String _tmpEn_pickthall;
        if (_cursor.isNull(_cursorIndexOfEnPickthall)) {
          _tmpEn_pickthall = null;
        } else {
          _tmpEn_pickthall = _cursor.getString(_cursorIndexOfEnPickthall);
        }
        final String _tmpFa_khorramdel;
        if (_cursor.isNull(_cursorIndexOfFaKhorramdel)) {
          _tmpFa_khorramdel = null;
        } else {
          _tmpFa_khorramdel = _cursor.getString(_cursorIndexOfFaKhorramdel);
        }
        final String _tmpKu_asan;
        if (_cursor.isNull(_cursorIndexOfKuAsan)) {
          _tmpKu_asan = null;
        } else {
          _tmpKu_asan = _cursor.getString(_cursorIndexOfKuAsan);
        }
        final String _tmpNote;
        if (_cursor.isNull(_cursorIndexOfNote)) {
          _tmpNote = null;
        } else {
          _tmpNote = _cursor.getString(_cursorIndexOfNote);
        }
        final Integer _tmpFav;
        if (_cursor.isNull(_cursorIndexOfFav)) {
          _tmpFav = null;
        } else {
          _tmpFav = _cursor.getInt(_cursorIndexOfFav);
        }
        _item = new QuranEntity(_tmpIndex,_tmpSura,_tmpAya,_tmpSimple,_tmpSimple_clean,_tmpUthmani,_tmpEn_transilation,_tmpEn_pickthall,_tmpFa_khorramdel,_tmpKu_asan,_tmpNote,_tmpFav);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public QuranEntity getVerseByIndex(final int index) {
    final String _sql = "SELECT * FROM quran_all WHERE `index`=?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, index);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "index");
      final int _cursorIndexOfSura = CursorUtil.getColumnIndexOrThrow(_cursor, "sura");
      final int _cursorIndexOfAya = CursorUtil.getColumnIndexOrThrow(_cursor, "aya");
      final int _cursorIndexOfSimple = CursorUtil.getColumnIndexOrThrow(_cursor, "simple");
      final int _cursorIndexOfSimpleClean = CursorUtil.getColumnIndexOrThrow(_cursor, "simple_clean");
      final int _cursorIndexOfUthmani = CursorUtil.getColumnIndexOrThrow(_cursor, "uthmani");
      final int _cursorIndexOfEnTransilation = CursorUtil.getColumnIndexOrThrow(_cursor, "en_transilation");
      final int _cursorIndexOfEnPickthall = CursorUtil.getColumnIndexOrThrow(_cursor, "en_pickthall");
      final int _cursorIndexOfFaKhorramdel = CursorUtil.getColumnIndexOrThrow(_cursor, "fa_khorramdel");
      final int _cursorIndexOfKuAsan = CursorUtil.getColumnIndexOrThrow(_cursor, "ku_asan");
      final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
      final int _cursorIndexOfFav = CursorUtil.getColumnIndexOrThrow(_cursor, "fav");
      final QuranEntity _result;
      if(_cursor.moveToFirst()) {
        final int _tmpIndex;
        _tmpIndex = _cursor.getInt(_cursorIndexOfIndex);
        final Integer _tmpSura;
        if (_cursor.isNull(_cursorIndexOfSura)) {
          _tmpSura = null;
        } else {
          _tmpSura = _cursor.getInt(_cursorIndexOfSura);
        }
        final Integer _tmpAya;
        if (_cursor.isNull(_cursorIndexOfAya)) {
          _tmpAya = null;
        } else {
          _tmpAya = _cursor.getInt(_cursorIndexOfAya);
        }
        final String _tmpSimple;
        if (_cursor.isNull(_cursorIndexOfSimple)) {
          _tmpSimple = null;
        } else {
          _tmpSimple = _cursor.getString(_cursorIndexOfSimple);
        }
        final String _tmpSimple_clean;
        if (_cursor.isNull(_cursorIndexOfSimpleClean)) {
          _tmpSimple_clean = null;
        } else {
          _tmpSimple_clean = _cursor.getString(_cursorIndexOfSimpleClean);
        }
        final String _tmpUthmani;
        if (_cursor.isNull(_cursorIndexOfUthmani)) {
          _tmpUthmani = null;
        } else {
          _tmpUthmani = _cursor.getString(_cursorIndexOfUthmani);
        }
        final String _tmpEn_transilation;
        if (_cursor.isNull(_cursorIndexOfEnTransilation)) {
          _tmpEn_transilation = null;
        } else {
          _tmpEn_transilation = _cursor.getString(_cursorIndexOfEnTransilation);
        }
        final String _tmpEn_pickthall;
        if (_cursor.isNull(_cursorIndexOfEnPickthall)) {
          _tmpEn_pickthall = null;
        } else {
          _tmpEn_pickthall = _cursor.getString(_cursorIndexOfEnPickthall);
        }
        final String _tmpFa_khorramdel;
        if (_cursor.isNull(_cursorIndexOfFaKhorramdel)) {
          _tmpFa_khorramdel = null;
        } else {
          _tmpFa_khorramdel = _cursor.getString(_cursorIndexOfFaKhorramdel);
        }
        final String _tmpKu_asan;
        if (_cursor.isNull(_cursorIndexOfKuAsan)) {
          _tmpKu_asan = null;
        } else {
          _tmpKu_asan = _cursor.getString(_cursorIndexOfKuAsan);
        }
        final String _tmpNote;
        if (_cursor.isNull(_cursorIndexOfNote)) {
          _tmpNote = null;
        } else {
          _tmpNote = _cursor.getString(_cursorIndexOfNote);
        }
        final Integer _tmpFav;
        if (_cursor.isNull(_cursorIndexOfFav)) {
          _tmpFav = null;
        } else {
          _tmpFav = _cursor.getInt(_cursorIndexOfFav);
        }
        _result = new QuranEntity(_tmpIndex,_tmpSura,_tmpAya,_tmpSimple,_tmpSimple_clean,_tmpUthmani,_tmpEn_transilation,_tmpEn_pickthall,_tmpFa_khorramdel,_tmpKu_asan,_tmpNote,_tmpFav);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<QuranEntity> getAllBookmarks() {
    final String _sql = "SELECT * FROM quran_all WHERE fav==1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "index");
      final int _cursorIndexOfSura = CursorUtil.getColumnIndexOrThrow(_cursor, "sura");
      final int _cursorIndexOfAya = CursorUtil.getColumnIndexOrThrow(_cursor, "aya");
      final int _cursorIndexOfSimple = CursorUtil.getColumnIndexOrThrow(_cursor, "simple");
      final int _cursorIndexOfSimpleClean = CursorUtil.getColumnIndexOrThrow(_cursor, "simple_clean");
      final int _cursorIndexOfUthmani = CursorUtil.getColumnIndexOrThrow(_cursor, "uthmani");
      final int _cursorIndexOfEnTransilation = CursorUtil.getColumnIndexOrThrow(_cursor, "en_transilation");
      final int _cursorIndexOfEnPickthall = CursorUtil.getColumnIndexOrThrow(_cursor, "en_pickthall");
      final int _cursorIndexOfFaKhorramdel = CursorUtil.getColumnIndexOrThrow(_cursor, "fa_khorramdel");
      final int _cursorIndexOfKuAsan = CursorUtil.getColumnIndexOrThrow(_cursor, "ku_asan");
      final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
      final int _cursorIndexOfFav = CursorUtil.getColumnIndexOrThrow(_cursor, "fav");
      final List<QuranEntity> _result = new ArrayList<QuranEntity>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final QuranEntity _item;
        final int _tmpIndex;
        _tmpIndex = _cursor.getInt(_cursorIndexOfIndex);
        final Integer _tmpSura;
        if (_cursor.isNull(_cursorIndexOfSura)) {
          _tmpSura = null;
        } else {
          _tmpSura = _cursor.getInt(_cursorIndexOfSura);
        }
        final Integer _tmpAya;
        if (_cursor.isNull(_cursorIndexOfAya)) {
          _tmpAya = null;
        } else {
          _tmpAya = _cursor.getInt(_cursorIndexOfAya);
        }
        final String _tmpSimple;
        if (_cursor.isNull(_cursorIndexOfSimple)) {
          _tmpSimple = null;
        } else {
          _tmpSimple = _cursor.getString(_cursorIndexOfSimple);
        }
        final String _tmpSimple_clean;
        if (_cursor.isNull(_cursorIndexOfSimpleClean)) {
          _tmpSimple_clean = null;
        } else {
          _tmpSimple_clean = _cursor.getString(_cursorIndexOfSimpleClean);
        }
        final String _tmpUthmani;
        if (_cursor.isNull(_cursorIndexOfUthmani)) {
          _tmpUthmani = null;
        } else {
          _tmpUthmani = _cursor.getString(_cursorIndexOfUthmani);
        }
        final String _tmpEn_transilation;
        if (_cursor.isNull(_cursorIndexOfEnTransilation)) {
          _tmpEn_transilation = null;
        } else {
          _tmpEn_transilation = _cursor.getString(_cursorIndexOfEnTransilation);
        }
        final String _tmpEn_pickthall;
        if (_cursor.isNull(_cursorIndexOfEnPickthall)) {
          _tmpEn_pickthall = null;
        } else {
          _tmpEn_pickthall = _cursor.getString(_cursorIndexOfEnPickthall);
        }
        final String _tmpFa_khorramdel;
        if (_cursor.isNull(_cursorIndexOfFaKhorramdel)) {
          _tmpFa_khorramdel = null;
        } else {
          _tmpFa_khorramdel = _cursor.getString(_cursorIndexOfFaKhorramdel);
        }
        final String _tmpKu_asan;
        if (_cursor.isNull(_cursorIndexOfKuAsan)) {
          _tmpKu_asan = null;
        } else {
          _tmpKu_asan = _cursor.getString(_cursorIndexOfKuAsan);
        }
        final String _tmpNote;
        if (_cursor.isNull(_cursorIndexOfNote)) {
          _tmpNote = null;
        } else {
          _tmpNote = _cursor.getString(_cursorIndexOfNote);
        }
        final Integer _tmpFav;
        if (_cursor.isNull(_cursorIndexOfFav)) {
          _tmpFav = null;
        } else {
          _tmpFav = _cursor.getInt(_cursorIndexOfFav);
        }
        _item = new QuranEntity(_tmpIndex,_tmpSura,_tmpAya,_tmpSimple,_tmpSimple_clean,_tmpUthmani,_tmpEn_transilation,_tmpEn_pickthall,_tmpFa_khorramdel,_tmpKu_asan,_tmpNote,_tmpFav);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<QuranEntity> getAllNotes() {
    final String _sql = "SELECT * FROM quran_all WHERE note!='-'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "index");
      final int _cursorIndexOfSura = CursorUtil.getColumnIndexOrThrow(_cursor, "sura");
      final int _cursorIndexOfAya = CursorUtil.getColumnIndexOrThrow(_cursor, "aya");
      final int _cursorIndexOfSimple = CursorUtil.getColumnIndexOrThrow(_cursor, "simple");
      final int _cursorIndexOfSimpleClean = CursorUtil.getColumnIndexOrThrow(_cursor, "simple_clean");
      final int _cursorIndexOfUthmani = CursorUtil.getColumnIndexOrThrow(_cursor, "uthmani");
      final int _cursorIndexOfEnTransilation = CursorUtil.getColumnIndexOrThrow(_cursor, "en_transilation");
      final int _cursorIndexOfEnPickthall = CursorUtil.getColumnIndexOrThrow(_cursor, "en_pickthall");
      final int _cursorIndexOfFaKhorramdel = CursorUtil.getColumnIndexOrThrow(_cursor, "fa_khorramdel");
      final int _cursorIndexOfKuAsan = CursorUtil.getColumnIndexOrThrow(_cursor, "ku_asan");
      final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
      final int _cursorIndexOfFav = CursorUtil.getColumnIndexOrThrow(_cursor, "fav");
      final List<QuranEntity> _result = new ArrayList<QuranEntity>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final QuranEntity _item;
        final int _tmpIndex;
        _tmpIndex = _cursor.getInt(_cursorIndexOfIndex);
        final Integer _tmpSura;
        if (_cursor.isNull(_cursorIndexOfSura)) {
          _tmpSura = null;
        } else {
          _tmpSura = _cursor.getInt(_cursorIndexOfSura);
        }
        final Integer _tmpAya;
        if (_cursor.isNull(_cursorIndexOfAya)) {
          _tmpAya = null;
        } else {
          _tmpAya = _cursor.getInt(_cursorIndexOfAya);
        }
        final String _tmpSimple;
        if (_cursor.isNull(_cursorIndexOfSimple)) {
          _tmpSimple = null;
        } else {
          _tmpSimple = _cursor.getString(_cursorIndexOfSimple);
        }
        final String _tmpSimple_clean;
        if (_cursor.isNull(_cursorIndexOfSimpleClean)) {
          _tmpSimple_clean = null;
        } else {
          _tmpSimple_clean = _cursor.getString(_cursorIndexOfSimpleClean);
        }
        final String _tmpUthmani;
        if (_cursor.isNull(_cursorIndexOfUthmani)) {
          _tmpUthmani = null;
        } else {
          _tmpUthmani = _cursor.getString(_cursorIndexOfUthmani);
        }
        final String _tmpEn_transilation;
        if (_cursor.isNull(_cursorIndexOfEnTransilation)) {
          _tmpEn_transilation = null;
        } else {
          _tmpEn_transilation = _cursor.getString(_cursorIndexOfEnTransilation);
        }
        final String _tmpEn_pickthall;
        if (_cursor.isNull(_cursorIndexOfEnPickthall)) {
          _tmpEn_pickthall = null;
        } else {
          _tmpEn_pickthall = _cursor.getString(_cursorIndexOfEnPickthall);
        }
        final String _tmpFa_khorramdel;
        if (_cursor.isNull(_cursorIndexOfFaKhorramdel)) {
          _tmpFa_khorramdel = null;
        } else {
          _tmpFa_khorramdel = _cursor.getString(_cursorIndexOfFaKhorramdel);
        }
        final String _tmpKu_asan;
        if (_cursor.isNull(_cursorIndexOfKuAsan)) {
          _tmpKu_asan = null;
        } else {
          _tmpKu_asan = _cursor.getString(_cursorIndexOfKuAsan);
        }
        final String _tmpNote;
        if (_cursor.isNull(_cursorIndexOfNote)) {
          _tmpNote = null;
        } else {
          _tmpNote = _cursor.getString(_cursorIndexOfNote);
        }
        final Integer _tmpFav;
        if (_cursor.isNull(_cursorIndexOfFav)) {
          _tmpFav = null;
        } else {
          _tmpFav = _cursor.getInt(_cursorIndexOfFav);
        }
        _item = new QuranEntity(_tmpIndex,_tmpSura,_tmpAya,_tmpSimple,_tmpSimple_clean,_tmpUthmani,_tmpEn_transilation,_tmpEn_pickthall,_tmpFa_khorramdel,_tmpKu_asan,_tmpNote,_tmpFav);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<QuranEntity> getAll() {
    final String _sql = "SELECT * FROM quran_all";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "index");
      final int _cursorIndexOfSura = CursorUtil.getColumnIndexOrThrow(_cursor, "sura");
      final int _cursorIndexOfAya = CursorUtil.getColumnIndexOrThrow(_cursor, "aya");
      final int _cursorIndexOfSimple = CursorUtil.getColumnIndexOrThrow(_cursor, "simple");
      final int _cursorIndexOfSimpleClean = CursorUtil.getColumnIndexOrThrow(_cursor, "simple_clean");
      final int _cursorIndexOfUthmani = CursorUtil.getColumnIndexOrThrow(_cursor, "uthmani");
      final int _cursorIndexOfEnTransilation = CursorUtil.getColumnIndexOrThrow(_cursor, "en_transilation");
      final int _cursorIndexOfEnPickthall = CursorUtil.getColumnIndexOrThrow(_cursor, "en_pickthall");
      final int _cursorIndexOfFaKhorramdel = CursorUtil.getColumnIndexOrThrow(_cursor, "fa_khorramdel");
      final int _cursorIndexOfKuAsan = CursorUtil.getColumnIndexOrThrow(_cursor, "ku_asan");
      final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
      final int _cursorIndexOfFav = CursorUtil.getColumnIndexOrThrow(_cursor, "fav");
      final List<QuranEntity> _result = new ArrayList<QuranEntity>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final QuranEntity _item;
        final int _tmpIndex;
        _tmpIndex = _cursor.getInt(_cursorIndexOfIndex);
        final Integer _tmpSura;
        if (_cursor.isNull(_cursorIndexOfSura)) {
          _tmpSura = null;
        } else {
          _tmpSura = _cursor.getInt(_cursorIndexOfSura);
        }
        final Integer _tmpAya;
        if (_cursor.isNull(_cursorIndexOfAya)) {
          _tmpAya = null;
        } else {
          _tmpAya = _cursor.getInt(_cursorIndexOfAya);
        }
        final String _tmpSimple;
        if (_cursor.isNull(_cursorIndexOfSimple)) {
          _tmpSimple = null;
        } else {
          _tmpSimple = _cursor.getString(_cursorIndexOfSimple);
        }
        final String _tmpSimple_clean;
        if (_cursor.isNull(_cursorIndexOfSimpleClean)) {
          _tmpSimple_clean = null;
        } else {
          _tmpSimple_clean = _cursor.getString(_cursorIndexOfSimpleClean);
        }
        final String _tmpUthmani;
        if (_cursor.isNull(_cursorIndexOfUthmani)) {
          _tmpUthmani = null;
        } else {
          _tmpUthmani = _cursor.getString(_cursorIndexOfUthmani);
        }
        final String _tmpEn_transilation;
        if (_cursor.isNull(_cursorIndexOfEnTransilation)) {
          _tmpEn_transilation = null;
        } else {
          _tmpEn_transilation = _cursor.getString(_cursorIndexOfEnTransilation);
        }
        final String _tmpEn_pickthall;
        if (_cursor.isNull(_cursorIndexOfEnPickthall)) {
          _tmpEn_pickthall = null;
        } else {
          _tmpEn_pickthall = _cursor.getString(_cursorIndexOfEnPickthall);
        }
        final String _tmpFa_khorramdel;
        if (_cursor.isNull(_cursorIndexOfFaKhorramdel)) {
          _tmpFa_khorramdel = null;
        } else {
          _tmpFa_khorramdel = _cursor.getString(_cursorIndexOfFaKhorramdel);
        }
        final String _tmpKu_asan;
        if (_cursor.isNull(_cursorIndexOfKuAsan)) {
          _tmpKu_asan = null;
        } else {
          _tmpKu_asan = _cursor.getString(_cursorIndexOfKuAsan);
        }
        final String _tmpNote;
        if (_cursor.isNull(_cursorIndexOfNote)) {
          _tmpNote = null;
        } else {
          _tmpNote = _cursor.getString(_cursorIndexOfNote);
        }
        final Integer _tmpFav;
        if (_cursor.isNull(_cursorIndexOfFav)) {
          _tmpFav = null;
        } else {
          _tmpFav = _cursor.getInt(_cursorIndexOfFav);
        }
        _item = new QuranEntity(_tmpIndex,_tmpSura,_tmpAya,_tmpSimple,_tmpSimple_clean,_tmpUthmani,_tmpEn_transilation,_tmpEn_pickthall,_tmpFa_khorramdel,_tmpKu_asan,_tmpNote,_tmpFav);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
