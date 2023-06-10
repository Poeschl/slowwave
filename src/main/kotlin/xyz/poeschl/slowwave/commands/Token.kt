package xyz.poeschl.slowwave.commands

import xyz.poeschl.slowwave.FilterException
import xyz.poeschl.slowwave.PxRequest
import xyz.poeschl.slowwave.Request
import xyz.poeschl.slowwave.Statistics
import xyz.poeschl.slowwave.filter.BaseFilter
import java.time.Duration
import java.time.ZonedDateTime
import java.util.*

class Token(private val enabled: Boolean, private val useCountPerToken: Int, private val statistics: Statistics) :
  BaseCommand {

  override val command = "TOKEN"

  private val tokenMap = mutableMapOf<String, String>()
  private val tokenCounter = mutableMapOf<String, Int>()
  private val lastTokenRequest = mutableMapOf<String, ZonedDateTime>()

  override suspend fun handleCommand(request: Request): String {
    if (enabled) {
      val host = getHost(request.remote)
      if (!tokenMap.containsKey(host)) {
        val now = ZonedDateTime.now()
        if (!lastTokenRequest.containsKey(host) || Duration.between(lastTokenRequest[host], now).toSeconds() >= 1) {
          return genToken(host)
        } else {
          return "ERR Too many TOKEN requests per second"
        }

      } else {
        val token = tokenMap[getHost(request.remote)]

        return if (!tokenCounter.containsKey(token) || tokenCounter[token] == 0) {
          genToken(host)
        } else {
          "TOKEN $token ${tokenCounter[token].toString()}"
        }
      }
    } else {
      return "ERR Token inactive"
    }
  }

  private fun genToken(host: String): String {
    val newToken = generateToken()
    tokenMap[host] = newToken
    tokenCounter[newToken] = useCountPerToken
    lastTokenRequest[host] = ZonedDateTime.now()

    statistics.increaseTokenCount()
    return "TOKEN $newToken $useCountPerToken"
  }

  fun removeTokensForSocket(socket: String) {
    tokenMap[getHost(socket)]?.let { tokenCounter.remove(it) }
    tokenMap.remove(getHost(socket))
  }

  fun useTokenForSocket(socket: String, token: String): Boolean {
    if (!enabled) {
      return true
    }

    val storedToken: String? = tokenMap[getHost(socket)]
    if (storedToken != null && token != storedToken) {
      return false
    }

    val currentCount = tokenCounter.getOrDefault(token, 0)
    val update = currentCount - 1
    if (update >= 0) {
      tokenCounter[token] = update
      return true
    } else {
      tokenCounter.remove(token)
      tokenMap.remove(storedToken)
      return false
    }
  }

  private fun generateToken(): String {
    return UUID.randomUUID().toString()
  }

  private fun getHost(socket: String): String {
    return socket.substringBefore(":")
  }

  fun getFilter(): TokenFilter {
    return TokenFilter(this)
  }

  class TokenFilter(private val tokenCommand: Token) : BaseFilter<PxRequest> {
    override fun applyFilter(input: PxRequest): PxRequest {
      val token = input.sourceCmd.getOrElse(4) { "" }
      if (tokenCommand.useTokenForSocket(input.remote, token)) {
        return input
      } else {
        throw FilterException("Invalid Token. Request a new one with 'TOKEN'")
      }
    }
  }
}
