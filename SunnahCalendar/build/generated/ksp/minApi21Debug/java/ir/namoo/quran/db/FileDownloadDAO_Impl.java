package ir.namoo.quran.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
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
public final class FileDownloadDAO_Impl implements FileDownloadDAO {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<FileDownloadEntity> __insertionAdapterOfFileDownloadEntity;

  private final SharedSQLiteStatement __preparedStmtOfRemoveByFileId;

  public FileDownloadDAO_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfFileDownloadEntity = new EntityInsertionAdapter<FileDownloadEntity>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `FileDownloadEntity` (`id`,`downloadRequest`,`downloadFile`,`folderPath`,`position`) VALUES (?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, FileDownloadEntity value) {
        stmt.bindLong(1, value.getId());
        stmt.bindLong(2, value.getDownloadRequest());
        if (value.getDownloadFile() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getDownloadFile());
        }
        if (value.getFolderPath() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getFolderPath());
        }
        stmt.bindLong(5, value.getPosition());
      }
    };
    this.__preparedStmtOfRemoveByFileId = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM FileDownloadEntity WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final FileDownloadEntity fileDownloadEntity,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfFileDownloadEntity.insert(fileDownloadEntity);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object removeByFileId(final int id, final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRemoveByFileId.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
          __preparedStmtOfRemoveByFileId.release(_stmt);
        }
      }
    }, continuation);
  }

  @Override
  public Object findAllDownloads(
      final Continuation<? super List<FileDownloadEntity>> continuation) {
    final String _sql = "SELECT * FROM FileDownloadEntity";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<FileDownloadEntity>>() {
      @Override
      public List<FileDownloadEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDownloadRequest = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadRequest");
          final int _cursorIndexOfDownloadFile = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadFile");
          final int _cursorIndexOfFolderPath = CursorUtil.getColumnIndexOrThrow(_cursor, "folderPath");
          final int _cursorIndexOfPosition = CursorUtil.getColumnIndexOrThrow(_cursor, "position");
          final List<FileDownloadEntity> _result = new ArrayList<FileDownloadEntity>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final FileDownloadEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final long _tmpDownloadRequest;
            _tmpDownloadRequest = _cursor.getLong(_cursorIndexOfDownloadRequest);
            final String _tmpDownloadFile;
            if (_cursor.isNull(_cursorIndexOfDownloadFile)) {
              _tmpDownloadFile = null;
            } else {
              _tmpDownloadFile = _cursor.getString(_cursorIndexOfDownloadFile);
            }
            final String _tmpFolderPath;
            if (_cursor.isNull(_cursorIndexOfFolderPath)) {
              _tmpFolderPath = null;
            } else {
              _tmpFolderPath = _cursor.getString(_cursorIndexOfFolderPath);
            }
            final int _tmpPosition;
            _tmpPosition = _cursor.getInt(_cursorIndexOfPosition);
            _item = new FileDownloadEntity(_tmpId,_tmpDownloadRequest,_tmpDownloadFile,_tmpFolderPath,_tmpPosition);
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
