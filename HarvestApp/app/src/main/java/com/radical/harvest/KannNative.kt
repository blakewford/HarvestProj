package com.radical.harvest

public class KannNative
{
    @Throws(IllegalArgumentException::class)
    external fun run(latitude: Int, longitude: Int, model: ByteArray?): Int

    companion object {
        init {
            System.loadLibrary("kann")
        }
    }
}