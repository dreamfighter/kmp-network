import id.dreamfighter.multiplatform.api.ApiRequest
import id.dreamfighter.multiplatform.api.Transaction
import id.dreamfighter.multiplatform.api.client
import id.dreamfighter.multiplatform.api.req
import id.dreamfighter.multiplatform.api.setBaseUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import platform.posix.printf
import kotlin.reflect.ExperimentalAssociatedObjects
import kotlin.reflect.findAssociatedObject
import kotlin.test.Test

class IosHttpClientTest {
    private val scope = CoroutineScope(Dispatchers.Default)
    @kotlinx.cinterop.ExperimentalForeignApi
    @Test
    fun `test 3rd element`() {
        //ApiUtils.getPropertyWithObj(Transaction(1))
        //print("associated: ${Transaction(1)::class.findAssociatedObject<Get>()}")
        //assertEquals(7, fibi.take(3).last())
        client.setBaseUrl("http://127.0.0.1:3000")
        scope.launch {
            val result = req<Transaction>(request = ApiRequest.Transaction(10))
            println(result)
        }


    }
}