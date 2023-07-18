package org.objectionary.ddr

/**
 * This is a custom exception class that is thrown when a specific test not found
 */
class TestNotFoundException(message: String?) : RuntimeException(message)
