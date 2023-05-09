package xyz.poeschl.slowwave.commands

import xyz.poeschl.slowwave.FilterException
import xyz.poeschl.slowwave.PxRequest
import xyz.poeschl.slowwave.Request
import xyz.poeschl.slowwave.Statistics
import xyz.poeschl.slowwave.filter.BaseFilter
import java.util.*

class Token(private val enabled: Boolean, private val useCountPerToken: Int, private val statistics: Statistics) : BaseCommand {

  override val command = "TOKEN"

  private val tokenMap = mutableMapOf<String, String>()
  private val tokenCounter = mutableMapOf<String, Int>()

  override fun handleCommand(request: Request): String {
    if (enabled) {
      if (!tokenMap.containsKey(getHost(request.remote))) {
        val newToken = generateToken()
        tokenMap[getHost(request.remote)] = newToken
        tokenCounter[newToken] = useCountPerToken
        statistics.increaseTokenCount()
        return "TOKEN $newToken $useCountPerToken"
      } else {
        val token = tokenMap[getHost(request.remote)]
        return "TOKEN $token ${tokenCounter[token].toString()}"
      }
    } else {
      return "Token inactive"
    }
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
    return if (currentCount > 0) {
      tokenCounter[token] = currentCount - 1
      true
    } else {
      tokenCounter.remove(token)
      tokenMap.remove(storedToken)
      false
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
