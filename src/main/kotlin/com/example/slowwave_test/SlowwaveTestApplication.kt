package com.example.slowwave_test

import reactor.netty.tcp.TcpServer

class SlowwaveTestApplication {
    fun run() {
        val server = TcpServer
                .create()
                .host("localhost")
                .port(8080)
                .wiretap(true)
                .handle { inbound, outbound ->
                    val inputString = inbound.receive().asString()
                    outbound.sendString(inputString)
                }
                .bindNow()

        server.onDispose().block()
    }
}

fun main(args: Array<String>) {
    SlowwaveTestApplication().run()
}
