package mfdevelopement.eggmanager.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import kotlinx.coroutines.withContext

class MyCoroutines {

    companion object {
//
//        *
//         * Helper function to use Kotlin coroutine to execute a task in the background
//        fun <T> doAsync(func: () -> Void, dispatcher: CoroutineDispatcher = Dispatchers.Default) : CompletableFuture<Void> =
//                GlobalScope.future {
//
//                    // Move the execution of the coroutine a dispatcher
//                    withContext(dispatcher) {
//                        // Call the function
//                        func()
//                    }
//                }

        /**
         * Helper function to use Kotlin coroutine to execute a task in the background
         */
        fun doAsync(func: () -> Unit, dispatcher: CoroutineDispatcher = Dispatchers.Default) =
            GlobalScope.future {

                // Move the execution of the coroutine a dispatcher
                withContext(dispatcher) {
                    // Call the function
                    func()
                }
            }
    }
}