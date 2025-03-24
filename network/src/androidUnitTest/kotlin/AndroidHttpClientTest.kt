import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.test.Test

class AndroidHttpClientTest {

    @Test
    fun testApi(){
        MainScope().launch(Dispatchers.Default) {
            Log.d("LOGGING","test")

        }

    }
}