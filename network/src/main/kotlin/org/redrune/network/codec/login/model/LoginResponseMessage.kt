package org.redrune.network.codec.login.model

import org.redrune.network.codec.message.Message
import org.redrune.tools.LoginReturnCode

data class LoginResponseMessage(val code: LoginReturnCode) : Message