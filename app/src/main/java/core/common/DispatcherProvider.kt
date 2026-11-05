
package com.flagquiz.app.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * DispatcherProvider: The Thread Traffic Cop.
 *
 * Phones have multiple CPU cores (threads):
 * - Main thread: Only paints pixels on the screen (must never be blocked).
 * - IO thread: Does heavy lifting like reading databases and web requests.
 * By using this interface, we keep our app buttery smooth without freezing!
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
}

class DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher get() = Dispatchers.Main
    override val io: CoroutineDispatcher get() = Dispatchers.IO
    override val default: CoroutineDispatcher get() = Dispatchers.Default
    override val unconfined: CoroutineDispatcher get() = Dispatchers.Unconfined
}