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
public final class ChaptersDao_Impl implements ChaptersDao {
  private final RoomDatabase __db;

  private final EntityDeletionOrUpdateAdapter<ChapterEntity> __updateAdapterOfChapterEntity;

  public ChaptersDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__updateAdapterOfChapterEntity = new EntityDeletionOrUpdateAdapter<ChapterEntity>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE OR ABORT `chapters` SET `sura` = ?,`ayas_count` = ?,`first_aya_id` = ?,`name_arabic` = ?,`name_transliteration` = ?,`type` = ?,`revelation_order` = ?,`rukus` = ?,`bismillah` = ?,`fav` = ? WHERE `sura` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, ChapterEntity value) {
        stmt.bindLong(1, value.getSura());
        if (value.getAyaCount() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindLong(2, value.getAyaCount());
        }
        if (value.getFirstAyaId() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindLong(3, value.getFirstAyaId());
        }
        if (value.getNameArabic() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getNameArabic());
        }
        if (value.getNameTransliteration() == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, value.getNameTransliteration());
        }
        if (value.getType() == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, value.getType());
        }
        if (value.getRevelationOrder() == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindLong(7, value.getRevelationOrder());
        }
        if (value.getRukus() == null) {
          stmt.bindNull(8);
        } else {
          stmt.bindLong(8, value.getRukus());
        }
        if (value.getBismillah() == null) {
          stmt.bindNull(9);
        } else {
          stmt.bindLong(9, value.getBismillah());
        }
        if (value.getFav() == null) {
          stmt.bindNull(10);
        } else {
          stmt.bindLong(10, value.getFav());
        }
        stmt.bindLong(11, value.getSura());
      }
    };
  }

  @Override
  public void update(final ChapterEntity chaptersEntity) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfChapterEntity.handle(chaptersEntity);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<ChapterEntity> getAllChapters() {
    final String _sql = "SELECT * FROM chapters";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfSura = CursorUtil.getColumnIndexOrThrow(_cursor, "sura");
      final int _cursorIndexOfAyaCount = CursorUtil.getColumnIndexOrThrow(_cursor, "ayas_count");
      final int _cursorIndexOfFirstAyaId = CursorUtil.getColumnIndexOrThrow(_cursor, "first_aya_id");
      final int _cursorIndexOfNameArabic = CursorUtil.getColumnIndexOrThrow(_cursor, "name_arabic");
      final int _cursorIndexOfNameTransliteration = CursorUtil.getColumnIndexOrThrow(_cursor, "name_transliteration");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final int _cursorIndexOfRevelationOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "revelation_order");
      final int _cursorIndexOfRukus = CursorUtil.getColumnIndexOrThrow(_cursor, "rukus");
      final int _cursorIndexOfBismillah = CursorUtil.getColumnIndexOrThrow(_cursor, "bismillah");
      final int _cursorIndexOfFav = CursorUtil.getColumnIndexOrThrow(_cursor, "fav");
      final List<ChapterEntity> _result = new ArrayList<ChapterEntity>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final ChapterEntity _item;
        final int _tmpSura;
        _tmpSura = _cursor.getInt(_cursorIndexOfSura);
        final Integer _tmpAyaCount;
        if (_cursor.isNull(_cursorIndexOfAyaCount)) {
          _tmpAyaCount = null;
        } else {
          _tmpAyaCount = _cursor.getInt(_cursorIndexOfAyaCount);
        }
        final Integer _tmpFirstAyaId;
        if (_cursor.isNull(_cursorIndexOfFirstAyaId)) {
          _tmpFirstAyaId = null;
        } else {
          _tmpFirstAyaId = _cursor.getInt(_cursorIndexOfFirstAyaId);
        }
        final String _tmpNameArabic;
        if (_cursor.isNull(_cursorIndexOfNameArabic)) {
          _tmpNameArabic = null;
        } else {
          _tmpNameArabic = _cursor.getString(_cursorIndexOfNameArabic);
        }
        final String _tmpNameTransliteration;
        if (_cursor.isNull(_cursorIndexOfNameTransliteration)) {
          _tmpNameTransliteration = null;
        } else {
          _tmpNameTransliteration = _cursor.getString(_cursorIndexOfNameTransliteration);
        }
        final String _tmpType;
        if (_cursor.isNull(_cursorIndexOfType)) {
          _tmpType = null;
        } else {
          _tmpType = _cursor.getString(_cursorIndexOfType);
        }
        final Integer _tmpRevelationOrder;
        if (_cursor.isNull(_cursorIndexOfRevelationOrder)) {
          _tmpRevelationOrder = null;
        } else {
          _tmpRevelationOrder = _cursor.getInt(_cursorIndexOfRevelationOrder);
        }
        final Integer _tmpRukus;
        if (_cursor.isNull(_cursorIndexOfRukus)) {
          _tmpRukus = null;
        } else {
          _tmpRukus = _cursor.getInt(_cursorIndexOfRukus);
        }
        final Integer _tmpBismillah;
        if (_cursor.isNull(_cursorIndexOfBismillah)) {
          _tmpBismillah = null;
        } else {
          _tmpBismillah = _cursor.getInt(_cursorIndexOfBismillah);
        }
        final Integer _tmpFav;
        if (_cursor.isNull(_cursorIndexOfFav)) {
          _tmpFav = null;
        } else {
          _tmpFav = _cursor.getInt(_cursorIndexOfFav);
        }
        _item = new ChapterEntity(_tmpSura,_tmpAyaCount,_tmpFirstAyaId,_tmpNameArabic,_tmpNameTransliteration,_tmpType,_tmpRevelationOrder,_tmpRukus,_tmpBismillah,_tmpFav);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public ChapterEntity getChapter(final int sura) {
    final String _sql = "SELECT * FROM chapters where sura=?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sura);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfSura = CursorUtil.getColumnIndexOrThrow(_cursor, "sura");
      final int _cursorIndexOfAyaCount = CursorUtil.getColumnIndexOrThrow(_cursor, "ayas_count");
      final int _cursorIndexOfFirstAyaId = CursorUtil.getColumnIndexOrThrow(_cursor, "first_aya_id");
      final int _cursorIndexOfNameArabic = CursorUtil.getColumnIndexOrThrow(_cursor, "name_arabic");
      final int _cursorIndexOfNameTransliteration = CursorUtil.getColumnIndexOrThrow(_cursor, "name_transliteration");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final int _cursorIndexOfRevelationOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "revelation_order");
      final int _cursorIndexOfRukus = CursorUtil.getColumnIndexOrThrow(_cursor, "rukus");
      final int _cursorIndexOfBismillah = CursorUtil.getColumnIndexOrThrow(_cursor, "bismillah");
      final int _cursorIndexOfFav = CursorUtil.getColumnIndexOrThrow(_cursor, "fav");
      final ChapterEntity _result;
      if(_cursor.moveToFirst()) {
        final int _tmpSura;
        _tmpSura = _cursor.getInt(_cursorIndexOfSura);
        final Integer _tmpAyaCount;
        if (_cursor.isNull(_cursorIndexOfAyaCount)) {
          _tmpAyaCount = null;
        } else {
          _tmpAyaCount = _cursor.getInt(_cursorIndexOfAyaCount);
        }
        final Integer _tmpFirstAyaId;
        if (_cursor.isNull(_cursorIndexOfFirstAyaId)) {
          _tmpFirstAyaId = null;
        } else {
          _tmpFirstAyaId = _cursor.getInt(_cursorIndexOfFirstAyaId);
        }
        final String _tmpNameArabic;
        if (_cursor.isNull(_cursorIndexOfNameArabic)) {
          _tmpNameArabic = null;
        } else {
          _tmpNameArabic = _cursor.getString(_cursorIndexOfNameArabic);
        }
        final String _tmpNameTransliteration;
        if (_cursor.isNull(_cursorIndexOfNameTransliteration)) {
          _tmpNameTransliteration = null;
        } else {
          _tmpNameTransliteration = _cursor.getString(_cursorIndexOfNameTransliteration);
        }
        final String _tmpType;
        if (_cursor.isNull(_cursorIndexOfType)) {
          _tmpType = null;
        } else {
          _tmpType = _cursor.getString(_cursorIndexOfType);
        }
        final Integer _tmpRevelationOrder;
        if (_cursor.isNull(_cursorIndexOfRevelationOrder)) {
          _tmpRevelationOrder = null;
        } else {
          _tmpRevelationOrder = _cursor.getInt(_cursorIndexOfRevelationOrder);
        }
        final Integer _tmpRukus;
        if (_cursor.isNull(_cursorIndexOfRukus)) {
          _tmpRukus = null;
        } else {
          _tmpRukus = _cursor.getInt(_cursorIndexOfRukus);
        }
        final Integer _tmpBismillah;
        if (_cursor.isNull(_cursorIndexOfBismillah)) {
          _tmpBismillah = null;
        } else {
          _tmpBismillah = _cursor.getInt(_cursorIndexOfBismillah);
        }
        final Integer _tmpFav;
        if (_cursor.isNull(_cursorIndexOfFav)) {
          _tmpFav = null;
        } else {
          _tmpFav = _cursor.getInt(_cursorIndexOfFav);
        }
        _result = new ChapterEntity(_tmpSura,_tmpAyaCount,_tmpFirstAyaId,_tmpNameArabic,_tmpNameTransliteration,_tmpType,_tmpRevelationOrder,_tmpRukus,_tmpBismillah,_tmpFav);
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
  public ChapterEntity getChapter1(final int sura) {
    final String _sql = "SELECT * FROM chapters where sura=?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sura);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfSura = CursorUtil.getColumnIndexOrThrow(_cursor, "sura");
      final int _cursorIndexOfAyaCount = CursorUtil.getColumnIndexOrThrow(_cursor, "ayas_count");
      final int _cursorIndexOfFirstAyaId = CursorUtil.getColumnIndexOrThrow(_cursor, "first_aya_id");
      final int _cursorIndexOfNameArabic = CursorUtil.getColumnIndexOrThrow(_cursor, "name_arabic");
      final int _cursorIndexOfNameTransliteration = CursorUtil.getColumnIndexOrThrow(_cursor, "name_transliteration");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final int _cursorIndexOfRevelationOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "revelation_order");
      final int _cursorIndexOfRukus = CursorUtil.getColumnIndexOrThrow(_cursor, "rukus");
      final int _cursorIndexOfBismillah = CursorUtil.getColumnIndexOrThrow(_cursor, "bismillah");
      final int _cursorIndexOfFav = CursorUtil.getColumnIndexOrThrow(_cursor, "fav");
      final ChapterEntity _result;
      if(_cursor.moveToFirst()) {
        final int _tmpSura;
        _tmpSura = _cursor.getInt(_cursorIndexOfSura);
        final Integer _tmpAyaCount;
        if (_cursor.isNull(_cursorIndexOfAyaCount)) {
          _tmpAyaCount = null;
        } else {
          _tmpAyaCount = _cursor.getInt(_cursorIndexOfAyaCount);
        }
        final Integer _tmpFirstAyaId;
        if (_cursor.isNull(_cursorIndexOfFirstAyaId)) {
          _tmpFirstAyaId = null;
        } else {
          _tmpFirstAyaId = _cursor.getInt(_cursorIndexOfFirstAyaId);
        }
        final String _tmpNameArabic;
        if (_cursor.isNull(_cursorIndexOfNameArabic)) {
          _tmpNameArabic = null;
        } else {
          _tmpNameArabic = _cursor.getString(_cursorIndexOfNameArabic);
        }
        final String _tmpNameTransliteration;
        if (_cursor.isNull(_cursorIndexOfNameTransliteration)) {
          _tmpNameTransliteration = null;
        } else {
          _tmpNameTransliteration = _cursor.getString(_cursorIndexOfNameTransliteration);
        }
        final String _tmpType;
        if (_cursor.isNull(_cursorIndexOfType)) {
          _tmpType = null;
        } else {
          _tmpType = _cursor.getString(_cursorIndexOfType);
        }
        final Integer _tmpRevelationOrder;
        if (_cursor.isNull(_cursorIndexOfRevelationOrder)) {
          _tmpRevelationOrder = null;
        } else {
          _tmpRevelationOrder = _cursor.getInt(_cursorIndexOfRevelationOrder);
        }
        final Integer _tmpRukus;
        if (_cursor.isNull(_cursorIndexOfRukus)) {
          _tmpRukus = null;
        } else {
          _tmpRukus = _cursor.getInt(_cursorIndexOfRukus);
        }
        final Integer _tmpBismillah;
        if (_cursor.isNull(_cursorIndexOfBismillah)) {
          _tmpBismillah = null;
        } else {
          _tmpBismillah = _cursor.getInt(_cursorIndexOfBismillah);
        }
        final Integer _tmpFav;
        if (_cursor.isNull(_cursorIndexOfFav)) {
          _tmpFav = null;
        } else {
          _tmpFav = _cursor.getInt(_cursorIndexOfFav);
        }
        _result = new ChapterEntity(_tmpSura,_tmpAyaCount,_tmpFirstAyaId,_tmpNameArabic,_tmpNameTransliteration,_tmpType,_tmpRevelationOrder,_tmpRukus,_tmpBismillah,_tmpFav);
      } else {
        _result = null;
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
