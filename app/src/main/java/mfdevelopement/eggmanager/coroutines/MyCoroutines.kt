package mfdevelopement.eggmanager.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import kotlinx.coroutines.withContext

class MyCoroutines {

    companion object {

        /**
         * Helper function to use Kotlin coroutine to run a function in another thread
         */
        fun doAsync(func: () -> Unit, dispatcher: CoroutineDispatcher = Dispatchers.Default) =
                GlobalScope.future {

                    // Move the execution of the coroutine to the I/O dispatcher
                    withContext(dispatcher) {
                        // Call the function
                        func()
                    }
                }
    }
}