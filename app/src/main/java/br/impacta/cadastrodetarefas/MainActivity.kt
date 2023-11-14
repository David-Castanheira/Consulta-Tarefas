package br.impacta.cadastrodetarefas

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import br.impacta.cadastrodetarefas.ui.theme.CadastroDeTarefasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CadastroDeTarefasTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var dbHelper = TaskDBHelper(this)
                    CRUDFunction(dbHelper)
                }
            }
        }
    }
}

@Composable
fun CRUDFunction(db: TaskDBHelper?) {
    if(db != null) {
        Column() {
            db.addTasks(Task(-1, "Exemplo para AC5", "Consumo e alimentação do BD de usuários"))
            db.updateTask(Task(2, "Outro exemplo", "Consumo do ViaCep"))
            db.deleteTask(4)
            var lista = db.getAllTasks()
            for(item in lista) {
                Text(text = "${item.id} - ${item.titulo} - ${item.valorDescricao}")
            }
        }
        Text(text = "")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CadastroDeTarefasTheme {
        CRUDFunction(null)
    }
}

data class Task (val id: Long, val titulo: String, val valorDescricao: String)

class TaskDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "task.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "task"
        const val COLUMN_ID = "id"
        const val COLUMN_TITULO = "titulo"
        const val COLUMN_DESCRICAO = "descricao"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_TITULO TEXT, $COLUMN_DESCRICAO TEXT)"
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
    }

    fun addTasks(task: Task) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_TITULO, task.titulo)
        values.put(COLUMN_DESCRICAO, task.valorDescricao)
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getAllTasks() : List<Task> {
        val tasks = mutableListOf<Task>()
        val query = "SELECT * FROM $TABLE_NAME"
        val db = this.readableDatabase
        val cursor = db.rawQuery(query, null)

        if(cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val titulo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITULO))
                val descricao = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRICAO))
                tasks.add(Task(id, titulo, descricao))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return tasks
    }

    fun getSingle(taskId : Long) : Task {
        var tasks = Task(-1, "", "")
        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = $taskId"
        val db = this.readableDatabase
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val titulo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITULO))
            val descricao = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRICAO))
            tasks = Task(id, titulo, descricao)
        }

        cursor.close()
        db.close()
        return tasks
    }


    fun updateTask(task: Task) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_TITULO, task.titulo)
        values.put(COLUMN_DESCRICAO, task.valorDescricao)
        db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(task.id.toString()))
        db.close()
    }

    fun deleteTask(taskId: Long) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(taskId.toString()))
        db.close()
    }
}