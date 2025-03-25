import id.dreamfighter.kmp.network.Api
import id.dreamfighter.kmp.network.flowReq
import id.dreamfighter.kmp.network.req
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.After
import kotlin.test.Test

class JvmHttpClientTest {
    private val scope = CoroutineScope(Dispatchers.Default)
    @Test
    fun `test 3rd element`(): Unit = runBlocking {

        //client.setBaseUrl("http://127.0.0.1:3000")
        //val result = flowReq(Api.getProfile(20, mapOf()))
        Api.getProfile(20, mapOf())
        //println(result)
    }
    @After
    fun tearDown() {
        scope.cancel()
    }
}