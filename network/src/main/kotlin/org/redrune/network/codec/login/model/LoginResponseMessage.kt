package org.redrune.network.codec.login.model

import org.redrune.network.message.Message
import org.redrune.tools.ReturnCode

class LoginResponseMessage(val code: ReturnCode) : Message